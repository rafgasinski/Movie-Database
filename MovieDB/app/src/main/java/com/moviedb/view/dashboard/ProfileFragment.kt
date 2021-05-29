package com.moviedb.view.dashboard

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.moviedb.R
import com.moviedb.activities.DashboardActivity
import com.moviedb.activities.LoginActivity
import com.moviedb.adapters.ProfileMovieAdapter
import com.moviedb.databinding.DialogSignoutBinding
import com.moviedb.databinding.FragmentProfileBinding
import com.moviedb.listeners.OnClickProfileMovie
import com.moviedb.model.repositories.FirebaseMovie
import com.moviedb.utils.Constants
import com.moviedb.viewmodel.ProfileViewModel


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    /**
     * FirebaseAuth Instance
     * */
    private lateinit var mAuth : FirebaseAuth
    var changeProfilePic = false

    /**
     * Firebase variables
     * */
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val databaseFirebase = Firebase.database.reference

    private var watchedList : ArrayList<FirebaseMovie> = arrayListOf()
    private var toWatchList : ArrayList<FirebaseMovie> = arrayListOf()

    /**
     * Menu items
     * */
    private var menuItemSearch: MenuItem? = null
    private var searchView: SearchView? = null
    private lateinit var searchEditText : EditText

    /**
     * ViewModel and recyclerView adapter
     * */
    private lateinit var viewModel: ProfileViewModel
    private val adapterProfileMovies: ProfileMovieAdapter by lazy {
        ProfileMovieAdapter(viewModel)
    }

    private val backdropRequestCode = 2000
    private val profilePictureReqeustCode = 1000

    private lateinit var toolbar : androidx.appcompat.widget.Toolbar

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

        setHasOptionsMenu(true)
        toolbar = activity?.findViewById(R.id.toolbar)!!

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        /**
         * Remove recommended recycler movie position
         * and search position from SharedPreferences
         * */
        val sharedPrefRecommended: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY, Constants.PRIVATE_MODE)
        sharedPrefRecommended?.edit()?.remove(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY)?.clear()?.apply()

        val sharedPrefSearch: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_SEARCH_POSITION_KEY, Constants.PRIVATE_MODE)
        sharedPrefSearch?.edit()?.remove(Constants.SH_SEARCH_POSITION_KEY)?.clear()?.apply()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Reference to firebase storage user's folders
         * */
        val profilePicReference = firebaseStorage.child("users/" + mAuth.currentUser!!.uid + "/profile_pic")
        val backgroundPicReference = firebaseStorage.child("users/" + mAuth.currentUser!!.uid + "/background_pic")

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /**
         * Load user's profile picture from Firebase or
         * from his login account (e.g Google)
         * */
        profilePicReference.downloadUrl.addOnSuccessListener {
            Glide.with(view)
                    .load(it)
                    .placeholder(R.drawable.placeholder_transparent)
                    .into(binding.profilePicture)
        }.addOnFailureListener {
            if(mAuth.currentUser?.photoUrl != null){
                Glide.with(view)
                        .load(mAuth.currentUser?.photoUrl)
                        .placeholder(R.drawable.placeholder_transparent)
                        .into(binding.profilePicture)
            }

        }

        /**
         * Load user's backdrop picture from Firebase
         * */
        backgroundPicReference.downloadUrl.addOnSuccessListener {
            Glide.with(view)
                .load(it)
                .placeholder(R.drawable.placeholder_transparent)
                .centerCrop()
                .into(binding.profileBackground)
        }

        /**
         * Animate pic changes
         * */
        binding.changeProfilePic.animate().setDuration(800).alpha(1f)
        binding.changeBackgroundPic.animate().setDuration(800).alpha(0.9f)

        /**
         * OnClickListeners for changing both pictures, starting intent
         * */
        binding.changeProfilePic.setOnClickListener {
            val intentOpenGallery = Intent()
            intentOpenGallery.action = Intent.ACTION_GET_CONTENT
            intentOpenGallery.type = "image/*"
            startActivityForResult(intentOpenGallery, profilePictureReqeustCode)
        }

        binding.changeBackgroundPic.setOnClickListener {
            val intentOpenGallery = Intent()
            intentOpenGallery.action = Intent.ACTION_GET_CONTENT
            intentOpenGallery.type = "image/*"
            startActivityForResult(intentOpenGallery, backdropRequestCode)
        }

        /**
         * Adding tabs
         * */
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Watchlist"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Watched"))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        /**
         * Observe Event from FirebaseRepository, to show Toast with it
         * */
        viewModel.firebaseRepository.databaseMessage.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        /**
         * Get movie data from Firebase Realtime Database
         * */
       val getAllMovies = object : ValueEventListener {
           override fun onDataChange(snapshot: DataSnapshot) {
               binding.progressBar.visibility = View.VISIBLE
               binding.recyclerViewProfile.alpha = 1f

               val objectsGTypeInd: GenericTypeIndicator<HashMap<String, FirebaseMovie>> = object : GenericTypeIndicator<HashMap<String, FirebaseMovie>>() {}
               var objectHashMap: Map<String, FirebaseMovie>? = snapshot.getValue(objectsGTypeInd)

               objectHashMap = objectHashMap?.toList()?.sortedByDescending { (_, value) -> value}?.toMap()

               watchedList.clear()
               toWatchList.clear()

               objectHashMap?.values?.forEach {
                   if(it.watched){
                       watchedList.add(it)
                   } else {
                       toWatchList.add(it)
                   }
               }


               binding.progressBar.visibility = View.GONE

               if(searchEditText.text.isNullOrEmpty()){
                   if(binding.tabLayout.selectedTabPosition == 0){
                       adapterProfileMovies.setData(toWatchList)
                   } else {
                       adapterProfileMovies.setData(watchedList)
                   }
               }



           }

           override fun onCancelled(error: DatabaseError) {
           }

       }

        /**
         * Listen to changes in user's movie records
         * */
        databaseFirebase.child(mAuth.currentUser!!.uid).addValueEventListener(getAllMovies)
        observeList()
        setListMovies()

        /**
         * TabLayout, change list based on selected tab,
         * clear searchView text
         * */
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {


                if (tab?.position == 0) {
                    adapterProfileMovies.setData(toWatchList)
                    searchEditText.setHint(R.string.search_watchlist)
                } else {
                    adapterProfileMovies.setData(watchedList)
                    searchEditText.setHint(R.string.search_watched)
                }
                searchEditText.setText(R.string.empty_string)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

    }

    override fun onPause() {
        super.onPause()

        /**
         * Save last selected tab in sharedPreferences
         * */
        val sharedPrefSelectedTab: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_PROFILE_SELECTED_TAB_KEY, Constants.PRIVATE_MODE)
        sharedPrefSelectedTab?.edit()?.putInt(Constants.SH_PROFILE_SELECTED_TAB_KEY, binding.tabLayout.selectedTabPosition)?.clear()?.apply()

    }

    override fun onResume() {
        super.onResume()

        /**
         * Get last selected tab from sharedPreferences
         * */
        val sharedPrefSelectedTab: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_PROFILE_SELECTED_TAB_KEY, Constants.PRIVATE_MODE)
        val lastSelectedTab = sharedPrefSelectedTab?.getInt(Constants.SH_PROFILE_SELECTED_TAB_KEY, 0)

        /**
         * Switch to selected tab from sharedPreferences
         * */
        if(lastSelectedTab==1){

            binding.tabLayout.scrollX = binding.tabLayout.width

            binding.recyclerViewProfile.alpha = 0f
            binding.recyclerViewProfile.animate().withStartAction{
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.tabLayout.getTabAt(lastSelectedTab)?.select()
                }, 150)
            }.setDuration(1250).alpha(1f)

        }
    }

    private fun observeList() {
        viewModel.moviesToWatch.observe(viewLifecycleOwner, {
            adapterProfileMovies.setData(it)
        })
    }

    /**
     * Intent results
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /**
         * Start CropImage activity for profile picture
         * */
        if(requestCode == profilePictureReqeustCode && resultCode == RESULT_OK && data != null){
            changeProfilePic = true
            CropImage.activity(data.data)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(context as Activity, this)
        }

        /**
         * Start CropImage activity for backdrop picture
         * */
        if(requestCode == backdropRequestCode && resultCode == RESULT_OK && data != null){
            changeProfilePic = false
            CropImage.activity(data.data)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(context as Activity, this)
        }

        /**
         * Upload picture to Firebase Storage and change it in app
         * */
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                if(changeProfilePic){
                    uploadProfileImageToFirebase(resultUri)
                } else {
                    uploadBackgroundImageToFirebase(resultUri)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error: Exception = result.error
            }
        }
    }

    /**
     * Set list in adapter and override onClickListener
     * */
    private fun setListMovies() {
        binding.recyclerViewProfile.adapter = adapterProfileMovies
        adapterProfileMovies.onClickHomeListener = object : OnClickProfileMovie {
            override fun onClick(firebaseMovie: FirebaseMovie) {
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailsFragment(firebaseMovie.id)

                val sharedPrefId: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, Constants.PRIVATE_MODE)
                sharedPrefId?.edit()?.putString(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, firebaseMovie.id)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }
    }

    /**
     * Upload profile picture to Firebase
     * */
    private fun uploadProfileImageToFirebase(imageUri: Uri) {
        val fileRef = firebaseStorage.child("users/" + mAuth.currentUser!!.uid + "/profile_pic")
        fileRef.putFile(imageUri).addOnSuccessListener {

            fileRef.downloadUrl.addOnSuccessListener {
                Glide.with(requireContext())
                        .load(it)
                        .placeholder(R.drawable.placeholder_transparent)
                        .into(binding.profilePicture)
            }

            Toast.makeText(requireContext(), "Profile Picture Uploaded", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener{

            Toast.makeText(requireContext(), "Profile Picture Upload Failed", Toast.LENGTH_SHORT).show()

        }
    }

    /**
     * Upload backdrop picture to Firebase
     * */
    private fun uploadBackgroundImageToFirebase(imageUri: Uri) {
        val fileRef = firebaseStorage.child("users/" + mAuth.currentUser!!.uid + "/background_pic")
        fileRef.putFile(imageUri).addOnSuccessListener {

            fileRef.downloadUrl.addOnSuccessListener {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.placeholder_transparent)
                    .centerCrop()
                    .into(binding.profileBackground)
            }

            Toast.makeText(requireContext(), "Backdrop Picture Uploaded", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener{

            Toast.makeText(requireContext(), "Backdrop Picture Upload Failed", Toast.LENGTH_SHORT).show()

        }
    }

    /**
     * Override menu options
     * */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar, menu)

        menuItemSearch = menu.findItem(R.id.itemSearch)
        searchView = menuItemSearch!!.actionView as SearchView

        searchEditText = searchView?.findViewById(androidx.appcompat.R.id.search_src_text) as EditText

        if(binding.tabLayout.selectedTabPosition == 0){
            searchEditText?.setHint(R.string.search_watchlist)
        } else {
            searchEditText?.setHint(R.string.search_watched)
        }

        searchView!!.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(textInput: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapterProfileMovies.filter.filter(newText)
                    return false
                }

            }
        )
    }

    /**
     * Logout method
     * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.logOut -> {
                val bindingDialog = DialogSignoutBinding.inflate(LayoutInflater.from(context))
                val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                builder.setView(bindingDialog.root)

                val mAlertDialog = builder.show()

                bindingDialog.confirm.setOnClickListener {
                    mAuth.signOut()
                    mAlertDialog.dismiss()

                    val activity = activity as DashboardActivity
                    activity.removeSharedPreferences()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    requireActivity().finish()
                }

                bindingDialog.cancel.setOnClickListener {
                    mAlertDialog.dismiss()
                }

                true
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }

}
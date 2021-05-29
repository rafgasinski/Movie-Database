package com.moviedb.view.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemChangeListener
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.moviedb.R
import com.moviedb.activities.DashboardActivity
import com.moviedb.activities.LoginActivity
import com.moviedb.adapters.HomeMovieAdapter
import com.moviedb.databinding.DialogSignoutBinding
import com.moviedb.databinding.FragmentHomeBinding
import com.moviedb.listeners.OnClickHomeMovie
import com.moviedb.model.response.discover.DiscoverMovies
import com.moviedb.model.response.genre.Genre
import com.moviedb.model.response.home.HomeMovie
import com.moviedb.utils.Constants
import com.moviedb.utils.Constants.BACKDROP_IMAGE
import com.moviedb.viewmodel.HomeViewModel
import com.orhanobut.hawk.Hawk


open class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolbar : androidx.appcompat.widget.Toolbar

    /**
     * FirebaseAuth Instance
     * */
    lateinit var mAuth : FirebaseAuth

    private var genresList : List<Genre> = listOf()

    /**
     * ViewModel and recyclerView adapter
     * */
    private lateinit var viewModel: HomeViewModel
    private val adapterDiscoverGenre: HomeMovieAdapter by lazy {
        HomeMovieAdapter()
    }
    private val adapterTrending: HomeMovieAdapter by lazy {
        HomeMovieAdapter()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()

        toolbar = activity?.findViewById(R.id.toolbar)!!
        toolbar.setNavigationIcon(R.drawable.ic_search)

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

        toolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigate(R.id.searchFragment)
        }

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)


        /**
         * Get last chosen position in discover recyclerView
         * */
        Hawk.init(context).build()
        if(Hawk.contains(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY)){
            val lastRecyclerDiscoverPosition : Int = Hawk.get(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY)

            /**
             * Scroll to last chosen position in discover recyclerView
             * */
            if(lastRecyclerDiscoverPosition != 0 && lastRecyclerDiscoverPosition != null){
                binding.selectedGenreRecyclerView.alpha = 0f
                binding.selectedGenreRecyclerView.animate().withStartAction{
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.selectedGenreRecyclerView.smoothScrollToPosition(lastRecyclerDiscoverPosition)
                    }, 300)
                }.setDuration(1200).alpha(1f)
            }
        }

        /**
         * Get last selected genre tab
         * */
        val sharedPrefSelectedTab: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_DISCOVER_SELECTED_TAB_KEY, Constants.PRIVATE_MODE)
        val lastSelectedTab = sharedPrefSelectedTab?.getInt(Constants.SH_DISCOVER_SELECTED_TAB_KEY, 0)

        /**
         * Scroll to last selected genre tab and select it
         * */
        if(lastSelectedTab != 0 && lastSelectedTab != null){

            binding.tabLayout.scrollX = binding.tabLayout.width
            Handler(Looper.getMainLooper()).postDelayed({
                binding.tabLayout.getTabAt(lastSelectedTab)?.select()
            }, 200)
        }

        observeDiscover()
        viewModel.getDiscoverMovies()

        viewModel.getGenreList()
        observeGenresList()

        observeDiscoverGenre()
        setListDiscoverGenre()

        binding.tabLayout.isSmoothScrollingEnabled = true;
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_START

        /**
         * TabLayout listener
         * */
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                /**
                 * Save selected tab position in SharedPreferences
                 * */
                val sharedPrefSelectedTab: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_DISCOVER_SELECTED_TAB_KEY, Constants.PRIVATE_MODE)
                tab?.let { sharedPrefSelectedTab?.edit()?.putInt(Constants.SH_DISCOVER_SELECTED_TAB_KEY, it.position)?.apply() }

                /**
                 * Change recycler item list based on selected genre
                 * */
                if (tab != null) {
                    viewModel.getDiscoverGenre(genresList[tab.position].id)
                    binding.selectedGenreRecyclerView.smoothScrollToPosition(0)
                }

                Hawk.put(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY, 0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        setListTrending()
        observeTrending()
        viewModel.getTrending()
    }

    /**
     * Observe movies to discover list from API,
     * load them to imageSlider
     * */
    private fun observeDiscover() {
        viewModel.discoverMoviesResponseList.observe(viewLifecycleOwner, {
            loadImageList(it)
            imageSliderToDetailMovie(it)
        })
    }

    /**
     * Observe trending movies list from API
     * */
    private fun observeTrending() {
        viewModel.homeResponseList.observe(viewLifecycleOwner, {
            adapterTrending.setDataHomeMovies(it)
        })
    }

    /**
     * Observe genre list from API and add them to tabLayout
     * */
    private fun observeGenresList() {
        viewModel.genreResponseList.observe(viewLifecycleOwner, {
            genresList = it
            genresList.forEach { it ->
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it.nameGenre))
            }
        })

    }

    /**
     * Observe movies to discover (genreVariant) list from API
     * */
    private fun observeDiscoverGenre() {
        viewModel.discoverGenreResponseList.observe(viewLifecycleOwner, {
            adapterDiscoverGenre.setDataHomeMovies(it)
        })
    }

    /**
     * Set list in adapter and override onClickListener
     * */
    private fun setListTrending() {
        binding.trendingRecyclerView.setHasFixedSize(true)
        binding.trendingRecyclerView.adapter = adapterTrending
        adapterTrending.onClickHomeListener = object : OnClickHomeMovie{
            override fun onClick(homeMovie: HomeMovie) {
                val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(homeMovie.id)

                /**
                 * Save movieId in SharedPreferences,
                 * navigate to movie details fragment
                 * */
                val sharedPrefId: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, Constants.PRIVATE_MODE)
                sharedPrefId?.edit()?.putString(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, homeMovie.id)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }
    }

    /**
     * Set list in adapter and override onClickListener
     * */
    private fun setListDiscoverGenre() {
        binding.selectedGenreRecyclerView.setHasFixedSize(true)
        binding.selectedGenreRecyclerView.adapter = adapterDiscoverGenre
        adapterDiscoverGenre.onClickHomeListener = object : OnClickHomeMovie{
            override fun onClick(homeMovie: HomeMovie) {
                /**
                 * Save selected movie position in recycler, id
                 * and navigate to movie details fragment
                 * */
                val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(homeMovie.id)

                Hawk.put(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY, adapterDiscoverGenre.list.indexOf(homeMovie))

                activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, Constants.PRIVATE_MODE)
                    ?.edit()?.putString(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, homeMovie.id)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }
    }


    /**
     * Load images into ImageSlider
     * */
    private fun loadImageList(data: ArrayList<DiscoverMovies>) {

        val imageList = ArrayList<SlideModel>()

        for (i in 0..5) {
            if(data[i].backdropPath != null){
                imageList.add(SlideModel("$BACKDROP_IMAGE${data[i].backdropPath}"))
            }
        }

        binding.titleDiscover.text = data[0].original_title

        binding.imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)
        binding.imageSlider.setItemChangeListener(object : ItemChangeListener {
            override fun onItemChanged(position: Int) {
                binding.titleDiscover.text = data[position].original_title
            }
        })
    }

    /**
     * Image Slider onClickListener override
     * */
    private fun imageSliderToDetailMovie(data: ArrayList<DiscoverMovies>) {
        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {

                /**
                 * Save movieId in SharedPreferences,
                 * navigate to movie details fragment
                 * */
                val sharedPrefId: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, Constants.PRIVATE_MODE)
                sharedPrefId?.edit()?.putString(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, data[position].id)?.clear()?.apply()

                val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(
                        data[position].id
                )

                Navigation.findNavController(view!!).navigate(action)
            }
        })
    }

    /**
     * Override menu options
     * */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_discover, menu)
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

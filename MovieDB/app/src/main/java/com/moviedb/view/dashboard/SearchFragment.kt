package com.moviedb.view.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.moviedb.R
import com.moviedb.activities.DashboardActivity
import com.moviedb.activities.LoginActivity
import com.moviedb.adapters.SearchAdapter
import com.moviedb.databinding.DialogSignoutBinding
import com.moviedb.databinding.FragmentSearchBinding
import com.moviedb.listeners.OnClickItemSearch
import com.moviedb.model.response.search.SearchMovie
import com.moviedb.utils.Constants
import com.moviedb.viewmodel.SearchViewModel


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    lateinit var mAuth : FirebaseAuth

    private lateinit var viewModel: SearchViewModel
    private val adapterSearch: SearchAdapter by lazy {
        SearchAdapter()
    }
    private val adapterRecommended: SearchAdapter by lazy {
        SearchAdapter()
    }

    private lateinit var toolbar : androidx.appcompat.widget.Toolbar

    private var menuItemSearch: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()

        toolbar = activity?.findViewById(R.id.toolbar)!!

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        observeViewModelList()
        setList()

        val sharedPrefMovieDetail: SharedPreferences? = activity?.getSharedPreferences(
                Constants.SH_LAST_MOVIE_DETAIL_ID_KEY,
                Constants.PRIVATE_MODE
        )

        val movieId = sharedPrefMovieDetail?.getString(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, "")

        if(!movieId.isNullOrEmpty()) {

            viewModel.getRecommendations(movieId)

            binding.recyclerViewRecommendedFragment.visibility = View.VISIBLE
            binding.recommendedFragmentInfo.visibility = View.VISIBLE

            val sharedPrefRecommendedPosition: SharedPreferences? = activity?.getSharedPreferences(
                    Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY,
                    Constants.PRIVATE_MODE
            )

            val recommendedPosition = sharedPrefRecommendedPosition?.getInt(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY, 0)

            if(recommendedPosition != 0 && recommendedPosition != null){
                binding.recyclerViewRecommendedFragment.alpha = 0f
                binding.recyclerViewRecommendedFragment.animate().withStartAction{
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.recyclerViewRecommendedFragment.scrollToPosition(recommendedPosition)
                    }, 100)
                }.setDuration(1000).alpha(1f)
            }
        } else {
            binding.recyclerViewRecommendedFragment.visibility = View.GONE
            binding.recommendedFragmentInfo.visibility = View.GONE
        }

        val sharedPrefSearchQuery: SharedPreferences? = activity?.getSharedPreferences(
                Constants.SH_LAST_SEARCHED_QUERY_KEY,
                Constants.PRIVATE_MODE
        )

        val searchQuery = sharedPrefSearchQuery?.getString(Constants.SH_LAST_SEARCHED_QUERY_KEY, "")

        if(!searchQuery.isNullOrEmpty()) {

            viewModel.getSearchMovie(searchQuery)

            val sharedPrefSearchPosition: SharedPreferences? = activity?.getSharedPreferences(
                    Constants.SH_SEARCH_POSITION_KEY,
                    Constants.PRIVATE_MODE
            )

            val searchPosition = sharedPrefSearchPosition?.getInt(Constants.SH_SEARCH_POSITION_KEY, 0)

            if(searchPosition != 0 && searchPosition != null){
                binding.recyclerViewSearchFragment.alpha = 0f
                binding.recyclerViewSearchFragment.animate().withStartAction{
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.recyclerViewSearchFragment.scrollToPosition(searchPosition)
                    }, 100)
                }.setDuration(1000).alpha(1f)

            }

            val resources: Resources = resources
            binding.recyclerViewSearchFragment.visibility = View.VISIBLE
            binding.searchFragmentInfo.visibility = View.VISIBLE

            binding.searchFragmentInfo.text = resources.getString(R.string.search_results, searchQuery)
        } else {
            binding.recyclerViewSearchFragment.visibility = View.GONE
            binding.searchFragmentInfo.visibility = View.GONE
        }

    }

    private fun observeViewModelList() {
        viewModel.responseSearchList.observe(viewLifecycleOwner, {
            adapterSearch.setDataSearch(it)

            val sharedPrefTitle: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_SEARCHED_QUERY_KEY, Constants.PRIVATE_MODE)
            var lastQuery = sharedPrefTitle?.getString(Constants.SH_LAST_SEARCHED_QUERY_KEY, "")

            if(adapterSearch.list.isNullOrEmpty() && !lastQuery.isNullOrEmpty()){
                binding.searchFragmentInfo.text = resources.getString(R.string.no_search_results, lastQuery)
            } else {
                binding.searchFragmentInfo.text = resources.getString(R.string.search_results, lastQuery)
            }
        })

        viewModel.responseRecommendedList.observe(viewLifecycleOwner, {
            adapterRecommended.setDataSearch(it)
        })
    }

    private fun setList() {
        binding.recyclerViewRecommendedFragment.setHasFixedSize(true)
        binding.recyclerViewRecommendedFragment.adapter = adapterRecommended
        adapterRecommended.onClickItemSearch = object : OnClickItemSearch {
            override fun onClick(searchMovie: SearchMovie) {

                val sharedPrefRecommendedPosition: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY, Constants.PRIVATE_MODE)
                sharedPrefRecommendedPosition?.edit()?.putInt(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY, adapterRecommended.list.indexOf(searchMovie))?.clear()?.apply()

                val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(searchMovie.id)

                val sharedPrefTitle: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_TITLE_KEY, Constants.PRIVATE_MODE)
                sharedPrefTitle?.edit()?.putString(Constants.SH_LAST_MOVIE_TITLE_KEY, searchMovie.title)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }

        binding.recyclerViewSearchFragment.setHasFixedSize(true)
        binding.recyclerViewSearchFragment.adapter = adapterSearch
        adapterSearch.onClickItemSearch = object : OnClickItemSearch {
            override fun onClick(searchMovie: SearchMovie) {

                val sharedPrefRecommendedPosition: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_SEARCH_POSITION_KEY, Constants.PRIVATE_MODE)
                sharedPrefRecommendedPosition?.edit()?.putInt(Constants.SH_SEARCH_POSITION_KEY, adapterSearch.list.indexOf(searchMovie))?.clear()?.apply()

                val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(searchMovie.id, )

                val sharedPrefTitle: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_TITLE_KEY, Constants.PRIVATE_MODE)
                sharedPrefTitle?.edit()?.putString(Constants.SH_LAST_MOVIE_TITLE_KEY, searchMovie.title)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar, menu)

        menuItemSearch = menu.findItem(R.id.itemSearch)
        searchView = menuItemSearch!!.actionView as SearchView

        val searchEditText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.setHint(R.string.search_the_movie_database)

        searchView!!.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(textInput: String?): Boolean {

                        searchView?.clearFocus()

                        if (textInput != null) {
                            viewModel.getSearchMovie(textInput)

                            if (binding.recyclerViewSearchFragment.visibility != View.VISIBLE) {
                                binding.recyclerViewSearchFragment.visibility = View.VISIBLE

                                binding.searchFragmentInfo.visibility = View.VISIBLE

                                binding.recyclerViewSearchFragment.alpha = 0f
                                binding.recyclerViewSearchFragment.animate().setDuration(600).alpha(1f)
                            } else {
                                binding.recyclerViewSearchFragment.smoothScrollToPosition(0)
                            }

                            val resources: Resources = resources
                            binding.searchFragmentInfo.text = resources.getString(R.string.search_results, textInput)

                            binding.searchFragmentInfo.alpha = 0f
                            binding.searchFragmentInfo.animate().setDuration(600).alpha(1f)

                            val sharedPrefTitle: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_SEARCHED_QUERY_KEY, Constants.PRIVATE_MODE)
                            sharedPrefTitle?.edit()?.putString(Constants.SH_LAST_SEARCHED_QUERY_KEY, textInput)?.clear()?.apply()
                        }

                        return true
                    }

                    override fun onQueryTextChange(p0: String?): Boolean = true
                }
        )

        menuItemSearch!!.setOnActionExpandListener(
                object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem?): Boolean = true

                    override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                        return true
                    }
                }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.logOut -> {
                var bindingDialog = DialogSignoutBinding.inflate(LayoutInflater.from(context))
                val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                builder.setView(bindingDialog.root)

                val mAlertDialog = builder.show()

                bindingDialog.confirm.setOnClickListener {
                    mAuth.signOut()
                    mAlertDialog.dismiss()

                    var activity = activity as DashboardActivity
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
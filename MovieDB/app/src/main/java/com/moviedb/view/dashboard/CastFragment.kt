package com.moviedb.view.dashboard

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.moviedb.R
import com.moviedb.adapters.HomeMovieAdapter
import com.moviedb.databinding.FragmentCastDetailsBinding
import com.moviedb.listeners.OnClickHomeMovie
import com.moviedb.model.response.cast.CastDetails
import com.moviedb.model.response.home.HomeMovie
import com.moviedb.utils.Constants
import com.moviedb.viewmodel.CastViewModel

class CastFragment : Fragment() {

    private var _binding: FragmentCastDetailsBinding? = null
    private val binding get() = _binding!!

    /**
     * Navigation args
     * */
    private val args : CastFragmentArgs by navArgs()

    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    private var toolbarTitle : TextView? = null

    /**
     * ViewModel and recyclerView adapter
     * */
    private lateinit var viewModel: CastViewModel
    private val adapterMovieCredits: HomeMovieAdapter by lazy {
        HomeMovieAdapter()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCastDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(CastViewModel::class.java)

        toolbar = activity?.findViewById(R.id.toolbar)!!
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarTitle = activity?.findViewById(R.id.toolbar_title)

        viewModel.getPersonDetails(args.castId)
        viewModel.getMovieCredits(args.castId)
        setMovieCreditsLists()
        observeMovieCredits()
        observePersonDetails()

    }

    private fun observePersonDetails(){
        viewModel.personDetails.observe(viewLifecycleOwner, {
            loadProfilePicture(it.profilePath)
            loadPersonDetails(it)
        })
    }

    private fun setMovieCreditsLists(){
        binding.recyclerViewPersonMovies.setHasFixedSize(true)
        binding.recyclerViewPersonMovies.adapter = adapterMovieCredits
        adapterMovieCredits.onClickHomeListener = object : OnClickHomeMovie {
            override fun onClick(homeMovie: HomeMovie) {
                val action = CastFragmentDirections.actionCastFragmentToDetailsFragment(homeMovie.id)

                /**
                 * Save movie id in SharedPreferences,
                 * navigate to movie details fragment
                 * */
                val sharedPrefId: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, Constants.PRIVATE_MODE)
                sharedPrefId?.edit()?.putString(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, homeMovie.id)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }
    }

    private fun observeMovieCredits(){
        viewModel.movieCreditsResponse.observe(viewLifecycleOwner, {
            adapterMovieCredits.setDataHomeMovies(it)
            if(it.isNotEmpty()){
                binding.castInfo.visibility = View.VISIBLE
                binding.recyclerViewPersonMovies.visibility = View.VISIBLE
            }
        })
    }

    private fun loadProfilePicture(profilePath: String) {
        Glide.with(this)
                .load("${Constants.POSTER_IMAGE}${profilePath}")
                .placeholder(R.drawable.placeholder_transparent)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.cast_error)
                .centerCrop()
                .into(binding.personProfilePicture)
    }

    private fun loadPersonDetails(castDetails: CastDetails) {
        if(!castDetails.name.isNullOrEmpty()){
            binding.personName.text = castDetails.name
            toolbarTitle?.text = castDetails.name
            binding.personName.visibility = View.VISIBLE
        }

        if(!castDetails.birthday.isNullOrEmpty()){
            binding.birthday.text = castDetails.birthday
        } else {
            binding.birthday.text = resources.getString(R.string.unknown)
        }

        if(!castDetails.deathday.isNullOrEmpty()){
            binding.deathday.text = castDetails.deathday
            binding.deathday.visibility = View.VISIBLE
            binding.deathdayInfo.visibility = View.VISIBLE
        }

        if(!castDetails.placeOfBirth.isNullOrEmpty()){
            binding.birthplace.text = castDetails.placeOfBirth
        } else {
            binding.birthplace.text = resources.getString(R.string.unknown)
        }

        if(!castDetails.biography.isNullOrEmpty()){
            binding.biography.text = castDetails.biography
            binding.biography.visibility = View.VISIBLE
            binding.biographyInfo.visibility = View.VISIBLE
        }
    }

}
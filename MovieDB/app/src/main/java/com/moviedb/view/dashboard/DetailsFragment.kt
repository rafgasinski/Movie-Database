package com.moviedb.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.moviedb.R
import com.moviedb.adapters.CastAdapter
import com.moviedb.adapters.MovieDetailGenreAdapter
import com.moviedb.adapters.VideosAdapter
import com.moviedb.databinding.FragmentDetailsBinding
import com.moviedb.listeners.OnClickCastItem
import com.moviedb.listeners.OnClickVideoTrailer
import com.moviedb.model.repositories.FirebaseMovie
import com.moviedb.model.response.cast.Cast
import com.moviedb.model.response.movie.DetailResponse
import com.moviedb.model.response.movie.MovieVideo
import com.moviedb.utils.Constants
import com.moviedb.utils.Constants.BACKDROP_IMAGE
import com.moviedb.viewmodel.DetailViewModel
import java.text.SimpleDateFormat
import java.util.*


class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var trailerSnapHelper: SnapHelper

    private lateinit var firebaseMovie : FirebaseMovie
    val sdf = SimpleDateFormat("yyyy/M/d HH:mm:ss")

    /**
     * Navigation args
     * */
    private val args : DetailsFragmentArgs by navArgs()

    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    private var toolbarTitle : TextView? = null

    /**
     * FirebaseAuth Instance
     * */
    private lateinit var mAuth : FirebaseAuth

    /**
     * ViewModel and recyclerView adapter
     * */
    private lateinit var viewModel: DetailViewModel
    private val adapterCast: CastAdapter by lazy {
        CastAdapter(binding.castInfo)
    }
    private val adapterGenres: MovieDetailGenreAdapter by lazy {
        MovieDetailGenreAdapter()
    }
    private val adapterTrailers: VideosAdapter by lazy {
        VideosAdapter(binding.trailersInfo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)

        firebaseMovie = FirebaseMovie()

        toolbar = activity?.findViewById(R.id.toolbar)!!

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = args.movieId

        toolbarTitle = activity?.findViewById(R.id.toolbar_title)

        trailerSnapHelper = LinearSnapHelper()

        /**
         * Get movie data from API
         * */
        viewModel.getDetails(movieId)
        viewModel.getCast(movieId)

        viewModel.firebaseRepository.getMovie(movieId)

        /**
         * Check if movie is in user's records,
         * if so change corresponding checkbox
         * */
        viewModel.firebaseRepository.wasMovieWatched.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if(it == "false"){
                    binding.toWatchCheckbox.isChecked = true
                } else if(it == "true") {
                    binding.watchedCheckbox.isChecked = true
                }
            }
        })

        /**
         * Observe Event from FirebaseRepository, to show Toast with it
         * */
        viewModel.firebaseRepository.databaseMessage.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        /**
         * Change checkbox if movie was added to Firebase
         * */
        viewModel.firebaseRepository.wasMovieAddedSuccessfully.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if(!it){
                    if(viewModel.firebaseRepository.addingMovieToWatchedList){
                        binding.watchedCheckbox.isChecked = false
                    } else {
                        binding.toWatchCheckbox.isChecked = false
                    }
                }
            }
        })

        /**
         * Checkbox listeners, adding or removing movies
         * in Firebase Realtime Database
         * */
        binding.toWatchCheckbox.setOnClickListener {
            if(binding.toWatchCheckbox.isChecked){
                binding.watchedCheckbox.isChecked = false

                firebaseMovie.watched = false
                firebaseMovie.additionDate = sdf.format(Date())
                viewModel.firebaseRepository.addMovie(firebaseMovie)
            } else {
                viewModel.firebaseRepository.removeMovie(movieId)
            }
        }

        binding.watchedCheckbox.setOnClickListener {
            if(binding.watchedCheckbox.isChecked){
                binding.toWatchCheckbox.isChecked = false

                firebaseMovie.watched = true
                firebaseMovie.additionDate = sdf.format(Date())
                viewModel.firebaseRepository.addMovie(firebaseMovie)
            } else {
                viewModel.firebaseRepository.removeMovie(movieId)
            }
        }

        observeDetailMovie()
        observeCast()
        setListCastGenresTrailers()
    }

    /**
     * Set list in adapter and override onClickListener
     * to start Intent with Youtube link
     * */
    private fun setListCastGenresTrailers(){
        binding.recyclerViewCast.setHasFixedSize(true)
        binding.recyclerViewCast.adapter = adapterCast

        adapterCast.onClickCastItem = object : OnClickCastItem {
            override fun onClick(cast: Cast) {
                val action = DetailsFragmentDirections.actionDetailsFragmentToCastFragment(cast.id)

                /**
                 * Save person name and id in SharedPreferences,
                 * navigate to cast details fragment
                 * */
                val sharedPrefTitle: SharedPreferences? = activity?.getSharedPreferences(Constants.SH_LAST_SELECTED_CAST_NAME, Constants.PRIVATE_MODE)
                sharedPrefTitle?.edit()?.putString(Constants.SH_LAST_SELECTED_CAST_NAME, cast.name)?.clear()?.apply()

                Navigation.findNavController(view!!).navigate(action)
            }
        }

        binding.recyclerViewMovieGenres.setHasFixedSize(true)
        binding.recyclerViewMovieGenres.adapter = adapterGenres

        binding.recyclerViewTrailers.setHasFixedSize(true)
        binding.recyclerViewTrailers.adapter = adapterTrailers
        trailerSnapHelper.attachToRecyclerView(binding.recyclerViewTrailers)

        adapterTrailers.onClickListener = object : OnClickVideoTrailer {
            override fun onClick(movieVideo: MovieVideo) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://www.youtube.com/watch?v=${movieVideo.key}")
                startActivity(Intent.createChooser(intent, "View Trailer:"))
            }
        }
    }

    /**
     * Observe castList
     * */
    private fun observeCast(){
        viewModel.castResponseList.observe(viewLifecycleOwner, {
            adapterCast.setDataCast(it)
        })
    }

    /**
     * Observe movieDetails
     * */
    private fun observeDetailMovie(){
        viewModel.detailResponse.observe(viewLifecycleOwner, {
            if (it.backdropPath == null) {
                binding.backdropCardview.visibility = View.GONE
            } else {
                loadBackdrop(it.backdropPath)
            }

            if (it.videosResult?.videos.isNullOrEmpty()) {
                binding.recyclerViewTrailers.visibility = View.GONE
                binding.trailersInfo.visibility = View.INVISIBLE
            } else {
                adapterTrailers.setDataCast(it.videosResult?.videos!!)
                adapterTrailers.notifyItemRangeInserted(
                    adapterTrailers.list.size - it.videosResult?.videos!!.size,
                    it.videosResult?.videos!!.size
                )
            }

            loadMovieDetails(it)
        })
    }

    /**
     * Load backdrop with given link from API
     * */
    private fun loadBackdrop(backdropPath: String) {
        Glide.with(this)
                .load("$BACKDROP_IMAGE${backdropPath}")
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(binding.backdrop)
    }

    /**
     * Load movie details
     * */
    @SuppressLint("Range")
    private fun loadMovieDetails(detailResponse: DetailResponse) {
        if(detailResponse.duration != 0){
            val duration = "${detailResponse.duration} min."
            binding.duration.text = duration
        } else {
            binding.duration.visibility = View.GONE
        }

        if(!detailResponse.description.isNullOrEmpty()){
            binding.overview.text = detailResponse.description
        }
        else {
            binding.overview.visibility = View.GONE
        }

        if(!detailResponse.releaseDate.isNullOrEmpty()){
            binding.releaseDate.text = detailResponse.releaseDate
        }
        else {
            binding.releaseDate.visibility = View.GONE
        }


        binding.toWatchCheckbox.visibility = View.VISIBLE
        binding.watchedCheckbox.visibility = View.VISIBLE

        binding.titleDetail.text = detailResponse.title
        toolbarTitle?.text = detailResponse.title
        binding.score.text = detailResponse.score
        binding.scoreStars.background.level = ((detailResponse.score.toDouble() * 1000) + 700).toInt()

        val genresList = detailResponse.genres.map { it.nameGenre }.toList()
        adapterGenres.setDataMovieGenres(genresList)

        firebaseMovie.id  = detailResponse.id.toString()
        firebaseMovie.title = detailResponse.title
        firebaseMovie.overview = detailResponse.description
        firebaseMovie.backdropPath = detailResponse.backdropPath
        firebaseMovie.score = detailResponse.score
    }
}
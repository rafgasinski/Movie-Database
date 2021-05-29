package com.moviedb.model.repositories

import com.moviedb.model.api.MoviesApi
import com.moviedb.model.response.cast.CastDetails
import com.moviedb.model.response.cast.CastResponse
import com.moviedb.model.response.discover.DiscoverMoviesResponse
import com.moviedb.model.response.genre.GenresList
import com.moviedb.model.response.home.HomeMovieResponse
import com.moviedb.model.response.home.MovieCreditsResponse
import com.moviedb.model.response.movie.DetailResponse
import com.moviedb.model.response.search.SearchResponse
import retrofit2.Response

val moviesApiService= MoviesApi()

class ApiRepository {

    suspend fun getTrending() : Response<HomeMovieResponse> {
        return moviesApiService.getTrending()
    }

    suspend fun getDiscoverMovies() : Response<DiscoverMoviesResponse> {
        return moviesApiService.getDiscoverMovies()
    }

    suspend fun getGenreList() : Response<GenresList> {
        return moviesApiService.getGenresList()
    }

    suspend fun getDiscoverSelectedGenre(genre : String) : Response<HomeMovieResponse> {
        return moviesApiService.getDiscoverSelectedGenre(genre)
    }

    suspend fun getDetailMovie(movieId : String) : Response<DetailResponse> {
        return moviesApiService.getDetailMovie(movieId)
    }

    suspend fun getCast(movieId : String) : Response<CastResponse> {
        return  moviesApiService.getCast(movieId)
    }

    suspend fun getSearchMovie(query : String) : Response<SearchResponse> {
        return  moviesApiService.getSearchMovie(query)
    }

    suspend fun getRecommendations(movieId : String) : Response<SearchResponse> {
        return  moviesApiService.getRecommendations(movieId)
    }

    suspend fun getPersonDetails(personId : String) : Response<CastDetails> {
        return  moviesApiService.getPersonDetails(personId)
    }

    suspend fun getMovieCredits(personId : String) : Response<MovieCreditsResponse> {
        return moviesApiService.getMovieCredits(personId)
    }
}
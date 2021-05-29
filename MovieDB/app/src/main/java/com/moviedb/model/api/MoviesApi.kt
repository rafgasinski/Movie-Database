package com.moviedb.model.api

import com.moviedb.model.response.cast.CastDetails
import com.moviedb.model.response.cast.CastResponse
import com.moviedb.model.response.discover.DiscoverMoviesResponse
import com.moviedb.model.response.movie.DetailResponse
import com.moviedb.model.response.genre.GenresList
import com.moviedb.model.response.search.SearchResponse
import com.moviedb.model.response.home.HomeMovieResponse
import com.moviedb.model.response.home.MovieCreditsResponse
import com.moviedb.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesApi {

    @GET("trending/movie/week")
    suspend fun getTrending(): Response<HomeMovieResponse>

    @GET("movie/{movie_id}")
    suspend fun getDetailMovie(@Path("movie_id") movieId: String, @Query("append_to_response") atr: String = Constants.APPEND_TO_DETAIL): Response<DetailResponse>

    @GET("movie/now_playing")
    suspend fun getDiscoverMovies(): Response<DiscoverMoviesResponse>

    @GET("genre/movie/list")
    suspend fun getGenresList(): Response<GenresList>

    @GET("discover/movie")
    suspend fun getDiscoverSelectedGenre(@Query("with_genres") genres: String, @Query("sort_by") sortBy: String = "popularity.desc"): Response<HomeMovieResponse>

    @GET("movie/{cast_id}/credits")
    suspend fun getCast(@Path("cast_id") cast: String): Response<CastResponse>

    @GET("search/movie")
    suspend fun getSearchMovie(
        @Query("query") query: String,
        @Query("page") page: String = Constants.PAGE): Response<SearchResponse>

    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendations(@Path("movie_id") movieId: String, @Query("page") page: String = Constants.PAGE): Response<SearchResponse>

    @GET("person/{person_id}")
    suspend fun getPersonDetails(@Path("person_id") personId: String) : Response<CastDetails>

    @GET("person/{person_id}/movie_credits")
    suspend fun getMovieCredits(@Path("person_id") personId: String) : Response<MovieCreditsResponse>

    companion object {
        operator fun invoke(): MoviesApi {
            val requestInterceptor = Interceptor { chain ->
                val url = chain.request()
                    .url
                    .newBuilder()
                    .addQueryParameter("api_key", Constants.API_KEY)
                    .addQueryParameter("language", Constants.LANGUAGE)
                    .build()

                val request = chain.request()
                    .newBuilder()
                    .url(url)
                    .build()

                return@Interceptor chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(requestInterceptor)
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) // Retrofit must know how serialize the data
                .build()
                .create(MoviesApi::class.java)
        }
    }
}


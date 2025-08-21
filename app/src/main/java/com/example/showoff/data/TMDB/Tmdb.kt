package com.example.showoff.data.TMDB

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


// Show.kt
data class Show2(
    val id: Int,
    val name: String,
    val overview: String,
    val poster_path: String,
    val vote_average: Float,
    val first_air_date: String

)

// Episode.kt
data class Episode2(
    val id: Int,
    val name: String,
    val overview: String,
    val episode_number: Int,
    val season_number: Int,
    val runtime: Int,
    val vote_average: Float,
    val poster_path: String?,
)
interface TMDbApi {
    @GET("tv/popular")
    suspend fun getPopularTV(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): PopularTVResponse

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonEpisodes(
        @Path("tv_id") showId: Int,
        @Path("season_number") season: Int,
        @Query("api_key") apiKey: String,
    ): SeasonResponse

    @GET("tv/{tv_id}")
    suspend fun getShowDetails(
        @Path("tv_id") showId: Int,
        @Query("api_key") apiKey: String,
    ): ShowDetailsResponse

}


data class PopularTVResponse(val results: List<Show2>)
data class SeasonResponse(val episodes: List<Episode2>)

data class ShowDetailsResponse(
    val id: Int,
    val created_by: List<Creator>
)

data class Creator(
    val id: Int,
    val name: String,
    val profile_path: String?
)

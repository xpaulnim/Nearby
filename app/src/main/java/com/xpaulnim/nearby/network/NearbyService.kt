package com.xpaulnim.nearby.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val WIKIPEDIA_URL = "https://wikipedia.org/"
private const val PREFERRED_THUMB_SIZE = 320

// https://www.mediawiki.org/wiki/Wikimedia_Apps/iOS_FAQ
// https://www.mediawiki.org/wiki/API:Query#Generators
private const val MW_API_PREFIX = "w/api.php?" +
        "format=json&" +
        "formatversion=2&" +
//        "errorformat=plaintext&" +
        "generator=geosearch&" +   // use generator=geosearch or list=geosearch, not both. when generator, use extra g eg ggradius vs gradius
        "action=query&" +
        "prop=coordinates|pageimages&" +
//        "colimit=50&" +
//        "piprop=thumbnail&" +
        "pithumbsize=${PREFERRED_THUMB_SIZE}&" +
        ""

// page summary  - https://stackoverflow.com/questions/8555320/is-there-a-wikipedia-api-just-for-retrieve-the-content-summary
// pageid to url - https://stackoverflow.com/questions/6168020/what-is-wikipedia-pageid-how-to-change-it-into-real-page-url
private const val PAGE_SUMMARY = "w/api.php?" +
        "action=query&" +
        "errorformat=plaintext&" +
        "format=json&" +
        "formatversion=2&" +
        "redirects=1&" +
        "prop=extracts|pageimages|description|coordinates&" +
        "explaintext&exintro&" +                // prop=extracts
        "pithumbsize=${PREFERRED_THUMB_SIZE}&"  // prop=pageimages

private const val LIST_GEOSEARCH_PREFIX = "w/api.php?" +
        "action=query&" +
        "errorformat=plaintext&" +
        "format=json&" +
        "formatversion=2&" +
        "list=geosearch&"

interface NearbyService {
    @GET(LIST_GEOSEARCH_PREFIX)
    suspend fun searchPointRadius(
        @Query("gscoord", encoded = true) gscoord: String,
        @Query("gsradius") gsradius: Int = 100,  // radius in meters
        @Query("gslimit") gslimit: Int = 50,
    ): WikipediaResponse

    @GET(LIST_GEOSEARCH_PREFIX)
    suspend fun searchBoundingBox(
        @Query("gsbbox", encoded = true) gscoord: String,
        @Query("gslimit") gslimit: Int = 50,
    ): WikipediaResponse

    @GET(PAGE_SUMMARY)
    suspend fun getPageIdSummary(
        @Query("pageids") pageid: Int
    ): WikipediaResponse
}

object NearByApi {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logger = HttpLoggingInterceptor().apply { level = Level.BASIC }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(WIKIPEDIA_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val retrofitService: NearbyService by lazy {
        retrofit.create(NearbyService::class.java)
    }
}

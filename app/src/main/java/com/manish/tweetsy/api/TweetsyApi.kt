package com.manish.tweetsy.api

import com.manish.tweetsy.model.TweetsItem
import com.manish.tweetsy.model.TweetsyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface TweetsyApi {


    @GET("/v3/b/691feab1d0ea881f40f5f101?meta=false")
    suspend fun getList(@Header("X-JSON-Path") category: String): Response<List<TweetsItem>> // here we pass dynamic header

    @GET("/v3/b/691feab1d0ea881f40f5f101?meta=false")
    @Headers("X-JSON-Path:tweets..category") // here we pass static header
    suspend fun getCategories(): Response<List<String>>


}
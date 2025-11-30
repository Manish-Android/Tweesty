package com.manish.tweetsy.repository

import android.annotation.SuppressLint
import com.manish.tweetsy.api.TweetsyApi
import com.manish.tweetsy.model.TweetsItem
import com.manish.tweetsy.model.TweetsyResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class TweetRepository @Inject constructor(private val tweetsyApi: TweetsyApi) {

    private val _categories = MutableStateFlow<List<String>?>(emptyList())
    val categories: StateFlow<List<String>?>
        get() =   _categories

    private val _tweets: MutableStateFlow<List<TweetsItem?>?> = MutableStateFlow(TweetsyResponse().tweets)
    val tweets: StateFlow<List<TweetsItem?>?>
        get() =   _tweets

    @SuppressLint("SuspiciousIndentation")
    suspend fun getTweets(category: String) {
      val response = tweetsyApi.getList("tweets[?(@.category==\"$category\")]")
        if(response.isSuccessful && response.body() != null){
            response.body().let {
                if (it != null) {
                    _tweets.emit(it)
                }
            }


        }
    }
    suspend fun getCategories() {
        val response = tweetsyApi.getCategories()
        if(response.isSuccessful && response.body() != null) {

            _categories.emit(response.body()?.distinct())


        }
    }
}
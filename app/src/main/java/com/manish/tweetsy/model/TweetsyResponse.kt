package com.manish.tweetsy.model

import com.google.gson.annotations.SerializedName

data class TweetsyResponse(

	@field:SerializedName("tweets")
	val tweets: List<TweetsItem?>? = null
)

data class TweetsItem(

	@field:SerializedName("text")
	val text: String? = null,

	@field:SerializedName("category")
	val category: String? = null
)

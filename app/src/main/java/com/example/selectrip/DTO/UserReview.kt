package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserReview(
    @Expose
    @SerializedName("STORENAME")
    var placeName : String,
    @Expose
    @SerializedName("REVIEW")
    var review : String,
    @Expose
    @SerializedName("USERNAME")
    var userName : String,
    @Expose
    @SerializedName("rating")
    var rating : Double,
    @Expose
    @SerializedName("userid")
    var userId : String,
    @Expose
    @SerializedName("date_now")
    var date : String,
    @Expose
    @SerializedName("image")
    var image : String,
    @Expose
    @SerializedName("storageUrl")
    var storageUrl : String,
    @Expose
    @SerializedName("like_count")
    var like : Int
): Serializable

package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

//장소의 리뷰 가져올 클래스
class PlaceReview(
    @Expose
    @SerializedName("storename")
    var stName : String,
    @Expose
    @SerializedName("reviewCount")
    var cnt : Int,
    @Expose
    @SerializedName("rating")
    var rating : Double
)
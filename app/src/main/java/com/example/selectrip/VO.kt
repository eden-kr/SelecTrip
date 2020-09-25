package com.example.selectrip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

//날씨 정보 가져올 클래스
data class WeatherInfo(
    @SerializedName("lat") var lat : Double,
    @SerializedName("lon") var lon : Double,
    @SerializedName("daily") var daily : List<Daily>
    )
data class Daily (
    @SerializedName("dt") var dt : String,
    @SerializedName("temp") var temp : Temp,
    @SerializedName("weather") var weather : List<Weather>,
    @SerializedName("pop") var pop : Double,
    @SerializedName("rain") var rain : Double
)
data class Temp (
    @SerializedName("day") var day : Double,
    @SerializedName("min") var min : Double,
    @SerializedName("max") var max : Double
)
data class Weather (
    @SerializedName("id") var id : String,
    @SerializedName("icon") var icon : String
)

//DetailActivity - 환율 클래스
data class Exchange (
    @SerializedName("result")
    @Expose
    var result : Int,
    @SerializedName("cur_unit")
    @Expose
    var cur_unit : String,
    @SerializedName("ttb")
    @Expose
    var ttb : String,
    @SerializedName("tts")
    @Expose
    var tts : String,
    @SerializedName("deal_bas_r")
    @Expose
    var deal_bas_r : String,
    @SerializedName("bkpr")
    @Expose
    var bkpr : String,
    @SerializedName("yy_efee_r")
    @Expose
    var yy_efee_r : String,
    @SerializedName("ten_dd_efee_r")
    @Expose
    var ten_dd_efee_r : String,
    @SerializedName("kftc_bkpr")
    @Expose
    var kftc_bkpr : String,
    @SerializedName("kftc_deal_bas_r")
    @Expose
    var kftc_deal_bas_r : String,
    @SerializedName("cur_nm")
    @Expose
    var cur_nm : String)

//장소 가져올 place 클래스
//인텐트로 넘겨줘야 해서 Serializable로 변경
class Place(
    @Expose
    @SerializedName("ID")
    var id: String,
    @Expose
    @SerializedName("NAME")
    var name : String,
    @Expose
    @SerializedName("TYPE")
    var type : String,
    @Expose
    @SerializedName("ADDRESS")
    var address : String,
    @Expose
    @SerializedName("tel")
    var tel : String,
    @Expose
    @SerializedName("lat")
    var lat : Float,
    @Expose
    @SerializedName("lng")
    var lng : Float,
    @Expose
    @SerializedName("image")
    var image : String,
    @Expose
    @SerializedName("cityname")
    var cityname : String,
    @Expose
    @SerializedName("hour")
    var hour : String
 ): Serializable {}

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
class UserReview(
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
):Serializable

class UserReviewVO(
    @Expose
    @SerializedName("ID")
    var id : Int,
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
):Serializable

class UpdateReviewVO(
    var postId: Int,
    var imageUrl: String,
    var imagePath: String,
    var rate: Double,
    var newReview: String
)
class ImageVO(
    var url : String,
    var storageUrl : String
)
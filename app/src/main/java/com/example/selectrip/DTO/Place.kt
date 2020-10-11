package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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

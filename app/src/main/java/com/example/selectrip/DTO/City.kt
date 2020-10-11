package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class City(
    @SerializedName("ID")
    @Expose
    val id: Int,
    @SerializedName("CITY")
    @Expose
    val city: String,
    @SerializedName("IMAGE")
    @Expose
    var img: String,
    @SerializedName("COUNTRY")
    @Expose
    var country: String,
    @SerializedName("MAINIMAGE")
    @Expose
    var mainImg : String,
    @SerializedName("MONEY")
    @Expose
    var money : String,
    @SerializedName("LAT")
    @Expose
    var lat : Double,
    @SerializedName("LON")
    @Expose
    var lon : Double
): Serializable {}

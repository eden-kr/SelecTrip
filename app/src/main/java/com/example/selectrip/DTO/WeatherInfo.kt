package com.example.selectrip.DTO

import com.google.gson.annotations.SerializedName

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
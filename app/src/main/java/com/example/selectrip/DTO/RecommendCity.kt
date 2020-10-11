package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RecommendCity(
    @Expose
    @SerializedName("name")
    var name : String
)
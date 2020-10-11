package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CheckUser(
    @SerializedName("check")
    @Expose
    val check: String,
    @SerializedName("nickname")
    @Expose
    val nickname: String
) : Serializable

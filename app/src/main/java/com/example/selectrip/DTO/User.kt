package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class User(        //회원가입에 사용될 User
    @Expose
    @SerializedName("id")
    var id: String,
    @Expose
    @SerializedName("password")
    var password: String,
    @Expose
    @SerializedName("nickname")
    var nickname: String
) {}

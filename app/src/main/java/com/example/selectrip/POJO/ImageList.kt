package com.example.selectrip.POJO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ImageList(
    @Expose
    @SerializedName("NAME")
    var name : String,
    @Expose
    @SerializedName("image")
    var img : String
): Serializable
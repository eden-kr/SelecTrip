package com.example.selectrip.DTO

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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

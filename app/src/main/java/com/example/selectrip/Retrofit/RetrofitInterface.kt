package com.example.selectrip.Retrofit

import com.example.selectrip.DTO.*
import com.example.selectrip.POJO.ImageList
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.ArrayList

//외부 url 연결 API
//환율 정보
interface InternetAPI{
    @GET("exchangeJSON?authkey=")
    fun getExchangeRate(
        @Query("searchdate") date : String,     //동적으로 GET하려면 Query를 이용해야 함!!
        @Query("data") dt : String
    ) : Single<List<Exchange>>

//날씨 정보
    @GET("onecall?")
    fun getWeather(
        @Query("lat") lat : String,
        @Query("lon") lon : String,
        @Query("exclude") exclude : String,
        @Query("appid") uid : String
    ) : Single<WeatherInfo>
}

interface RetrofitInterface {
    @FormUrlEncoded
    @POST("change_password")
    fun changePassword(
        @Field("email") email: String,
        @Field("prev_pw") prev: String,
        @Field("new_pw") new: String
    ): Call<String>

    @Multipart
    @POST("/selectrip")
    fun getRequest(
        @Part file: MultipartBody.Part
    ): Call<String>

    //회원가입 API
    @POST("/signup")
    fun signUp(
        @Body user: User
    ): Call<String>

    //로그인 API
    @FormUrlEncoded
    @POST("/login")
    fun logIn(
        @Field("userid") id: String,
        @Field("password") ps: String
    ): Call<CheckUser>

    //회원가입 시 중복체크 API
    @FormUrlEncoded
    @POST("/check")
    fun check(
        @Field("userid") id: String
    ): Call<String>

    //PlaceAcitivity 화면 정보를 가져올 API
    @FormUrlEncoded
    @POST("/place")
    fun getDb(
        @Field("city") name: String,
        @Field("type") type: String
    ): Call<List<Place>>

    //메인 화면 정보를 가져올  API
    @POST("/main")
    fun mainDB(
    ): Call<List<City>>

    //메인 화면 정보를 가져올  API
    @POST("/main")
    fun getMainView(
    ): Observable<List<City>>

    //유저 리뷰를 가져올 API
    @FormUrlEncoded
    @POST("/review")
    fun getReview(
        @Field("stName") stName: String
    ): Call<List<UserReviewDTO>>

    //리뷰 수 및 평점을 가져올 API
    @POST("/preview")
    fun getReview(
    ): Call<List<PlaceReview>>

    //리뷰 작성 API
    @POST("/writereview")
    fun setReview(
        @Body userReview: UserReview
    ): Call<String>

    //하버사인을 이용해 가까운 도시를 계산 API
    @FormUrlEncoded
    @POST("/distance")
    fun getDistance(
        @Field("lat") lat: Double,
        @Field("lng") lng: Double
    ): Call<List<RecommendCity>>

    @FormUrlEncoded
    @POST("/photo")
    fun getImage(
        @Field("placeName") name: String
    ): Call<List<ImageList>>

    @FormUrlEncoded
    @POST("/bookmark")
    fun setBookmark(
        @Field("click") click: String,
        @Field("userid") id: String,
        @Field("place") place: String
    ): Call<String>

    @FormUrlEncoded
    @POST("/getbookmark")
    fun getBookmark(
        @Field("place") place: String,
        @Field("userid") id: String
    ): Call<Char>

    @FormUrlEncoded
    @POST("/mypage")
    fun setReview(
        @Field("userid") id: String,
        @Field("type") type: String

    ): Call<ArrayList<UserReviewDTO>>

    //마이페이지 북마크 API
    @FormUrlEncoded
    @POST("/mypage")
    fun setBookmark(
        @Field("userid") id: String,
        @Field("type") type: String
    ): Call<List<Place>>

    //장소 검색 API

    @FormUrlEncoded
    @POST("search")
    fun getPlace(
        @Field("word") word: String
    ): Call<List<Place>>

    //리뷰 삭제 API
    @FormUrlEncoded
    @POST("delete")
    fun deleteReview(
        @Field("ID") id: Int
    ): Call<String>

    //리뷰 갱신 API
    @FormUrlEncoded
    @POST("update")
    fun updateReview(
        @Field("ID") id: Int,
        @Field("newReview") newReview: String,
        @Field("rate") rate: Double,            //별점
        @Field("image") image: String,         //파이어베이스 이미지 로딩용 url
        @Field("url") url: String      //파이어베이스 스토리지 url
    ): Call<String>

    //좋아요 카운팅하는 API
    @FormUrlEncoded
    @POST("like")
    fun setLikeCount(
        @Field("ID") id: Int,
        @Field("count") count: Int,
        @Field("userId") userId: String,
        @Field("type") type: String
    ): Call<String>

    //비밀번호 찾기 API
    @FormUrlEncoded
    @POST("find_password")
    fun findPassword(
        @Field("email") email: String
    ): Call<String>
}
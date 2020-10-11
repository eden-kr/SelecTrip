package com.example.selectrip.Retrofit

import android.content.Context
import com.example.selectrip.ect.HostVerifier
import com.example.selectrip.ect.SSLConnection
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//singleton retrofit
object MyRetrofit {
    private const val url = "https://192.168.0.4:5000"
    private val hostVr = HostVerifier()
    private val sslC = SSLConnection()
    private val gs = GsonBuilder()
        .setLenient()
        .create()
    @Volatile private var server: RetrofitInterface? = null

    fun getInstance(context: Context): RetrofitInterface =
        server ?: synchronized(this) {
            server
                ?: retrofit(
                    url,
                    context
                ).also { server = it }
        }

    private fun retrofit(url : String, context: Context): RetrofitInterface {
        return Retrofit.Builder().baseUrl(url)
            .client(
                OkHttpClient.Builder()
                    .sslSocketFactory(sslC.getSocketFactory(context))
                    .hostnameVerifier(hostVr)
                    .build()
            ).addConverterFactory(GsonConverterFactory.create(gs))
            .build().create(RetrofitInterface::class.java)
    }
}

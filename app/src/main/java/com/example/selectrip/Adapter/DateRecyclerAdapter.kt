package com.example.selectrip.Adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.DTO.Daily
import com.example.selectrip.R
import kotlinx.android.synthetic.main.weather_layout.view.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//날씨 리사이클러뷰
class DetailRecyclerAdapter(var context: Context, private val dailyList: ArrayList<Daily>) : RecyclerView.Adapter<DetailHolder>(){
    override fun getItemCount(): Int {
        return dailyList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DetailHolder, position: Int) {
        var weather = dailyList[position]
        holder.date.text = timeStampToGMT(weather.dt.toInt())   //타임스탬프 ->날짜
        holder.max.text = "${(weather.temp.max-273.15).toInt()}"   //최고온도
        holder.min.text = "${(weather.temp.min-273.15).toInt()}"   //최저온도
        holder.rain.text = "${(weather.pop*100).toInt()}%"  //강수량

        for(i in weather.weather.indices){
            var num = weather.weather[i].id.toInt()
            when (num) {        //id에 맞춰 날씨 이미지 설정
                in 200..233 -> holder.weatherImg.setImageResource(R.drawable.c12)
                in 300..322 -> holder.weatherImg.setImageResource(R.drawable.c8)
                in 500..531 -> holder.weatherImg.setImageResource(R.drawable.c2)
                in 600..623 -> holder.weatherImg.setImageResource(R.drawable.c5)
                in 700..782 -> holder.weatherImg.setImageResource(R.drawable.c9)
                800 -> holder.weatherImg.setImageResource(R.drawable.c3)
                in 801..805 -> holder.weatherImg.setImageResource(R.drawable.c4)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.weather_layout, null)
        return DetailHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun timeStampToGMT(timeStamp : Int):String{
        var format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var formatStamp = Instant.ofEpochSecond(timeStamp.toLong())
            .atZone(ZoneId.of("GMT-4"))
            .format(format)
        return formatStamp
    }

}
class DetailHolder(view : View): RecyclerView.ViewHolder(view){
    var max = view.maxTmp   //최고온도
    var min = view.minTmp   //최저온도
    var date = view.date    //날짜
    var weatherImg = view.weatherImg
    var rain = view.rain    //강수확률
}
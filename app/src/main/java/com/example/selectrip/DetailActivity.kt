package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.weather_layout.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {
    var exList = ExchangeTh(this).execute()?.get() //환율정보 받아오기
    var kfRate : Double = 0.0     //환율
    var lat : Double = 0.0
    var lon : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val intent = intent
        val bundle = intent.getBundleExtra("bundle") ?: null
        val city = bundle?.getSerializable("City") as? City
        val cityName = intent.getStringExtra("CITYNAME")    //도시이름 받아오기

        var money : String = ""
        var fMoney : String = ""

        //뷰 그리기
        detail_country_name.text = "${cityName},"
        Glide.with(this).load(city?.mainImg).into(detail_country_img)
        money = city?.money.toString()
        lat = city!!.lat
        lon = city!!.lon
        //환율 설정
        for(i in exList!!.indices){
            if(exList!![i].cur_unit.contains(money)){   //같은 통화를 사용하면
                fMoney = exList!![i].cur_unit  //해외 통화단위를 변경
                kfRate = exList!![i].deal_bas_r.replace(",","").toDouble()     // 환율 가져오기
            }
        }
        fmoneyName.text = fMoney

        //리사이클러뷰, 날씨 설정
        var weather = WeatherTh(this,lat.toString(),lon.toString()).execute().get()  //Retrofit2으로 json 가져오기 값 잘 들어옴
        var dailyList = arrayListOf<Daily>()
        dailyList.addAll(weather.daily)

        var wrView  : RecyclerView = findViewById(R.id.detailWeather)
        val lMananer = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        wrView.adapter = DetailRecyclerAdapter(this,dailyList)
        wrView.layoutManager = lMananer

        Log.d("myTag","$kfRate 환율정보  $money 돈")


        //핫플레이스 리스트 실행
        hotplace.setOnClickListener {
            val intent = Intent(this,CityActivity::class.java)
            intent.putExtra("cityName",detail_country_name.text.toString())     //인텐트에 도시명과 플레이스 타입 함께 보냄
            intent.putExtra("place","핫플레이스")
            intent.putExtra("lat",lat)
            intent.putExtra("lon",lon)
            startActivity(intent)

        }
        //맛집 리스트 실행
        restaurant.setOnClickListener {
            val intent = Intent(this,CityActivity::class.java)
            intent.putExtra("cityName",detail_country_name.text.toString())
            intent.putExtra("place","맛집")
            intent.putExtra("lat",lat)
            intent.putExtra("lon",lon)
            startActivity(intent)
        }
        skyScanner.setOnClickListener {
            val uri = Uri.parse("https://www.skyscanner.co.kr/")        //스카이스캐너 웹으로 연결
            val intent = Intent(Intent.ACTION_VIEW,uri)
            startActivity(intent)
        }   //비행기 예약 -> 스카이스캐너 사이트로 이동

        //뒤로가기
        close_detail.setOnClickListener {
            finish()
        }
        drawer_detail.setOnClickListener {
            val intent = Intent(this,MyPageActivity::class.java)
            startActivity(intent)
        }

        //환율 계산기
        //두개의 EditText가 함께 동작함
        var kwh : TextWatcher? = null
        var fwh : TextWatcher? = null
        //외화
        fwh = object  : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (fmoney.text.isNotEmpty()) {
                    var fm = fmoney.text.toString().toInt()
                    var convMoney = ((fm * kfRate) * 100 / 100).roundToInt().toString()
                    kwh?.let { setTextCal(kmoney, it,convMoney) }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }
        //한화
         kwh = object : TextWatcher{
             override fun afterTextChanged(p0: Editable?) {
                 if (kmoney.text.isNotEmpty()) {
                     var km = kmoney.text.toString().toInt()
                     var convMoney = ((km / kfRate) * 100 / 100).roundToInt().toString()
                     fwh?.let { setTextCal(fmoney, it, convMoney) }

                 }
             }
             override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
             }
             override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
             }

         }
        kmoney.addTextChangedListener(kwh)
        fmoney.addTextChangedListener(fwh)

    }
    fun setTextCal(editText: EditText,textWatcher: TextWatcher,text : String){
        editText.removeTextChangedListener(textWatcher)
        editText.setText(text)
        editText.addTextChangedListener(textWatcher)
    }

}
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
class DetailHolder(view : View):RecyclerView.ViewHolder(view){
    var max = view.maxTmp   //최고온도
    var min = view.minTmp   //최저온도
    var date = view.date    //날짜
    var weatherImg = view.weatherImg
    var rain = view.rain    //강수확률
}
//날씨 정보를 가져오는 쓰레드
class WeatherTh(var context: Context,var lat: String,var lon: String) : AsyncTask<Void,Void,WeatherInfo>(){
    override fun doInBackground(vararg p0: Void?): WeatherInfo? {
        val uid = "f23facc81f46689549e5d5532a430c37"        //API KEY
        var exclude = "current,minutely,hourly"             //현재,분,시간당 단위 제외
        var weather : WeatherInfo?
        val u = "http://api.openweathermap.org/data/2.5/"

        try {
            weather = getServer(u).getWeather(lat, lon, exclude, uid).execute().body()
        }catch (e : Exception){
            Log.d("myTag", e.printStackTrace().toString())
            throw e
        }
        return weather
    }
}
/*https://www.koreaexim.go.kr/site/program/openapi/openApiView?menuid=001003002002001&apino=2&viewtype=C*/
//환율을 가져올 스레드
class ExchangeTh(var context: Context): AsyncTask<Void,Void,List<Exchange>>(){
    var dt = getDate()
    var exList = arrayListOf<Exchange>()
    override fun doInBackground(vararg p0: Void?): List<Exchange> {
        val u = "https://www.koreaexim.go.kr/site/program/financial/"
        try {
            getServer(u).getExchangeRate(dt,"AP01").execute().body()?.let { exList.addAll(it) }
            Log.d("myTag","success")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return exList
    }

    fun getDate(): String {
        //API 조건 토,일요일은 null값을 반환
        //토,일요일이면 금요일의 환율 값을 가져와서 받아와야함.
        var cal = Calendar.getInstance()
        var df = SimpleDateFormat("yyyyMMdd")
        var sdf = SimpleDateFormat("h",Locale.KOREA)        //한국 시간 가져오기
        var date = Date()
        var hour = sdf.format(date).toInt()
        var dt : String = ""
        Log.d("myTag","현재시각${hour}")

        dt = if(hour  in 6..10){  //영업일 11시 이전 데이터는 null을 반환 만약 시간이 1-11시 사이면 전날 데이터를 받아오도록 설정
            cal.add(Calendar.DATE,-1)
            df.format(cal.time)
        } else{
            cal = Calendar.getInstance()
            df.format(cal.time)
        }

        when (date.day) {
            0 -> {       //일요일이면 금요일의 환율 정보를 사용
                cal.add(Calendar.DATE, -2)
                dt = df.format(cal.time)

            }
            6 -> {      //토요일이면
                cal.add(Calendar.DATE, -1)
                dt = df.format(cal.time)
            }
        }
        return dt
    }
}
fun getServer(url : String) : InternetAPI{
    return Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build().create(InternetAPI::class.java)
}
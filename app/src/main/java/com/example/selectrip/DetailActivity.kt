package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selectrip.Adapter.DetailRecyclerAdapter
import com.example.selectrip.DTO.City
import com.example.selectrip.DTO.Daily
import com.example.selectrip.DTO.Exchange
import com.example.selectrip.DTO.WeatherInfo
import com.example.selectrip.Retrofit.InternetAPI
import com.example.selectrip.Retrofit.RxRetrofit
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {
   // private var exList = ExchangeTh(this).execute()?.get() //환율정보 받아오기
    private var kfRate : Double = 0.0     //환율
    private var lat : Double = 0.0
    private var lon : Double = 0.0
    private val composite = CompositeDisposable()

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



        var disposable : Disposable = Retrofit.Builder()
            .baseUrl(getString(R.string.weather_url))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(InternetAPI::class.java)
            .getWeather(lat.toString(),lon.toString(),"current,minutely,hourly",getString(R.string.weather_API_key))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { items->
                //리사이클러뷰, 날씨 설정
                detailWeather.adapter = DetailRecyclerAdapter(this, items.daily.toMutableList() as ArrayList<Daily>)
                detailWeather.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
            },{
                Log.d("myTag","weather data can not access ${it.printStackTrace()}")
                throw it
            }).apply { composite.add(this) }

        var rateDisposable : Disposable = Retrofit.Builder()
            .baseUrl(getString(R.string.rate_url))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(InternetAPI::class.java)
            .getExchangeRate(getDate(),"AP01")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ items ->
                //환율 설정
                for(i in items.indices){
                    if(items[i].cur_unit.contains(money)){   //같은 통화를 사용하면
                        fMoney = items[i].cur_unit  //해외 통화단위를 변경
                        kfRate = items[i].deal_bas_r.replace(",","").toDouble()     // 환율 가져오기
                    }
                }
                fmoneyName.text = fMoney
            },{
                Log.d("myTag","change Rate data can not access ${it.printStackTrace()}")
                throw it
            }).apply { composite.add(this) }
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
            this.finish()
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

    override fun onDestroy() {
        super.onDestroy()
        composite.dispose()
    }

}

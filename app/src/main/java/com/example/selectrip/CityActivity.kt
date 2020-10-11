package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.PlaceAdapter
import com.example.selectrip.DTO.Place
import com.example.selectrip.DTO.PlaceReview
import com.example.selectrip.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_city.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class CityActivity : AppCompatActivity() {
        var pList : List<Place>? = null    //성공 시 받을 pList
        var lat : Double = 0.0      //구글맵에 넘겨 줄 위도 경도
        var lon : Double = 0.0
        var cityName : String = ""
        var type : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        setSupportActionBar(place_toorbar)  //상단 툴바 설정

        val intent = intent
        cityName = intent.getStringExtra("cityName")    //인텐트에 실어온 도시명, 타입 받아오기
        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("lon", 0.0)
        cityName = cityName.substring(0,cityName.length-1)      //마지막 ,가 도시명과 함께 딸려오기때문에 잘라줌
        type = intent.getStringExtra("place")
        place_toorbar_text.text = if(type=="핫플레이스") type else "맛집"

        var rList = ReviewTh(this).execute().get()  //리뷰리스트 가져오기
        getPlace(cityName,type,rList)   //여행지 정보 가져오기 + 리사이클러뷰 설정


        place_back.setOnClickListener {     //뒤로가기
            finish()
        }

    }
    //item에 만들어놓은 아이콘 붙이기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toorbar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.place_map -> {
                val intent = Intent(this,MapActivity::class.java)
                intent.putExtra("lat",lat)
                intent.putExtra("lon",lon)
                intent.putExtra("type",type)
                intent.putExtra("name",cityName)
                intent.putExtra("case",1)   //카메라 이동 제어, cityAcitivity에서 클릭 시 1, PlaceAcitivy에서 클릭 시 2 서로 다른 화면을 구성
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }
    //retrofit 비동기 호출
    fun getPlace(cityname : String, type : String, rList : List<PlaceReview>){
        MyRetrofit.getInstance(this).getDb(cityname,type).enqueue(object : Callback<List<Place>>{
            override fun onFailure(call: Call<List<Place>>, t: Throwable) {
                Log.d("myTag",t.message)
            }

            override fun onResponse(call: Call<List<Place>>, response: Response<List<Place>>) {
                Log.d("myTag","${response.isSuccessful}")
                try {
                    var pRecycle: RecyclerView = findViewById(R.id.place_recyle)
                    pRecycle.adapter = PlaceAdapter(this@CityActivity, response.body()!!, rList)
                }catch (e : Exception){
                    Log.d("myTag","${e.printStackTrace()}")
                }

            }

        })
    }
}
class ReviewTh(var context: Context) : AsyncTask<Void,Void,List<PlaceReview>>(){
    var list = arrayListOf<PlaceReview>()
    override fun doInBackground(vararg p0: Void?): List<PlaceReview> {
        try {
            MyRetrofit.getInstance(context).getReview().execute().body()?.let { list.addAll(it) }
        }catch (e : Exception){
            throw  e
        }
        return list
    }
}


package com.example.selectrip

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.RecAdapter
import com.example.selectrip.DTO.City
import com.example.selectrip.DTO.RecommendCity
import com.example.selectrip.Retrofit.MyRetrofit
import com.example.selectrip.Retrofit.RxRetrofit
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_recommand.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecommandActivity : AppCompatActivity() {
    private val composite = CompositeDisposable()
    override fun onStart() {
        super.onStart()
        var id_rec = findViewById<TextView>(R.id.id_recommend)
        id_rec.text = "${nickName!!}님"
        recText.text = "${nickName!!}님의 취향저격 Pick"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommand)
        setSupportActionBar(recToolbar)

        //리사이클뷰 설정
        var cityList = SearchActivity.getCityDb(this).execute()?.get()     //도시 받아오기
        var city = cityList?.let { getLatLng(it) }
        getRecommendCity(this,city!!.lat,city!!.lon,cityList!!)          //추천도시 리스트

        //예측 도시 받아와서 뷰 설정
        when (cityName) {
            "그린델발트" -> {
                imgBackground.setBackgroundResource(R.drawable.swiss_rec)   //편집해둔 이미지로 배경 설정
                country_rec.text = cityName
            }
            "파리" -> {
                imgBackground.setBackgroundResource(R.drawable.paris_rec)
                country_rec.text = cityName
            }
        }
        //next버튼 클릭 시 도시 상세 액티비티
        next_rec.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("CITYNAME", cityName)
            var city : City? = null
            for(i in cityList.indices){
                if(cityList[i].city == cityName){
                    city = cityList[i]
                }
            }
            val bundle = Bundle()
            bundle.putSerializable("City",city)
            intent.putExtra("bundle",bundle)
            startActivity(intent)
        }

        toolbarBack_rec.setOnClickListener {
            finish()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_button, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home ->{
                val intent = Intent(this,MainViewActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //도시 추천 알고리즘 , haversine 사용
    fun getRecommendCity(context: Context,lat : Double, lng : Double, cityList : List<City>){
        MyRetrofit.getInstance(this).getDistance(lat,lng).enqueue(object  : Callback<List<RecommendCity>>{
            override fun onFailure(call: Call<List<RecommendCity>>, t: Throwable) {
                Log.d("myTag",t.message)
            }

            override fun onResponse(call: Call<List<RecommendCity>>, response: Response<List<RecommendCity>>) {
                var recommendList = arrayListOf<City>() //결과값 담을 리스트
                var resList  = arrayListOf<RecommendCity>()
                response.body()?.let { resList.addAll(it) }

                for(i in 0 until resList.size){
                    for(j in cityList.indices){
                        if(resList[i].name == cityList[j].city){
                            recommendList.add(cityList[j])
                        }
                    }
                }
                //리사이클러뷰 적용
                val rcView: RecyclerView = findViewById(R.id.recycle_rec)
                val lMananer = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
                rcView.adapter = RecAdapter(recommendList,context)
                rcView.layoutManager = lMananer
            }
        })
    }
    fun getLatLng(cityList: List<City>) : City? {
        var city : City? = null
        for(i in cityList.indices){
            if(cityList[i].city == cityName){
                city = cityList[i]
            }
        }
        return city
    }
}



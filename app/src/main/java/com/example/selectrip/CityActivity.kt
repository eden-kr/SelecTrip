package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_city.*
import kotlinx.android.synthetic.main.place_layout.view.*
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
                pList = response.body()!!
                var pRecycle : RecyclerView = findViewById(R.id.place_recyle)
                pRecycle.adapter = PlaceAdapter(this@CityActivity, pList!!,rList)

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

class PlaceAdapter(var context: Context,val list : List<Place>,var rList : List<PlaceReview>) : RecyclerView.Adapter<PlaceHolder>(){
    override fun getItemCount(): Int {
        return list.size
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        var p = list[position]
        holder.pName.text = p.name
        holder.pCity.text = p.cityname
        holder.pType.text = p.type
        
        //리뷰 수와 평점 설정하기
        for(i in rList.indices){
            if(p.name == rList[i].stName){
                holder.pRate.rating = rList[i].rating.toFloat()
                holder.pCount.text = "(${rList[i].cnt})"
            }
        }


        Glide.with(context).load(p.image).into(holder.pImg)
        holder.pImg.clipToOutline = true        //이미지 둥글게 자르기

        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition    //각 뷰의 position을 가져옴
            val intent = Intent(context,PlaceActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("place",list[pos])
            intent.putExtra("bundle",bundle)      //가게정보 객체를 PlaceActivity에 넘겨주고 실행
            context.startActivity(intent)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.place_layout,null)
        return PlaceHolder(view)
    }


}
class PlaceHolder(view : View) : RecyclerView.ViewHolder(view){
    var pName = view.place_name
    var pType = view.place_type
    var pCity = view.place_city
    var pRate = view.rating_place
    var pCount = view.review_num
    var pImg = view.place_img
}

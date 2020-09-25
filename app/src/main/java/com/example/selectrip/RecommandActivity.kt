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
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_recommand.*
import kotlinx.android.synthetic.main.recommend_city.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecommandActivity : AppCompatActivity() {

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
        var cityList = getCityDb(this).execute()?.get()     //도시 받아오기
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

//리사이클뷰 어댑터
class RecAdapter(private var List: ArrayList<City>,var context: Context) : RecyclerView.Adapter<RecViewHolder>() {
    override fun getItemCount(): Int {
        return List.size
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {

        holder.name.text = List[position].city
        Glide.with(context).load(List[position].img).into(holder.image)

        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition
            if(pos != NO_POSITION){     //포지션이 없는지 검사
                val intent = Intent(context, DetailActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("City",List[pos])
                intent.putExtra("bundle",bundle)
                intent.putExtra("CITYNAME",holder.name.text.toString())
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recommend_city, parent, false)
        return RecViewHolder(view)

    }
}
//리사이클러뷰 홀더
class RecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var image = itemView.city_rec
    var name = itemView.cityname_rec

    //리스트뷰와 다르게 아이템 뷰에서 클릭을 처리함
}
class RecommendCity(
    @Expose
    @SerializedName("name")
    var name : String
)
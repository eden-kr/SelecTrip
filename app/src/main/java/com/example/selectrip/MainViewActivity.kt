package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main_view.*
import kotlinx.android.synthetic.main.country.view.*
import java.io.Serializable

class MainViewActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view)

        textView3.text="${nickName!!}님, 어디로 떠나볼까요?"
        search_main.bringToFront()
        searchBtn_main.bringToFront()

        //리사이클러뷰
        var cityList = getCityDb(this).execute()?.get()
        var rec = findViewById<RecyclerView>(R.id.recycle_main)
        val manager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rec.adapter = cityList?.let { MainRecyclerView(it,this) }
        rec.layoutManager = manager

        selectPhoto.setOnClickListener {
            val intent = Intent(this, SelectImageActivity::class.java)
            startActivity(intent)
        }
        //검색클릭 시
        searchBtn_main.setOnClickListener {
            val text : EditText = findViewById(R.id.search_main)
            val topic = text.text.toString()        //검색할 단어
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("topic",topic)
            startActivity(intent)

        }

        //드로어 버튼 클릭 시
        drawer_madin.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

    }
    class MainRecyclerView(var list : List<City>,var context: Context) : RecyclerView.Adapter<MainViewHolder>(){
        override fun getItemCount(): Int {
            return list.size
        }
        //바인딩
        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            var city = list[position]
            holder.cName.text = city.country
            holder.ctr.text = city.city
            Glide.with(context).load(city.img).into(holder.src)

            //리사이클러뷰는 리스트뷰와 다르게 리스너를 설정해줌
            //각각 이미지 클릭 시 나라명을 인텐트에 담아서 DetailAcitivity로 넘기기
            holder.itemView.setOnClickListener {
                var pos = holder.adapterPosition
                if(pos!=null){
                    val intent = Intent(context, DetailActivity::class.java)
                    var cName = list[pos].city
                    val bundle = Bundle()
                    bundle.putSerializable("City",list[pos])
                    intent.putExtra("bundle",bundle)
                    intent.putExtra("CITYNAME",cName)
                    context.startActivity(intent)
                }
            }
        }
        //뷰 홀더 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
            var view = LayoutInflater.from(context).inflate(R.layout.country, null)
            return MainViewHolder(view)
        }
    }
}
class MainViewHolder(view : View) : RecyclerView.ViewHolder(view){
    var cName = view.countryName //도시명
    var ctr = view.cityName     //국가명
    var src = view.countrySrc   //이미지
}


//동기식 Retrofit
class getCityDb(var context: Context): AsyncTask<Void,Void,List<City>>() {
    var cityList = arrayListOf<City>()
    override fun doInBackground(vararg p0: Void?): List<City> {
        try {
            MyRetrofit.getInstance(context).mainDB().execute().body()?.let { cityList.addAll(it) }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return cityList
    }
}

data class City(
    @SerializedName("ID")
    @Expose
    val id: Int,
    @SerializedName("CITY")
    @Expose
    val city: String,
    @SerializedName("IMAGE")
    @Expose
    var img: String,
    @SerializedName("COUNTRY")
    @Expose
    var country: String,
    @SerializedName("MAINIMAGE")
    @Expose
    var mainImg : String,
    @SerializedName("MONEY")
    @Expose
    var money : String,
    @SerializedName("LAT")
    @Expose
    var lat : Double,
    @SerializedName("LON")
    @Expose
    var lon : Double
): Serializable {}

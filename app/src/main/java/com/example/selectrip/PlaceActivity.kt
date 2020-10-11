package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selectrip.Adapter.ReviewAdapter
import com.example.selectrip.DTO.Place
import com.example.selectrip.DTO.PlaceReview
import com.example.selectrip.DTO.UserReviewDTO
import com.example.selectrip.Retrofit.MyRetrofit
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_place.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class PlaceActivity : AppCompatActivity(), OnMapReadyCallback {
    var lat: Double = 0.0
    var lon: Double = 0.0
    var type: String = ""
    var cityname: String = ""
    lateinit var place: Place
    var state: Boolean = false

    override fun onResume() {
        super.onResume()
        loadRecycleView()   //리뷰 작성 후 리사이클러뷰 갱신
        getBookmark(p_bookmark)     //북마크 갱신
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)
        setSupportActionBar(p_toorbar)      //툴바 설정
        //번들 데이터 가져오기
        val bundle = intent.getBundleExtra("bundle") ?: null
        place = bundle?.getSerializable("place") as Place
        var rList: List<PlaceReview> = ReviewTh(this).execute().get()   //리뷰수, 평점

        lat = place.lat.toDouble()        //위도
        lon = place.lng.toDouble()        //경도
        type = place.type                 //타입
        cityname = place.cityname         //도시명
        setView(place, rList)              //뷰설정
        var name = place.name

        //구글맵 객체 가져오기
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.p_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //리사이클러뷰 구성
        loadRecycleView()
        getBookmark(p_bookmark)
        //이미지 갤러리 들어가기
        p_image.setOnClickListener {
            val intent = Intent(this, ImageViewActivity::class.java)
            intent.putExtra("name", name)    //도시명 인텐트로 전달
            startActivity(intent)
        }

        //북마크 버튼 설정 및 insert & delete
        p_bookmark.setOnClickListener {
            state = !state
            if (state) {
                p_bookmark.setImageResource(R.drawable.clicked)
                var res = BookrmarkThread(this, "true", Id!!, place!!.name).execute()
                    .get()       //클릭 감지 시 insert
                Toast.makeText(this, "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                p_bookmark.setImageResource(R.drawable.unclicked)
                var res = BookrmarkThread(this, "false", Id!!, place!!.name).execute()
                    .get()      //클릭 감지 시 delete
                Toast.makeText(this, "즐겨찾기에서 제거되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        //뒤로가기
        p_back.setOnClickListener {
            finish()
        }
        //리뷰 작성
        p_review_write.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("placeName", place.name)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, R.anim.stay)      //위로 올라가는 애니메이션 설정
        }
    }

    //액티비티 시작 시 즐겨찾기 세팅
    fun getBookmark(bookmarkImage: ImageView) {
        MyRetrofit.getInstance(this).getBookmark(place.name, Id!!).enqueue(object : Callback<Char> {
            override fun onFailure(call: Call<Char>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<Char>, response: Response<Char>) {
                var flag = response.body()
                when (flag) {
                    '1' -> {            //서버에서 사용자가 특정 store를 클릭했다면 1을 반환
                        bookmarkImage.setImageResource(R.drawable.clicked)
                        state = true
                    }
                    '0' -> {            //db에 정보가 없으면 0을 반환
                        bookmarkImage.setImageResource(R.drawable.unclicked)
                        state = false
                    }
                }
            }

        })
    }

    fun loadRecycleView() {
        //유저 리뷰 받아오기
       UserReviewThread(this, place.name,p_recycle).execute()
    }

    //뷰 갱신
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setView(place: Place, rList: List<PlaceReview>) {
        p_address.text = place.address        //주소
        p_hour.text = "영업 시간 : ${place.hour}"             //영업 시간
        p_tel.text = "tel : ${place.tel}"        //전화번호
        p_name.text = place.name                //가게명
        p_type.text = "${place.cityname} / ${place.type}"       //도시명+타입
        Glide.with(this).load(place.image).into(p_image)  //이미지 로딩
        p_image.clipToOutline = true      //이미지 둥글게

        for (i in rList.indices) {
            if (rList[i].stName == place.name) {
                p_reviewNum.text = "(${rList[i].cnt})"           //리뷰 수
                ratingBar.rating = rList[i].rating.toFloat()      //평점
            }
        }
    }

    //툴바에 아이콘 메뉴 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toorbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //툴바 map에 리스너 달기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.place_map -> {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("lat", lat)
                intent.putExtra("lon", lon)
                intent.putExtra("type", type)
                intent.putExtra("name", cityname)
                intent.putExtra(
                    "case",
                    2
                )   //카메라 이동 제어, cityAcitivity에서 클릭 시 1, PlaceAcitivy에서 클릭 시 2 서로 다른 화면을 구성
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    //구글맵 뷰에 현재 플레이스 위치 띄우기
    override fun onMapReady(googleMap: GoogleMap?) {
        val mMap = googleMap
        var latlon = LatLng(lat, lon)
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latlon))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlon, 17f))
        mMap?.addMarker(MarkerOptions().position(latlon).title(place!!.name))
    }

}

//리뷰를 가져올 스레드
class UserReviewThread(var context: Context, var stName: String,val recyclerView: RecyclerView) :
    AsyncTask<Void, Void, List<UserReviewDTO>>() {
    override fun doInBackground(vararg p0: Void?): List<UserReviewDTO> {
        var list = arrayListOf<UserReviewDTO>()
        try {
            MyRetrofit.getInstance(context).getReview(stName).execute().body()
                ?.let { list.addAll(it) }
        } catch (e: Exception) {
            Log.d("myTag", "${e.printStackTrace()}")
        }
        return list
    }

    override fun onPostExecute(result: List<UserReviewDTO>?) {
        super.onPostExecute(result)
        //리사이클러뷰
        var adapter = ReviewAdapter(context, result as ArrayList<UserReviewDTO>)
        adapter.setHasStableIds(true)       //각 항목마다 고유의 아이디 부여
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}

//즐겨찾기 스레드  - setBookmark
class BookrmarkThread(
    var context: Context,
    var flag: String,
    var userid: String,
    var place: String
) : AsyncTask<Void, Void, String>() {
    var res: String = ""
    override fun doInBackground(vararg p0: Void?): String {
        try {
            res = MyRetrofit.getInstance(context).setBookmark(flag, userid, place).execute().body()
                .toString()          //flag가 true면 insert false면 delete
        } catch (e: Exception) {
            Log.d("myTag", "${e.printStackTrace()}")
        }
        return res
    }
}

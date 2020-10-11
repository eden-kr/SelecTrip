package com.example.selectrip

import android.content.Context
import android.graphics.Point
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import com.example.selectrip.DTO.City
import com.example.selectrip.DTO.Place
import com.example.selectrip.Fragment.CityFragment
import com.example.selectrip.Fragment.CountryFragment
import com.example.selectrip.Fragment.PlaceFragment
import com.example.selectrip.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

var case = 1

class SearchActivity : AppCompatActivity() {
    lateinit var cityFrag: Fragment
    lateinit var countryFrag: Fragment
    lateinit var placeFrag: Fragment

    var x: Float = 0.toFloat()
    var newlist = arrayListOf<City>()
    lateinit var text: String
    val THREAD_POOL = Executors.newFixedThreadPool(8)       //스레드 병렬 실행을 위한 풀

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        var cList = getCityDb(this).executeOnExecutor(THREAD_POOL).get() as ArrayList<City>
        var pList = arrayListOf<Place>()            //null list

        cityFrag = CityFragment.newInstance(cList)
        placeFrag = PlaceFragment.newInstance(pList)
        countryFrag = CountryFragment.newInstance(cList)

        init(cList, pList)        //초기화

        backSearch.setOnClickListener {
            finish()
        }
        clear.setOnClickListener {
            topic.text = null
        }

        topic.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                var text = topic.text.toString()
                Log.d("myTag", "case 진입점 $case")
                when (case) {
                    1 -> {
                        newlist = getData(cList, text)
                        cityFrag = CityFragment.newInstance(newlist)
                        onClick(cityFrag)
                        Log.d("myTag", "case 1 진입점 $case")

                    }
                    2 -> {
                        newlist = getCountryData(cList, text)
                        countryFrag = CountryFragment.newInstance(newlist)
                        onClick(countryFrag)
                        Log.d("myTag", "case 2 진입점 $case")

                    }
                    3 -> {
                        getAllPlaceDate(text)       //서버와 실시간으로 통신하여 데이터 받아옴
                        Log.d("myTag", "case 3 진입점 $case")

                    }
                }
                Log.d("myTag", newlist.size.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        //도시명 클릭 시 애니메이션 변경 및, 프래그먼트 변경
        serachCity.setOnClickListener {
            case = 1
            onClick(cityFrag)

            val ani = TranslateAnimation(x, 0F, 0F, 0F)
            x = 0F
            onMove(ani, searchSlider)
        }
        searchPlace.setOnClickListener {
            case = 3
            onClick(placeFrag)

            x = if (x > 500) {
                getWidth()/3*2.toFloat()
            } else {
                0F
            }
            val ani = TranslateAnimation(x, getWidth()/3.toFloat(), 0F, 0F)
            x = getWidth()/3.toFloat()
            onMove(ani, searchSlider)
        }
        searchCountry.setOnClickListener {
            case = 2
            onClick(countryFrag)

            val ani = TranslateAnimation(x,getWidth()/3*2.toFloat(), 0F, 0F)
            x = getWidth()/3*2.toFloat()
            onMove(ani, searchSlider)
        }
    }

    //도시명으로 데이터 검색
    fun getData(list: ArrayList<City>, text: String): ArrayList<City> {
        var newlist = arrayListOf<City>()
        if (text.isNullOrEmpty()) {
            newlist.addAll(list)
        } else {
            for (i in 0 until list.size) {
                if (list[i].city.contains(text)) {
                    newlist.add(list[i])
                }
            }
        }
        return newlist
    }
    fun getWidth() : Int{
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size.x
    }
    //retrofit! 실시간 통신
    fun getAllPlaceDate(text: String) {

        MyRetrofit.getInstance(this).getPlace(text).enqueue(object : Callback<List<Place>> {
            override fun onFailure(call: Call<List<Place>>, t: Throwable) {
                Log.d("myTag", t.message)
            }

            override fun onResponse(call: Call<List<Place>>, response: Response<List<Place>>) {
                var list = response.body()
                placeFrag = PlaceFragment.newInstance(list as ArrayList<Place>)
                onClick(placeFrag)
            }
        })
    }

    //국가명으로 데이터 검색
    fun getCountryData(list: ArrayList<City>, text: String): ArrayList<City> {
        var newlist = arrayListOf<City>()
        if (text.isNullOrEmpty()) {
            newlist.addAll(list)
        } else {
            for (i in 0 until list.size) {
                if (list[i].country.contains(text)) {
                    newlist.add(list[i])
                }
            }
        }
        return newlist
    }

    //초기화 함수
    fun init(cList: ArrayList<City>, pList: ArrayList<Place>) {
        val fragmentManager = supportFragmentManager
        val frag = fragmentManager.beginTransaction()

        when (case) {
            //임시용 초기화
            1 -> {
                if (frag.isEmpty) {
                    frag.add(R.id.searchFragment, cityFrag)  //맨 처음 실행할 프래그먼트 띄우기
                    frag.commit()
                }
            }
            2 -> {
                if (frag.isEmpty) {
                    frag.add(R.id.searchFragment, placeFrag)  //맨 처음 실행할 프래그먼트 띄우기
                    frag.commit()
                }
            }
            3 -> {
                if (frag.isEmpty) {
                    frag.add(R.id.searchFragment, countryFrag)  //맨 처음 실행할 프래그먼트 띄우기
                    frag.commit()
                }
            }
        }
    }

    fun onMove(ani: Animation, view: View) {        //애니메이션
        ani.duration = 1200
        ani.fillAfter = true        //이미지가 유지되도록
        view.startAnimation(ani)
    }

    //데이터 교체
    fun onClick(fragment: Fragment) {           //클릭 시 프래그먼트 변경
        val fragmentManager = supportFragmentManager
        val frag = fragmentManager.beginTransaction()
        frag.replace(R.id.searchFragment, fragment)
        frag.commit()
    }
    //동기식 Retrofit
    class getCityDb(var context: Context) : AsyncTask<Void, Void, List<City>>() {
        var cityList = arrayListOf<City>()
        override fun doInBackground(vararg p0: Void?): List<City> {
            try {
                MyRetrofit.getInstance(context).mainDB().execute().body()
                    ?.let { cityList.addAll(it) }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            return cityList
        }
    }
}



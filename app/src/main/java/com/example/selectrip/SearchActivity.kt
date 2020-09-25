package com.example.selectrip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    var newPlist = arrayListOf<Place>()
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
                Log.d("myTag","case 진입점 $case")
                when (case) {
                    1 -> {
                        newlist = getData(cList, text)
                        cityFrag = CityFragment.newInstance(newlist)
                        onClick(cityFrag)
                        Log.d("myTag","case 1 진입점 $case")

                    }
                    2 -> {
                        newlist = getCountryData(cList, text)
                        countryFrag = CountryFragment.newInstance(newlist)
                        onClick(countryFrag)
                        Log.d("myTag","case 2 진입점 $case")

                    }
                    3 -> {
                        getAllPlaceDate(text)       //서버와 실시간으로 통신하여 데이터 받아옴
                        Log.d("myTag","case 3 진입점 $case")

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
                (700).toFloat()
            } else {
                0F
            }
            val ani = TranslateAnimation(x, (350).toFloat(), 0F, 0F)
            x = 350.toFloat()
            onMove(ani, searchSlider)
        }
        searchCountry.setOnClickListener {
            case = 2
            onClick(countryFrag)

            val ani = TranslateAnimation(x, (700).toFloat(), 0F, 0F)
            x = 700.toFloat()
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
    //retrofit! 실시간 통신
    fun getAllPlaceDate(text : String){

        MyRetrofit.getInstance(this).getPlace(text).enqueue(object : Callback<List<Place>>{
            override fun onFailure(call: Call<List<Place>>, t: Throwable) {
                Log.d("myTag",t.message)
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
                if(frag.isEmpty) {
                    frag.add(R.id.searchFragment, cityFrag)  //맨 처음 실행할 프래그먼트 띄우기
                    frag.commit()
                }
            }
            2 -> {
                if(frag.isEmpty) {
                    frag.add(R.id.searchFragment, placeFrag)  //맨 처음 실행할 프래그먼트 띄우기
                    frag.commit()
                }
            }
            3 -> {
                if(frag.isEmpty) {
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
}
//도시명 검색
class CityFragment : Fragment() {
    lateinit var mAdapter: RecyclerView.Adapter<CityHolder>
    lateinit var recycleAdapter: RecyclerView

    companion object {
        const val KEY = "list"
        @JvmStatic
        fun newInstance(list: ArrayList<City>) = CityFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY, list)
            }
        }
    }
    val list by lazy { requireArguments().getSerializable(KEY) }
    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
        recycleAdapter.adapter = mAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myTag", "${list}")

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout, container, false)
        recycleAdapter = view.findViewById(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        mAdapter = activity?.let { CityRecyclerAdapter(it, list as ArrayList<City>) }!!
        recycleAdapter.adapter = mAdapter
        return view
    }
}

//국가명 검색
class CountryFragment : Fragment() {
    lateinit var mAdapter: RecyclerView.Adapter<CityHolder>
    lateinit var recycleAdapter: RecyclerView

    companion object {
        const val KEY = "list"
        @JvmStatic
        fun newInstance(list: ArrayList<City>) = CountryFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY, list)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
        recycleAdapter.adapter = mAdapter
    }
    val clist by lazy { requireArguments().getSerializable(KEY) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myTag", "${clist}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout, container, false)
        recycleAdapter = view.findViewById(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        mAdapter = activity?.let { CityRecyclerAdapter(it, clist as ArrayList<City>) }!!
        recycleAdapter.adapter = mAdapter
        return view
    }
}

//플레이스명 검색
class PlaceFragment : Fragment() {
    lateinit var mAdapter: RecyclerView.Adapter<PlaceRecyclerHolder>
    lateinit var recycleAdapter: RecyclerView

    companion object {
        const val KEY = "list"
        @JvmStatic
        fun newInstance(list: ArrayList<Place>) = PlaceFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY, list)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
        recycleAdapter.adapter = mAdapter
    }
    val plist by lazy { requireArguments().getSerializable(KEY) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myTag", "${plist}")

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout, container, false)
        recycleAdapter = view.findViewById(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        mAdapter = activity?.let { PlaceRecyclerAdapter(it, plist as ArrayList<Place>) }!!
        recycleAdapter.adapter = mAdapter
        return view
    }
}

package com.example.selectrip

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.example.selectrip.Adapter.ImageAdapter
import com.example.selectrip.POJO.ImageList
import com.example.selectrip.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_image_pager_acitivity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//이미지앨범에서 클릭된 이미지를 확대해서 볼 수 있는 슬라이더
class ImagePagerAcitivity : AppCompatActivity() {
    var index : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_pager_acitivity)
        val intent = intent
        val name : String = intent.getStringExtra("place")  //플레이스명
        index = intent.getIntExtra("index",0)    //선택된 인덱스

        selectedText.text = name    //플레이스명으로 텍스트 설정
        getImageList(name!!,this)

        left.bringToFront()
        right.bringToFront()    //버튼을 맨 위로 포커스

        //백버튼
        back.setOnClickListener {
            onBackPressed()
        }
        //왼쪽버튼 클릭시 position-1
        left.setOnClickListener {      
            var pos = pager.currentItem
            pager.setCurrentItem(pos-1,true)
        }
        right.setOnClickListener {
            var pos = pager.currentItem
            pager.setCurrentItem(pos+1,true)
        }
    }

    fun getImageList(name : String,context: Context){
        MyRetrofit.getInstance(this).getImage(name).enqueue(object : Callback<List<ImageList>> {
            override fun onFailure(call: Call<List<ImageList>>, t: Throwable) {
                Log.d("myTag",t.message)
            }
            override fun onResponse(call: Call<List<ImageList>>, response: Response<List<ImageList>>) {
                val imgList  = response.body()!!
                val pager : ViewPager = findViewById(R.id.pager)
                val pAdapter = ImageAdapter(context, imgList as ArrayList<ImageList>)
                pager.adapter = pAdapter
                pager.currentItem = index
            }
        })
    }
}


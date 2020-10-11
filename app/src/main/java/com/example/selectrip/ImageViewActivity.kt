package com.example.selectrip

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import com.example.selectrip.Adapter.ImageListView
import com.example.selectrip.POJO.ImageList
import com.example.selectrip.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_image_view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//사용자 리뷰 및 해당 가게 사진을 모아서 띄우는 이미지 앨범
class ImageViewActivity : AppCompatActivity() {
    lateinit var imageList : List<ImageList>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        val intent = intent
        var name : String = intent.getStringExtra("name")  //플레이스명

        name_image.text = name

        back_imageList.setOnClickListener {
            finish()
        }

        val grdView : GridView = findViewById(R.id.grdView)
        getImageList(name!!,this,grdView)
        grdView.setOnItemClickListener { adapterView, view, position, id ->
            //사진 경로를 가져올 때마다 url받아옴
            val intent = Intent(this, ImagePagerAcitivity::class.java)
            intent.putExtra("place",name)
            intent.putExtra("index",position)
            startActivity(intent)

        }
    }

    fun getImageList(name : String,context: Context, grdView : GridView){
        MyRetrofit.getInstance(this).getImage(name).enqueue(object : Callback<List<ImageList>>{
            override fun onFailure(call: Call<List<ImageList>>, t: Throwable) {
                Log.d("myTag",t.message)
            }
            override fun onResponse(call: Call<List<ImageList>>, response: Response<List<ImageList>>) {
                imageList  = response.body()!!
                grdView.adapter = ImageListView(context, imageList as ArrayList<ImageList>)

            }
        })
    }
}


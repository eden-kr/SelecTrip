package com.example.selectrip

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_image_view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

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
class ImageListView(var context: Context,var list : ArrayList<ImageList>) : BaseAdapter(){
    lateinit var view : View
    lateinit var holder : ImageViewHolder
    override fun getView(postion: Int, counterView: View?, parent: ViewGroup?): View {
        holder = ImageViewHolder()
        if(counterView == null){
            view = LayoutInflater.from(context).inflate(R.layout.image,null)
            holder.image = view.findViewById(R.id.each_image)
            view.tag = holder
        }else{
            holder = counterView.tag as ImageViewHolder
            view = counterView
        }
        Glide.with(context).load(list[postion].img).into(holder.image!!)        //이미지 설정

        holder.image?.setPadding(2,2,2,2)
        holder.image?.scaleType = ImageView.ScaleType.FIT_XY

        return view
    }

    override fun getItem(p0: Int): Any {
        return p0
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
}
//이미지 홀더
class ImageViewHolder{
    var image : ImageView? = null
}

//이미지를 받아올 클래스
//koltin Pojo class
class ImageList(
    @Expose
    @SerializedName("NAME")
    var name : String,
    @Expose
    @SerializedName("image")
    var img : String
):Serializable
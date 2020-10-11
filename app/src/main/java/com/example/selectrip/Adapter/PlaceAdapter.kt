package com.example.selectrip.Adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selectrip.DTO.Place
import com.example.selectrip.DTO.PlaceReview
import com.example.selectrip.PlaceActivity
import com.example.selectrip.R
import kotlinx.android.synthetic.main.place_layout.view.*

class PlaceAdapter(var context: Context, val list : List<Place>, var rList : List<PlaceReview>) : RecyclerView.Adapter<PlaceHolder>(){
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
            val intent = Intent(context, PlaceActivity::class.java)
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

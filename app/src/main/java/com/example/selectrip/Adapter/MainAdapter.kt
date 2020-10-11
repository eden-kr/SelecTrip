package com.example.selectrip.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selectrip.DTO.City
import com.example.selectrip.DetailActivity
import com.example.selectrip.R
import kotlinx.android.synthetic.main.country.view.*

class MainAdapter(var list : List<City>, var context: Context) : RecyclerView.Adapter<MainViewHolder>(){
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

class MainViewHolder(view : View) : RecyclerView.ViewHolder(view){
    var cName = view.countryName //도시명
    var ctr = view.cityName     //국가명
    var src = view.countrySrc   //이미지
}

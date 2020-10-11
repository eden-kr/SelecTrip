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
import com.example.selectrip.DTO.City
import com.example.selectrip.DetailActivity
import com.example.selectrip.R
import kotlinx.android.synthetic.main.search_layout.view.*

class CityRecyclerAdapter(var context : Context, private var list : ArrayList<City>) : RecyclerView.Adapter<CityHolder>(){
    override fun getItemCount(): Int {
        return list.size
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        var city = list[position]
        holder.name.text = city.city
        Glide.with(context).load(city.mainImg).into(holder.image)
        holder.image.clipToOutline = true

        //클릭 리스너
        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition
            val intent = Intent(context, DetailActivity::class.java)
            //도시이름, city객체
            val bundle = Bundle()
            bundle.putSerializable("City",list[pos])
            intent.putExtra("bundle",bundle)
            intent.putExtra("CITYNAME",list[pos].city)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.search_layout,null)

        return CityHolder(view)
    }
}
class CityHolder(view : View) : RecyclerView.ViewHolder(view) {
    var name = view.searchName
    var image = view.searchImg
}


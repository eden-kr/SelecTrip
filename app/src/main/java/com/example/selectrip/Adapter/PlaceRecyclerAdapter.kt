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
import com.example.selectrip.PlaceActivity
import com.example.selectrip.R
import kotlinx.android.synthetic.main.search_layout.view.*

class PlaceRecyclerAdapter(var context : Context, private var list : ArrayList<Place>) : RecyclerView.Adapter<PlaceRecyclerHolder>(){
    override fun getItemCount(): Int {
        return list.size
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: PlaceRecyclerHolder, position: Int) {
        var place = list[position]
        holder.name.text = place.name
        Glide.with(context).load(place.image).into(holder.image)
        holder.image.clipToOutline = true

        //클릭 리스너
        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition
            val intent = Intent(context, PlaceActivity::class.java)
            //도시이름, city객체
            val bundle = Bundle()
            bundle.putSerializable("place",list[pos])
            intent.putExtra("bundle",bundle)
            context.startActivity(intent)
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceRecyclerHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.search_layout,null)
        return PlaceRecyclerHolder(view)
    }
}
class PlaceRecyclerHolder(view : View) : RecyclerView.ViewHolder(view) {
    var name = view.searchName
    var image = view.searchImg

}
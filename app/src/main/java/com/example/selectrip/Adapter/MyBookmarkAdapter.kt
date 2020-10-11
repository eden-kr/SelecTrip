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
import kotlinx.android.synthetic.main.bookmark_layout.view.*

class MyBookmarkAdapter(var context : Context, var list : ArrayList<Place>) : RecyclerView.Adapter<BookmarkHolder>() {
    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: BookmarkHolder, position: Int) {
        var place = list[position]
        holder.add.text = place.address
        holder.city.text = place.cityname
        holder.place.text = place.name
        holder.hour.text = place.hour
        holder.type.text = place.type
        Glide.with(context).load(place.image).into(holder.image)
        holder.image.clipToOutline = true


        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition        //position
            val intent = Intent(context, PlaceActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("place",list[pos])
            intent.putExtra("bundle",bundle)      //가게정보 객체를 PlaceActivity에 넘겨주고 실행
            context.startActivity(intent)
        }
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.bookmark_layout,null)

        return BookmarkHolder(view)
    }
}
class BookmarkHolder(view: View) : RecyclerView.ViewHolder(view){
    var place = view.myPlace
    var type = view.myType
    var city = view.myCity
    var hour = view.myHour
    var add = view.myAddress
    var image = view.myImage

}
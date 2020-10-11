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
import kotlinx.android.synthetic.main.recommend_city.view.*

//리사이클뷰 어댑터
class RecAdapter(private var List: ArrayList<City>, var context: Context) : RecyclerView.Adapter<RecViewHolder>() {
    override fun getItemCount(): Int {
        return List.size
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {

        holder.name.text = List[position].city
        Glide.with(context).load(List[position].img).into(holder.image)

        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition
            if(pos != RecyclerView.NO_POSITION){     //포지션이 없는지 검사
                val intent = Intent(context, DetailActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("City",List[pos])
                intent.putExtra("bundle",bundle)
                intent.putExtra("CITYNAME",holder.name.text.toString())
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recommend_city, parent, false)
        return RecViewHolder(view)

    }
}
//리사이클러뷰 홀더
class RecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var image = itemView.city_rec
    var name = itemView.cityname_rec

    //리스트뷰와 다르게 아이템 뷰에서 클릭을 처리함
}
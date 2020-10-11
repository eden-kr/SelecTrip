package com.example.selectrip.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.selectrip.POJO.ImageList
import com.example.selectrip.R
//listView Adapter
class ImageListView(var context: Context, var list : ArrayList<ImageList>) : BaseAdapter(){
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

package com.example.selectrip.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.selectrip.POJO.ImageList
import com.example.selectrip.R

class ImageAdapter(var context : Context, var list : ArrayList<ImageList>) : PagerAdapter(){
    lateinit var view : View

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        view = LayoutInflater.from(context).inflate(R.layout.viewpager_image,null)
        var viewPagerImage : ImageView = view.findViewById(R.id.selectedImage)

        Glide.with(context).load(list[position].img).into(viewPagerImage)        //이미지 설정
        container.addView(view)
        return view
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view==`object`
    }
    override fun getCount(): Int {
        return list.size
    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
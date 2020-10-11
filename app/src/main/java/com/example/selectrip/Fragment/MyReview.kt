package com.example.selectrip.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.ReviewAdapter
import com.example.selectrip.DTO.UserReviewDTO
import com.example.selectrip.R

class MyReview() : Fragment(){
    var list : ArrayList<UserReviewDTO>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = arguments?.getSerializable("list") as? ArrayList<UserReviewDTO>
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout,container,false)
        var recycleAdapter = view.findViewById<RecyclerView>(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        var mReview = list?.let { ReviewAdapter(requireActivity(), it) }
        recycleAdapter.adapter = mReview

        return view
    }
}
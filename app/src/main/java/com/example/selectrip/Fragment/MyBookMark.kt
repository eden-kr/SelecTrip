package com.example.selectrip.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.MyBookmarkAdapter
import com.example.selectrip.DTO.Place
import com.example.selectrip.R

class MyBookmark() : Fragment(){
    lateinit var list : ArrayList<Place>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = arguments?.getSerializable("list") as ArrayList<Place>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout,container,false)
        var recycleAdapter = view.findViewById<RecyclerView>(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        var mBook = MyBookmarkAdapter(
            requireActivity(),
            list
        )
        //   var mBook = activity?.let { MyBookmarkAdapter(it,list) }
        recycleAdapter.adapter = mBook

        // recycleAdapter.adapter = activity?.let { MyBookmarkAdapter(it,list) }
        return view
    }


}
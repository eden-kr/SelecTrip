package com.example.selectrip.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.PlaceRecyclerAdapter
import com.example.selectrip.Adapter.PlaceRecyclerHolder
import com.example.selectrip.DTO.Place
import com.example.selectrip.R

//플레이스명 검색
class PlaceFragment : Fragment() {
    lateinit var mAdapter: RecyclerView.Adapter<PlaceRecyclerHolder>
    lateinit var recycleAdapter: RecyclerView

    companion object {
        const val KEY = "list"
        @JvmStatic
        fun newInstance(list: ArrayList<Place>) = PlaceFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY, list)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
        recycleAdapter.adapter = mAdapter
    }
    val plist by lazy { requireArguments().getSerializable(KEY) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myTag", "${plist}")

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout, container, false)
        recycleAdapter = view.findViewById(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        mAdapter = activity?.let { PlaceRecyclerAdapter(it, plist as ArrayList<Place>) }!!
        recycleAdapter.adapter = mAdapter
        return view
    }
}

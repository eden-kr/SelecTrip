package com.example.selectrip.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.CityHolder
import com.example.selectrip.Adapter.CityRecyclerAdapter
import com.example.selectrip.DTO.City
import com.example.selectrip.R

//도시명 검색
class CityFragment : Fragment() {
    lateinit var mAdapter: RecyclerView.Adapter<CityHolder>
    lateinit var recycleAdapter: RecyclerView

    companion object {
        const val KEY = "list"
        @JvmStatic
        fun newInstance(list: ArrayList<City>) = CityFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY, list)
            }
        }
    }
    val list by lazy { requireArguments().getSerializable(KEY) }
    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
        recycleAdapter.adapter = mAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myTag", "${list}")

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
        mAdapter = activity?.let { CityRecyclerAdapter(it, list as ArrayList<City>) }!!
        recycleAdapter.adapter = mAdapter
        return view
    }
}

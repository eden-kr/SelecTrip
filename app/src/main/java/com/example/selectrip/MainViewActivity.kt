package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selectrip.Adapter.MainAdapter
import com.example.selectrip.DTO.City
import com.example.selectrip.Retrofit.MyRetrofit
import com.example.selectrip.Retrofit.RetrofitInterface
import com.example.selectrip.Retrofit.RxRetrofit
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_view.*

class MainViewActivity : AppCompatActivity() {
    private val composite = CompositeDisposable()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view)


        //툴바 설정
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (appBarLayout.totalScrollRange == 0 || verticalOffset == 0) {
                gone_layout.visibility = View.VISIBLE
            } else {
                gone_layout.visibility = View.GONE

            }
        })

        textView3.text = "${nickName!!}님, 어디로 떠나볼까요?"
        search_main.bringToFront()
        var disposable : Disposable? = null
        //리사이클러뷰
        disposable = RxRetrofit.getInstance(this).create(RetrofitInterface::class.java).getMainView()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { items ->
                recycle_main.adapter = MainAdapter(items.toMutableList(),this)
                recycle_main.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            }.apply { composite.add((this)) }


        selectPhoto.setOnClickListener {
            val intent = Intent(this, SelectImageActivity::class.java)
            startActivity(intent)
        }
        //검색클릭 시
        search_main.setOnClickListener {
            val text: EditText = findViewById(R.id.search_main)
            val topic = text.text.toString()        //검색할 단어
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("topic", topic)
            startActivity(intent)

        }

        //드로어 버튼 클릭 시
        drawer_madin.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        composite.dispose()
    }

}


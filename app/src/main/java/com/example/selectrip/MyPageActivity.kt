package com.example.selectrip

import android.content.Context
import android.graphics.Point
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import com.example.selectrip.DTO.Place
import com.example.selectrip.DTO.UserReviewDTO
import com.example.selectrip.Fragment.MyBookmark
import com.example.selectrip.Fragment.MyReview
import com.example.selectrip.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_my_page.*
import java.lang.Exception

class MyPageActivity : AppCompatActivity() {
    private val bookmarkFrag = MyBookmark()
    private val reviewFrag = MyReview()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        myId.text = Id
        mynickname.text = nickName

        //프래그먼트에 인자 전달
        MyReviewTh(this).execute()

        val fragmentManager = supportFragmentManager
        val frag = fragmentManager.beginTransaction()
        frag.add(R.id.myfragment,reviewFrag)    //프래그먼트 실행
        frag.commit()


        //북마크 클릭 시
        myBookmark.setOnClickListener {
            val ani = TranslateAnimation(0F, (getWidth()/2).toFloat(), 0F,0F)       //애니메이션 설정
            ani.duration = 1200
            ani.fillAfter = true        //이미지가 유지되도록
            view1.startAnimation(ani)
            MyBookmarTh(this).execute()
        }
        //리뷰 클릭 시
        myReview.setOnClickListener {
            val ani = TranslateAnimation( (getWidth()/2).toFloat(), 0F, 0F,0F)
            ani.duration = 1000
            ani.fillAfter = true        //이미지가 유지되도록
            view1.startAnimation(ani)
            MyReviewTh(this).execute()
            onClick(reviewFrag)
        }

        myPageBack.setOnClickListener {
            finish()
        }
    }
    fun getWidth() : Int{
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size.x
    }
    //프래그먼트 전환
    fun onClick(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val frag = fragmentManager.beginTransaction()
        frag.replace(R.id.myfragment,fragment)
        frag.commit()
    }
    //마이페이지 리뷰가져오기
    inner class MyReviewTh(var context: Context) : AsyncTask<Void,Void,ArrayList<UserReviewDTO>>(){
        var list = arrayListOf<UserReviewDTO>()
        override fun doInBackground(vararg p0: Void?): ArrayList<UserReviewDTO> {
            try {
                MyRetrofit.getInstance(context).setReview(Id!!,"review").execute().body()?.let { list.addAll(it) }
            }catch (e : Exception){
                throw e
            }
            return list
        }

        override fun onPostExecute(result: ArrayList<UserReviewDTO>?) {
            super.onPostExecute(result)
            val bundle = Bundle()
            bundle.putSerializable("list",result)
            reviewFrag.arguments = bundle
        }
    }
    //마이페이지 즐겨찾기 가져오기
    inner class MyBookmarTh(var context : Context) : AsyncTask<Void,Void,ArrayList<Place>>(){
        var list = arrayListOf<Place>()
        override fun doInBackground(vararg p0: Void?): ArrayList<Place> {
            try {
                MyRetrofit.getInstance(context).setBookmark(Id!!,"bookmark").execute().body()?.let { list.addAll(it) }
            }catch (e : Exception){
                throw e
            }
            return list
        }
        override fun onPostExecute(result: ArrayList<Place>?) {
            super.onPostExecute(result)
            val bundle = Bundle()
            bundle.putSerializable("list",result)
            bookmarkFrag.arguments = bundle
            //프래그먼트 이동
            onClick(bookmarkFrag)
        }
    }
}



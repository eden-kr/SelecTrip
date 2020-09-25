package com.example.selectrip

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_my_page.*
import java.lang.Exception

class MyPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        myId.text = Id
        mynickname.text = nickName

        //프래그먼트에 인자 전달
        val myReviewFrag = MyReview()
        val rList = MyReviewTh(this).execute().get()
        Log.d("myTag","결과가 몇개나 들어왔나요? ${rList.size}")
        val bundle = Bundle()
        bundle.putSerializable("list",rList)
        myReviewFrag.arguments = bundle

        val fragmentManager = supportFragmentManager
        val frag = fragmentManager.beginTransaction()
        frag.add(R.id.myfragment,myReviewFrag)    //프래그먼트 실행
        frag.commit()


        //북마크 클릭 시
        myBookmark.setOnClickListener {
            val ani = TranslateAnimation(0F, (560).toFloat(), 0F,0F)       //애니메이션 설정
            ani.duration = 1200
            ani.fillAfter = true        //이미지가 유지되도록
            view1.startAnimation(ani)

            //데이터 전달
            val myBookmarkFrag = MyBookmark()
            val bList = MyBookmarTh(this).execute().get()
            val bundle = Bundle()
            bundle.putSerializable("list",bList)
            myBookmarkFrag.arguments = bundle
            //프래그먼트 이동
            onClick(myBookmarkFrag)

        }
        //리뷰 클릭 시
        myReview.setOnClickListener {
            val ani = TranslateAnimation((560).toFloat(), 0F, 0F,0F)
            ani.duration = 1000
            ani.fillAfter = true        //이미지가 유지되도록
            view1.startAnimation(ani)

            val myReviewFrag = MyReview()
            myReviewFrag.arguments = bundle
            onClick(myReviewFrag)
        }

        myPageBack.setOnClickListener {
            finish()
        }
    }
    //프래그먼트 전환
    fun onClick(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val frag = fragmentManager.beginTransaction()
        frag.replace(R.id.myfragment,fragment)
        frag.commit()
    }
    //마이페이지 리뷰가져오기
    inner class MyReviewTh(var context: Context) : AsyncTask<Void,Void,ArrayList<UserReviewVO>>(){
        var list = arrayListOf<UserReviewVO>()
        override fun doInBackground(vararg p0: Void?): ArrayList<UserReviewVO> {
            try {
                MyRetrofit.getInstance(context).setReview(Id!!,"review").execute().body()?.let { list.addAll(it) }
            }catch (e : Exception){
                throw e
            }
            return list
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
    }
}
class MyReview() : Fragment(){
    lateinit var list : ArrayList<UserReviewVO>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         list = arguments?.getSerializable("list") as ArrayList<UserReviewVO>
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.my_fragment_layout,container,false)
        var recycleAdapter = view.findViewById<RecyclerView>(R.id.myFragRecycle)
        val layoutManager = LinearLayoutManager(activity)
        recycleAdapter.layoutManager = layoutManager
        var mReview = ReviewAdapter(requireActivity(),list)
        recycleAdapter.adapter = mReview

        return view
    }
}
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
        var mBook = MyBookmarkAdapter(requireActivity(),list)
     //   var mBook = activity?.let { MyBookmarkAdapter(it,list) }
        recycleAdapter.adapter = mBook

       // recycleAdapter.adapter = activity?.let { MyBookmarkAdapter(it,list) }
        return view
    }


}

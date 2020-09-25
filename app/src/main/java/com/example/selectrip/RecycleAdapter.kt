package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.review_layout.view.*
import kotlinx.android.synthetic.main.search_layout.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class CityRecyclerAdapter(var context : Context, private var list : ArrayList<City>) : RecyclerView.Adapter<CityHolder>(){
    override fun getItemCount(): Int {
        return list.size
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        var city = list[position]
        holder.name.text = city.city
        Glide.with(context).load(city.mainImg).into(holder.image)
        holder.image.clipToOutline = true
        
        //클릭 리스너
        holder.itemView.setOnClickListener {                
            var pos = holder.adapterPosition
            val intent = Intent(context,DetailActivity::class.java)
            //도시이름, city객체
            val bundle = Bundle()
            bundle.putSerializable("City",list[pos])
            intent.putExtra("bundle",bundle)
            intent.putExtra("CITYNAME",list[pos].city)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.search_layout,null)

        return CityHolder(view)
    }
}
class CityHolder(view : View) : RecyclerView.ViewHolder(view) {
    var name = view.searchName
    var image = view.searchImg
}

class PlaceRecyclerAdapter(var context : Context, private var list : ArrayList<Place>) : RecyclerView.Adapter<PlaceRecyclerHolder>(){
    override fun getItemCount(): Int {
        return list.size
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: PlaceRecyclerHolder, position: Int) {
        var place = list[position]
        holder.name.text = place.name
        Glide.with(context).load(place.image).into(holder.image)
        holder.image.clipToOutline = true

        //클릭 리스너
        holder.itemView.setOnClickListener {
            var pos = holder.adapterPosition
            val intent = Intent(context,PlaceActivity::class.java)
            //도시이름, city객체
            val bundle = Bundle()
            bundle.putSerializable("place",list[pos])
            intent.putExtra("bundle",bundle)
            context.startActivity(intent)
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceRecyclerHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.search_layout,null)
        return PlaceRecyclerHolder(view)
    }
}
class PlaceRecyclerHolder(view : View) : RecyclerView.ViewHolder(view) {
    var name = view.searchName
    var image = view.searchImg

}
//리뷰 가져올 어댑터
class ReviewAdapter(var context: Context, private var reviewList: ArrayList<UserReviewVO>) :
    RecyclerView.Adapter<ReviewHolder>() {
    private var state = false

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        var review = reviewList[position]
        holder.userId.text = review.userName
        holder.date.text = review.date
        holder.review.text = review.review
        holder.rating.rating = review.rating.toFloat()
        holder.count.text = review.like.toString()
        Log.d("myTag","포스트 넘버"+review.id)
        if(review.image!="p") {
            Glide.with(context).load(review.image).into(holder.userImage)
        }
        //좋아요 버튼
        holder.like.setOnClickListener {
            var pos = holder.adapterPosition
            var r = reviewList[pos]
            var cnt = 0
            state = !state
            if (state) {
                cnt = 1
                var res = LikeCountThread(context,r.id,reviewList[position].like+cnt,"plus").execute().get()
                Log.d("myTag",res.toString())
                when(res){
                    "ok" -> {
                        reviewList[position].like +=cnt
                        holder.count.text = reviewList[position].like.toString()
                    }
                    "reok" -> {
                        reviewList[position].like +=cnt
                        holder.count.text = reviewList[position].like.toString()
                    }
                }
                notifyDataSetChanged()
            } else {
                cnt= if(reviewList[position].like==0) 0 else -1
                Log.d("myTag","카운트 $cnt")
                var res = LikeCountThread(context,r.id,reviewList[position].like+cnt,"minus").execute().get()
                Log.d("myTag",res.toString())

                if(res == "reset"){
                    reviewList[position].like +=cnt
                    holder.count.text = reviewList[position].like.toString()
                }
                notifyDataSetChanged()
            }
        }

        //DELETE, UPDATE
        if (review.userId == Id) {    //현재 로그인된 아이디와 리뷰의 아이디가 같으면 삭제 버튼을 활성화
            holder.delete.isClickable = true
            holder.delete.isVisible = true
            var pos = holder.adapterPosition

            holder.delete.setOnClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.review_ud,null)
                val dlg = BottomSheetDialog(context)
                dlg.setContentView(view)
                val delete = view.findViewById<TextView>(R.id.delete_review)
                val update = view.findViewById<TextView>(R.id.update_review)

                //DELETE
                delete.setOnClickListener {
                    var ref : StorageReference? = null
                    if(reviewList[pos].storageUrl!="x") {
                        ref = FirebaseStorage.getInstance().reference.child(reviewList[pos].storageUrl)
                    }
                    context.alert("삭제하시겠습니까?"){yesButton {
                        MyRetrofit.getInstance(context).deleteReview(reviewList[pos].id).enqueue(object :
                            Callback<String> {
                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.d("myTag",t.message)
                            }
                            //플라스크, 파이어베이스 스토리지에서 파일 삭제
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                ref?.delete()?.addOnSuccessListener {
                                    Log.d("myTag","파이어베이스 스토리지에서 파일 삭제가 완료되었습니다.")
                                }?.addOnFailureListener {
                                    Log.d("myTag",it.message)
                                }
                                reviewList.removeAt(pos)
                                Log.d("myTag","플라스크 서버에서 파일 삭제가 완료되었습니다.")
                                Toast.makeText(context,"삭제가 완료되었습니다.",Toast.LENGTH_SHORT).show()
                                notifyItemRemoved(position)     //리사이클러뷰 갱신
                                notifyDataSetChanged()
                            }
                        })
                    }}.show()

                }
                update.setOnClickListener {
                        val intent = Intent(context, ReviewActivity::class.java)
                        intent.putExtra("postId",reviewList[pos].id)
                        intent.putExtra("prevName",reviewList[pos].placeName)      //가게명
                        intent.putExtra("storageUrl",reviewList[pos].storageUrl)
                    Log.d("myTag",reviewList[pos].storageUrl)
                        intent.putExtra("id",reviewList[pos].userId)    //id
                        intent.putExtra("review",reviewList[pos].review)    //review content
                        context.startActivity(intent)        //액티비티에서 끌어와서 리뷰 정보 받기기
                    notifyDataSetChanged()

                }
                dlg.show()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.review_layout, null)
        return ReviewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
class ReviewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var userId = view.user_id
    var date = view.user_date
    var rating = view.user_rating
    var review = view.user_review
    var userImage = view.user_image
    var delete = view.review_delete
    var like = view.like_review
    var count = view.review_count
}
class LikeCountThread(var context: Context, var id: Int, var cnt: Int,var type : String) : AsyncTask<Void,Void,String>(){
    var res = ""
    override fun doInBackground(vararg p0: Void?): String {
        try{
            res =MyRetrofit.getInstance(context).setLikeCount(id,cnt,Id!!,type).execute().body().toString()
        }catch (e: Exception){
            Log.d("myTag", e.printStackTrace().toString())
        }
        return res
    }
}
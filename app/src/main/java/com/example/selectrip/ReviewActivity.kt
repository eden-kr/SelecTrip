package com.example.selectrip

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.selectrip.DTO.ImageDTO
import com.example.selectrip.DTO.UpdateReviewDTO
import com.example.selectrip.DTO.UserReview
import com.example.selectrip.Retrofit.MyRetrofit
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_review.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

//Review Create, Update
class ReviewActivity : AppCompatActivity() {
    private val REQUEST_READ_EXTERNAL_STORAGE = 1000 //
    private val PICK_IMAGE = 1111
    var placeName : String? = ""
    lateinit var uReview : UserReview
    var updateRv : UpdateReviewDTO? = null
    var prevUrl : String? = null
    var prevName : String? = null
    var case = 1        //update 시 1이면 글만 저장 2면 사진 저장
    var postId : Int? = null
    lateinit var imageVo : ImageDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        setSupportActionBar(review_toolbar)
        val intent = intent
        placeName  = intent?.getStringExtra("placeName")        //도시명
        postId = intent?.getIntExtra("postId",0)       //게시글 기본키
        prevName  = intent?.getStringExtra("prevName")              //플레이스명
        prevUrl = intent?.getStringExtra("storageUrl")              //firebase storage Url

        review_title.text = placeName
        if(prevName!= null){
            review_title.text = prevName
        }
        //back 버튼 설정
        back_review.setOnClickListener {
            finish()
        }
        //별점을 매길수 있도록 ratingbar 리스너 설정
        review_rating.setOnRatingBarChangeListener { ratingBar: RatingBar, fl: Float, b: Boolean ->
        }
        //이미지 업로드
        review_addPhoto.setOnClickListener {
            //권한 요청
            if (ContextCompat.checkSelfPermission(  //권한 확인
                    this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE)) {   //권한 재요청
                    //anko-library 사용
                    alert("사진 정보를 얻기 위해서 권한이 필요합니다.") {
                        yesButton {
                            //권한 요청
                            ActivityCompat.requestPermissions(this@ReviewActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
                        }
                        noButton { }
                    }.show()
                } else {    // 권한요청
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_EXTERNAL_STORAGE
                    )
                }
            } else {
                //이미 권한이 허용된 경우
                val intent2 = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )   //갤러리에서 이미지 가져오기
                startActivityForResult(intent2, PICK_IMAGE)
            }
        }
    }
    //파일 절대 경로 가져오기
    private fun getPath(uri: Uri): String {
        var dt: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c = contentResolver.query(uri, dt, null, null, null)
        var index = c?.getColumnIndex(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        var result = index?.let { c?.getString(it) }

        return result.toString()
    }
    //사진 선택 후 결과값을 storage -> db에 저장
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val r = Random.nextInt(1000)

        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            //외부 저장소에서 사진 받아오기
            val ips: InputStream? = data?.data?.let { contentResolver.openInputStream(it) }
            var img: Bitmap = BitmapFactory.decodeStream(ips)
            photo1.setImageBitmap(img)      //이미지 설정
            photo1.isVisible = true
            var f: File = File(data?.data?.let { getPath(it) })        //파일 절대 경로 생성
            var file = Uri.fromFile(f)

            //CREATE
            //firebase Storage에 이미지 저장
            if (prevUrl.isNullOrEmpty() ) {
                var stRef = Id?.let {
                    Firebase.storage.reference.child(it).child("$placeName$r")
                }       //파이어베이스 레퍼런스 생성
                val uploadTask = stRef?.putFile(file)?.addOnSuccessListener {
                    var task = it.metadata?.reference?.downloadUrl
                    var ref = it.metadata?.path     //파이어베이스에서 이미지 지울 경로 DB에 넣어야함
                    task?.addOnSuccessListener {
                        var url = it.toString()         //image url
                        var path = it.lastPathSegment.toString()
                        imageVo = ImageDTO(url,path)
                    }
                }
                //UPDATE
            } else {
                //리뷰 수정
                Log.d("myTag","$prevUrl")

                var updateRef: StorageReference?

                updateRef = if(prevUrl!= "x"){
                    var p = prevUrl!!.split("/")
                    var child1 = p[0]
                    var child2 = p[1]
                    Firebase.storage.reference.child(child1).child(child2)
                }else{
                    Id?.let {
                        Firebase.storage.reference.child(it).child("${review_title.text}$r")
                    }      //기존 리뷰에 이미지가 없었다면 새로 추가할 수 있도록 레퍼런스 생성
                }
                val uploadTask = updateRef?.putFile(file)?.addOnSuccessListener {
                    var task = it.metadata?.reference?.downloadUrl
                    var ref = it.metadata?.path
                    task?.addOnSuccessListener {
                        var url = it.toString()         //image url
                        var path = it.lastPathSegment.toString()
                        imageVo = ImageDTO(url,path)
                        Log.d("myTag","fireStorage! catch? ${imageVo.url}")
                        case = 2
                    }
                }
            }
            ips?.close()
        }
    }
    //리뷰 수정
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateReview(url : String, path : String, id : Int):UpdateReviewDTO {
        var rv : String = review.text.toString()                  //리뷰내용
        var rate : Double? = if(review_rating.rating.equals(0)) 1.0 else review_rating.rating.toDouble()          //평점
        return UpdateReviewDTO(id,url,path,rate!!,rv)
    }
    //툴바에 아이콘 메뉴 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.review_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //툴바 아이콘에 리스너 달기
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.review_bar -> {
                if (prevUrl == null) {       //CREATE
                    uReview = if (photo1.visibility == View.INVISIBLE){
                        writeReview(review, review_title, review_rating, "p", "x")
                    }else{
                        writeReview(review, review_title, review_rating, imageVo.url, imageVo.storageUrl)    //userReview 객체 생성 -> Flask로 전송
                    }
                    //리뷰 작성 시 데이터베이스에 기록할 비동기 Retrofit
                    MyRetrofit.getInstance(this).setReview(uReview).enqueue(object : Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d("myTag", t.message)
                        }
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            var res = response.body()
                            Log.d("myTag", "${response.body()}")
                            Log.d("myTag", "uReview 두번째 초기화 확인 -> ${uReview.image}")
                            if (res == "리뷰작성") {
                                Toast.makeText(this@ReviewActivity, "리뷰가 작성되었습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@ReviewActivity, "다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
                else{           //UPDATE
                    //사진이 없으면 url에 "p" ,path에 "x"로 초기화
                    Log.d("myTag",case.toString())
                    updateRv = if(case==1){
                        updateReview("p","x",postId!!)
                    } else{
                        Log.d("myTag","catch? ${imageVo.url}")
                        updateReview(imageVo.url,imageVo.storageUrl,postId!!)
                    }
                    MyRetrofit.getInstance(this).updateReview(updateRv!!.postId, updateRv!!.newReview, updateRv!!.rate, updateRv!!.imageUrl, updateRv!!.imagePath)
                        .enqueue(object : Callback<String>{
                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.d("myTag",t.message)
                            }
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                Log.d("myTag",response.message())
                                Toast.makeText(this@ReviewActivity,"리뷰가 수정되었습니다.",Toast.LENGTH_SHORT).show()
                            }
                        })
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
//userReview 초기화
@RequiresApi(Build.VERSION_CODES.O)
fun writeReview(review : TextView, title : TextView, rating : RatingBar, url : String, storageUrl : String) : UserReview{
    var rv = review.text.toString()                  //리뷰내용
    var rv_title = title.text.toString()     //리뷰 쓸 가게명
    var rate = rating.rating.toDouble()             //평점
    var now =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))     //현재시간
    var like =  0

    return UserReview(rv_title, rv, nickName!!, rate, Id!!, now, url,storageUrl,like)
}

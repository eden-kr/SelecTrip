package com.example.selectrip

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.selectrip.Retrofit.MyRetrofit
import kotlinx.android.synthetic.main.activity_select_image.*
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*

var cityName: String = ""  //컴퓨터가 select한 도시명  (추천 도시명)

class SelectImageActivity : AppCompatActivity() {

    private val REQUEST_READ_EXTERNAL_STORAGE = 1000 //
    var file: File? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)
        val selectedImage = findViewById<ImageView>(R.id.selectedImage)
        setSupportActionBar(selectToolbar)

        // select_skip.paintFlags = Paint.UNDERLINE_TEXT_FLAG      //밑줄치기
        Log.d("myTag", "userNickname $nickName")
        selectedImage.clipToOutline = true

        Log.d("myTag", "사용자아이디 $Id")

        //스킵 시 메인으로
        select_skip.setOnClickListener {
            val intent = Intent(this, MainViewActivity::class.java)
            intent.putExtra("ID", nickName)   //사용자 아이디도 같이 보내서 즐겨찾기 기능 구현
            startActivity(intent)
        }

        //사진 선택
        //버튼을 나눈 이유 -> 텐서플로우 구동 속도 때문에
        select_ok.setOnClickListener {
            openGallery()
        }
        nextbtn_select.setOnClickListener {
            //Retrofit으로 python flask와 통신 시작
            file?.let { it1 ->
                RetrofitConn(this, it1) }

            val progressDlg = ProgressDialog(this)
            progressDlg.setMessage("이미지 분석 중..")
            progressDlg.show()
            val timer = Timer()
            timer.schedule(object : TimerTask(){
                override fun run() {
                    progressDlg.dismiss()   //시간 지나면 프로그레스가 꺼짐
                    timer.cancel()
                }
            },8000)         //텐서플로우 구동시간+서버에서 응답 받아오는 시간만큼 프로그레스바 실행

        }

    }

    //back버튼 불가
    override fun onBackPressed() {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.review_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun openGallery() {
        //권한 요청
        if (ContextCompat.checkSelfPermission(  //권한 확인
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {   //권한 재요청
                //anko-library 사용
                alert("사진 정보를 얻기 위해서 권한이 필요합니다.") {
                    yesButton {
                        //권한 요청
                        ActivityCompat.requestPermissions(
                            this@SelectImageActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_READ_EXTERNAL_STORAGE
                        )
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
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )   //갤러리에서 이미지 가져오기
            //intent.setType("image/*")
            //intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent, 1)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.review_bar -> {
                if (file?.path != null) {
                    val intent = Intent(this, RecommandActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "이미지를 선택해 주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    //user 저장소에서 사진 받아오기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    //외부 저장소에서 사진 받아오기
                    val ips = data?.data?.let { contentResolver.openInputStream(it) }
                    //Log.d("myTag","${data!!.data}")
                    file = File(data!!.data?.let { getPath(it) })   //갤러리에서 받아온 파일 객체 생성
                    Log.d("myTag", "파일명은 ${file.toString()}")    //파일 만들어졌는지 확인

                    var img: Bitmap = BitmapFactory.decodeStream(ips)
                    selectedImage.setImageBitmap(img)   //받아온 이미지를 이미지뷰에 표시
                    selectedImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    ips?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

    }

    //파일의 절대 경로 가져오기
    private fun getPath(uri: Uri): String {
        var dt: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c = contentResolver.query(uri, dt, null, null, null)
        var index = c?.getColumnIndex(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        var result = index?.let { c?.getString(it) }

        return result.toString()
    }

    //Retrofit2 - Flask
    private fun RetrofitConn(context: Context, file: File) {
        var requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        var body =
            MultipartBody.Part.createFormData("file", "file", requestBody)   //서버의 파일명이랑 맞춰줘야함

        MyRetrofit.getInstance(context).getRequest(body).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(" myTag", t.message)  //통신 실패 시 로그
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("myTag", response.body().toString())
                if (response.body().toString() != null) {
                    cityName = response.body().toString()   //통신 성공 시 이미지 보내고 도시명 받아옴
                }
            }
        })
    }
}

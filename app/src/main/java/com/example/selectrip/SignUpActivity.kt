package com.example.selectrip

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    var checkStatus: Int? = null   //아이디 중복을 확인할 상태 1 = 중복아님

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signId.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(signId.text.toString()).matches()) {     //이메일 형식이 아니라면
                    signId.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.nu, 0)
                    val red = ColorStateList.valueOf(Color.rgb(244,67,54))
                    ViewCompat.setBackgroundTintList(signId,red)        //비밀번호 틀리면 빨간색으로 강조
                    next_signup.isClickable = false         //회원가입 완료 버튼의 클릭 이벤트 취소
                    checkId.setOnClickListener {
                        alert("아이디 형식이 잘못되었습니다!") { yesButton { } }.show()
                    }
                }else{
                    signId.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    val default = ColorStateList.valueOf(Color.rgb(3,218,197))
                    ViewCompat.setBackgroundTintList(signId,default)
                    next_signup.isClickable = true
                    //id 중복체크
                    checkId.setOnClickListener {
                        if (!signId.text.toString().isNullOrEmpty()) {
                            var id = signId.text.toString()    //textView -> text
                            //val server = retrofit.create(CheckAPI::class.java)
                            MyRetrofit.getInstance(this@SignUpActivity).check(id).enqueue(object : Callback<String>{
                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Log.d("myTag",t.message)
                                }
                                override fun onResponse(call: Call<String>, response: Response<String>) {
                                    var flag = response.body().toString()
                                    when(flag){
                                        "중복아님" -> {
                                            checkStatus = 1
                                            alert("사용할 수 있는 아이디입니다.") { yesButton { } }.show()
                                        }
                                        "중복" -> {
                                            checkStatus = 0
                                            alert("아이디가 중복되었습니다.") { yesButton { } }.show()
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
        //회원가입 폼 작성 후 버튼 클릭 시
        next_signup.setOnClickListener {
            //val server = retrofit.create(SignUpAPI::class.java)
                if (!checkNull(signNickName.text.toString()) || checkLength(signId.text.toString(),signPassword.text.toString()) || checkStatus ==1) {     //공백 검사
                    var id = signId.text.toString()
                    var ps = signPassword.text.toString()
                    var nick = signNickName.text.toString()
                    MyRetrofit.getInstance(this).signUp(User(id,ps,nick)).enqueue(object : Callback<String>{
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d("myTag",t.message)
                        }
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            Toast.makeText(this@SignUpActivity,"회원가입이 완료되었습니다.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    })
                        } else {
                            Toast.makeText(this, "아이디 중복 검사가 필요합니다.", Toast.LENGTH_SHORT).show()
                        }
            }

        back_sighup.setOnClickListener {
            finish()
        }

    }
    fun checkNull(nickname: String) : Boolean{
        var flag = false
        if(nickname.isNullOrEmpty()){
            flag = true
            Toast.makeText(this,"닉네임을 입력해주세요.",Toast.LENGTH_SHORT).show()
        }
        return flag
    }
    fun checkLength(id : String, password : String) : Boolean{
        var flag = true
        if(id.length<=6 || password.length <=6) {
            flag= false
            Toast.makeText(this,"아이디와 비밀번호는 최소 6자 이상입니다.",Toast.LENGTH_SHORT).show()
        }
        return flag
    }
}
data class User(        //회원가입에 사용될 User
    @Expose
    @SerializedName("id")
    var id: String,
    @Expose
    @SerializedName("password")
    var password: String,
    @Expose
    @SerializedName("nickname")
    var nickname: String
) {}

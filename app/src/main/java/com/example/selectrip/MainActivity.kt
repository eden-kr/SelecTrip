package com.example.selectrip

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.example.selectrip.DTO.CheckUser
import com.example.selectrip.DTO.User
import com.example.selectrip.Retrofit.MyRetrofit
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*
import kotlin.system.exitProcess

var nickName: String? = null      //현재 사용자 닉네임을 담아 놓은 전역변수
var Id: String? = null
var imageArr = arrayListOf<Int>(R.drawable.p1, R.drawable.p3, R.drawable.p7)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        //메인 화면 바꾸기
        var r = Random()
        main.setBackgroundResource(imageArr[r.nextInt(imageArr.size)])
        var keyHash = Utility.getKeyHash(this)

        //로그인 애니메이션
        setAnim()
        kakaoLogin.setOnClickListener {
            getKaKaoLogin(this)

        }
        id.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                //이메일 형식 검증
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(id.text.toString()).matches()) {     //이메일 형식이 아니라면
                    id.setCompoundDrawablesWithIntrinsicBounds(R.drawable.id, 0, R.drawable.nu, 0)
                    loginButton.setOnClickListener {
                        Toast.makeText(this@MainActivity, "이메일 형식을 확인해주세요.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    id.setCompoundDrawablesWithIntrinsicBounds(R.drawable.id, 0, 0, 0)
                    loginButton.isClickable = true
                    loginButton.setOnClickListener {
                        if (password.text.toString().isNotEmpty()) {
                            var id = id.text.toString()
                            var ps = password.text.toString()
                            var status =
                                LoginIn(this@MainActivity, id, ps).execute()?.get()        //로그인 결과값
                            //body로 보내는 이유 -> POST+body를 통해 조금이나마 보안적 요소를 추가하기 위해
                            when (status?.check) {
                                "로그인" -> {
                                    Id = id
                                    nickName = status.nickname
                                    val intent =
                                        Intent(this@MainActivity, SelectImageActivity::class.java)
                                    startActivity(intent)
                                }
                                else -> Toast.makeText(
                                    this@MainActivity,
                                    "아이디와 비밀번호를 확인해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } else {
                            Toast.makeText(this@MainActivity, "비밀번호는 최소 5자입니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        //비밀번호 찾기 SMTP 사용
        findPassword.setOnClickListener {
            val intent = Intent(this, FindPasswordActivity::class.java)
            startActivity(intent)
        }

        changePw.setOnClickListener {
            val intent = Intent(this, ChangePwActivity::class.java)
            startActivity(intent)
        }

        //회원가입
        signupButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    //backbutton불가
    override fun onBackPressed() {
    }

    fun setAnim() {
        val ani = TranslateAnimation(0F, 0F, 0F, (-370).toFloat())
        ani.duration = 2000
        ani.fillAfter = true
        ani.startOffset = 200

        val login = AlphaAnimation(0F, 1F)
        login.duration = 900
        login.startOffset = 2000

        selectrip.animation = ani
        loginMain.animation = login
    }

    fun getKaKaoLogin(context: Context) {
        var userMail: String?
        var userName: String?
        var password = "kakaoLogin%$#@!@#kakaoLogin@#"
        var u: User? = null
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.d("myTag", "카카오 로그인 성공")
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.d("myTag", "사용자 정보 요청 실패", error)
                    } else if (user != null) {
                        userMail = user.kakaoAccount?.email
                        userName = user.kakaoAccount?.profile?.nickname
                        u = User(userMail!!, password!!, userName!!)
                        checkKaKaoLogin(u!!)
                    }
                }
            }
        }
        if (LoginClient.instance.isKakaoTalkLoginAvailable(context)) {
            LoginClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            LoginClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    //카카오 로그인 중복검사
    fun checkKaKaoLogin(user: User) {
        MyRetrofit.getInstance(this).check(user.id).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("myTag", t.message)
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {
                var res = response.body().toString()
                if (res == "중복") {        //중복이면 회원가입 거치지 않고 바로 로그인
                    Id = user.id
                    nickName = user.nickname
                    val intent = Intent(this@MainActivity, SelectImageActivity::class.java)
                    startActivity(intent)
                } else {
                    setKaKaoLogin(user)
                }
            }
        })
    }

    //DB에 kakao회원정보 입력
    fun setKaKaoLogin(user: User) {
        MyRetrofit.getInstance(this).signUp(user).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("myTag", t.message)
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {       //정보 없으면 db에 정보 입력하고 로그인
                Toast.makeText(this@MainActivity, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                Id = user.id
                nickName = user.nickname
                val intent = Intent(this@MainActivity, SelectImageActivity::class.java)
                startActivity(intent)
            }
        })
    }
}

//동기 Retrofit 서버에서 바로 응답 받아옴
class LoginIn(var context: Context, private val id: String, private val password: String) :
    AsyncTask<Void, Void, CheckUser>() {
    override fun doInBackground(vararg p0: Void?): CheckUser? {
        var sign: CheckUser? = null
        var res = MyRetrofit.getInstance(context).logIn(id, password)
        try {
            sign = res.execute().body()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sign
    }
}

//비정상적인 앱 종료시 메인으로
class ExceptionHandler(var context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)

        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }
}


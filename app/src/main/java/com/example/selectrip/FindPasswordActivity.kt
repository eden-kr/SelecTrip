package com.example.selectrip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_find_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_password)
        var email = edit.text.toString()

        find_close.setOnClickListener {
            finish()
        }

        find_ok.setOnClickListener {
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this,"이메일 형식이 맞지 않습니다.",Toast.LENGTH_SHORT).show()
            }else{
                    MyRetrofit.getInstance(this).findPassword(edit.text.toString()).enqueue(object : Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d("myTag", t.message)
                        }
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            Toast.makeText(this@FindPasswordActivity, "입력하신 이메일로 비밀번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    })
                 }
        }
    }
}

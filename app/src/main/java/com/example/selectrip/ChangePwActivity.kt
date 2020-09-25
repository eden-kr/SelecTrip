package com.example.selectrip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_change_pw.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePwActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pw)
        val fManager = supportFragmentManager
        val frag = fManager.beginTransaction();
        val emailFrag = EmailFragment()
        frag.add(R.id.change_frag,emailFrag).commit()
        change_close.setOnClickListener {
            finish()
        }
    }
    fun replaceFragment(fragment : Fragment, email : String){
        val fManager = supportFragmentManager
        val transaction = fManager.beginTransaction();
        val bundle = Bundle()
        bundle.putString("email",email)
        fragment.arguments = bundle
        transaction.replace(R.id.change_frag, fragment).commit();
    }
}
//이메일 입력 프래그먼트
class EmailFragment : Fragment(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.input_email,container,false)
        val button = view.findViewById<Button>(R.id.email_ok)
        val email = view.findViewById<EditText>(R.id.set_email)
        button.setOnClickListener {
            val act = activity as ChangePwActivity
            act.replaceFragment(PasswordFragment().newInstance(),email.text.toString())
        }
        return view
    }
}
//패스워드 입력 프래그먼트
class PasswordFragment() : Fragment(){
    lateinit var email : String
    fun newInstance() : PasswordFragment{
        return PasswordFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email").toString()
        Log.d("myTag",email)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.input_password,container,false)
        val prev = view.findViewById<EditText>(R.id.prev_pw)
        val new = view.findViewById<EditText>(R.id.new_pw)
        val button = view.findViewById<Button>(R.id.set_ok)

        button.setOnClickListener {
            val prevPw = prev.text.toString()
            val newPw = new.text.toString()

            MyRetrofit.getInstance(requireActivity()).changePassword(email,prevPw,newPw).enqueue(object : Callback<String>{
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("myTag",t.message)
                }
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    var res = response.body().toString()
                    when(res){
                        "success" ->{
                            Toast.makeText(requireActivity(),"변경이 완료되었습니다.",Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                        }
                        else -> {
                            Toast.makeText(requireActivity(),"비밀번호를 확인해주세요.",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
        return view
    }
}
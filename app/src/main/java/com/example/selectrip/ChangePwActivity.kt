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
import com.example.selectrip.Fragment.EmailFragment
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


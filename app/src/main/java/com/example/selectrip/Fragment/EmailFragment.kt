package com.example.selectrip.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.selectrip.ChangePwActivity
import com.example.selectrip.R

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
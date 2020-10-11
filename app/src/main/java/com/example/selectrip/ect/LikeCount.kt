package com.example.selectrip.ect

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.example.selectrip.Id
import com.example.selectrip.Retrofit.MyRetrofit
import java.lang.Exception

class LikeCountThread(var context: Context, var id: Int, var cnt: Int,var type : String) : AsyncTask<Void,Void,String>(){
    var res = ""
    override fun doInBackground(vararg p0: Void?): String {
        try{
            res = MyRetrofit.getInstance(context)
                .setLikeCount(id,cnt, Id!!,type).execute().body().toString()
        }catch (e: Exception){
            Log.d("myTag", e.printStackTrace().toString())
        }
        return res
    }
}
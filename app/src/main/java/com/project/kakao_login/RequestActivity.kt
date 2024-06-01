package com.project.kakao_login

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.common.util.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class RequestActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = javaClass.simpleName
    private val BASE_URL = "http://54.79.101.135:8080"
    private lateinit var mMyAPI: RequestAPI
    private lateinit var mListTv: TextView
    private lateinit var mGetButton: Button
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "keyhash : 일단 호출은됨")

        setContentView(R.layout.request_get)

        mListTv = findViewById(R.id.result1)
        mGetButton = findViewById(R.id.button1)
        mGetButton.setOnClickListener(this)

        // Get User ID from Intent
        userId = intent.getStringExtra("USER_ID")

        initRequestAPI(BASE_URL)
    }

    private fun initRequestAPI(baseUrl: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mMyAPI = retrofit.create(RequestAPI::class.java)
    }

    override fun onClick(v: View) {
//        Log.d(TAG, "keyhash : 일단 클릭은됨")
        if (v == mGetButton) {
            Log.d(TAG, "GET")
            val headers = HashMap<String, String>()
            headers["user-id"] = userId ?: ""

            // 코루틴을 사용하여 백그라운드에서 네트워크 요청 실행
            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        mMyAPI.getPosts(headers).execute()
                    }
                    if (response.isSuccessful) {
                        val mList = response.body()
                        val result = StringBuilder()
                        mList?.forEach { item ->
                            result.append("room: ").append(item.room).append("\n")
                                .append("reply_list: ").append(item.reply_list).append("\n")
                        }

                        // 백그라운드 작업이 완료된 후에 UI 업데이트
                        withContext(Dispatchers.Main) {
                            mListTv.text = result.toString()
                        }
                    } else {
                        Log.d(TAG, "Status Code : ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Fail msg : ${e.message}")
                }
            }
        }
    }
}

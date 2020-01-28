package com.guddu.demointent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context = this

        btn_explict.setOnClickListener {
            val intent = Intent(context,Main2Activity::class.java)
            startActivity(intent)
            finish()
        }
        btn_implicit.setOnClickListener {
            var intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT,"This is the text to be sent")
            intent.type = "text/plain"
            startActivity(intent)

        }
    }
}

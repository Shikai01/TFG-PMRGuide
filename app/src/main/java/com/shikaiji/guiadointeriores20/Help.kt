package com.shikaiji.guiadointeriores20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Help : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val help = findViewById <AppCompatButton> (R.id.back)
        help.setOnClickListener { goBack() }

        val webView = findViewById<WebView>(R.id.webview)

        webView.loadUrl("file:///android_res/raw/instructions.html")


    }

    private fun goBack(){
        val intent= Intent(this, MainView::class.java)

        startActivity(intent)
    }

}
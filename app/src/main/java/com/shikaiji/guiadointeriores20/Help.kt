package com.shikaiji.guiadointeriores20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val textView = findViewById<TextView>(R.id.instructions)
        textView.text = leerArchivo()


    }

    private fun goBack(){
        val intent= Intent(this, MainView::class.java)

        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun leerArchivo(): CharSequence? {
        var text= ""
        try{
            val inputStream: InputStream = resources.openRawResource(R.raw.instructions)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                text += line + "\n"
            }
            reader.close()


        }catch (ex:Exception){
            Toast.makeText(this,"Alerta", Toast.LENGTH_SHORT).show()
            text += ex
        }
        return text
    }
}
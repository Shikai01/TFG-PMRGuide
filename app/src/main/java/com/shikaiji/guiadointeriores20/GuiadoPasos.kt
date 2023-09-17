package com.shikaiji.guiadointeriores20

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class GuiadoPasos : AppCompatActivity() {

    private var edificio: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guiado_pasos)

        val extras= intent.extras

        if (extras != null) {
            edificio = extras.getString("edificio", "")
        }

        val salaD = findViewById<EditText>(R.id.salaD)
        val salaI = findViewById<EditText>(R.id.salaI)
        val buscar = findViewById<Button>(R.id.BotonAccion)

        buscar.setOnClickListener { generateGuide(salaD, salaI) }

    }

    private fun generateGuide(salaD: EditText, salaI: EditText){

        val idSalaD= sqliteHelper.buscarIdSalaPorNombreYEdificio(salaD.text.toString(),edificio.toLong()).toString()
        val idSalaI= sqliteHelper.buscarIdSalaPorNombreYEdificio(salaI.text.toString(),edificio.toLong()).toString()


        if(idSalaD=="-1" || idSalaI=="-1"){
            Toast.makeText(this, "Las salas son incorrectas, por favor compruebe los nombres antes de continuar", Toast.LENGTH_SHORT).show()
            salaD.setText("")
            salaI.setText("")
        }else{
            val intent= Intent(this, MostrarInstrucciones::class.java)
            intent.putExtra("idSalaD", idSalaD)
            intent.putExtra("idSalaO", idSalaI)
            intent.putExtra("idEdificio", edificio)
            startActivity(intent)
        }
    }
}
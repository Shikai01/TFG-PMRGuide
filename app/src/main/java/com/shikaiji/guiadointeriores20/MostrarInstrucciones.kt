package com.shikaiji.guiadointeriores20

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MostrarInstrucciones : AppCompatActivity() {
    private var edificio: String = ""
    private var idSalaO: String = ""
    private var idSalaD: String = ""
    private lateinit var conexiones: List<Conexion>
    private lateinit var camino: DataCamino
    private var primeraConexion: Int = 0
    private val sqliteHelper = SQLite(this, "main", null, 1)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_instrucciones)

        val extras= intent.extras

        if (extras != null) {
            edificio = extras.getString("idEdificio", "")
            idSalaO = extras.getString("idSalaO", "")
            idSalaD = extras.getString("idSalaD", "")
        }

        val instrucciones= findViewById<TextView>(R.id.instrucciones)

        camino= sqliteHelper.generarCamino(edificio,idSalaO,idSalaD)

        instrucciones.text = camino.instrucciones



        val boton = findViewById<Button>(R.id.button3)
        boton.setOnClickListener { nuevoGuiado() }
    }


    private fun nuevoGuiado(){
        camino= sqliteHelper.nuevoCamino(camino)

        val instrucciones= findViewById<TextView>(R.id.instrucciones)
        instrucciones.text = camino.instrucciones
    }
}
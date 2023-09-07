package com.shikaiji.guiadointeriores20

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class MapearSalas : AppCompatActivity() {
    private lateinit var popupWindow: PopupWindow
    private var idEdificio: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)
    private lateinit var salasContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapear_rutas)

        val extras= intent.extras
        if (extras != null) {
            idEdificio = extras.getString("idEdificio", "")
        }


        val popupView = layoutInflater.inflate(R.layout.activity_mapear_popup2, null)
        popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        salasContainer = findViewById<LinearLayout>(R.id.edificiosContainer)

        val btnCerrar = popupView.findViewById<Button>(R.id.salir)
        btnCerrar.setOnClickListener { cerrarPopup() }

        val agregar= findViewById <Button> (R.id.agregar)
        agregar.setOnClickListener { mostrarPopup() }

        val nombreSala = popupView.findViewById<EditText>(R.id.nombre)

        val coordenadaX = popupView.findViewById<EditText>(R.id.CoordenadaX)

        val coordenadaY = popupView.findViewById<EditText>(R.id.CoordenadaY)

        val btnAgregar = popupView.findViewById<Button>(R.id.agregar)

        btnAgregar.setOnClickListener { guardar(nombreSala, idEdificio.toInt(), coordenadaX, coordenadaY) }

        cargarSalas()

    }


    private fun cargarSalas() {
        try {
            val nombreYId = sqliteHelper.getSalas(idEdificio.toInt())

            for ((nombre, idSala, coordenadas) in nombreYId) {
                val edificioLayout = layoutInflater.inflate(R.layout.activity_mapear_edificios, null) as ConstraintLayout

                val edificioTextView = edificioLayout.findViewById<TextView>(R.id.nombre)
                val coordenadasTextView = edificioLayout.findViewById<TextView>(R.id.calle)
                val agregarInstruc = edificioLayout.findViewById<Button>(R.id.button)
                val eliminar = edificioLayout.findViewById<Button>(R.id.button2)

                agregarInstruc.setOnClickListener { goSalas(idSala) }
                eliminar.setOnClickListener { eliminarSala(idSala) }

                edificioTextView.text = nombre
                coordenadasTextView.text = "("+ coordenadas?.first+","+ coordenadas?.second+ ")"

                salasContainer.addView(edificioLayout)
            }
        } catch (e: Exception) {
            println("Error al cargar edificios: ${e.message}")
        }

    }


    private fun mostrarPopup() {
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val screenWidth = resources.displayMetrics.widthPixels
        popupWindow.width = screenWidth
        val screenHeight = resources.displayMetrics.heightPixels
        val popupHeight = popupWindow.contentView.measuredHeight

        val verticalPosition = (screenHeight - popupHeight) / 2

        popupWindow.showAtLocation(rootView, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, verticalPosition)
    }

    private fun cerrarPopup() {
        popupWindow.dismiss()
    }

    private fun guardar(nombreSala: EditText, idEdificio: Int, x: EditText, y: EditText) {
        if(sqliteHelper.insertSala(nombreSala.text.toString(),idEdificio, x.text.toString().toInt() , y.text.toString().toInt() )==-1){
            Toast.makeText(this,"Esta Sala ya esta en el sistema", Toast.LENGTH_SHORT).show()
        }
        salasContainer.removeAllViews()
        cargarSalas()
        popupWindow.dismiss()
    }



    private fun goSalas(IDSala:Int){
        val intent= Intent(this, MapearConexiones::class.java)
        intent.putExtra("idSala", IDSala.toString())
        intent.putExtra("idEdificio", idEdificio)
        startActivity(intent)
    }

    private fun eliminarSala(IDSala:Int){
        sqliteHelper.eliminarSala(IDSala)
        salasContainer.removeAllViews()
        cargarSalas()

    }

}
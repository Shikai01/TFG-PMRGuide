package com.shikaiji.guiadointeriores20

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class Mapear : AppCompatActivity() {
    private lateinit var popupWindow: PopupWindow
    private var usuario: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)
    private lateinit var edificiosContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        val extras= intent.extras

        if (extras != null) {
            usuario = extras.getString("usuario", "")
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapear)


        val popupView = layoutInflater.inflate(R.layout.activity_mapear_popup1, null)
        popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        val agregar= findViewById <Button> (R.id.agregar)
        agregar.setOnClickListener { mostrarPopup() }

        val btnCerrar = popupView.findViewById<Button>(R.id.salir)
        btnCerrar.setOnClickListener { cerrarPopup() }

        val nombreEdificio = popupView.findViewById<EditText>(R.id.nombre)

        val calle = popupView.findViewById<EditText>(R.id.Dirección)

        val btnAgregar = popupView.findViewById<Button>(R.id.agregar)
        btnAgregar.setOnClickListener { guardar(nombreEdificio, calle) }

        cargarEdificios()

    }

    private fun cargarEdificios() {
        try {
            val edificiosYCalles = sqliteHelper.getEdificiosYCallesPorUsuario(usuario)
            edificiosContainer = findViewById<LinearLayout>(R.id.edificiosContainer)

            for ((idEdificio, edificio, calle) in edificiosYCalles) {
                val edificioLayout = layoutInflater.inflate(R.layout.activity_mapear_edificios, null) as ConstraintLayout

                val edificioTextView = edificioLayout.findViewById<TextView>(R.id.nombre)
                val calleTextView = edificioLayout.findViewById<TextView>(R.id.calle)
                val agregarInstruc = edificioLayout.findViewById<Button>(R.id.button)
                val eliminar = edificioLayout.findViewById<Button>(R.id.button2)

                agregarInstruc.setOnClickListener { goEdificio(idEdificio) }
                eliminar.setOnClickListener { eliminarEdificio(idEdificio) }

                edificioTextView.text = edificio
                calleTextView.text = calle

                edificiosContainer.addView(edificioLayout)
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

    private fun guardar(nombreEdificio: EditText, calle: EditText) {
        if(sqliteHelper.insertOrUpdateEdificio(usuario, nombreEdificio.text.toString(), calle.text.toString())==-1){
            Toast.makeText(this,"Este edificio ya esta en el sistema", Toast.LENGTH_SHORT).show()
        }
        edificiosContainer.removeAllViews()
        cargarEdificios()
        popupWindow.dismiss()
    }

    private fun goEdificio(idEdificio:Long){
        val intent= Intent(this, MapearSalas::class.java)
        intent.putExtra("idEdificio", idEdificio.toString())
        startActivity(intent)
    }

    private fun eliminarEdificio(idEdificio:Long){
        sqliteHelper.eliminarEdificios(idEdificio)
        edificiosContainer.removeAllViews()
        cargarEdificios()

    }
}
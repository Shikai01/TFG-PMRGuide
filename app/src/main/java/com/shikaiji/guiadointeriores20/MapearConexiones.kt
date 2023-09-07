package com.shikaiji.guiadointeriores20

import android.annotation.SuppressLint
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

class MapearConexiones : AppCompatActivity() {

    private lateinit var popupWindow: PopupWindow
    private var idSalas: String = ""
    private var idEdificio: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)
    private lateinit var conexionesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapear_conexiones)

        val extras= intent.extras
        if (extras != null) {
            idSalas = extras.getString("idSala", "")
            idEdificio = extras.getString("idEdificio", "")
        }

        val popupView = layoutInflater.inflate(R.layout.activity_mapear_popup3, null)
        popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        conexionesContainer = findViewById<LinearLayout>(R.id.ConexionesContainer)

        val btnCerrar = popupView.findViewById<Button>(R.id.salir)
        btnCerrar.setOnClickListener { cerrarPopup() }

        val agregar= findViewById <Button> (R.id.agregar)
        agregar.setOnClickListener { mostrarPopup() }

        val objetivo = popupView.findViewById<EditText>(R.id.objetivo)

        val btnAgregar = popupView.findViewById<Button>(R.id.agregar)

        btnAgregar.setOnClickListener { guardar(objetivo) }

        cargarConexiones()


    }

    private fun cargarConexiones() {
        try {
            val nombreYId = sqliteHelper.getConexiones(idSalas.toInt())

            for ((idConexion, nombre) in nombreYId) {
                val edificioLayout = layoutInflater.inflate(R.layout.activity_mapear_edificios, null) as ConstraintLayout

                val edificioTextView = edificioLayout.findViewById<TextView>(R.id.nombre)
                val agregarInstruc = edificioLayout.findViewById<Button>(R.id.button)
                val eliminar = edificioLayout.findViewById<Button>(R.id.button2)

                agregarInstruc.setOnClickListener { goConexion(idConexion) }
                eliminar.setOnClickListener { eliminarConexion(idConexion) }

                edificioTextView.text = nombre

                conexionesContainer.addView(edificioLayout)
            }
        } catch (e: Exception) {
            println("Error al cargar conexiones: ${e.message}")
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

    private fun guardar(objetivo: TextView) {

        when (sqliteHelper.insertConexion(objetivo.text.toString(),idSalas, idEdificio)) {
            -1 -> Toast.makeText(this,"Esta Conexion ya esta en el sistema", Toast.LENGTH_SHORT).show()
            -2 -> Toast.makeText(this,"La sala no existe en el sistema, cree la sala antes de continuar", Toast.LENGTH_SHORT).show()
            -3 -> Toast.makeText(this,"No puedes crear una conexion desde esta sala a la misma sala", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this,"Guardado con exito, recuerde crear la conexion en la otra sala tambien", Toast.LENGTH_SHORT).show()
        }
        conexionesContainer.removeAllViews()
        cargarConexiones()
        popupWindow.dismiss()


    }

    private fun goConexion(IDConexion:Int){
        val intent= Intent(this, MapearPasos::class.java)
        intent.putExtra("IDConexion", IDConexion.toString())
        startActivity(intent)
    }

    private fun eliminarConexion(IDConexion:Int){
        sqliteHelper.eliminarConexion(IDConexion)
        conexionesContainer.removeAllViews()
        cargarConexiones()

    }



}
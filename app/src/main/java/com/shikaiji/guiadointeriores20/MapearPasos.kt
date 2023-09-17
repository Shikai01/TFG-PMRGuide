package com.shikaiji.guiadointeriores20

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
import androidx.constraintlayout.widget.ConstraintLayout

class MapearPasos : AppCompatActivity() {

    private lateinit var popupWindow: PopupWindow
    private var idConexiones: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)
    private lateinit var pasosContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapear_pasos)

        val extras= intent.extras
        if (extras != null) {
            idConexiones = extras.getString("IDConexion", "")
        }

        val popupView = layoutInflater.inflate(R.layout.activity_mapear_popup4, null)
        popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        pasosContainer = findViewById<LinearLayout>(R.id.PasosContainer)

        val btnCerrar = popupView.findViewById<Button>(R.id.salir)
        btnCerrar.setOnClickListener { cerrarPopup() }

        val agregar= findViewById <Button> (R.id.agregar)
        agregar.setOnClickListener { mostrarPopup() }

        val objetivo = popupView.findViewById<EditText>(R.id.Instrucciones)

        val btnAgregar = popupView.findViewById<Button>(R.id.agregar)

        btnAgregar.setOnClickListener { guardar(objetivo) }

        cargarPasos()

    }

    private fun cargarPasos() {
        var orden = 1
        try {
            val nombreYId = sqliteHelper.getPasos(idConexiones.toInt())

            for ((idPaso, Instruccion) in nombreYId) {
                val edificioLayout = layoutInflater.inflate(R.layout.activity_mapear_mostrar_instrucciones, null) as ConstraintLayout
                val edificioTextView = edificioLayout.findViewById<TextView>(R.id.instruccion)
                val eliminar = edificioLayout.findViewById<Button>(R.id.button)

                edificioLayout.findViewById<TextView>(R.id.Orden).text = orden.toString()
                orden += 1
                edificioTextView.text = Instruccion
                eliminar.setOnClickListener { eliminarPaso(idPaso) }
                pasosContainer.addView(edificioLayout)
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
        sqliteHelper.insertPaso(idConexiones,objetivo.text.toString())
        pasosContainer.removeAllViews()
        cargarPasos()
        popupWindow.dismiss()
    }

    private fun eliminarPaso(IDPaso:Int){
        sqliteHelper.eliminarPaso(IDPaso)
        pasosContainer.removeAllViews()
        cargarPasos()

    }
}
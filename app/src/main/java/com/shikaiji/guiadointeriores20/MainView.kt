package com.shikaiji.guiadointeriores20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class MainView : AppCompatActivity() {

    private var usuario: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        val extras= intent.extras

        if (extras != null) {
            usuario = extras.getString("usuario", "")
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view)

        val subVista= findViewById <ConstraintLayout> (R.id.sub_layout_main)

        val guiado = subVista.findViewById <Button> (R.id.EmpGuiado)
        guiado.setOnClickListener { goToEmpGuiado() }

        val mapear = subVista.findViewById <Button> (R.id.Mapear)
        mapear.setOnClickListener { goToMapear() }

        val pasos = subVista.findViewById <Button> (R.id.Pasos)
        pasos.setOnClickListener { goToPasos() }

        val help = subVista.findViewById <Button> (R.id.Help)
        help.setOnClickListener { goToHelp() }
    }


    private fun goToEmpGuiado(){
        val intent= Intent(this, Guiado::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }



    private fun goToPasos(){
        val intent= Intent(this, MedirPasos::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }


    private fun goToHelp(){
        val intent= Intent(this, Help::class.java)
        startActivity(intent)
    }

    private fun goToMapear(){
        val intent= Intent(this, Mapear::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }
}
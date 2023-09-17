package com.shikaiji.guiadointeriores20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class SignIn : AppCompatActivity() {
    private val sqliteHelper = SQLite(this, "main", null, 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val subVista= findViewById <ConstraintLayout> (R.id.sub_layout2)

        val signIn = subVista.findViewById <Button> (R.id.SignIn)
        signIn.setOnClickListener { goToSignIn() }
    }

    private fun goToSignIn(){
        val subVista= findViewById <ConstraintLayout> (R.id.sub_layout2)

        val email = subVista.findViewById <EditText> (R.id.Rname).text.toString()
        val usuario = subVista.findViewById <EditText> (R.id.name).text.toString()
        val password = subVista.findViewById <EditText> (R.id.password).text.toString()
        val rpassword = subVista.findViewById <EditText> (R.id.Rpassword).text.toString()
        var cv = "0"
        if(subVista.findViewById <CheckBox> (R.id.checkBoxCeguera).isChecked){
            cv="1"
        }
        var mr="0"
        if(subVista.findViewById <CheckBox> (R.id.checkBoxMR).isChecked){
            mr="1"
        }


        if(email.isNotEmpty() && usuario.isNotEmpty() && password.isNotEmpty() && rpassword.isNotEmpty() && password==rpassword){
            if(sqliteHelper.insertOrUpdateUsuario(usuario, password,email, cv=="1" ,mr=="1")==-1){
                Toast.makeText(this,"Alerta, El usuario ya esta en el sistema", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                val intent= Intent(this, IniciarSesion::class.java)
                startActivity(intent)
            }

        }else{
            Toast.makeText(this,"Alerta, campos vacios o contraseñas diferentes, por favor revise los campos", Toast.LENGTH_SHORT).show()
        }
    }
}
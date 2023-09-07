package com.shikaiji.guiadointeriores20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class IniciarSesion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion2)

        val subVista= findViewById <ConstraintLayout> (R.id.sub_layout)

        val signIn = subVista.findViewById <Button> (R.id.SignIn)
        signIn.setOnClickListener { goToSignIn() }

        val logIn = subVista.findViewById <Button> (R.id.LogIn)
        logIn.setOnClickListener { goToLogIn() }
    }

    private fun goToSignIn(){
        val intent= Intent(this, SignIn::class.java)
        startActivity(intent)
    }

    private fun goToLogIn(){

        val subVista= findViewById <ConstraintLayout> (R.id.sub_layout)

        val nombre = subVista.findViewById <EditText> (R.id.name).text.toString()

        val password = subVista.findViewById <EditText> (R.id.password).text.toString()

        if (checkUserCredentials(nombre, password)) {

            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            val intent2= Intent(this, MainView::class.java)
            intent2.putExtra("usuario", nombre)
            startActivity(intent2)

        } else {
            Toast.makeText(this, "Credenciales incorrectas, por favor revisa los datos", Toast.LENGTH_SHORT).show()
            subVista.findViewById <EditText> (R.id.name).setText("")
            subVista.findViewById <EditText> (R.id.password).setText("")
        }


    }


    private fun checkUserCredentials(nombre: String, password: String): Boolean {
        val con = SQLite(this, "main", null, 1)
        val baseDatos = con.readableDatabase

        val consulta = "SELECT * FROM Usuarios WHERE Usuario = ? AND contraseña = ?"
        val args = arrayOf(nombre, password)

        val cursor = baseDatos.rawQuery(consulta, args)
        val existeUsuario = cursor.count > 0

        cursor.close()
        baseDatos.close()

        return existeUsuario
    }
}
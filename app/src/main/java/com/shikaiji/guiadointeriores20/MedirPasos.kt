package com.shikaiji.guiadointeriores20

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.shikaiji.guiadointeriores20.databinding.ActivityMedirPasosBinding
import java.text.DecimalFormat
import kotlin.math.*

var latitud = 0.0
var latA = 0.0
var longitud = 0.0
var lonA = 0.0

class MedirPasos : AppCompatActivity() {

    private lateinit var binding: ActivityMedirPasosBinding

    private val CodePermiso = 100
    private var isPermisos = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var usuario: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)


    override fun onCreate(savedInstanceState: Bundle?) {

        val extras= intent.extras

        if (extras != null) {
            usuario = extras.getString("usuario", "")
        }



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medir_pasos)

        verificarPermisos()

        val loadingCircleView: LoadingCircleView = findViewById(R.id.loadingCircleView)
        loadingCircleView.startLoadingAnimation()

        val imageView = findViewById<ImageView>(R.id.imageView)

        // Establecemos la imagen utilizando el recurso de la imagen
        imageView.setImageResource(R.drawable.movil)

        val boton = findViewById<Button>(R.id.BotonAccion)
        boton.setOnClickListener { comenzar() }
    }

    private fun verificarPermisos() {
        val permisos = arrayListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val permisosArray = permisos.toTypedArray()
        if (tienePermisos(permisosArray)) {
            isPermisos = true
            onPermisosConcedidos()
        } else {
            solicitarPermisos(permisosArray)
        }
    }

    private fun solicitarPermisos(permisosArray: Array<String>) {
        requestPermissions(permisosArray, CodePermiso)
    }

    @SuppressLint("MissingPermission")
    private fun onPermisosConcedidos() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    imprimirUbicacion(it)
                } else {
                    Toast.makeText(this, "Nose puede obtener la ubicacion", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 500
            ).apply {
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)

                    for (location in p0.locations) {
                        imprimirUbicacion(location)
                    }
                }
            }


            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {

        }
    }

    @SuppressLint("WrongViewCast")
    private fun imprimirUbicacion(ubicacion: Location) {
        latitud = ubicacion.latitude
        longitud = ubicacion.longitude
        Log.d("GPS", "LAT :${ubicacion.latitude} - LON:${ubicacion.longitude}")
    }

    private fun tienePermisos(permisos: Array<String>): Boolean {
        return permisos.all {
            return ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CodePermiso) {
            val todosPermisosConcedidos = grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }

            if (grantResults.isNotEmpty() && todosPermisosConcedidos) {
                isPermisos = true
                onPermisosConcedidos()
            }
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }



    private fun comenzar() {
        binding = ActivityMedirPasosBinding.inflate(layoutInflater)

        val icon = findViewById<ImageView>(R.id.imageView)
        icon.visibility = View.INVISIBLE

        val circle = findViewById<LoadingCircleView>(R.id.loadingCircleView)
        circle.visibility = View.VISIBLE

        val handler = Handler()
        val delayMillis: Long = 2000 // 2 segundos

        handler.postDelayed({
            val texto = findViewById<TextView>(R.id.TextoGuia)
            texto.text = getString(R.string.PT2)
            texto.contentDescription = getString(R.string.PT2)

            val boton = findViewById<Button>(R.id.BotonAccion)
            boton.text = getString(R.string.Terminar)
            boton.setOnClickListener { terminar() }

            latA = latitud
            lonA = longitud
        }, delayMillis)


    }

    private fun terminar() {
        val icon = findViewById<ImageView>(R.id.imageView)
        icon.setImageResource(R.drawable.tick)
        icon.visibility = View.VISIBLE

        val circle = findViewById<LoadingCircleView>(R.id.loadingCircleView)
        circle.visibility = View.INVISIBLE

        val texto = findViewById<TextView>(R.id.TextoGuia)
        texto.text = getString(R.string.PT3)
        texto.contentDescription = getString(R.string.PT3)

        val boton = findViewById<Button>(R.id.BotonAccion)
        boton.visibility = View.INVISIBLE

        val intent= Intent(this, MainView::class.java)
        val handler = Handler()
        val delayMillis: Long = 2000 // 2 segundos

        handler.postDelayed({
            val distancia = haversine(latitud, longitud, latA, lonA) * 10

            val formato = DecimalFormat("#.00")

            val distanciaFormateada = formato.format(distancia) // Formatear la distancia como una cadena

            val distanciaN = distanciaFormateada.replace(",", ".").toDouble() // Reemplazar coma por punto y convertir a Double

            sqliteHelper.insertOrUpdateUsuarioPaso(usuario, distanciaN)

            startActivity(intent)

        }, delayMillis)

    }
}
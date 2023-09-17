package com.shikaiji.guiadointeriores20

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shikaiji.guiadointeriores20.adapter.BuildingAdapter

class Guiado : AppCompatActivity() {

    private lateinit var buildinglist: List<GuiadoBuilding>
    private var usuario: String = ""
    private val sqliteHelper = SQLite(this, "main", null, 1)
    private lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        val extras= intent.extras

        if (extras != null) {
            usuario = extras.getString("usuario", "")
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guiado)

        val buscador = findViewById<SearchView>(R.id.searchView)

        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    performSearch(newText)
                } else {
                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = BuildingAdapter(context, buildinglist)
                }
                return true
            }
        })
        loadBuildingList()
        initRecyclerView()
    }

    private fun initRecyclerView(){
        val recyclerView= findViewById<RecyclerView>(R.id.recyclerView)
        context = this
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter= BuildingAdapter(this,buildinglist)

    }

    private fun loadBuildingList() {
        val edificios = sqliteHelper.getEdificiosFiltrados(usuario)

        buildinglist = edificios.map { GuiadoBuilding(it.nombreE, it.calle, it.usuario) }
    }

    fun performSearch(query: String) {
        // Filtrar los elementos de BuildingList basados en la consulta 'query'
        val filteredList = buildinglist.filter { building ->
            building.building.contains(query, ignoreCase = true) ||
                    building.street.contains(query, ignoreCase = true) ||
                    building.user.contains(query, ignoreCase = true)
        }

        // Actualizar el RecyclerView con la lista filtrada
        updateRecyclerView(filteredList)
    }

    private fun updateRecyclerView(filteredBuildingList: List<GuiadoBuilding>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BuildingAdapter(this,filteredBuildingList)
    }

}
package com.shikaiji.guiadointeriores20

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Guiado : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EdificiosAdapter
    private val dataList: MutableList<String> = mutableListOf() // Tu lista de datos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guiado)

        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)
        adapter = EdificiosAdapter(dataList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText)
                return true
            }
        })
    }

    private fun filterData(query: String?) {
        val filteredList = dataList.filter { it.contains(query.orEmpty(), true) }
        //adapter.submitList(filteredList)
    }
}
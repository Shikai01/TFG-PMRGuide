package com.shikaiji.guiadointeriores20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EdificiosAdapter (private val dataList: List<String>) : RecyclerView.Adapter<EdificiosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView) // Reemplaza con el ID de tu vista de texto
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_guiado_edificio_adapter, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.textView.text = item
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
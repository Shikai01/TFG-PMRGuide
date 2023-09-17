package com.shikaiji.guiadointeriores20.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.shikaiji.guiadointeriores20.Guiado
import com.shikaiji.guiadointeriores20.GuiadoBuilding
import com.shikaiji.guiadointeriores20.GuiadoPasos
import com.shikaiji.guiadointeriores20.R
import com.shikaiji.guiadointeriores20.SQLite

class BuildingAdapter (private val context: Context, private val list:List<GuiadoBuilding>): RecyclerView.Adapter<BuildingViewHolder>() {

    private val sqliteHelper = SQLite(context, "main", null, 1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        val layaoutInflater = LayoutInflater.from(parent.context)
        return BuildingViewHolder(layaoutInflater.inflate(R.layout.activity_item_building, parent, false))
    }

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        val item = list[position]
        holder.render(item)
        val user= holder.itemView.findViewById<TextView>(R.id.NombreU)
        val calle= holder.itemView.findViewById<TextView>(R.id.NombreS)
        val nombre= holder.itemView.findViewById<TextView>(R.id.NombreE)
        holder.itemView.findViewById<Button>(R.id.buscar).setOnClickListener {
            onButtonClick(holder.itemView.context, user, nombre,calle)
        }
    }

    override fun getItemCount(): Int = list.size

    private fun onButtonClick(context: Context,usuario:TextView, edificio:TextView, calle: TextView){
        val id = sqliteHelper.getRowIdForEdificio(usuario.text.toString(), edificio.text.toString(), calle.text.toString())
        val intent= Intent(context, GuiadoPasos::class.java)
        intent.putExtra("edificio", id.toString())
        context.startActivity(intent)
    }

}
package com.shikaiji.guiadointeriores20.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.shikaiji.guiadointeriores20.GuiadoBuilding
import com.shikaiji.guiadointeriores20.R

class BuildingViewHolder (view:View) : ViewHolder(view){

    val name = view.findViewById<TextView>(R.id.NombreE)
    val street = view.findViewById<TextView>(R.id.NombreS)
    val user = view.findViewById<TextView>(R.id.NombreU)

    fun render(building: GuiadoBuilding){
        name.text = building.building
        street.text = building.street
        user.text = building.user
    }

}
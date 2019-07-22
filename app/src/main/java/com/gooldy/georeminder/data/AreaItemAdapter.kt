package com.gooldy.georeminder.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gooldy.georeminder.R

class AreaItemAdapter(private val areas: List<Area>) : RecyclerView.Adapter<AreaItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val areaItemView = inflater.inflate(R.layout.item_area, parent, false)

        return ViewHolder(areaItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val area = areas[position]

        val textView = holder.tvItemArea
        val x = String.format("%.4f", area.latitude).replace(',', '.')
        val y = String.format("%.4f", area.longitude).replace(',', '.')
        textView.text = "X: $x; Y: $y; radius: ${area.radius}m"
    }

    override fun getItemCount(): Int {
        return areas.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemArea: TextView = itemView.findViewById(R.id.tvItemArea)
    }
}
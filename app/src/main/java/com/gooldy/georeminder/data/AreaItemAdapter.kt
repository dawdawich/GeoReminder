package com.gooldy.georeminder.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alespero.expandablecardview.ExpandableCardView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.gooldy.georeminder.R

// TODO implement fun for edit and remove
class AreaItemAdapter(private val areas: MutableList<Area>, private val editFun: (Area) -> Unit,
                      private val removeFun: (Area) -> Unit) : RecyclerView.Adapter<AreaItemAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val areaItemView = inflater.inflate(R.layout.item_area, parent, false)
        context = parent.context

        return ViewHolder(areaItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val area = areas[position]
        val areaPosition = LatLng(area.latitude, area.longitude)

        holder.mapView.getMapAsync {
            with(it) {
                val circleOptions = CircleOptions()
                circleOptions.radius(area.radius)
                circleOptions.center(areaPosition)
                circleOptions.fillColor(0x44FF0000FF.toInt()) // blue
                addCircle(circleOptions)
                moveCamera(CameraUpdateFactory.newLatLngZoom(areaPosition, 15f))
                uiSettings.isMapToolbarEnabled = false
            }
        }
        holder.mapView.isClickable = false

        val cardReminder = holder.ecvItemReminder
        cardReminder.setTitle(area.streetName)
        cardReminder.setOnExpandedListener { v, _ ->
            val cardEdit: TextView = v.findViewById(R.id.edit_area)
            cardEdit.setOnClickListener { editFun.invoke(area) }
            val cardRemove: TextView = v.findViewById(R.id.remove_area)
            cardRemove.setOnClickListener {
                cardReminder.collapse()
                removeFun.invoke(area)
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.mapView.getMapAsync {
            it.clear()
            it.mapType = GoogleMap.MAP_TYPE_NONE
        }
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return areas.size
    }

    fun getAreasList(): MutableList<Area> {
        return areas
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ecvItemReminder: ExpandableCardView = itemView.findViewById(R.id.area_card)
        val mapView: MapView

        init {
            mapView = ecvItemReminder.findViewById(R.id.map_item)
            mapView
            mapView.onCreate(null)
        }
    }
}

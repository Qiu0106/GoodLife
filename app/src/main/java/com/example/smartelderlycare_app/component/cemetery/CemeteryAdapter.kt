package com.example.smartelderlycare_app.component.cemetery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Cemetery

/**
 * 公墓列表 RecyclerView 适配器
 * 支持列表项点击回调（用于地图联动）和高亮选中状态
 */
class CemeteryAdapter(
    private var cemeteryList: List<Cemetery>,
    private val onItemClick: (Cemetery, Int) -> Unit
) : RecyclerView.Adapter<CemeteryAdapter.ViewHolder>() {

    private var highlightedPosition: Int = RecyclerView.NO_POSITION

    fun updateData(newList: List<Cemetery>) {
        cemeteryList = newList
        highlightedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun setHighlighted(position: Int) {
        val previous = highlightedPosition
        highlightedPosition = position
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous)
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cemetery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cemetery = cemeteryList[position]
        val isHighlighted = position == highlightedPosition

        holder.tvName.text = cemetery.name
        holder.tvDistrict.text = cemetery.district
        holder.tvIntro.text = cemetery.introduction
        holder.tvIntroFull.text = cemetery.introduction
        holder.tvLocation.text = "经度: ${"%.4f".format(cemetery.longitude)}  纬度: ${"%.4f".format(cemetery.latitude)}"

        if (isHighlighted) {
            holder.tvIntro.visibility = View.GONE
            holder.tvIntroFull.visibility = View.VISIBLE
            holder.itemView.setBackgroundResource(R.drawable.shape_search_bg)
        } else {
            holder.tvIntro.visibility = View.VISIBLE
            holder.tvIntroFull.visibility = View.GONE
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }

        holder.btnViewOnMap.setOnClickListener {
            onItemClick(cemetery, position)
        }

        holder.itemView.setOnClickListener {
            onItemClick(cemetery, position)
        }
    }

    override fun getItemCount(): Int = cemeteryList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCemeteryName)
        val tvDistrict: TextView = view.findViewById(R.id.tvCemeteryDistrict)
        val tvIntro: TextView = view.findViewById(R.id.tvCemeteryIntro)
        val tvIntroFull: TextView = view.findViewById(R.id.tvCemeteryIntroFull)
        val tvLocation: TextView = view.findViewById(R.id.tvCemeteryLocation)
        val btnViewOnMap: TextView = view.findViewById(R.id.btnViewOnMap)
    }
}
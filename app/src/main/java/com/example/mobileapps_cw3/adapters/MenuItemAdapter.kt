package com.example.mobileapps_cw3.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.MenuItem

class MenuItemAdapter(private var menuItems: List<MenuItem>) : RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ui_menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newMenuItems: List<MenuItem>) {
        menuItems = newMenuItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.name)
        private val itemCostTextView: TextView = itemView.findViewById(R.id.price)

        @SuppressLint("SetTextI18n")
        fun bind(menuItem: MenuItem) {
            itemNameTextView.text = menuItem.getName()
            itemCostTextView.text = "£${menuItem.getCost()}" // Assuming cost is a Double
        }
    }
}

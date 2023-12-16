package com.example.mobileapps_cw3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.adapters.MenuItemAdapter
import com.example.mobileapps_cw3.structures.MenuItem
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Locale

class MenuFragment : Fragment() {

    private lateinit var menuAdapter: MenuItemAdapter
    private val allMenuItems = mutableListOf<MenuItem>()

    private var sortBy: SortBy = SortBy.NONE

    private enum class SortBy {
        NONE,
        NAME,
        COST
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rest_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = arguments?.getSerializable("restaurant") as? Restaurant
        val user = arguments?.getSerializable("user") as? Profile
        val recyclerView = view.findViewById<RecyclerView>(R.id.items)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val menuItems = mutableListOf<MenuItem>()
        menuAdapter = MenuItemAdapter(menuItems)
        recyclerView.adapter = menuAdapter

        if (restaurant != null) {
            view.findViewById<TextView>(R.id.name).text = restaurant.getName()

            val storageRef = FirebaseStorage.getInstance()
            if (restaurant.getLogo() != "") {
                val localFile = File.createTempFile("images", "jpg")
                storageRef.getReference(restaurant.getLogo()).getFile(localFile)
                    .addOnSuccessListener {
                        Glide.with(view.context).load(localFile).into(view.findViewById(R.id.logo))
                    }
            }

            val menu = restaurant.getMenu()
            for (id in menu) {
                val menuReference = FirebaseFirestore.getInstance()
                    .collection("menu").document(id.toString())

                menuReference.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot != null && snapshot.exists()) {
                            val item = snapshot.toObject(MenuItem::class.java)

                            if (item != null) {
                                println(item.getCost())
                                menuItems.add(item)
                                allMenuItems.add(item)
                            }

                            menuAdapter.updateItems(menuItems)
                        }
                    }
                }
            }
        }

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMenuItems(newText)
                return true
            }
        })

        val filter = view.findViewById<ImageButton>(R.id.btnFilter)
        filter.setOnClickListener {
            showFilters(it)
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            replaceFragment(RestaurantProfileFragment(), restaurant, user)
        }
    }

    private fun filterMenuItems(query: String?) {
        val filteredMenuItems = mutableListOf<MenuItem>()

        if (!query.isNullOrBlank()) {
            for (item in allMenuItems) {
                if (item.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                    filteredMenuItems.add(item)
                }
            }
        } else {
            filteredMenuItems.addAll(allMenuItems)
        }

        menuAdapter.updateItems(filteredMenuItems)
    }

    private fun showFilters(itemView: View) {
        val popupMenu = PopupMenu(itemView.context, itemView)
        popupMenu.inflate(R.menu.pop_up_menu_items)

        popupMenu.setOnMenuItemClickListener { item: android.view.MenuItem ->
            when (item.itemId) {
                R.id.menu_name -> {
                    sortBy = SortBy.NAME
                    sortMenuItems()
                    true
                }
                R.id.menu_cost -> {
                    sortBy = SortBy.COST
                    sortMenuItems()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun replaceFragment(
        fragment: Fragment,
        restaurant: Restaurant? = null,
        user: Profile? = null
    ) {
        val newFragment = fragment.apply {
            arguments = Bundle().apply {
                putSerializable("user", user)
                putSerializable("restaurant", restaurant)
            }
        }
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment).commit()
    }

    private fun sortMenuItems() {
        when (sortBy) {
            SortBy.NAME -> {
                allMenuItems.sortBy { it.getName() }
                menuAdapter.updateItems(allMenuItems)
            }
            SortBy.COST -> {
                allMenuItems.sortBy { it.getCost() }
                menuAdapter.updateItems(allMenuItems)
            }
            else -> {
            }
        }
    }
}

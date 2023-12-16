package com.example.mobileapps_cw3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.databinding.UiRestaurantBinding
import com.example.mobileapps_cw3.structures.Restaurant
import com.google.firebase.storage.FirebaseStorage
import java.io.File

interface RestaurantClickListener {
    fun onRestaurantClick(restaurant: Restaurant)
}

class RestaurantAdapter(private val restaurants: MutableList<Restaurant>,
                        private val restaurantClickListener: RestaurantClickListener) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UiRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, restaurants, restaurantClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount(): Int {
        return restaurants.size
    }

    class ViewHolder(
        private val binding: UiRestaurantBinding,
        private val restaurants: List<Restaurant>,
        private val restaurantClickListener: RestaurantClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: Restaurant) {
            val storageRef = FirebaseStorage.getInstance().getReference(restaurant.getImage())
            val localFile = File.createTempFile("images", "jpg")
            val costText = binding.root.context.getString(R.string.meals_from) + restaurant.getCost()

            storageRef.getFile(localFile).addOnSuccessListener {
                Glide.with(binding.root.context).load(localFile).into(binding.itemImage)
            }

            binding.itemName.text = restaurant.getName()
            binding.itemPrice.text = costText
            binding.itemRating.rating = restaurant.getRating()
        }

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    restaurantClickListener?.onRestaurantClick(restaurants[position])
                }
            }
        }
    }

}

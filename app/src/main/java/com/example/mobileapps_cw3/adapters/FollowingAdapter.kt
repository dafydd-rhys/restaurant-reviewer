import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.fragments.ProfileFragment
import com.example.mobileapps_cw3.fragments.RestaurantProfileFragment
import com.example.mobileapps_cw3.fragments.ViewingProfileFragment
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.example.mobileapps_cw3.structures.SystemData
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FollowingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<Any> = mutableListOf()
    private val filteredItems: MutableList<Any> = mutableListOf()

    fun addProfile(profile: Profile) {
        items.add(profile)
        updateFilteredItems()
    }

    fun addRestaurant(restaurant: Restaurant) {
        items.add(restaurant)
        updateFilteredItems()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateFilteredItems() {
        filteredItems.clear()
        filteredItems.addAll(items.filter { item ->
            when (item) {
                is Profile -> item.getUsername().contains(searchQuery, ignoreCase = true)
                is Restaurant -> item.getName().contains(searchQuery, ignoreCase = true)
                else -> false
            }
        })
        notifyDataSetChanged()
    }

    private var searchQuery: String = ""

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchQuery(query: String) {
        searchQuery = query
        updateFilteredItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_PROFILE) {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.ui_profile, parent, false)
            ProfileViewHolder(view)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.ui_profile, parent, false)
            RestaurantViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProfileViewHolder) {
            holder.bindProfile(items[position] as Profile)
        } else if (holder is RestaurantViewHolder) {
            holder.bindRestaurant(items[position] as Restaurant)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is Profile) {
            VIEW_TYPE_PROFILE
        } else {
            VIEW_TYPE_RESTAURANT
        }
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindProfile(profile: Profile) {
            itemView.findViewById<TextView>(R.id.username).text = profile.getUsername()

            if (profile.getImage() != "") {
                val storageRef = FirebaseStorage.getInstance().getReference(profile.getImage())
                val localFile = File.createTempFile("images", "jpg")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Glide.with(itemView.context).load(localFile)
                        .into(itemView.findViewById(R.id.userPicture))
                }
            }

            itemView.findViewById<ImageView>(R.id.navigateUser).setOnClickListener {
                showProfile(itemView, profile)
            }
        }

        private fun showProfile(itemView: View, profile: Profile) {
            val popupMenu = PopupMenu(itemView.context, itemView)
            popupMenu.inflate(R.menu.pop_up_menu_noauth)

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_navigate -> {

                        val fragmentManager =
                            (itemView.context as AppCompatActivity).supportFragmentManager
                        var fragment: Fragment? = null

                        if (profile == SystemData.getLoggedIn()) {
                            fragment = ProfileFragment().apply {
                                arguments = Bundle().apply {
                                    putSerializable("user", profile)
                                }
                            }
                        } else {
                            fragment = ViewingProfileFragment().apply {
                                arguments = Bundle().apply {
                                    putSerializable("lookedAtProfile", profile)
                                }
                            }
                        }

                        fragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack(null) // Optional: Add the transaction to the back stack
                            .commit()

                        true
                    }

                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindRestaurant(restaurant: Restaurant) {
            itemView.findViewById<TextView>(R.id.username).text = restaurant.getName()

            if (restaurant.getLogo() != "") {
                val storageRef = FirebaseStorage.getInstance().getReference(restaurant.getLogo())
                val localFile = File.createTempFile("images", "jpg")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Glide.with(itemView.context).load(localFile)
                        .into(itemView.findViewById(R.id.userPicture))
                }
            }

            itemView.findViewById<ImageView>(R.id.navigateUser).setOnClickListener {
                showRestaurant(itemView, restaurant)
            }
        }

        private fun showRestaurant(itemView: View, restaurant: Restaurant) {
            val popupMenu = PopupMenu(itemView.context, itemView)
            popupMenu.inflate(R.menu.pop_up_menu_restaurant)

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_navigate -> {
                        val fragmentManager =
                            (itemView.context as AppCompatActivity).supportFragmentManager

                        val user = SystemData.getLoggedIn() ?:
                        Profile(-1, "", "", emptyList(), emptyList(), "",
                            emptyList(), emptyList(), false, emptyList(), emptyList(), 0)

                        val fragment = RestaurantProfileFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable("user", user)
                                putSerializable("restaurant", restaurant)
                            }
                        }

                        fragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack(null) // Optional: Add the transaction to the back stack
                            .commit()
                        true
                    }

                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }
    }

    companion object {
        const val VIEW_TYPE_PROFILE = 1
        const val VIEW_TYPE_RESTAURANT = 2
    }

}

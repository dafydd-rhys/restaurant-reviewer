import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.adapters.RestaurantAdapter
import com.example.mobileapps_cw3.adapters.RestaurantClickListener
import com.example.mobileapps_cw3.databinding.FragmentHomeBinding
import com.example.mobileapps_cw3.fragments.RestaurantProfileFragment
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HomeFragment : Fragment(), RestaurantClickListener {

    private lateinit var binding: FragmentHomeBinding
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var allRestaurants = mutableListOf<Restaurant>()
    private var displayedRestaurants = mutableListOf<Restaurant>()
    private var sortBy: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch all restaurants from Firestore
        firebase.collection("restaurants").get().addOnSuccessListener { querySnapshot ->
            allRestaurants.clear()

            for (document in querySnapshot.documents) {
                val restaurant = document.toObject(Restaurant::class.java)
                restaurant?.let {
                    allRestaurants.add(it)
                }
            }

            // Set up RecyclerView with a LinearLayoutManager
            val layoutManager = LinearLayoutManager(requireContext())
            binding.recView.layoutManager = layoutManager

            // Initialize and set up RecyclerView with the full list of restaurants
            displayedRestaurants = ArrayList(allRestaurants)
            val adapter = RestaurantAdapter(displayedRestaurants, this)
            binding.recView.adapter = adapter
        }

        val filter = view.findViewById<ImageButton>(R.id.btnFilter)
        filter.setOnClickListener {
            showFilters(it)
        }

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                displayedRestaurants.clear()
                if (!newText.isNullOrBlank()) {
                    val searchQuery = newText.trim().toLowerCase(Locale.getDefault())
                    displayedRestaurants.addAll(
                        allRestaurants.filter { restaurant ->
                            restaurant.getName().toLowerCase(Locale.getDefault()).contains(searchQuery)
                        }
                    )
                } else {
                    displayedRestaurants.addAll(allRestaurants)
                }
                binding.recView.adapter?.notifyDataSetChanged()

                return true
            }
        })
    }

    private fun showFilters(itemView: View) {
        val popupMenu = PopupMenu(itemView.context, itemView)
        popupMenu.inflate(R.menu.pop_up_filter_restaurant)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_name -> {
                    sortBy = "name"
                    sortAndRefreshList()
                    true
                }
                R.id.menu_cost -> {
                    sortBy = "cost"
                    sortAndRefreshList()
                    true
                }
                R.id.menu_rating -> {
                    sortBy = "rating"
                    sortAndRefreshList()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortAndRefreshList() {
        when (sortBy) {
            "name" -> {
                displayedRestaurants.sortBy { it.getName() }
            }
            "cost" -> {
                displayedRestaurants.sortBy { it.getCost() }
            }
            "rating" -> {
                displayedRestaurants.sortByDescending { it.getRating() }
            }
        }

        binding.recView.adapter?.notifyDataSetChanged()
    }

    override fun onRestaurantClick(restaurant: Restaurant) {
        val user = arguments?.getSerializable("user") as? Profile

        replaceFragment(RestaurantProfileFragment().apply {
            arguments = Bundle().apply {
                putSerializable("restaurant", restaurant)
                putSerializable("user", user)
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }
}

package com.example.mobileapps_cw3.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.adapters.ReviewAdapter
import com.example.mobileapps_cw3.databinding.FragmentReviewsBinding
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.example.mobileapps_cw3.structures.Review
import com.google.firebase.firestore.FirebaseFirestore

class ReviewsFragment : Fragment(), ReviewAdapter.ReviewAdapterListener {

    private var sortBy: String? = null
    private lateinit var binding: FragmentReviewsBinding
    private var displayedReviews = mutableListOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //retrieves restaurant object
        val restaurant = arguments?.getSerializable("restaurant") as? Restaurant

        //retrieves review reference in database
        val reviewsCollection = FirebaseFirestore.getInstance().collection("reviews")
        val reviews = mutableListOf<Review>()

        if (restaurant!= null) {
            //retrieves reviews and initializes current processed count
            val reviewIds = restaurant.getReviews()
            var reviewsProcessed = 0

            // load a UI instance for each review
            for (reviewID in reviewIds) {
                reviewsCollection.document(reviewID.toString()).get().addOnSuccessListener { documentSnapshot ->
                    //if review exists with the ID
                    if (documentSnapshot.exists()) {
                        documentSnapshot.toObject(Review::class.java)?.let {
                            reviews.add(it)
                        }
                    }

                    reviewsProcessed++
                    if (reviewsProcessed == reviewIds.size) {
                        updateAdapter(reviews)
                    }
                }
            }
        }

        val user = arguments?.getSerializable("user") as? Profile
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recReviews.layoutManager = layoutManager
        val adapter = ReviewAdapter(emptyList(), this, false)
        binding.recReviews.adapter = adapter

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredReviews = filterReviewsByDescription(reviews, newText.orEmpty())
                updateAdapter(filteredReviews)
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

    private fun showFilters(itemView: View) {
        val popupMenu = PopupMenu(itemView.context, itemView)
        popupMenu.inflate(R.menu.pop_up_filter_review)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_date -> {
                    sortBy = "date"
                    sortAndRefreshList()
                    true
                }
                R.id.menu_rating -> {
                    sortBy = "rating"
                    sortAndRefreshList()
                    true
                }
                R.id.menu_likes -> {
                    sortBy = "likes"
                    sortAndRefreshList()
                    true
                }
                R.id.menu_dislikes -> {
                    sortBy = "dislikes"
                    sortAndRefreshList()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortAndRefreshList() {
        when (sortBy) {
            "date" -> {
                displayedReviews.sortBy { it.getDate() }
            }
            "rating" -> {
                displayedReviews.sortByDescending { it.getRating() }
            }
            "likes" -> {
                displayedReviews.sortByDescending { it.getLikes() }
            }
            "dislikes" -> {
                displayedReviews.sortByDescending { it.getDislikes() }
            }
        }

        binding.recReviews.adapter?.notifyDataSetChanged()
    }

    private fun filterReviewsByDescription(reviews: List<Review>, query: String): List<Review> {
        return reviews.filter { review ->
            review.getDescription().contains(query, ignoreCase = true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(reviews: List<Review>) {
        displayedReviews.clear()
        displayedReviews.addAll(reviews)

        val adapter = binding.recReviews.adapter as ReviewAdapter
        adapter.updateReviews(displayedReviews)
        adapter.notifyDataSetChanged()
    }

    private fun replaceFragment(fragment: Fragment, restaurant: Restaurant? = null, user: Profile? = null) {
        val newFragment = fragment.apply {
            arguments = Bundle().apply {
                putSerializable("user", user)
                putSerializable("restaurant", restaurant)
            }
        }
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment).commit()
    }

    override fun onReviewDeleted(review: Review) {
        //No implementation
    }

}

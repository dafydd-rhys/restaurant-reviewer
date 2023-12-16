package com.example.mobileapps_cw3.fragments

import FollowingAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.example.mobileapps_cw3.structures.SystemData
import com.google.firebase.firestore.FirebaseFirestore

class FollowingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = arguments?.getSerializable("user") as? Profile
        val following = user?.getFollowing()
        val recFollowing: RecyclerView = view.findViewById(R.id.recFollowing)
        val layoutManager = LinearLayoutManager(requireContext())

        recFollowing.layoutManager = layoutManager

        val adapter = FollowingAdapter()
        recFollowing.adapter = adapter

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        val restaurantCollection = FirebaseFirestore.getInstance().collection("restaurants")

        following?.let {
            for (id in it) {
                val type = id[0] // Get the first character to determine the type (u or r)
                val actualID = id.substring(1)
                println(actualID)

                if (type == 'u') {
                    userCollection.document(actualID).get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val profile = documentSnapshot.toObject(Profile::class.java)

                            if (profile != null) {
                                adapter.addProfile(profile)
                            }
                        }
                    }
                } else if (type == 'r') {
                    restaurantCollection.document(actualID).get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val restaurant = documentSnapshot.toObject(Restaurant::class.java)
                            println(restaurant)

                            if (restaurant != null) {
                                adapter.addRestaurant(restaurant)
                            }
                        }
                    }
                }
            }
        }

        val searchViewFollowing = view.findViewById<SearchView>(R.id.searchView)
        searchViewFollowing.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            if (user == SystemData.getLoggedIn()) {
                val newFragment = ProfileFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("user", user)
                    }
                }
                parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment).commit()
            } else {
                val newFragment = ViewingProfileFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("lookedAtProfile", user)
                    }
                }
                parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment).commit()
            }
        }
    }

}
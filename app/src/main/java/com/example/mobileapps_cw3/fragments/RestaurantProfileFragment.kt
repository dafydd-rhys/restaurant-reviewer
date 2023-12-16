package com.example.mobileapps_cw3.fragments

import CreateReviewFragment
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.example.mobileapps_cw3.structures.SystemData
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class RestaurantProfileFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rest_profile, container, false)
        val restaurant = arguments?.getSerializable("restaurant") as? Restaurant
        val user = arguments?.getSerializable("user") as? Profile
        val ttlRestaurantName = view.findViewById<TextView>(R.id.res_name)
        val txtFollowers = view.findViewById<TextView>(R.id.txtFollowerCount)
        val txtReviews = view.findViewById<TextView>(R.id.txtReviewCount)
        val txtChildren = view.findViewById<TextView>(R.id.txtChildrenAllowed)
        val btnFollow = view.findViewById<MaterialButton>(R.id.btnFollow)
        val txtLocation = view.findViewById<TextView>(R.id.txtLocation)

        if (restaurant != null) {
            if (restaurant.getId() != -1) {
                ttlRestaurantName.text = restaurant.getName()
                txtFollowers.text = restaurant.getFollowers().size.toString()
                txtReviews.text = restaurant.getReviews().size.toString()
                txtLocation.text = restaurant.getLocation()

                if (restaurant.getChildren()) {
                    txtChildren.text = "Yes"
                } else {
                    txtChildren.text = "No"
                }
            }

            if (!SystemData.getGuest()) {

                if (user != null) {
                    if (!restaurant.getFollowers().contains(user.getId())) {
                        btnFollow.text = "Follow"
                    } else {
                        btnFollow.text = "Following"
                    }
                }
            }

            val storageRef = FirebaseStorage.getInstance()

            if (restaurant.getImage() != "") {
                val localFile = File.createTempFile("images", "jpg")
                storageRef.getReference(restaurant.getImage()).getFile(localFile).addOnSuccessListener {
                    Glide.with(view.context).load(localFile).into(view.findViewById(R.id.banner))
                }
            }

            if (restaurant.getLogo() != "") {
                val localFile = File.createTempFile("images", "jpg")
                storageRef.getReference(restaurant.getLogo()).getFile(localFile).addOnSuccessListener {
                    Glide.with(view.context).load(localFile).into(view.findViewById(R.id.resLogo))
                }
            }
        }

        view.findViewById<LinearLayout>(R.id.btnReviews).setOnClickListener {
            val reviewsFragment = ReviewsFragment()
            reviewsFragment.arguments = Bundle().apply {
                putSerializable("restaurant", restaurant)
                putSerializable("user", user)
            }
            replaceFragment(reviewsFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnOpening).setOnClickListener {
            val openingHoursFragment = OpeningHoursFragment()
            openingHoursFragment.arguments = Bundle().apply {
                putSerializable("restaurant", restaurant)
                putSerializable("user", user)
            }
            replaceFragment(openingHoursFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnMenu).setOnClickListener {
            val menuFragment = MenuFragment()
            menuFragment.arguments = Bundle().apply {
                putSerializable("restaurant", restaurant)
                putSerializable("user", user)
            }
            replaceFragment(menuFragment)
        }


        view.findViewById<MaterialButton>(R.id.btnLeaveReview).setOnClickListener {
            if (isGuest(user)) {
                Toast.makeText(
                    context, "Can't write reviews as a guest!", Toast.LENGTH_SHORT
                ).show()
            } else {
                val createReviewFragment = CreateReviewFragment()
                createReviewFragment.arguments = Bundle().apply {
                    putSerializable("restaurant", restaurant)
                    putSerializable("user", user)
                }
                replaceFragment(createReviewFragment)
            }
        }

        btnFollow.setOnClickListener {
            if (!SystemData.getGuest()) {

                if (user != null && restaurant != null) {
                    val loggedInFollowing = user.getFollowing().toMutableList()
                    val restaurantFollowers = restaurant.getFollowers().toMutableList()

                    if (!user.getFollowing().contains("r${restaurant.getId()}")) {
                        loggedInFollowing.add(("r${restaurant.getId()}"))
                        restaurantFollowers.add(user.getId())

                        user.setFollowing(loggedInFollowing)
                        restaurant.setFollowers(restaurantFollowers)
                        user.updateInDatabase()
                        restaurant.updateInDatabase()

                        txtFollowers.text = (txtFollowers.text.toString().toInt() + 1).toString()
                        btnFollow.text = "Following"
                    } else {
                        loggedInFollowing.remove("r${restaurant.getId()}")
                        restaurantFollowers.remove(user.getId())

                        user.setFollowing(loggedInFollowing)
                        restaurant.setFollowers(restaurantFollowers)
                        user.updateInDatabase()
                        restaurant.updateInDatabase()

                        txtFollowers.text = (txtFollowers.text.toString().toInt() - 1).toString()
                        btnFollow.text = "Follow"
                    }
                }
            } else {
                Toast.makeText(context, "Can't do this as a guest.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun isGuest(user: Profile?): Boolean {
        if (user != null) {
            return user.getId() == -1
        }
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }

}
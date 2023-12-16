package com.example.mobileapps_cw3.fragments

import HomeFragment
import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

class ViewingProfileFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewing_profile, container, false)
        val user = arguments?.getSerializable("lookedAtProfile") as? Profile
        val restaurant = arguments?.getSerializable("lookedAtRestaurant") as? Restaurant
        val btnFollow = view.findViewById<MaterialButton>(R.id.btnFollow)
        val ttlUsername = view.findViewById<TextView>(R.id.username)
        val txtFollowers = view.findViewById<TextView>(R.id.txtFollowerCount)
        val txtReviews = view.findViewById<TextView>(R.id.txtReviewCount)


        if (!SystemData.getGuest()) {
            val loggedIn = SystemData.getLoggedIn()

            println(loggedIn)
            println(user)
            if (loggedIn != null && user != null && loggedIn != user) {
                println("oi")
                if (!user.getFollowers().contains(loggedIn.getId().toLong())) {
                    btnFollow.text = "Follow"
                } else {
                    btnFollow.text = "Following"
                }
            }
        }

        if (user != null) {
            if (user.getId() != -1) {
                ttlUsername.text = user.getUsername()
                txtFollowers.text = user.getFollowers().size.toString()
                txtReviews.text = user.getReviews().size.toString()
                view.findViewById<TextView>(R.id.txtLikes).text = user.getUpvotes().toString()

                if (user.getImage() != "") {
                    val storageRef = FirebaseStorage.getInstance().getReference(user.getImage())
                    val localFile = File.createTempFile("images", "jpg")

                    storageRef.getFile(localFile).addOnSuccessListener {
                        Glide.with(view.context).load(localFile)
                            .into(view.findViewById(R.id.profilePicture))
                    }
                }
            }
        }

        btnFollow.setOnClickListener {
            if (!SystemData.getGuest()) {
                val loggedIn = SystemData.getLoggedIn()

                if (loggedIn != null && user != null && loggedIn != user) {
                    val loggedInFollowing = loggedIn.getFollowing().toMutableList()
                    val userFollowers = user.getFollowers().toMutableList()

                    if (!loggedIn.getFollowing().contains("u${user.getId()}")) {
                        loggedInFollowing.add("u${user.getId()}")
                        userFollowers.add(loggedIn.getId().toLong())

                        loggedIn.setFollowing(loggedInFollowing)
                        user.setFollowers(userFollowers)
                        loggedIn.updateInDatabase()
                        user.updateInDatabase()

                        txtFollowers.text = (txtFollowers.text.toString().toInt() + 1).toString()
                        btnFollow.text = "Following"
                    } else {
                        loggedInFollowing.remove("u${user.getId()}")
                        userFollowers.remove(loggedIn.getId().toLong())

                        loggedIn.setFollowing(loggedInFollowing)
                        user.setFollowers(userFollowers)
                        loggedIn.updateInDatabase()
                        user.updateInDatabase()

                        txtFollowers.text = (txtFollowers.text.toString().toInt() - 1).toString()
                        btnFollow.text = "Follow"
                    }
                }
            } else {
                Toast.makeText(context, "Can't do this as a guest.", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<LinearLayout>(R.id.btnFollowing).setOnClickListener {
            val followingFragment = FollowingFragment()
            followingFragment.arguments = Bundle().apply {
                putSerializable("user", user)
            }
            replaceFragment(followingFragment)
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            if (restaurant == null) {
                var prof = SystemData.getLoggedIn()
                if (prof == null) {
                    prof = Profile(
                        -1, "", "", emptyList(), emptyList(), "",
                        emptyList(), emptyList(), false, emptyList(), emptyList(), 0
                    )
                }
                val newFragment = HomeFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("user", prof)
                    }
                }

                replaceFragment(newFragment)
            } else {
                val reviewsFragment = ReviewsFragment()
                reviewsFragment.arguments = Bundle().apply {
                    putSerializable("restaurant", restaurant)
                }
                replaceFragment(reviewsFragment)
            }
        }

        view.findViewById<LinearLayout>(R.id.btnProfileReviews).setOnClickListener {
            val reviewsFragment = ReviewsProfileFragment()
            reviewsFragment.arguments = Bundle().apply {
                putSerializable("lookedAtProfile", user)
                putSerializable("lookedAtRestaurant", restaurant)
            }
            replaceFragment(reviewsFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnBadge).setOnClickListener {
            val badgeFragment = BadgeFragment()
            badgeFragment.arguments = Bundle().apply {
                putSerializable("user", user)
            }
            replaceFragment(badgeFragment)
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }

}
package com.example.mobileapps_cw3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.google.firebase.firestore.FirebaseFirestore

class OpeningHoursFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rest_opening_hours, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = arguments?.getSerializable("restaurant") as? Restaurant
        val user = arguments?.getSerializable("user") as? Profile

        if (restaurant != null) {
            val hours = restaurant.getOpeningHours();

            view.findViewById<TextView>(R.id.txtMonday).text = hours[0]
            view.findViewById<TextView>(R.id.txtTuesday).text = hours[1]
            view.findViewById<TextView>(R.id.txtWednesday).text = hours[2]
            view.findViewById<TextView>(R.id.txtThursday).text = hours[3]
            view.findViewById<TextView>(R.id.txtFriday).text = hours[4]
            view.findViewById<TextView>(R.id.txtSaturday).text = hours[5]
            view.findViewById<TextView>(R.id.txtSunday).text = hours[6]
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            replaceFragment(RestaurantProfileFragment(), restaurant, user)
        }
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

}
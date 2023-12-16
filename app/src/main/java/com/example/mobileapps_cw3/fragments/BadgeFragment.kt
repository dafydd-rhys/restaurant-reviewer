package com.example.mobileapps_cw3.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.SystemData

class BadgeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_badges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = arguments?.getSerializable("user") as? Profile

        if (user != null) {
            checkProgress(view.findViewById(R.id.reviewProgress), user.getReviews().size, 4)
            checkProgress(view.findViewById(R.id.likesProgress), user.getLiked().size, 1)
            checkProgress(view.findViewById(R.id.followersProgress), user.getFollowers().size, 1)
            checkProgress(
                view.findViewById(R.id.badgeProgress), 1 + ((user.getReviews().size * 4) / 3)
                        + (user.getLiked().size / 3) + (user.getFollowers().size / 3), 1
            )
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            if (user == SystemData.getLoggedIn()) {
                val newFragment = ProfileFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("user", user)
                    }
                }
                parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment)
                    .commit()
            } else {
                val newFragment = ViewingProfileFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("lookedAtProfile", user)
                    }
                }
                parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment)
                    .commit()
            }
        }
    }

    private fun checkProgress(bar: ProgressBar?, i: Int, x: Int) {
        if (bar != null) {
            bar.progress = i * x

            if (bar.progress == 100) {
                //-16711936 = green
                bar.progressTintList = ColorStateList.valueOf(-16711936)
            }
        }
    }

}
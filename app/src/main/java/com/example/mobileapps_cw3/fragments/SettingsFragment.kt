package com.example.mobileapps_cw3.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import com.example.mobileapps_cw3.LoginActivity
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.SystemData
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout = view.findViewById<RelativeLayout>(R.id.btnLogout)
        val nightmode = view.findViewById<SwitchMaterial>(R.id.nightmode)

        btnLogout.setOnClickListener {
            SystemData.setLoggedIn(null)
            requireActivity().startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }

        /*
        commented until fully done

        nightmode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Recreate the activity for the changes to take effect
            recreate(requireActivity())
        }
         */
    }

    override fun onDestroy() {
        super.onDestroy()
        SystemData.clear()
    }

}
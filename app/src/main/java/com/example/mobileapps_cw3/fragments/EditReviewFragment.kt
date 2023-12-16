package com.example.mobileapps_cw3.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.Review
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Locale

class EditReviewFragment : Fragment() {

    private val imageRequest = 1
    private var selectedImageBitmap: Bitmap? = null
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val review = arguments?.getSerializable("review") as? Review
        val rating = view.findViewById<RatingBar>(R.id.ratingBar)
        val title = view.findViewById<EditText>(R.id.txtTitle)
        val tagged = view.findViewById<TextView>(R.id.txtTagged)
        val description = view.findViewById<EditText>(R.id.description)

        if (review != null) {
            rating.rating = review.getRating()
            title.setText(review.getTitle())
            tagged.text = review.getLocation()
            description.setText(review.getDescription())

            if (review.getImage() != "") {
                val storageRef = FirebaseStorage.getInstance().getReference(review.getImage())
                val localFile = File.createTempFile("images", "jpg")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Glide.with(view.context).load(localFile)
                        .into(view.findViewById(R.id.reviewPhoto))
                }
            }
        }

        view.findViewById<ImageView>(R.id.btnLocation).setOnClickListener {
            if (tagged.text == "") {
                getLocationAndSetTaggedText(tagged)
            } else {
                tagged.text = ""
            }
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            val reviewsFragment = ReviewsProfileFragment()
            review?.getUserProfile {
                reviewsFragment.arguments = Bundle().apply {
                    putSerializable("user", it)
                }

                parentFragmentManager.beginTransaction().replace(
                    R.id.frame_layout,
                    reviewsFragment
                ).commit()
            }
        }

        val reviewPhotoImageView = view.findViewById<ImageView>(R.id.reviewPhoto)

        reviewPhotoImageView.setOnClickListener {
            showPopupRemove(it, review, reviewPhotoImageView)
        }

        view.findViewById<MaterialButton>(R.id.btnConfirmEdit).setOnClickListener {
            if (review != null) {
                review.setRating(rating.rating)
                review.setTitle(title.text.toString())
                review.setLocation(tagged.text.toString())
                review.setDescription(description.text.toString())

                if (selectedImageBitmap != null) {
                    uploadImageToFirebase(selectedImageBitmap!!) { imagePath ->
                        review.setImage(imagePath)
                        updateReview(review)
                    }
                } else {
                    updateReview(review)
                }
            }
        }
    }

    private fun updateReview(review: Review) {
        review.edit { success ->
            if (success) {
                val reviewsFragment = ReviewsProfileFragment()
                review.getUserProfile {
                    reviewsFragment.arguments = Bundle().apply {
                        putSerializable("user", it)
                    }

                    parentFragmentManager.beginTransaction().replace(
                        R.id.frame_layout,
                        reviewsFragment
                    ).commit()
                }

                Toast.makeText(
                    context, "Successfully updated review.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context, "Error updating data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imageRequest && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageBitmap =
                MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    selectedImageUri
                )

            view?.findViewById<ImageView>(R.id.reviewPhoto)?.setImageBitmap(selectedImageBitmap)
        }
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap, callback: (String) -> Unit) {
        getCountOfItemsInStorageDirectory("reviews/") { count ->
            if (count != -1) {
                val updatedCount = count * 3
                val imageRef = storageRef.child("reviews/photo_$updatedCount.jpg")

                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

                val data = byteArrayOutputStream.toByteArray()
                val uploadTask = imageRef.putBytes(data)

                uploadTask.addOnSuccessListener {
                    callback.invoke(imageRef.path)
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Image upload failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error connecting with database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getLocationAndSetTaggedText(txtTagged: TextView) {
        if (isLocationPermissionGranted()) {
            if (isLocationProviderEnabled()) {
                try {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener(requireActivity(), OnSuccessListener { location ->
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude

                                setTaggedTextWithLocation(latitude, longitude, txtTagged)
                            }
                        })
                } catch (e: SecurityException) {
                     e.printStackTrace()
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun isLocationProviderEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            permCode
        )
    }

    private val permCode = 1001

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    view?.let { getLocationAndSetTaggedText(it.findViewById(R.id.txtTagged)) }
                }
            }
        }
    }

    private fun setTaggedTextWithLocation(latitude: Double, longitude: Double, txtTagged: TextView) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address: Address = addresses[0]
                val city = address.locality.toString().plus(", ".plus(address.countryName))

                println(address)
                txtTagged.text = city
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getCountOfItemsInStorageDirectory(directory: String, callback: (Int) -> Unit) {
        storageRef.child(directory).listAll().addOnSuccessListener { listResult ->
            val count = listResult.items.size
            callback.invoke(count)
        }.addOnFailureListener {
            callback.invoke(-1)
        }
    }

    private fun showPopupRemove(it: View?, review: Review?, photo: ImageView) {
        val popupMenu = PopupMenu(it?.context, it)
        popupMenu.inflate(R.menu.pop_up_menu_remove)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.remove_picture -> {
                    review?.setImage("")
                    photo.scaleType = ImageView.ScaleType.FIT_XY
                    photo.setImageResource(R.drawable.add_photo)

                    true
                }
                R.id.add_photo -> {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, imageRequest)

                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

}

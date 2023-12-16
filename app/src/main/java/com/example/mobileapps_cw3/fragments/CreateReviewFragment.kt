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
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.fragments.RestaurantProfileFragment
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Restaurant
import com.example.mobileapps_cw3.structures.Review
import com.example.mobileapps_cw3.structures.UpdateCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

class CreateReviewFragment : Fragment() {

    private val imageRequest = 1
    private var selectedImageBitmap: Bitmap? = null
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var savedLocation: String = ""
    private var savedTitle: String = ""
    private var savedDesc: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_write_review, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("location", savedLocation)
        outState.putString("title", savedTitle)
        outState.putString("description", savedDesc)
    }

    override fun onPause() {
        super.onPause()

        savedLocation = view?.findViewById<TextView>(R.id.txtTagged)?.text.toString()
        savedTitle = view?.findViewById<EditText>(R.id.txtTitle)?.text.toString()
        savedDesc = view?.findViewById<EditText>(R.id.txtDescription)?.text.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = arguments?.getSerializable("restaurant") as? Restaurant
        val user = arguments?.getSerializable("user") as? Profile
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val tagged = view.findViewById<TextView>(R.id.txtTagged)
        view.findViewById<ImageView>(R.id.btnLocation).setOnClickListener {
            if (tagged.text == "") {
                getLocation(tagged)
            } else {
                tagged.text = ""
            }
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            replaceFragment(RestaurantProfileFragment(), user, restaurant)
        }

        view.findViewById<ImageView>(R.id.reviewPhoto).setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, imageRequest)
        }

        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val rating = view.findViewById<RatingBar>(R.id.ratingBar).rating
            val title = view.findViewById<EditText>(R.id.txtTitle).text.toString()
            val description = view.findViewById<EditText>(R.id.txtDescription).text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && restaurant != null && user != null) {
                firebase.collection("reviews")
                    .orderBy("reviewId", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
                        storageRef.child("reviews/").listAll().addOnSuccessListener { listResult ->
                            var id = 1
                            if (!documents.isEmpty) {
                                id = documents.documents[0].id.toInt() + 1
                            }

                            println("Next available ID: $id")
                            val count = listResult.items.size

                            val review = Review(
                                id,
                                user.getId(),
                                restaurant.getId(),
                                emptyList(),
                                Timestamp.now(),
                                description,
                                "reviews/photo_${count + 1}.jpg",
                                title,
                                "",
                                tagged.text.toString(),
                                0,
                                rating
                            )

                            if (selectedImageBitmap != null) {
                                uploadImageToFirebase(selectedImageBitmap!!, count + 1) {
                                    firebase.collection("reviews").document(id.toString())
                                        .set(review)
                                        .addOnSuccessListener {
                                            Snackbar.make(
                                                view,
                                                "Review submitted successfully!",
                                                Snackbar.LENGTH_SHORT
                                            ).show()

                                            user.addReview(id)
                                            user.update()

                                            restaurant.addReview(id)
                                            restaurant.update(object : UpdateCallback {
                                                override fun onUpdateComplete() {
                                                    replaceFragment(
                                                        RestaurantProfileFragment(),
                                                        user,
                                                        restaurant
                                                    )
                                                }
                                            })
                                        }
                                }
                            } else {
                                firebase.collection("reviews").document(id.toString())
                                    .set(review)
                                    .addOnSuccessListener {
                                        Snackbar.make(
                                            view,
                                            "Review submitted successfully!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        user.addReview(id)
                                        user.update()

                                        restaurant.addReview(id)
                                        restaurant.update(object : UpdateCallback {
                                            override fun onUpdateComplete() {
                                                replaceFragment(
                                                    RestaurantProfileFragment(),
                                                    user,
                                                    restaurant
                                                )
                                            }
                                        })
                                    }
                            }
                        }
                    }
            }
        }
    }

    private fun getLocation(txtTagged: TextView) {
        if (isLocationGranted()) {
            if (isLocationEnabled()) {
                try {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener(requireActivity(), OnSuccessListener { location ->
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude

                                setLocation(latitude, longitude, txtTagged)
                            }
                        })
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        } else {
            requestLocation()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun isLocationGranted(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    private fun requestLocation() {
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
                    view?.let { getLocation(it.findViewById(R.id.txtTagged)) }
                }
            }
        }
    }

    private fun setLocation(latitude: Double, longitude: Double, txtTagged: TextView) {
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

    private fun replaceFragment(
        fragment: Fragment,
        user: Profile? = null,
        restaurant: Restaurant? = null
    ) {
        val newFragment = fragment.apply {
            arguments = Bundle().apply {
                putSerializable("user", user)
                putSerializable("restaurant", restaurant)
            }
        }
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment).commit()
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap, count: Int, callback: () -> Unit) {
        val imageRef = storageRef.child("reviews/photo_$count.jpg")

        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            callback.invoke()
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                "Image upload failed: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

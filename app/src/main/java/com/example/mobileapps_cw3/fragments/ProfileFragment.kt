package com.example.mobileapps_cw3.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.SystemData
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileFragment : Fragment() {

    private var profile: Profile? = null
    private val imageRequest = 1
    private var selectedImageBitmap: Bitmap? = null
    private val storageRef = FirebaseStorage.getInstance().reference

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val user = arguments?.getSerializable("user") as? Profile
        this.profile = user
        val ttlUsername = view.findViewById<TextView>(R.id.username)
        val txtUsername = view.findViewById<TextView>(R.id.txtUsername)
        val txtFollowers = view.findViewById<TextView>(R.id.txtFollowerCount)
        val txtReviews = view.findViewById<TextView>(R.id.txtReviewCount)
        val picture = view.findViewById<ImageView>(R.id.profilePicture)

        user?.update()

        if (user != null) {
            if (user.getId() != -1) {
                txtUsername.text = user.getUsername()
                ttlUsername.text = user.getUsername()
                txtFollowers.text = user.getFollowers().size.toString()
                txtReviews.text = user.getReviews().size.toString()

                if (user.getImage() != "") {
                    val storageRef = FirebaseStorage.getInstance().getReference(user.getImage())
                    val localFile = File.createTempFile("images", "jpg")

                    storageRef.getFile(localFile).addOnSuccessListener {
                        Glide.with(view.context).load(localFile)
                            .into(picture)
                    }
                }
            } else {
                ttlUsername.text = getString(R.string.guest_account)
                txtUsername.text = getString(R.string.guest_account)
                view.findViewById<TextView>(R.id.txtPassword).text = "Unavailable"

                val storageRef = FirebaseStorage.getInstance().getReference("guest.png")
                val localFile = File.createTempFile("images", "jpg")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Glide.with(view.context).load(localFile)
                        .into(picture)
                }
            }
        }

        picture.setOnClickListener {
            if (user?.getId() != -1) {
                showPopupRemove(it, user, picture)
            }
        }

        view.findViewById<LinearLayout>(R.id.btnProfileReviews).setOnClickListener {
            if (!SystemData.getGuest()) {
                val reviewsFragment = ReviewsProfileFragment()
                reviewsFragment.arguments = Bundle().apply {
                    putSerializable("user", user)
                }
                replaceFragment(reviewsFragment)
            } else {
                Toast.makeText(context, "Error: You're a guest user!", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<LinearLayout>(R.id.btnProfileReviews).setOnClickListener {
            if (!SystemData.getGuest()) {
                val reviewsFragment = ReviewsProfileFragment()
                reviewsFragment.arguments = Bundle().apply {
                    putSerializable("user", user)
                }
                replaceFragment(reviewsFragment)
            } else {
                Toast.makeText(context, "Error: You're a guest user!", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<LinearLayout>(R.id.profFollowing).setOnClickListener {
            if (!SystemData.getGuest()) {
                val followingFragment = FollowingFragment()
                followingFragment.arguments = Bundle().apply {
                    putSerializable("user", user)
                }
                replaceFragment(followingFragment)
            } else {
                Toast.makeText(context, "Error: You're a guest user!", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<LinearLayout>(R.id.btnBadge).setOnClickListener {
            if (!SystemData.getGuest()) {
                val badgeFragment = BadgeFragment()
                badgeFragment.arguments = Bundle().apply {
                    putSerializable("user", user)
                }
                replaceFragment(badgeFragment)
            } else {
                Toast.makeText(context, "Error: You're a guest user!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showPopupRemove(it: View?, user: Profile?, picture: ImageView?) {
        val popupMenu = PopupMenu(it?.context, it)
        popupMenu.inflate(R.menu.pop_up_menu_remove)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.remove_picture -> {
                    user?.setImage("profile/default.jpg")
                    picture?.scaleType = ImageView.ScaleType.FIT_XY
                    picture?.setImageResource(R.drawable.img_default)

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

            view?.findViewById<ImageView>(R.id.profilePicture)?.setImageBitmap(selectedImageBitmap)

            if (selectedImageBitmap != null) {
                uploadImageToFirebase(selectedImageBitmap!!) { imagePath ->
                    profile?.setImage(imagePath)
                    profile?.updateInDatabase()
                }
            } else {
                profile?.updateInDatabase()
            }
        }
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap, callback: (String) -> Unit) {
        getCountOfItemsInStorageDirectory("profile/") { count ->
            if (count != -1) {
                val updatedCount = count * 3
                val imageRef = storageRef.child("profile/profile_$updatedCount.jpg")

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

    private fun getCountOfItemsInStorageDirectory(directory: String, callback: (Int) -> Unit) {
        storageRef.child(directory).listAll().addOnSuccessListener { listResult ->
            val count = listResult.items.size
            callback.invoke(count)
        }.addOnFailureListener {
            callback.invoke(-1)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }

}
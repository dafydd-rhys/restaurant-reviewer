package com.example.mobileapps_cw3.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.databinding.UiReviewBinding
import com.example.mobileapps_cw3.fragments.EditReviewFragment
import com.example.mobileapps_cw3.fragments.FocusedReviewFragment
import com.example.mobileapps_cw3.fragments.ViewingProfileFragment
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Review
import com.example.mobileapps_cw3.structures.SystemData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ReviewAdapter(
    //list of reviews
    private var reviews: List<Review>,
    private val listener: ReviewAdapterListener,
    private val fromProfile: Boolean
) :
    //attaches recycler view adapted
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UiReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener, fromProfile)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateReviews(newReviews: List<Review>) {
        //sets the reviews list to new list
        reviews = newReviews
        notifyDataSetChanged()
    }



    fun getReviews(): List<Review> {
        return reviews
    }

    interface ReviewAdapterListener {
        fun onReviewDeleted(review: Review)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ViewHolder(
        //Binding to UI
        private val binding: UiReviewBinding,
        private val listener: ReviewAdapterListener,
        private val fromProfile: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(review: Review) {
            //loads appropriate data from review into review UI
            review.getRestaurantName {
                binding.reviewDate.text = "${review.getReviewDate()} on $it"
            }
            binding.reviewRating.rating = review.getRating()
            binding.reviewTitle.text = review.getTitle()
            binding.reviewDescription.text = review.getDescription()
            binding.reviewLocation.text = review.getLocation()
            binding.txtLike.text = review.getLikes().toString()
            binding.txtDislike.text = review.getDislikes().toString()

            //Checks if logged in user is guest
            if (!SystemData.getGuest()) {
                val loggedIn = SystemData.getLoggedIn()

                if (loggedIn != null) {
                    //checks if the review has already been liked or dislikes
                    if (loggedIn.getLiked().contains(review.getReviewId().toLong())) {
                        binding.btnLikeButton.setImageResource(R.drawable.icon_thumbs_up_selected)
                    } else if (loggedIn.getDisliked().contains(review.getReviewId().toLong())) {
                        binding.btnDislike.setImageResource(R.drawable.baseline_thumb_down_24)
                    }
                }
            }

            //loads appropriate data from user into review UI
            FirebaseFirestore.getInstance().collection("users").document(
                review.getUserId().toString()).get().addOnSuccessListener { documentSnapshot ->
                    //if user exists
                    if (documentSnapshot.exists()) {
                        //converts snapshot to user instance
                        val user = documentSnapshot.toObject(Profile::class.java)

                        if (user != null) {
                            //sets username
                            binding.reviewerName.text = user.getUsername()

                            //if custom image is present
                            if (user.getImage() != "") {
                                val storageRef = FirebaseStorage.getInstance().getReference(user.getImage())
                                val localFile = File.createTempFile("images", "jpg")

                                //loads profile picture into review
                                storageRef.getFile(localFile).addOnSuccessListener {
                                    Glide.with(binding.root.context).load(localFile)
                                        .into(binding.reviewerPicture)
                                }
                            }
                        }
                    }
                }

            binding.manageReview.setOnClickListener {
                review.getUserProfile { prof ->
                    if (prof?.getId() == SystemData.getLoggedIn()?.getId()) {
                        showPopupMenuAuth(it, review)
                    } else {
                        showPopupMenuNoAuth(it, review)
                    }
                }
            }

            binding.like.setOnClickListener {
                if (!SystemData.getGuest()) {
                    val loggedIn = SystemData.getLoggedIn()

                    review.getUserProfile {prof ->
                        if (loggedIn != null && loggedIn != prof) {
                            val likedReviews = loggedIn.getLiked().toMutableList()
                            val dislikedReviews = loggedIn.getDisliked().toMutableList()

                            if (!likedReviews.contains(review.getReviewId().toLong())) {
                                likedReviews.add(review.getReviewId().toLong())
                                review.addLike(1)

                                if (dislikedReviews.contains(review.getReviewId().toLong())) {
                                    review.addDislike(-1)
                                    dislikedReviews.remove(review.getReviewId().toLong())
                                    loggedIn.setDisliked(dislikedReviews)
                                    binding.txtDislike.text =
                                        (binding.txtDislike.text.toString().toInt() - 1).toString()
                                }

                                loggedIn.setLiked(likedReviews)
                                loggedIn.updateInDatabase()
                                prof?.addUpvote(1)

                                binding.txtLike.text = (binding.txtLike.text.toString().toInt() + 1).toString()
                                binding.btnLikeButton.setImageResource(R.drawable.icon_thumbs_up_selected)
                                binding.btnDislike.setImageResource(R.drawable.thumbs_down)
                            } else {
                                review.addLike(-1)
                                likedReviews.remove(review.getReviewId().toLong())
                                loggedIn.setLiked(likedReviews)
                                loggedIn.updateInDatabase()
                                prof?.addUpvote(-1)

                                binding.txtLike.text = (binding.txtLike.text.toString().toInt() - 1).toString()
                                binding.btnLikeButton.setImageResource(R.drawable.thumbs_up)
                            }
                        } else {
                            Toast.makeText(it.context, "Cant interact with your own reviews!",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(it.context, "Cant interact as a guest!",
                        Toast.LENGTH_SHORT).show()
                }
            }

            binding.dislike.setOnClickListener {
                if (!SystemData.getGuest()) {
                    val loggedIn = SystemData.getLoggedIn()

                    review.getUserProfile {prof ->
                        if (loggedIn != null && loggedIn != prof) {
                            val likedReviews = loggedIn.getLiked().toMutableList()
                            val dislikedReviews = loggedIn.getDisliked().toMutableList()

                            // Check if the review ID is not already in liked reviews
                            if (!dislikedReviews.contains(review.getReviewId().toLong())) {
                                review.addDislike(1)
                                dislikedReviews.add(review.getReviewId().toLong())

                                if (likedReviews.contains(review.getReviewId().toLong())) {
                                    review.addLike(-1)
                                    likedReviews.remove(review.getReviewId().toLong())
                                    loggedIn.setLiked(likedReviews)
                                    binding.txtLike.text =
                                        (binding.txtLike.text.toString().toInt() - 1).toString()
                                    prof?.addUpvote(-1)
                                }

                                loggedIn.setDisliked(dislikedReviews)
                                loggedIn.updateInDatabase()

                                binding.txtDislike.text =
                                    (binding.txtDislike.text.toString().toInt() + 1).toString()
                                binding.btnLikeButton.setImageResource(R.drawable.thumbs_up)
                                binding.btnDislike.setImageResource(R.drawable.baseline_thumb_down_24)
                            } else {
                                review.addDislike(-1)
                                dislikedReviews.remove(review.getReviewId().toLong())
                                loggedIn.setDisliked(dislikedReviews)
                                loggedIn.updateInDatabase()

                                binding.txtDislike.text =
                                    (binding.txtDislike.text.toString().toInt() - 1).toString()
                                binding.btnDislike.setImageResource(R.drawable.thumbs_down)
                            }
                        } else {
                            Toast.makeText(it.context, "Cant interact with your own reviews!",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(it.context, "Cant interact as a guest!",
                        Toast.LENGTH_SHORT).show()
                }
            }

            binding.share.setOnClickListener {
                shareReview(review, it)
            }

            binding.reviewInstance.setOnClickListener {
                val fragmentManager = (it.context as AppCompatActivity).supportFragmentManager
                val fragment = FocusedReviewFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("review", review)

                        if (fromProfile) {
                            putSerializable("fromProfile", true)
                        } else {
                            putSerializable("fromProfile", false)
                        }
                    }
                }
                fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null) // Optional: Add the transaction to the back stack
                    .commit()
            }
        }

        private fun shareReview(review: Review, view: View) {
            val shareIntent = Intent(Intent.ACTION_SEND)

            shareIntent.type = "text/plain"
            review.getRestaurantName {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Review of $it")
            }
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this review:\n${review.getDescription()}")

            val chooser = Intent.createChooser(shareIntent, "Share Review")
            view.context.startActivity(chooser)
        }

        private fun showPopupMenuNoAuth(view: View, review: Review) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.pop_up_menu_noauth)

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_navigate -> {
                        review.getUserProfile {prof ->
                            review.getRestaurantProfile {
                                val fragmentManager = (view.context as AppCompatActivity).supportFragmentManager
                                val fragment = ViewingProfileFragment().apply {
                                    arguments = Bundle().apply {
                                        putSerializable("lookedAtProfile", prof)
                                        putSerializable("lookedAtRestaurant", it)
                                    }
                                }
                                fragmentManager.beginTransaction()
                                    .replace(R.id.frame_layout, fragment)
                                    .addToBackStack(null) // Optional: Add the transaction to the back stack
                                    .commit()
                            }

                        }
                        true
                    }
                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }

        private fun showPopupMenuAuth(view: View, review: Review) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.pop_up_menu_auth)

            // Set a listener for item clicks
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        val fragmentManager = (view.context as AppCompatActivity).supportFragmentManager
                        val fragment = EditReviewFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable("review", review)
                            }
                        }
                        fragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack(null) // Optional: Add the transaction to the back stack
                            .commit()

                        true
                    }

                    R.id.menu_delete -> {
                        deleteReview(review, view)
                        true
                    }

                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }

        private fun deleteReview(review: Review, view: View) {
            val reviewsCollection = FirebaseFirestore.getInstance().collection("reviews")
            val usersCollection = FirebaseFirestore.getInstance().collection("users")
            val commentCollection = FirebaseFirestore.getInstance().collection("comments")
            val userId = review.getUserId().toString()
            val comments = review.getReplies()

            // Delete the review
            reviewsCollection.document(review.getReviewId().toString()).delete()
                .addOnSuccessListener {
                    usersCollection.document(userId)
                        .update("reviews", FieldValue.arrayRemove(review.getReviewId()))
                        .addOnSuccessListener {
                            val restaurantsCollection =
                                FirebaseFirestore.getInstance().collection("restaurants")
                            restaurantsCollection.document(review.getRestaurantId().toString())
                                .update("reviews", FieldValue.arrayRemove(review.getReviewId()))
                                .addOnSuccessListener {
                                    for (id in comments) {
                                        commentCollection.document(id.toString()).delete()
                                    }

                                    Toast.makeText(
                                        view.context,
                                        "Review deleted successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    listener.onReviewDeleted(review)
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                view.context,
                                "Error deleting review!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }
    }

}
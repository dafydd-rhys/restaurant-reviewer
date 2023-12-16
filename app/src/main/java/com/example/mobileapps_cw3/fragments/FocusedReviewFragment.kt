package com.example.mobileapps_cw3.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.adapters.CommentAdapter
import com.example.mobileapps_cw3.structures.Comment
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.Review
import com.example.mobileapps_cw3.structures.SystemData
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FocusedReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_focused_review, container, false)
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val review = arguments?.getSerializable("review") as? Review
        val fromProfile = arguments?.getSerializable("fromProfile") as Boolean

        if (review != null) {
            view.findViewById<RatingBar>(R.id.reviewRating)
            view.findViewById<TextView>(R.id.reviewDate).text = review.getReviewDate()
            view.findViewById<TextView>(R.id.reviewTitle).text = review.getTitle()
            view.findViewById<TextView>(R.id.reviewDescription).text = review.getDescription()
            view.findViewById<RatingBar>(R.id.reviewRating).rating= review.getRating()
            view.findViewById<TextView>(R.id.reviewLocation).text = review.getLocation()
            view.findViewById<TextView>(R.id.txtLike).text = review.getLikes().toString()
            view.findViewById<TextView>(R.id.txtDislike).text = review.getDislikes().toString()

            val btnLike = view.findViewById<ImageView>(R.id.btnLike)
            val btnDislike = view.findViewById<ImageView>(R.id.btnDislike)

            loadComments(view, review)

            if (!SystemData.getGuest()) {
                val loggedIn = SystemData.getLoggedIn()

                if (loggedIn != null) {
                    if (loggedIn.getLiked().contains(review.getReviewId().toLong())) {
                        btnLike.setImageResource(R.drawable.icon_thumbs_up_selected)
                    } else if (loggedIn.getDisliked().contains(review.getReviewId().toLong())) {
                        btnDislike.setImageResource(R.drawable.baseline_thumb_down_24)
                    }
                }
            }

            if (review.getImage() != "") {
                val storageRef = FirebaseStorage.getInstance().getReference(review.getImage())
                val localFile = File.createTempFile("images", "jpg")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Glide.with(view.context).load(localFile)
                        .into(view.findViewById(R.id.reviewPhoto))
                }
            }

            val usersCollection = FirebaseFirestore.getInstance().collection("users")
            usersCollection.document(review.getUserId().toString()).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(Profile::class.java)

                        if (user != null) {
                            view.findViewById<TextView>(R.id.reviewerName).text = user.getUsername()

                            if (user.getImage() != "") {
                                val reference =
                                    FirebaseStorage.getInstance().getReference(user.getImage())

                                val photo = File.createTempFile("images", "jpg")

                                reference.getFile(photo).addOnSuccessListener {
                                    Glide.with(view.context).load(photo)
                                        .into(view.findViewById(R.id.reviewerPicture))
                                }
                            }
                        }
                    }
                }
        }

        view.findViewById<ImageView>(R.id.manageReview).setOnClickListener {
            review?.getUserProfile { prof ->
                if (prof?.getId() == SystemData.getLoggedIn()?.getId()) {
                    showPopupMenuAuth(it, review)
                } else {
                    showPopupMenuNoAuth(it, review)
                }
            }
        }

        view.findViewById<MaterialButton>(R.id.btnComment).setOnClickListener {
            if (!SystemData.getGuest()) {
                val commentText = view.findViewById<EditText>(R.id.txtComment)

                if (commentText.text.toString().isNotEmpty()) {
                    if (review != null) {
                        val commentsCollection =
                            FirebaseFirestore.getInstance().collection("comments")

                        commentsCollection.orderBy("id", Query.Direction.DESCENDING).limit(1)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                var highestId = 0

                                if (!querySnapshot.isEmpty) {
                                    val highestComment =
                                        querySnapshot.documents[0].toObject(Comment::class.java)
                                    highestId = highestComment?.getId() ?: 0
                                }

                                val newCommentId = highestId + 1
                                val newComment = Comment(
                                    id = newCommentId,
                                    reviewId = review.getReviewId(),
                                    userId = SystemData.getLoggedIn()?.getId() ?: 0,
                                    comment = commentText.text.toString(),
                                    date = Timestamp.now()
                                )

                                commentsCollection.document(newCommentId.toString())
                                    .set(newComment)
                                    .addOnSuccessListener {
                                        review.updateComments(newCommentId)
                                        commentText.setText("")

                                        Toast.makeText(
                                            view.context,
                                            "Comment added successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                    }
                } else {
                    Toast.makeText(view.context, "Please enter a comment.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(view.context, "Guests cannot add comments.", Toast.LENGTH_SHORT).show()
            }
        }

        val txtLike = view.findViewById<TextView>(R.id.txtLike)
        val txtDislike = view.findViewById<TextView>(R.id.txtDislike)
        val btnLike = view.findViewById<ImageView>(R.id.btnLike)
        val btnDislike = view.findViewById<ImageView>(R.id.btnDislike)

        view.findViewById<LinearLayout>(R.id.like).setOnClickListener {
            if (!SystemData.getGuest() && review != null) {
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
                                txtDislike.text =
                                    (txtDislike.text.toString().toInt() - 1).toString()
                            }

                            loggedIn.setLiked(likedReviews)
                            loggedIn.updateInDatabase()
                            prof?.addUpvote(1)

                            txtLike.text = (txtLike.text.toString().toInt() + 1).toString()
                            btnLike.setImageResource(R.drawable.icon_thumbs_up_selected)
                            btnDislike.setImageResource(R.drawable.thumbs_down)
                        } else {
                            review.addLike(-1)
                            likedReviews.remove(review.getReviewId().toLong())
                            loggedIn.setLiked(likedReviews)
                            loggedIn.updateInDatabase()
                            prof?.addUpvote(-1)

                            txtLike.text = (txtLike.text.toString().toInt() - 1).toString()
                            btnLike.setImageResource(R.drawable.thumbs_up)
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

        view.findViewById<LinearLayout>(R.id.dislike).setOnClickListener {
            if (!SystemData.getGuest() && review != null) {
                val loggedIn = SystemData.getLoggedIn()

                review.getUserProfile {prof ->
                    if (loggedIn != null && loggedIn != prof) {
                        val likedReviews = loggedIn.getLiked().toMutableList()
                        val dislikedReviews = loggedIn.getDisliked().toMutableList()

                        if (!dislikedReviews.contains(review.getReviewId().toLong())) {
                            review.addDislike(1)
                            dislikedReviews.add(review.getReviewId().toLong())

                            if (likedReviews.contains(review.getReviewId().toLong())) {
                                review.addLike(-1)
                                likedReviews.remove(review.getReviewId().toLong())
                                loggedIn.setLiked(likedReviews)
                                txtLike.text = (txtLike.text.toString().toInt() - 1).toString()
                                prof?.addUpvote(-1)
                            }

                            loggedIn.setDisliked(dislikedReviews)
                            loggedIn.updateInDatabase()

                            txtDislike.text = (txtDislike.text.toString().toInt() + 1).toString()
                            btnLike.setImageResource(R.drawable.thumbs_up)
                            btnDislike.setImageResource(R.drawable.baseline_thumb_down_24)
                        } else {
                            review.addDislike(-1)
                            dislikedReviews.remove(review.getReviewId().toLong())
                            loggedIn.setDisliked(dislikedReviews)
                            loggedIn.updateInDatabase()

                            txtDislike.text = (txtDislike.text.toString().toInt() - 1).toString()
                            btnDislike.setImageResource(R.drawable.thumbs_down)
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

        view.findViewById<LinearLayout>(R.id.share).setOnClickListener {
            if (review != null) {
                shareReview(review, it)
            }
        }

        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            goBack(fromProfile, review)
        }
    }

    private fun shareReview(review: Review, view: View) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        review.getRestaurantName {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Review of $it")
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this review by: \n${review.getDescription()}")

        val chooser = Intent.createChooser(shareIntent, "Share Review")
        view.context.startActivity(chooser)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadComments(view: View, review: Review) {
        val commentsList = view.findViewById<RecyclerView>(R.id.recComments)
        val layoutManager = LinearLayoutManager(view.context)
        val commentAdapter = CommentAdapter(emptyList())

        commentsList.layoutManager = layoutManager
        commentsList.adapter = commentAdapter

        val commentsCollection = FirebaseFirestore.getInstance().collection("comments")
        val commentIds = review.getReplies()
        val comments = mutableListOf<Comment>()

        val commentsListener = commentsCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            comments.clear()

            for (commentDocument in snapshot?.documents ?: emptyList()) {
                val comment = commentDocument.toObject(Comment::class.java)
                if (comment != null) {
                    comments.add(comment)
                }
            }

            commentAdapter.comments = comments
            commentAdapter.notifyDataSetChanged()
        }

        view.tag = commentsListener

        for (commentId in commentIds) {
            commentsCollection.document(commentId.toString()).get().addOnSuccessListener { commentDocument ->
                if (commentDocument.exists()) {
                    val comment = commentDocument.toObject(Comment::class.java)
                    if (comment != null) {
                        comments.add(comment)
                        commentAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val commentsListener = view?.tag as? ListenerRegistration
        commentsListener?.remove()
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }

    private fun showPopupMenuNoAuth(view: View, review: Review) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.pop_up_menu_noauth)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_navigate -> {
                    review.getUserProfile { prof ->
                        review.getRestaurantProfile {
                            val fragmentManager =
                                (view.context as AppCompatActivity).supportFragmentManager
                            val fragment = ViewingProfileFragment().apply {
                                arguments = Bundle().apply {
                                    putSerializable("lookedAtProfile", prof)
                                    putSerializable("lookedAtRestaurant", it)
                                }
                            }
                            fragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, fragment)
                                .addToBackStack(null)
                                .commit()
                        }

                    }
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showPopupMenuAuth(view: View, review: Review) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.pop_up_menu_auth)

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
                        .addToBackStack(null)
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

        popupMenu.show()
    }

    private fun deleteReview(review: Review, view: View) {
        val reviewsCollection = FirebaseFirestore.getInstance().collection("reviews")
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val commentCollection = FirebaseFirestore.getInstance().collection("comments")
        val userId = review.getUserId().toString()
        val comments = review.getReplies()

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
                                goBack(true, review)
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

    private fun goBack(fromProfile: Boolean, review: Review?) {
        if (fromProfile) {
            review?.getUserProfile {
                val reviewsFragment = ReviewsProfileFragment()
                reviewsFragment.arguments = Bundle().apply {
                    putSerializable("user", it)
                    putSerializable("fromViewing", true)
                    review.getRestaurantProfile {
                        putSerializable("lookedAtRestaurant", it)
                        replaceFragment(reviewsFragment)
                    }
                }
            }
        } else {
            review?.getRestaurantProfile {
                val reviewsFragment = ReviewsFragment()
                reviewsFragment.arguments = Bundle().apply {
                    putSerializable("restaurant", it)
                    replaceFragment(reviewsFragment)
                }
            }
        }
    }

}




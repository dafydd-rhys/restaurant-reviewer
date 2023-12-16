package com.example.mobileapps_cw3.structures

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

data class Profile(
    private var id: Int,
    private var username: String,
    private var password: String,
    private var badges: List<Long> = emptyList(),
    private var reviews: List<Long> = emptyList(),
    private var image: String,
    private var followers: List<Long> = emptyList(),
    private var following: List<String> = emptyList(),
    private var notifications: Boolean,
    private var liked: List<Long> = emptyList(),
    private var disliked: List<Long> = emptyList(),
    private var upvotes: Int
) : Serializable {

    constructor() : this(0, "", "", emptyList(), emptyList(),
        "", emptyList(), emptyList(), false, emptyList(), emptyList(), 0)

    fun getId(): Int {
        return this.id
    }

    fun getUsername(): String {
        return this.username
    }

    fun getPassword(): String {
        return this.password
    }

    fun getBadges(): List<Long> {
        return this.badges
    }

    fun getReviews(): List<Long> {
        return this.reviews
    }

    fun getImage(): String {
        return this.image
    }

    fun getFollowers(): List<Long> {
        return this.followers
    }

    fun getFollowing(): List<String> {
        return this.following
    }

    fun getNotifications(): Boolean {
        return this.notifications
    }

    fun getLiked(): List<Long> {
        return this.liked
    }

    fun getDisliked(): List<Long> {
        return this.disliked
    }

    fun getUpvotes(): Int {
        return this.upvotes
    }

    fun addReview(id: Int) {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("users").document(getId().toString())

        userDocumentReference.update("reviews", FieldValue.arrayUnion(id))
    }

    fun update() {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("users").document(getId().toString())

        userDocumentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val updatedUser = documentSnapshot.toObject(Profile::class.java)

                    updatedUser?.let {
                        this.username = it.getUsername()
                        this.password = it.getPassword()
                        this.badges = it.getBadges()
                        this.reviews = it.getReviews()
                        this.image = it.getImage()
                        this.followers = it.getFollowers()
                        this.following = it.getFollowing()
                        this.notifications = it.getNotifications()
                    }

                    Log.d(ContentValues.TAG, "User data updated successfully!")
                } else {
                    Log.w(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error updating user data", e)
            }
    }

    fun updateInDatabase() {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("users").document(getId().toString())

        userDocumentReference
            .update(
                "username", getUsername(),
                "liked", getLiked(),
                "password", getPassword(),
                "badges", getBadges(),
                "reviews", getReviews(),
                "image", getImage(),
                "followers", getFollowers(),
                "following", getFollowing(),
                "notifications", getNotifications(),
                "disliked", getDisliked(),
                "upvotes", getUpvotes()
            )
    }

    fun setFollowing(following: MutableList<String>) {
        this.following = following
    }

    fun setFollowers(followers: MutableList<Long>) {
        this.followers = followers
    }

    fun setLiked(likedReviews: MutableList<Long>) {
        this.liked = likedReviews
    }

    fun setDisliked(dislikedReviews: MutableList<Long>) {
        this.disliked = dislikedReviews
    }

    fun addUpvote(x:Int) {
        this.upvotes += x
        updateInDatabase()
    }

    fun setImage(s: String) {
        this.image = s
    }

}

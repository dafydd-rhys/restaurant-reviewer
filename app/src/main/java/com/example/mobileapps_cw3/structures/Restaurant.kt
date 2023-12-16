package com.example.mobileapps_cw3.structures

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

interface UpdateCallback {
    fun onUpdateComplete()
}

data class Restaurant(
    private var id: Int = 0,
    private var name: String = "",
    private var cost: Float = 0f,
    private var rating: Float = 0f,
    private var image: String = "",
    private var logo: String = "",
    private var description: String = "",
    private var children: Boolean = false,
    private var followers: List<Int> = emptyList(),
    private var reviews: List<Int> = emptyList(),
    private var openingHours: List<String> = emptyList(),
    private var location: String = "",
    private var menu: List<Int> = emptyList(),
) : Serializable {

    constructor() : this(
        0, "", 0f, 0f, "", "", "",
        false, emptyList(), emptyList(), emptyList(),"", emptyList()
    )

    fun getId(): Int {
        return id
    }

    fun getName(): String {
        return name
    }

    fun getCost(): Float {
        return cost
    }

    fun getRating(): Float {
        return rating
    }

    fun getImage(): String {
        return image
    }

    fun getDescription(): String {
        return description
    }

    fun getLogo(): String {
        return logo
    }

    fun getChildren(): Boolean {
        return children
    }

    fun getFollowers(): List<Int> {
        return followers
    }

    fun getReviews(): List<Int> {
        return reviews
    }

    fun getOpeningHours(): List<String> {
        return openingHours
    }

    fun getLocation(): String {
        return location
    }

    fun getMenu(): List<Int> {
        return menu
    }

    fun addReview(id: Int) {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("restaurants").document(getId().toString())

        userDocumentReference.update("reviews", FieldValue.arrayUnion(id))
            .addOnSuccessListener {
                Log.d(TAG, "Review added to user document successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding review to user document", e)
            }
    }

    fun update(callback: UpdateCallback) {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("restaurants").document(getId().toString())

        userDocumentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val updatedUser = documentSnapshot.toObject(Restaurant::class.java)

                    updatedUser?.let {
                        this.id = it.getId()
                        this.name = it.getName()
                        this.cost = it.getCost()
                        this.rating = it.getRating()
                        this.image = it.getImage()
                        this.logo = it.getLogo()
                        this.description = it.getDescription()
                        this.children = it.getChildren()
                        this.followers = it.getFollowers()
                        this.openingHours = it.getOpeningHours()
                        this.reviews = it.getReviews()
                        this.location = it.getLocation()
                    }

                    Log.d(TAG, "User data updated successfully!")
                    callback.onUpdateComplete()
                } else {
                    Log.w(TAG, "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user data", e)
            }
    }

    fun setFollowers(restaurantFollowers: MutableList<Int>) {
        this.followers = restaurantFollowers
    }

    fun updateInDatabase() {
        val userDocumentReference = FirebaseFirestore.getInstance().
        collection("restaurants").document(getId().toString())

        userDocumentReference
            .update(
                "followers", getFollowers(),
            )
    }


}

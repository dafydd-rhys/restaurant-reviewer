package com.example.mobileapps_cw3.structures

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

data class Review(
    private var reviewId: Int = 0,
    private var userId: Int = 0,
    private var restaurantId: Int = 0,
    private var replies: List<Int> = emptyList(),
    @Transient private var date: Timestamp = Timestamp.now(),
    private var description: String = "",
    private var image: String = "",
    private var title: String = "",
    private var url: String = "",
    private var location: String = "",
    private var dislikes: Int = 0,
    private var rating: Float = 0f,
    private var likes: Int = 0
): Serializable {

    constructor() : this(
        0, 0, 0, emptyList(),Timestamp.now(), "",
        "", "", "", "", 0, 0f, 0)

    fun getReviewId(): Int {
        return reviewId
    }

    fun getUserId(): Int {
        return userId
    }

    fun getReplies(): List<Int> {
        return replies
    }

    fun getRestaurantId(): Int {
        return restaurantId
    }

    fun getDate(): Timestamp {
        return date
    }

    fun getDescription(): String {
        return description
    }

    fun getImage(): String {
        return image
    }

    fun getTitle(): String {
        return title
    }

    fun getUrl(): String {
        return url
    }

    fun getLocation(): String {
        return location
    }

    fun getDislikes(): Int {
        return dislikes
    }

    fun getLikes(): Int {
        return likes
    }

    fun getRating(): Float {
        return rating
    }

    fun getRestaurantName(callback: (String) -> Unit) {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("restaurants").document(getRestaurantId().toString())

        userDocumentReference.get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val restaurantName = documentSnapshot.getString("name")
                        callback(restaurantName.orEmpty())
                    } else {
                        callback("Unknown")
                    }
                } else {
                    callback("Unknown")
                }
            }
    }

    fun getUserProfile(callback: (Profile?) -> Unit) {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("users").document(getUserId().toString())

        userDocumentReference.get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(Profile::class.java)
                        callback(user)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
    }

    fun getRestaurantProfile(callback: (Restaurant?) -> Unit) {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("restaurants").
            document(getRestaurantId().toString())

        userDocumentReference.get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val restaurant = documentSnapshot.toObject(Restaurant::class.java)
                        callback(restaurant)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
    }

    fun getReviewDate(): String {
        val reviewDate = getDate().toDate()
        val timeDifference = Date().time - reviewDate.time

        val formattedDate = when {
            timeDifference < TimeUnit.HOURS.toMillis(1) -> {
                "Review left less than an hour ago"
            }

            timeDifference < TimeUnit.DAYS.toMillis(1) -> {
                val hoursAgo = TimeUnit.MILLISECONDS.toHours(timeDifference)
                "Review left $hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
            }

            timeDifference < TimeUnit.DAYS.toMillis(30) -> {
                val daysAgo = TimeUnit.MILLISECONDS.toDays(timeDifference)
                "Review left $daysAgo ${if (daysAgo == 1L) "day" else "days"} ago"
            }

            timeDifference < TimeUnit.DAYS.toMillis(365) -> {
                val monthsAgo = TimeUnit.MILLISECONDS.toDays(timeDifference) / 30
                "Review left $monthsAgo ${if (monthsAgo == 1L) "month" else "months"} ago"
            }

            else -> {
                val yearsAgo = TimeUnit.MILLISECONDS.toDays(timeDifference) / 365
                "Review left $yearsAgo ${if (yearsAgo == 1L) "year" else "years"} ago"
            }
        }

        return formattedDate
    }

    fun edit(onComplete: (Boolean) -> Unit) {
        val reviewsCollection = FirebaseFirestore.getInstance().collection("reviews")
        val reviewId = this.reviewId.toString()

        val updatedReviewData = mapOf(
            "rating" to this.rating,
            "title" to this.title,
            "location" to this.location,
            "description" to this.description,
            "image" to this.image
        )

        reviewsCollection.document(reviewId)
            .update(updatedReviewData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }


    fun setRating(rating: Float) {
        this.rating = rating
    }

    fun setTitle(toString: String) {
        this.title = toString
    }

    fun setLocation(toString: String) {
        this.location = toString
    }

    fun setDescription(toString: String) {
        this.description = toString
    }

    fun setImage(s: String) {
        this.image = s
    }

    fun updateComments(commentId: Int) {
        val reviewDocumentReference =
            FirebaseFirestore.getInstance().collection("reviews").document(getReviewId().toString())

        reviewDocumentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentReplies = documentSnapshot.get("replies") as List<*>

                    val updatedReplies = currentReplies.toMutableList().apply {
                        add(commentId)
                    }

                    reviewDocumentReference.update("replies", updatedReplies)
                }
            }
    }

    fun addLike(x: Int) {
        this.likes += x
        updateInDatabase()
    }

    fun addDislike(x: Int) {
        this.dislikes += x
        updateInDatabase()
    }

    private fun updateInDatabase() {
        val userDocumentReference =
            FirebaseFirestore.getInstance().collection("reviews").document(getUserId().toString())

        userDocumentReference.update(
            "likes", getLikes(),
            "dislikes", getDislikes(),
        )
    }


}

package com.example.mobileapps_cw3.structures

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

data class Comment(
    private var reviewId: Int = 0,
    private var userId: Int = 0,
    private var id: Int = 0,
    @Transient private var date: Timestamp = Timestamp.now(),
    private var comment: String = ""
): Serializable {

    fun getReviewId(): Int {
        return reviewId
    }

    fun getUserId(): Int {
        return userId
    }

    fun getId(): Int {
        return id
    }

    fun getDate(): Timestamp {
        return date
    }

    fun getComment(): String {
        return comment
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

    fun getCommentDate(): String {
        val commentDate = getDate().toDate()
        val timeDifference = Date().time - commentDate.time

        val formattedDate = when {
            timeDifference < TimeUnit.HOURS.toMillis(1) -> {
                "Comment left less than an hour ago"
            }

            timeDifference < TimeUnit.DAYS.toMillis(1) -> {
                val hoursAgo = TimeUnit.MILLISECONDS.toHours(timeDifference)
                "Comment left $hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
            }

            timeDifference < TimeUnit.DAYS.toMillis(30) -> {
                val daysAgo = TimeUnit.MILLISECONDS.toDays(timeDifference)
                "Comment left $daysAgo ${if (daysAgo == 1L) "day" else "days"} ago"
            }

            timeDifference < TimeUnit.DAYS.toMillis(365) -> {
                val monthsAgo = TimeUnit.MILLISECONDS.toDays(timeDifference) / 30
                "Comment left $monthsAgo ${if (monthsAgo == 1L) "month" else "months"} ago"
            }

            else -> {
                val yearsAgo = TimeUnit.MILLISECONDS.toDays(timeDifference) / 365
                "Comment left $yearsAgo ${if (yearsAgo == 1L) "year" else "years"} ago"
            }
        }

        return formattedDate
    }

    fun getRestaurantProfile(callback: (Restaurant?) -> Unit) {
        println(this)
        val reviewDocumentReference =
            FirebaseFirestore.getInstance().collection("reviews")
                .document(getReviewId().toString())


        reviewDocumentReference.get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val review = documentSnapshot.toObject(Review::class.java)
                        review?.getRestaurantProfile {
                            callback(it)
                        }
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
    }



}
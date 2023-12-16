package com.example.mobileapps_cw3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.SystemData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {

    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val context = this

        btnLogin.setOnClickListener {
            val username = findViewById<EditText>(R.id.txtEmail).text.toString()
            val password = findViewById<EditText>(R.id.txtPassword).text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                firebase.collection("users")
                    .whereEqualTo("username", username)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result?.documents

                            if (!documents.isNullOrEmpty()) {
                                val user = getProfile(documents[0])
                                SystemData.setLoggedIn(user)
                                SystemData.setGuest(false)

                                startActivity(Intent(this, MainActivity::class.java)
                                    .putExtra("user", user))

                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Login Failed. Invalid username or password.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Error during login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please Fill All Data Entries.", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnContinue.setOnClickListener {
            SystemData.setGuest(true)
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun getProfile(documentSnapshot: DocumentSnapshot?): Profile {
        val id = documentSnapshot?.get("id").toString().toInt()
        val username = documentSnapshot?.getString("username") ?: ""
        val password = documentSnapshot?.getString("password") ?: ""
        val badges = (documentSnapshot?.get("badges") as? List<*>)?.map { it as Long } ?: emptyList()
        val reviews = (documentSnapshot?.get("reviews") as? List<*>)?.map { it as Long } ?: emptyList()
        val image = documentSnapshot?.getString("image") ?: ""
        val followers = (documentSnapshot?.get("followers") as? List<*>)?.map { it as Long } ?: emptyList()
        val following = (documentSnapshot?.get("following") as? List<*>)?.map { it as String } ?: emptyList()
        val notifications = documentSnapshot?.getBoolean("notifications") ?: false
        val liked = (documentSnapshot?.get("liked") as? List<*>)?.map { it as Long } ?: emptyList()
        val disliked = (documentSnapshot?.get("disliked") as? List<*>)?.map { it as Long } ?: emptyList()
        val upvotes = documentSnapshot?.get("upvotes").toString().toInt()

        return Profile(id, username, password, badges, reviews, image, followers,
            following, notifications, liked, disliked, upvotes)
    }

}
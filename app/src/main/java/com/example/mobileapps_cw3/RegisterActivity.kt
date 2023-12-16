package com.example.mobileapps_cw3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapps_cw3.databinding.MainRegisterBinding
import com.example.mobileapps_cw3.structures.Profile
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var binding: MainRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainRegisterBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(R.layout.main_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnRegister.setOnClickListener {
            val editTextUsername = findViewById<EditText>(R.id.txtEmail)
            val username = editTextUsername.text.toString()
            val editTextPassword = findViewById<EditText>(R.id.txtPassword)
            val password = editTextPassword.text.toString()
            val editRenterPassword = findViewById<EditText>(R.id.txtRenterPassword)
            val renterPassword = editRenterPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty() && password == renterPassword) {
                firebase.collection("users").get().addOnSuccessListener { documents ->
                    val id = documents.size() + 1
                    val user = Profile(id, username, password, emptyList(), emptyList(),
                        "profile/default.jpg", emptyList(), emptyList(), false,
                        emptyList(), emptyList(), 0)

                    firebase.collection("users").document(id.toString()).set(user).addOnSuccessListener {
                        Toast.makeText(this, "Successfully Created Account!", Toast.LENGTH_SHORT).show()
                        editTextUsername.setText("")
                        editTextPassword.setText("")
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting document count: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Make sure you've entered data correctly", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

}
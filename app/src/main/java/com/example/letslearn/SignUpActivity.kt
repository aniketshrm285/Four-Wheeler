package com.example.letslearn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var phoneNumber : String
    private lateinit var auth: FirebaseAuth
    val db by lazy {
        Firebase.firestore
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        phoneNumber = intent.getStringExtra(PHONE_NUMBER).toString()
        phoneEt.setText(phoneNumber)
        phoneEt.isEnabled = false
        auth = Firebase.auth

        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            val name = nameEt.text.toString()
            val standard = classEt.text.toString()
            val school = schoolEt.text.toString()
            if(name.isNullOrEmpty() || standard.isNullOrEmpty() || school.isNullOrEmpty()){
                Toast.makeText(this, "Field(s) can't be empty!", Toast.LENGTH_SHORT).show()
            }
            else {
                val user = User(name,phoneNumber,standard,school,auth.uid!!)
                db.collection("users").document(auth.uid!!).set(user)
                    .addOnCompleteListener {
                        Toast.makeText(this, "SignUp Successful.", Toast.LENGTH_SHORT).show()

                        startActivity(
                            Intent(
                                this,MainActivity::class.java
                            )
                        )
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Check your internet connection and try again.", Toast.LENGTH_SHORT).show()
                        nextBtn.isEnabled = true
                    }

            }
        }

    }
}
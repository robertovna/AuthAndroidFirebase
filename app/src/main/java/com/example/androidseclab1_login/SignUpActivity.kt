package com.example.androidseclab1_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.androidseclab1_login.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewBind: ActivitySignUpBinding

    private lateinit var actionBar: ActionBar

    private lateinit var firebaseAuth: FirebaseAuth

    private var email =""
    private var pass =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBind = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(viewBind.root)

        actionBar = supportActionBar!!
        actionBar.title = "Sign Up"
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()

        viewBind.signUpBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        email = viewBind.emailInput.text.toString().trim()
        pass = viewBind.passInput.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewBind.emailInput.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(pass)) {
            viewBind.passInput.error = "Empty password"
        }
        else if (pass.length < 8)
        {
            viewBind.passInput.error = "Password must contains atleast 8 characters"
        }
        else
        {
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                var firebaseUser = firebaseAuth.currentUser
                var email = firebaseUser!!.email
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, UserProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                e ->
                Toast.makeText(this, "SignUpFailes ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
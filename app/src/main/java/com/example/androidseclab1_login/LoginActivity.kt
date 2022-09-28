package com.example.androidseclab1_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.androidseclab1_login.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var viewBind:ActivityLoginBinding

    private lateinit var actionBar: ActionBar

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var email = ""
    private var pass = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBind.root)

        actionBar = supportActionBar!!
        actionBar.title = "Login"

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // getting the value of gso inside the GoogleSigninClient
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        viewBind.signupBtn.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        viewBind.loginBtn.setOnClickListener {
            validateData()
        }

        viewBind.SigninGoogle.setOnClickListener{ view: View? ->
            signInGoogle()
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
        else
        {
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                SavedPreference.setAuthMethod(this, "Email/Password")
                Toast.makeText(this, "Logged as: ${email}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, UserProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        var firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null)
        {
            startActivity(Intent(this, UserProfileActivity::class.java))
            finish()
        }
    }

    private fun signInGoogle(){
        val signInIntent:Intent=mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1002)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1002){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                SavedPreference.setAuthMethod(this, "Google")
                SavedPreference.setEmail(this,account.email.toString())
                SavedPreference.setUsername(this,account.displayName.toString())
                val intent = Intent(this, UserProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
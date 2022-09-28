package com.example.androidseclab1_login

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
//import com.bumptech.glide.Glide
import com.example.androidseclab1_login.databinding.ActivityUserProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class UserProfileActivity : AppCompatActivity() {
    private lateinit var viewBind: ActivityUserProfileBinding

    private lateinit var actionBar: ActionBar

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var storageReference: StorageReference
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBind = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(viewBind.root)

        actionBar = supportActionBar!!
        actionBar.title = "Profile"

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        storageReference = FirebaseStorage.getInstance().getReference()

        viewBind.logoutBtn.setOnClickListener{
            if (SavedPreference.getAuthMethod(this) == "Google")
            {
                mGoogleSignInClient.signOut().addOnCompleteListener {
                        Toast.makeText(this, "Google sign out completed", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                            e ->
                        Toast.makeText(this, "Google sign out failure! ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            firebaseAuth.signOut()
            checkUser()
        }
        viewBind.deleteBtn.setOnClickListener{
            if (SavedPreference.getAuthMethod(this) == "Google")
            {
                mGoogleSignInClient.revokeAccess().addOnCompleteListener {
                    Toast.makeText(this, "Google delete account completed", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    }
                    .addOnFailureListener {
                            e ->
                        Toast.makeText(this, "Google delete account error! ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            }
            firebaseAuth.currentUser!!.delete()
            firebaseAuth.signOut()
            checkUser()
        }

        viewBind.changeEmailBtn.setOnClickListener{
            changeEmail()
            checkUser()
        }

        viewBind.changeNameBtn.setOnClickListener{
            changeName()
            checkUser()
        }

        viewBind.changeAvatarBtn.setOnClickListener{
            changeAvatar()
        }
        viewBind.secureBtn.setOnClickListener{
            startActivity(Intent(this, SecureActivity::class.java))
            finish()
        }
        //Fresco.initialize(applicationContext)
        setAvatar()
    }

    private fun changeEmail() {
        val user = firebaseAuth.currentUser
        val email = viewBind.newEmailInput.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewBind.newEmailInput.error = "Invalid email format"
        }
        else {
            user!!.updateEmail(email)
                .addOnCompleteListener {
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show()
                    viewBind.emailTextValue.text = email;
                    viewBind.newEmailInput.setText("")
                }
                .addOnFailureListener {
                        e ->
                    Toast.makeText(this, "Email updating failure! ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun changeName() {
        val user = firebaseAuth.currentUser
        val name = viewBind.newNameInput.text.toString().trim()

        if (name.length <= 0)
        {
            Toast.makeText(this, "Name empty", Toast.LENGTH_SHORT).show()
        }
        else
        {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener {
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                    viewBind.userName.text = name;
                    viewBind.newNameInput.setText("")
                }
                .addOnFailureListener {
                        e ->
                    Toast.makeText(this, "Email updating failure! ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setAvatar() {
        storageReference.listAll()?.addOnCompleteListener () {taskSnapshot ->
            var list = taskSnapshot.result!!
            if (list != null)
            {
                if ((list.items.contains(storageReference.child(firebaseAuth.currentUser!!.uid + "\\Profile.jpg"))))
                {
                    storageReference.child(firebaseAuth.currentUser!!.uid + "\\Profile.jpg").downloadUrl?.addOnCompleteListener () {taskSnapshot ->
                        var url = taskSnapshot.result
                        if (url != null)
                        {
                            Glide.with(this).load(url).into(viewBind.avatar);
                        }

                    }
                        ?.addOnFailureListener {
                            Toast.makeText(this, "YOU SHOULD SET AVATAR", Toast.LENGTH_SHORT).show()
                        }
                }

            }


        }
    }

    private fun changeAvatar() {
        val openGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(openGalery, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                val imageUri = data!!.data;
                viewBind.avatar.setImageURI(imageUri)
                var fileRef = storageReference.child(firebaseAuth.currentUser!!.uid + "\\Profile.jpg")
                fileRef.putFile(imageUri!!)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Avatar updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                            e ->
                        Toast.makeText(this, "Avatar updating failure! ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun checkUser() {
        var firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            //viewBind.avatar.setImageURI()
            viewBind.userName.text = if (firebaseUser.displayName != null &&  firebaseUser.displayName !="") firebaseUser.displayName else "No name"
            viewBind.emailTextValue.text = firebaseUser.email
            viewBind.authMethodTextValue.text = SavedPreference.getAuthMethod(this);
        }
        else
        {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
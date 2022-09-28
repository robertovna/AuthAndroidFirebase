package com.example.androidseclab1_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.example.androidseclab1_login.databinding.ActivitySecureBinding
import com.example.androidseclab1_login.databinding.ActivityUserProfileBinding

class SecureActivity : AppCompatActivity() {

    private lateinit var viewBind: ActivitySecureBinding

    private lateinit var actionBar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBind = ActivitySecureBinding.inflate(layoutInflater)
        setContentView(viewBind.root)

        actionBar = supportActionBar!!
        actionBar.title = "Secure page"


        viewBind.returnProfile.setOnClickListener{
            startActivity(Intent(this, UserProfileActivity::class.java))
            finish()
        }
    }
}
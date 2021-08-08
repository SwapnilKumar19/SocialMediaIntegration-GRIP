package com.androiddev.socialmediaintegration

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView;


class UserProfile : AppCompatActivity() {
    lateinit var circleImageView: CircleImageView
    lateinit var logout: Button
    lateinit var fb_name: TextView
    lateinit var fb_email: TextView
    var Pfb_id: String? = null
    var Pfb_name: String? = null
    var Pfb_profileUrl: String? = null
    var Pfb_email: String? = null
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)
        circleImageView = findViewById(R.id.fb_profile_pic)
        logout = findViewById(R.id.logout)
        fb_name = findViewById(R.id.name)
        fb_email = findViewById(R.id.email)
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        Pfb_name = sharedPreferences.getString(Fbname, "")
        Pfb_email = sharedPreferences.getString(Fbemail, "")
        Pfb_profileUrl = sharedPreferences.getString(FbprofileUrl, "")
        Pfb_id = sharedPreferences.getString(Fbid, "")
        Picasso.get().load(Pfb_profileUrl).placeholder(R.drawable.profile).into(circleImageView)
        fb_name.setText(Pfb_name)
        fb_email.setText(Pfb_email)
        logout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val builder_exitbutton: android.app.AlertDialog.Builder =
                    android.app.AlertDialog.Builder(this@UserProfile)
                builder_exitbutton.setTitle("Really Logout?")
                    .setMessage("Are you sure?")
                    .setPositiveButton("yes",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            val settings = getSharedPreferences(FB_LOGIN, 0)
                            val editor = settings.edit()
                            editor.remove("fb_logged")
                            editor.clear()
                            editor.commit()
                            val editor1 = sharedPreferences.edit()
                            editor1.clear()
                            editor1.commit()
                            Toast.makeText(
                                this@UserProfile,
                                "Facebook Logged out successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@UserProfile, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }).setNegativeButton("No", null)
                val alertexit: android.app.AlertDialog? = builder_exitbutton.create()
                alertexit!!.show()
            }
        })
    }

    var accessTokenTracker: AccessTokenTracker = object : AccessTokenTracker() {
        override fun onCurrentAccessTokenChanged(
            oldAccessToken: AccessToken,
            currentAccessToken: AccessToken
        ) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker.stopTracking()
    }

    companion object {
        const val SHARED_PREFS = "sahredprefs"
        const val FbprofileUrl = "PfbprofileUrl"
        const val Fbname = "Pfb_name"
        const val Fbemail = "Pfb_email"
        const val Fbid = "Pfb_id"
        const val FB_LOGIN = "fb_login"
    }
}
package com.androiddev.socialmediaintegration

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class gmail_userprofile : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener {
    lateinit  var circleImageView: CircleImageView
    lateinit var logout: Button
    lateinit var name: TextView
    lateinit var email: TextView
    lateinit var Pemail_name: String
    lateinit var Pemail_profileUrl: String
    lateinit var Pemail_email: String
    lateinit var googleApiClient: GoogleApiClient
    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gmail_userprofile)
        circleImageView = findViewById(R.id.gmail_profile_pic)
        logout = findViewById(R.id.gmail_logout)
        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        progressDialog = ProgressDialog(this@gmail_userprofile)
        progressDialog.setTitle("Loading data...")
        progressDialog.show()
        val settings_save = getSharedPreferences(GMAIL_LOGIN, 0)
        if (settings_save.getString("gmail_saved", "").toString() == "gmail_saved") {
            sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            name.setText(sharedPreferences.getString(Emailname, ""))
            email.setText(sharedPreferences.getString(Emailemail, ""))
            Picasso.get().load(sharedPreferences.getString(EmailprofileUrl, ""))
                .placeholder(R.drawable.profile).into(circleImageView)
            progressDialog!!.dismiss()
        }
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Pemail_name = account.displayName
            Pemail_email = account.email
            Pemail_profileUrl = if (account.photoUrl != null) {
                account.photoUrl.toString()
            } else {
                "null"
            }
            Picasso.get().load(Pemail_profileUrl).placeholder(R.drawable.profile)
                .into(circleImageView)
            name.setText(Pemail_name)
            email.setText(Pemail_email)
            progressDialog!!.dismiss()
            sendemailData()
        }
        logout.setOnClickListener(View.OnClickListener {
            val builder_exitbutton = AlertDialog.Builder(this@gmail_userprofile)
            builder_exitbutton.setTitle("Really Logout?")
                .setMessage("Are you sure?")
                .setPositiveButton(
                    "yes"
                ) { dialogInterface, i ->
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback { status ->
                        if (status.isSuccess) {
                            val settings_save =
                                getSharedPreferences(GMAIL_LOGIN, 0)
                            val editor_save = settings_save.edit()
                            editor_save.remove("gmail_saved")
                            editor_save.clear()
                            editor_save.commit()
                            val settings =
                                getSharedPreferences(GMAIL_LOGIN, 0)
                            val editor = settings.edit()
                            editor.remove("gmail_logged")
                            editor.clear()
                            editor.commit()
                            val editor1 = sharedPreferences!!.edit()
                            editor1.clear()
                            editor1.commit()
                            Toast.makeText(
                                this@gmail_userprofile,
                                "Gmail Logged out successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(
                                this@gmail_userprofile,
                                MainActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@gmail_userprofile,
                                "Gmail Log out failed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.setNegativeButton("No", null)
            val alertexit = builder_exitbutton.create()
            alertexit.show()
        })
    }

    private fun sendemailData() {
        val settings = getSharedPreferences(GMAIL_SAVE, 0)
        val editor_save = settings.edit()
        editor_save.putString("gmail_saved", "gmail_saved")
        editor_save.commit()
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Emailname, Pemail_name)
        editor.putString(Emailemail, Pemail_email)
        editor.putString(EmailprofileUrl, Pemail_profileUrl)
        editor.apply()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        const val SHARED_PREFS = "sahredprefs"
        const val EmailprofileUrl = "PemailprofileUrl"
        const val Emailname = "Pemail_name"
        const val Emailemail = "Pemail_email"
        const val GMAIL_LOGIN = "gmail_login"
        const val GMAIL_SAVE = "gmail_save"
    }
}
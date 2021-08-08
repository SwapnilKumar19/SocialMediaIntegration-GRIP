package com.androiddev.socialmediaintegration


import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import org.json.JSONException
import java.util.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    lateinit var fb_loginButton: Button
    lateinit var gmail_loginButton: Button
    lateinit var callbackManager: CallbackManager
    lateinit var name: String
    lateinit var id: String
    lateinit var profilePicUrl: String
    lateinit var email: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var googleApiClient: GoogleApiClient
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fb_settings = getSharedPreferences(FB_LOGIN, 0)
        val email_settings = getSharedPreferences(GMAIL_LOGIN, 0)
        if (fb_settings.getString("fb_logged", "").toString() == "fb_logged") {
            startActivity(Intent(this@MainActivity, UserProfile::class.java))
            finish()
        } else if (email_settings.getString("gmail_logged", "").toString() == "gmail_logged") {
            startActivity(Intent(this@MainActivity, gmail_userprofile::class.java))
            finish()
        }
        val text_links = findViewById<TextView>(R.id.link_texts)
        val text =
            "By clicking on login, you are accepting our privacy policy \nand terms & conditions."
        val ss = SpannableString(text)
        val fcs = ForegroundColorSpan(Color.rgb(66, 103, 178))
        ss.setSpan(fcs, 44, 58, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(fcs, 64, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/SwapnilKumar19")
                    )
                )
            }
        }
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/SwapnilKumar19")
                    )
                )
            }
        }
        ss.setSpan(clickableSpan, 44, 58, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(clickableSpan1, 64, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_links.text = ss
        text_links.movementMethod = LinkMovementMethod.getInstance()
        fb_loginButton = findViewById(R.id.fb_loginbutton)
        callbackManager = CallbackManager.Factory.create()
        fb_loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this@MainActivity, Arrays.asList("email"))
                LoginManager.getInstance()
                    .registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                        override fun onSuccess(loginResult: LoginResult?) {
                            fb_loginButton.setVisibility(View.INVISIBLE)
                            gmail_loginButton.setVisibility(View.INVISIBLE)
                            progressDialog = ProgressDialog(this@MainActivity)
                            progressDialog!!.setTitle("Loading data...")
                            progressDialog!!.show()
                            val graphRequest = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken()
                            ) { `object`, response ->
                                Log.d("Demo", `object`.toString())
                                try {
                                    name = `object`.getString("name")
                                    id = `object`.getString("id")
                                    profilePicUrl =
                                        `object`.getJSONObject("picture").getJSONObject("data")
                                            .getString("url")
                                    email = if (`object`.has("email")) {
                                        `object`.getString("email")
                                    } else {
                                        " "
                                    }
                                    val settings =
                                        getSharedPreferences(FB_LOGIN, 0)
                                    val editor = settings.edit()
                                    editor.putString("fb_logged", "fb_logged")
                                    editor.apply()
                                    sendfbData()
                                    progressDialog!!.dismiss()
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Login successfully.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            UserProfile::class.java
                                        )
                                    )
                                    finish()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            val bundle = Bundle()
                            bundle.putString(
                                "fields",
                                "picture.type(large),gender, name, id, birthday, friends, email"
                            )
                            graphRequest.parameters = bundle
                            graphRequest.executeAsync()
                        }

                        override fun onCancel() {
                            fb_loginButton.setVisibility(View.VISIBLE)
                            gmail_loginButton.setVisibility(View.VISIBLE)
                            Toast.makeText(
                                this@MainActivity,
                                "Login cancelled.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onError(error: FacebookException) {
                            fb_loginButton.setVisibility(View.VISIBLE)
                            gmail_loginButton.setVisibility(View.VISIBLE)
                            progressDialog!!.dismiss()
                            Toast.makeText(this@MainActivity, "Login error.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
        })
        gmail_loginButton = findViewById(R.id.gmaillogin_button)
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
        gmail_loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                startActivityForResult(intent, SignIn_value)
            }
        })
    }

    private fun sendfbData() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Fbname, name)
        editor.putString(Fbemail, email)
        editor.putString(FbprofileUrl, profilePicUrl)
        editor.putString(Fbid, id)
        editor.apply()
    }

    override fun onConnectionFailed(@NonNull connectionResult: ConnectionResult) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SignIn_value) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSigninResult(task)
        }
    }

    private fun handleSigninResult(result: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = result.getResult(ApiException::class.java)
            fb_loginButton!!.setVisibility(View.INVISIBLE)
            gmail_loginButton!!.setVisibility(View.INVISIBLE)
            val settings = getSharedPreferences(GMAIL_LOGIN, 0)
            val editor = settings.edit()
            editor.putString("gmail_logged", "gmail_logged")
            editor.commit()
            Toast.makeText(this@MainActivity, "Login successfully.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity, gmail_userprofile::class.java))
            finish()
        } catch (e: ApiException) {
            fb_loginButton.setVisibility(View.VISIBLE)
            gmail_loginButton.setVisibility(View.VISIBLE)
            Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
            Log.v("Error", "signInResult:failed code = " + e.statusCode)
        }
    }

    companion object {
        const val SHARED_PREFS = "sahredprefs"
        const val FbprofileUrl = "PfbprofileUrl"
        const val Fbname = "Pfb_name"
        const val Fbemail = "Pfb_email"
        const val Fbid = "Pfb_id"
        const val FB_LOGIN = "fb_login"
        const val GMAIL_LOGIN = "gmail_login"
        const val SignIn_value = 1
    }
}
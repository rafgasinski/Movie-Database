package com.moviedb.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.moviedb.R
import com.moviedb.databinding.ActivityLoginBinding
import com.moviedb.utils.NetworkConnection
import com.moviedb.view.login.LoginPagerAdapter

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.accent)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("@string/login"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("@string/signup"))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = LoginPagerAdapter(this, binding.tabLayout.tabCount)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position){
                0 -> tab.text = "Login"
                1 -> tab.text = "Signup"
            }
        }.attach()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()

        binding.loginGoogle.setOnClickListener {
            signIn()
        }

        var snackBar = Snackbar.make(binding.constraintLayout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
            .setAction("Wifi Settings") {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.accent));
        val sbView: View = snackBar.view
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar))
        sbView.elevation = 10f
        val tv = sbView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        tv.setTextColor(ContextCompat.getColor(this, R.color.white))

        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { connected ->
            if (!connected) {
                snackBar.show()
            } else {
                snackBar.dismiss()
            }
        })

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent((data))
            val exception = task.exception
            if(task.isSuccessful){
                try {
                    val account = task.getResult((ApiException::class.java))!!
                    Log.d("Firebase", "firebaseAuthWithGoogle" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                }
                catch (e: ApiException) {
                    Log.w("Firebase", "Google sign in failed", e)
                }
            }
            else {
                Log.w("Firebase", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Firebase", "signInWithCredential:success")
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    Log.w("Firebase", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }
}
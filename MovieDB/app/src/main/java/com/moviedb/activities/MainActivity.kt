package com.moviedb.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.moviedb.R
import com.moviedb.databinding.ActivityMainBinding
import com.moviedb.utils.NetworkConnection


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.accent)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Check network connection
         * */
        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { connected ->

            if(connected) {
                /**
                 * If network is online go to login or dashboard activity
                 * based on if Firebase user isn't null
                 * */
                binding.logo.setImageResource(R.drawable.icon_film_reel)
                binding.networkInfo.alpha = 0f

                mAuth = FirebaseAuth.getInstance()
                val user = mAuth.currentUser

                binding.logo.alpha = 0f
                binding.logo.animate().setDuration(1000).alpha(1f).withEndAction{

                    if(user != null){
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                    else {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                }
            } else {
                /**
                 * Show information about no internet connection
                 * */
                binding.logo.setImageResource(R.drawable.icon_no_network)
                binding.logo.alpha = 0f
                binding.logo.animate().setDuration(1000).alpha(1f)
                binding.networkInfo.alpha = 0f
                binding.networkInfo.animate().setDuration(1000).alpha(1f)
            }

        })


    }
}
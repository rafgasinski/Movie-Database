package com.moviedb.activities

import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.moviedb.R
import com.moviedb.databinding.ActivityDashboardBinding
import com.moviedb.utils.Constants
import com.moviedb.utils.NetworkConnection


class DashboardActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityDashboardBinding
    lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.background)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()

        /**
         * Navigation setup
         * */
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { id ->
            binding.bottomNavigation.onNavDestinationSelected(id, navController)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.setItemSelected(destination.id, true)
            when(destination.id){
                R.id.homeFragment -> {
                    binding.toolbarTitle.gravity = Gravity.CENTER
                    binding.toolbarTitle.text = destination.label
                    binding.toolbar.contentInsetStartWithNavigation = 0
                }
                else -> {
                    this.getSharedPreferences(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY, Constants.PRIVATE_MODE)
                        ?.edit()?.putInt(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY, 0)?.apply()

                    binding.toolbarTitle.gravity = Gravity.CENTER_VERTICAL
                    binding.toolbarTitle.text = destination.label
                    binding.toolbar.contentInsetStartWithNavigation = 220
                }
            }
        }

        NavigationUI.setupActionBarWithNavController(this, navController)

        /**
         * Prevent user from browsing app if no internet connection,
         * switch to main activity
         * */
        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { connected ->
            if(!connected){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        })
    }

    /**
     * Remove selected SharedPreferences
     * on users logout
     * */
    fun removeSharedPreferences() {

        this.getSharedPreferences(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_LAST_MOVIE_DETAIL_ID_KEY)?.clear()?.apply()

        this.getSharedPreferences(Constants.SH_DISCOVER_SELECTED_TAB_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_DISCOVER_SELECTED_TAB_KEY)?.clear()?.apply()

        this.getSharedPreferences(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY)?.clear()?.apply()

        this.getSharedPreferences(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_SEARCH_RECOMMENDED_POSITION_KEY)?.clear()?.apply()

        this.getSharedPreferences(Constants.SH_SEARCH_POSITION_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_SEARCH_POSITION_KEY)?.clear()?.apply()

        this.getSharedPreferences(Constants.SH_LAST_SEARCHED_QUERY_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_LAST_SEARCHED_QUERY_KEY)?.clear()?.apply()

        this.getSharedPreferences(Constants.SH_PROFILE_SELECTED_TAB_KEY, Constants.PRIVATE_MODE)
            .edit()?.remove(Constants.SH_PROFILE_SELECTED_TAB_KEY)?.clear()?.apply()

    }


    /**
     * Remove movie data cache
     * */
    override fun onDestroy() {
        super.onDestroy()
        this.cacheDir.deleteRecursively()

        val sharedPrefDiscoverPosition: SharedPreferences? = this?.getSharedPreferences(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY, Constants.PRIVATE_MODE)
        sharedPrefDiscoverPosition?.edit()?.remove(Constants.SH_DISCOVER_RECYCLER_POSITION_KEY)?.clear()?.apply()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Navigation and ChipNavigationBar integrity
     * */
    private fun ChipNavigationBar.onNavDestinationSelected(
            itemId: Int,
            navController: NavController
    ): Boolean {
        val builder = NavOptions.Builder()
            .setLaunchSingleTop(true)
        if (navController.currentDestination!!.parent!!.findNode(itemId) is ActivityNavigator.Destination) {
            builder.setEnterAnim(R.animator.nav_default_enter_anim)
                .setExitAnim(R.animator.nav_default_exit_anim)
                .setPopEnterAnim(R.animator.nav_default_pop_enter_anim)
                .setPopExitAnim(R.animator.nav_default_pop_exit_anim)
        } else {
            builder.setEnterAnim(R.animator.nav_default_enter_anim)
                .setExitAnim(R.animator.nav_default_exit_anim)
                .setPopEnterAnim(R.animator.nav_default_pop_enter_anim)
                .setPopExitAnim(R.animator.nav_default_pop_exit_anim)
        }
        builder.setPopUpTo(itemId, true)
        val options = builder.build()
        return try {
            navController.navigate(itemId, null, options)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }


}


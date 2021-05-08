package com.moviedb.view.login

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoginPagerAdapter(fragmentActivity: FragmentActivity, var totalTabs : Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> LoginFragment()
            1 -> RegisterFragment()
            else -> LoginFragment()
        }
    }

}
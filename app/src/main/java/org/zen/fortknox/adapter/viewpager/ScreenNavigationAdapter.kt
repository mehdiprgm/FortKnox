package org.zen.fortknox.adapter.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.zen.fortknox.fragment.register.RegisterProfileBasicInformationFragment
import org.zen.fortknox.fragment.register.RegisterProfileCompleteSetupFragment
import org.zen.fortknox.fragment.screens.HomeFragment
import org.zen.fortknox.fragment.screens.MonitorFragment
import org.zen.fortknox.fragment.screens.ProfileFragment

class ScreenNavigationAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> MonitorFragment.newInstance()
            2 -> ProfileFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    fun getFragment(activity: AppCompatActivity, position: Int): Fragment? {
        return activity.supportFragmentManager.findFragmentByTag("f$position")
    }
}
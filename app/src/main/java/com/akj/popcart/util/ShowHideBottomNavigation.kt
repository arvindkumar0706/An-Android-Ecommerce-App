package com.akj.popcart.util

import android.view.View
import androidx.fragment.app.Fragment
import com.akj.popcart.R
import com.akj.popcart.activities.shoppingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigation(){
    val bottonNavigation=(activity as shoppingActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation)
    bottonNavigation.visibility= View.GONE
}

fun Fragment.showBottomNavigation(){
    val bottonNavigation=(activity as shoppingActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation)
    bottonNavigation.visibility= View.VISIBLE
}
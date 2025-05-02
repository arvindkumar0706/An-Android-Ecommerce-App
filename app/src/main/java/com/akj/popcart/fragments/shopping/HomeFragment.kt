package com.akj.popcart.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.akj.popcart.R
import com.akj.popcart.adapters.HomeViewAdapter
import com.akj.popcart.databinding.FragmentHomeBinding
import com.akj.popcart.fragments.categories.ComputerFragment
import com.akj.popcart.fragments.categories.ElectronicFragment
import com.akj.popcart.fragments.categories.FashionFragment
import com.akj.popcart.fragments.categories.FootwearFragment
import com.akj.popcart.fragments.categories.FurnitureFragment
import com.akj.popcart.fragments.categories.MainCategoryFragment
import com.akj.popcart.fragments.categories.MobilesFragment
import com.akj.popcart.fragments.categories.SportsFragment
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment :Fragment(R.layout.fragment_home) {
    private lateinit var binding:FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoriesFragments= arrayListOf<Fragment>(
            MainCategoryFragment(),
            FashionFragment(),
            MobilesFragment(),
            ElectronicFragment(),
            FootwearFragment(),
            ComputerFragment(),
            FurnitureFragment(),
            SportsFragment()

        )

        binding.viewpagerHome.isUserInputEnabled=false

        val viewPagerAdapter=HomeViewAdapter(categoriesFragments,childFragmentManager,lifecycle)
        binding.viewpagerHome.adapter=viewPagerAdapter
        TabLayoutMediator(binding.tabLayout,binding.viewpagerHome){tab,position->
            when(position){
                0 -> tab.text="Home"
                1 -> tab.text="Fashion"
                2 -> tab.text="Mobiles"
                3 -> tab.text="Electronic"
                4 -> tab.text="Footwears"
                5 -> tab.text="Computer"
                6 -> tab.text="Furniture"
                7 -> tab.text="Sports"
            }
        }.attach()

    }


}
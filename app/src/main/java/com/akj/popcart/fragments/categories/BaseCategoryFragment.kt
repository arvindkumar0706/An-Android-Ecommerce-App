package com.akj.popcart.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.R
import com.akj.popcart.adapters.BestProdAdapter
import com.akj.popcart.databinding.FragmentBaseCategoryBinding
import com.akj.popcart.util.showBottomNavigation

open class BaseCategoryFragment : Fragment(R.layout.fragment_base_category){
    private lateinit var binding:FragmentBaseCategoryBinding
    protected val offerAdapter:BestProdAdapter by lazy { BestProdAdapter() }
    protected val bestProdAdapter:BestProdAdapter by lazy { BestProdAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBaseCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOffer()
        setupbestProd()

        bestProdAdapter.onClick={
            val b =Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment,b)
        }

        offerAdapter.onClick={
            val b =Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment,b)
        }

        binding.Offer.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)&&dx!=0){
                    onOfferPagingRequest()
                }

            }
        })

        binding.nestedScrollBase.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{ v, _, scrollY, _, _->
            if(v.getChildAt(0).bottom<=v.height+scrollY){
                onBestProductsPagingRequest()
            }
        })



    }

    fun showOfferLoading(){
         binding.offerProgressBar.visibility=View.VISIBLE
    }

    fun hideOfferLoading(){
        binding.offerProgressBar.visibility=View.GONE
    }

    fun showBestProdLoading(){
        binding.bestProdProgressBar.visibility=View.VISIBLE
    }

    fun hideBestProdLoading(){
        binding.bestProdProgressBar.visibility=View.GONE
    }

    open fun onOfferPagingRequest(){

    }

    open fun onBestProductsPagingRequest(){

    }

    private fun setupbestProd() {

        binding.BestProd.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProdAdapter
        }
    }

    private fun setupOffer() {

        binding.Offer.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            adapter=offerAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigation()
    }


}
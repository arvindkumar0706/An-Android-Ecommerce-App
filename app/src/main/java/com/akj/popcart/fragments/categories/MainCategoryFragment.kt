package com.akj.popcart.fragments.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akj.popcart.R
import com.akj.popcart.adapters.BestDealAdapter
import com.akj.popcart.adapters.BestProdAdapter
import com.akj.popcart.adapters.SpecialProdAdapter
import com.akj.popcart.databinding.FragmentMainCategoryBinding
import com.akj.popcart.util.Resource
import com.akj.popcart.util.showBottomNavigation
import com.akj.popcart.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val TAG="MainCategoryFragment"

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var binding:FragmentMainCategoryBinding
    private lateinit var specialProdAdapter:SpecialProdAdapter
    private lateinit var bestDealAdapter: BestDealAdapter
    private lateinit var bestProdAdapter: BestProdAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMainCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpecialProducts()
        setupBestDeals()
        setupBestProd()

        specialProdAdapter.onClick={
            val b =Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment,b)
        }

        bestDealAdapter.onClick={
            val b =Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment,b)
        }

        bestProdAdapter.onClick={
            val b =Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment,b)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.specialprod.collectLatest {
                    when(it){
                        is Resource.Loading->{
                            showLoading()
                        }
                        is Resource.Success->{
                            specialProdAdapter.differ.submitList(it.data)
                            hideLoading()
                        }
                        is Resource.Error->{
                            hideLoading()
                            Log.e(TAG,it.message.toString())
                            Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                        }
                        else ->Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestDealProd.collectLatest {
                    when(it){
                        is Resource.Loading->{
                            showLoading()
                        }
                        is Resource.Success->{
                            bestDealAdapter.differ.submitList(it.data)
                            hideLoading()
                        }
                        is Resource.Error->{
                            hideLoading()
                            Log.e(TAG,it.message.toString())
                            Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                        }
                        else ->Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestProd.collectLatest {
                    when(it){
                        is Resource.Loading->{
                            binding.bestProdProgress.visibility=View.VISIBLE
                        }
                        is Resource.Success->{
                            bestProdAdapter.differ.submitList(it.data)
                            binding.bestProdProgress.visibility=View.GONE
                        }
                        is Resource.Error->{
                            binding.bestProdProgress.visibility=View.GONE
                            Log.e(TAG,it.message.toString())
                            Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                        }
                        else ->Unit
                    }
                }
            }
        }

        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{v,_,scrollY,_,_->
            if(v.getChildAt(0).bottom<=v.height+scrollY){
                viewModel.fetchBestProd()
            }
        })

    }

    private fun setupBestDeals() {
        bestDealAdapter= BestDealAdapter()
        binding.BestDeals.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            adapter=bestDealAdapter
        }
    }

    private fun setupBestProd() {
        bestProdAdapter= BestProdAdapter()
        binding.BestProd.apply {
            layoutManager=GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false)
            adapter=bestProdAdapter
        }
    }

    private fun hideLoading() {
        binding.mainCategoryProgress.visibility=View.GONE
    }

    private fun showLoading() {
        binding.mainCategoryProgress.visibility=View.VISIBLE
    }

    private fun setupSpecialProducts(){
        specialProdAdapter=SpecialProdAdapter()
        binding.specialProd.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            adapter=specialProdAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigation()
    }

}
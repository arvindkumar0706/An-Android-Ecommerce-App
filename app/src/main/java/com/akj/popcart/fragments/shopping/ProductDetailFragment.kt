package com.akj.popcart.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.akj.popcart.adapters.ColorsAdapter
import com.akj.popcart.adapters.SizesAdapter
import com.akj.popcart.adapters.ViewPager2Images
import com.akj.popcart.data.CartProd
import com.akj.popcart.databinding.FragmentProductDetailBinding
import com.akj.popcart.util.Resource
import com.akj.popcart.util.hideBottomNavigation
import com.akj.popcart.viewmodel.DetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {
    private val args by navArgs<ProductDetailFragmentArgs>()
    private lateinit var binding: FragmentProductDetailBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }
    private var selectedColor: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigation()
        binding = FragmentProductDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setupSizesRv()
        setupColorsRv()
        setupViewPager()

        binding.closeImage.setOnClickListener {
            findNavController().navigateUp()
        }

        sizesAdapter.onItemClick = {
            selectedSize = it
        }

        colorsAdapter.onItemClick = {
            selectedColor = it
        }

        binding.AddToCartBtn.setOnClickListener {
            viewModel.addUpdateProdinCart(CartProd(product, 1, selectedColor, selectedSize))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addtCart.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.AddToCartBtn.startAnimation()
                        }
                        is Resource.Success -> {
                            binding.AddToCartBtn.revertAnimation()
                            binding.AddToCartBtn.visibility=View.GONE
                            binding.AddToCartTv.visibility=View.VISIBLE
                            Toast.makeText(requireContext(), "Product added to Cart", Toast.LENGTH_LONG).show()
                        }
                        is Resource.Error -> {
                            binding.AddToCartBtn.revertAnimation()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }

        binding.apply {
            ProdName.text = product.name
            ProdPrice.text = "₹ ${product.price}"
            ProdDescrip.text = product.description

            if (product.colors.isNullOrEmpty())
                prodColor.visibility = View.INVISIBLE

            if (product.sizes.isNullOrEmpty())
                prodSize.visibility = View.INVISIBLE
        }

        viewPagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorsAdapter.differ.submitList(it) }
        product.sizes?.let { sizesAdapter.differ.submitList(it) }
    }

    private fun setupViewPager() {
        binding.viewPagerProductImages.adapter = viewPagerAdapter
    }

    private fun setupColorsRv() {
        binding.Colors.apply {
            adapter = colorsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupSizesRv() {
        binding.Sizes.apply {
            adapter = sizesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }
}

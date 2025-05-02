package com.akj.popcart.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.R
import com.akj.popcart.adapters.cartProdAdapter
import com.akj.popcart.databinding.FragmentCartBinding
import com.akj.popcart.firebase.FirebaseCommon
import com.akj.popcart.util.Resource
import com.akj.popcart.util.VerticalItemDecoration
import com.akj.popcart.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartFragment :Fragment(R.layout.fragment_cart) {
    private lateinit var binding:FragmentCartBinding
    private val cartAdapter by lazy { cartProdAdapter() }
    private val viewModel by activityViewModels<CartViewModel> ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRv()

        var totalPrice = 0f

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.productPrice.collectLatest {price->
                    price?.let {
                        totalPrice = it
                        binding.tvTotalPrice.text="₹ ${price}"
                    }

                }
            }
        }

        cartAdapter.onProdClick={
            val b = Bundle().apply { putParcelable("product",it.product)}
            findNavController().navigate(R.id.action_cartFragment_to_productDetailFragment,b)
        }

        cartAdapter.onPlusClick={
            viewModel.changeQuantity(it,FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartAdapter.onMinusClick ={
            viewModel.changeQuantity(it,FirebaseCommon.QuantityChanging.DECREASE)
        }

        binding.buttonCheckout.setOnClickListener {
            val action = CartFragmentDirections.actionCartFragmentToBillingFragment(totalPrice,cartAdapter.differ.currentList.toTypedArray(),true)
            findNavController().navigate(action)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.deleteDialog.collectLatest {
                    val alertDialog=AlertDialog.Builder(requireContext()).apply {
                        setTitle("Remove Item from cart")
                        setMessage("Do you want to remove this product from your Cart ? ")
                        setNegativeButton("Cancel"){dialog ,_->
                            dialog.dismiss()

                        }
                        setPositiveButton("Yes"){dialog,_ ->
                            viewModel.deleteCartProd(it)
                            dialog.dismiss()
                        }
                    }
                    alertDialog.create()
                    alertDialog.show()
                }
            }
        }



        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.cartProds.collectLatest {
                    when(it){
                        is Resource.Loading ->{
                            binding.progressbarCart.visibility=View.VISIBLE
                        }

                        is Resource.Success ->{
                            binding.progressbarCart.visibility=View.INVISIBLE
                            if(it.data!!.isEmpty()){
                                showEmptyCart()
                                hideOtherViews()
                            }else{
                                hideEmptyCart()
                                showOtherViews()
                                cartAdapter.differ.submitList(it.data)
                            }

                        }
                        is Resource.Error ->{
                            binding.progressbarCart.visibility=View.VISIBLE
                            Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }


    }

    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility=View.VISIBLE
            totalBoxContainer.visibility=View.VISIBLE
            buttonCheckout.visibility=View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility=View.GONE
            totalBoxContainer.visibility=View.GONE
            buttonCheckout.visibility=View.GONE
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility=View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility=View.VISIBLE
        }
    }

    private fun setupCartRv() {
        binding.rvCart.apply {
            layoutManager=LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            adapter=cartAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }

}
package com.akj.popcart.fragments.settings

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.adapters.AllOrdersadapter
import com.akj.popcart.databinding.FragmentOrdersBinding
import com.akj.popcart.util.Resource
import com.akj.popcart.viewmodel.AllOrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllOrdersFragment : Fragment() {

    private lateinit var binding:FragmentOrdersBinding
    val viewModel by viewModels<AllOrdersViewModel>()
    val ordersAdapter by lazy{AllOrdersadapter()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrdersBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrders()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.allOrders.collectLatest {
                    when(it){
                        is Resource.Loading ->{
                           binding.progressbarAllOrders.visibility=View.VISIBLE
                        }

                        is Resource.Success ->{
                            binding.progressbarAllOrders.visibility=View.GONE
                            ordersAdapter.differ.submitList(it.data)
                            if (it.data.isNullOrEmpty()){
                                binding.tvEmptyOrders.visibility=View.VISIBLE
                            }
                        }
                        is Resource.Error ->{
                            binding.progressbarAllOrders.visibility=View.GONE
                            Toast.makeText(requireContext(),it.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
        ordersAdapter.onClick= {
            val action=AllOrdersFragmentDirections.actionOrdersFragmentToOrderdetailFragment(it)
            findNavController().navigate(action)
        }

    }

    private fun setupOrders() {
        binding.rvAllOrders.apply {
            adapter=ordersAdapter
            layoutManager=LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        }
    }

}
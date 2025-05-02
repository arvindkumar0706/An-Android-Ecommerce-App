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
import com.akj.popcart.data.Address
import com.akj.popcart.databinding.FragmentAddressBinding
import com.akj.popcart.util.Resource
import com.akj.popcart.viewmodel.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddressFragment : Fragment() {

    private lateinit var binding:FragmentAddressBinding
    val viewModel by viewModels<AddressViewModel>()
    val args by navArgs<AddressFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentAddressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val address = args.address
        if (address == null){
            binding.buttonDelelte.visibility=View.GONE
        }else{
            binding.apply {
                edAddressTitle.setText(address.addressTitle)
                edFullName.setText(address.fName)
                edStreet.setText(address.street)
                edPhone.setText(address.phone)
                edCity.setText(address.city)
                edState.setText(address.state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.addNewAddress.collectLatest {
                    when(it){
                        is Resource.Loading ->{
                            binding.progressbarAddress.visibility=View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressbarAddress.visibility=View.INVISIBLE
                            findNavController().navigateUp()
                        }
                        is Resource.Error ->{
                            Toast.makeText(requireContext(),it.message, Toast.LENGTH_SHORT).show()

                        }
                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.error.collectLatest {
                    Toast.makeText(requireContext(),it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.apply {
            buttonSave.setOnClickListener {
                val addressTitle=edAddressTitle.text.toString()
                val fullname=edFullName.text.toString()
                val street=edStreet.text.toString()
                val phone=edPhone.text.toString()
                val city=edCity.text.toString()
                val state=edState.text.toString()
                val address=Address(addressTitle,fullname,street,phone,city,state)

                viewModel.addAddress(address)
            }
        }
    }


}
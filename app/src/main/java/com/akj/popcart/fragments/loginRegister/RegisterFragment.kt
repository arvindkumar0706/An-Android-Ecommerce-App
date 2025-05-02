package com.akj.popcart.fragments.loginRegister

import android.os.Bundle
import android.util.Log
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
import com.akj.popcart.R
import com.akj.popcart.data.User
import com.akj.popcart.databinding.FragmentRegisterBinding
import com.akj.popcart.util.RegisterValidation
import com.akj.popcart.util.Resource
import com.akj.popcart.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.apply {
            RegisterButton.setOnClickListener {
                val firstName = fname.text.toString().trim()
                val lastName = lname.text.toString().trim()
                val email = Email.text.toString().trim()
                val password = password.text.toString()

                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val user = User(firstName, lastName, email)
                viewModel.createAccwEmailandPass(user, password)
            }
        }

        // Collect the register state and update UI accordingly
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            binding.RegisterButton.startAnimation()
                            Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_LONG).show()
                            // Navigate to another fragment or activity
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            binding.RegisterButton.revertAnimation()
                        }
                        is Resource.Error -> {

                            Toast.makeText(requireContext(), resource.message ?: "Registration failed", Toast.LENGTH_LONG).show()
                            Log.e(TAG, resource.message.toString())
                        }
                        else -> Unit
                    }
                }
            }
        }

        // Collect the validation state and update UI accordingly
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { validation ->
                    withContext(Dispatchers.Main) {
                        if (validation.email is RegisterValidation.Failed) {
                            binding.Email.apply {
                                requestFocus()
                                error = validation.email.message
                            }
                        }
                        if (validation.password is RegisterValidation.Failed) {
                            binding.password.apply {
                                requestFocus()
                                error = validation.password.message
                            }
                        }
                    }
                }
            }
        }
    }
}

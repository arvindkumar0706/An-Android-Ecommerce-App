package com.akj.popcart.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akj.popcart.data.Category
import com.akj.popcart.util.Resource
import com.akj.popcart.viewmodel.CategoryViewModel
import com.akj.popcart.viewmodel.Factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class FashionFragment : BaseCategoryFragment(){
    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactory( firestore, Category.Fashion)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offerproducts.collectLatest {

                    when(it){
                        is Resource.Loading->{
                            showOfferLoading()
                        }
                        is Resource.Success->{
                            offerAdapter.differ.submitList(it.data)
                            hideOfferLoading()
                        }
                        is Resource.Error->{
                            Snackbar.make(requireView(),it.message.toString(), Snackbar.LENGTH_LONG).show()
                            hideOfferLoading()
                        }
                        else -> Unit
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestproducts.collectLatest {

                    when(it){
                        is Resource.Loading->{
                            showBestProdLoading()
                        }
                        is Resource.Success->{
                            bestProdAdapter.differ.submitList(it.data)
                            hideBestProdLoading()
                        }
                        is Resource.Error->{
                            Snackbar.make(requireView(),it.message.toString(), Snackbar.LENGTH_LONG).show()
                            hideBestProdLoading()
                        }
                        else -> Unit
                    }

                }
            }
        }



    }
}
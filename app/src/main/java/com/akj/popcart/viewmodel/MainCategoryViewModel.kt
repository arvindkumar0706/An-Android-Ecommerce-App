package com.akj.popcart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akj.popcart.data.Products
import com.akj.popcart.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) :ViewModel(){
    private val _specialprod=MutableStateFlow<Resource<List<Products>>>(Resource.Unspecified())
       val specialprod:StateFlow<Resource<List<Products>>> =_specialprod

    private val _bestDealProd=MutableStateFlow<Resource<List<Products>>>(Resource.Unspecified())
    val bestDealProd:StateFlow<Resource<List<Products>>> =_bestDealProd

    private val _bestProd=MutableStateFlow<Resource<List<Products>>>(Resource.Unspecified())
    val bestProd:StateFlow<Resource<List<Products>>> =_bestProd

    private val pagingInfo =PagingInfo()

    init {
        fetchSpecialProd()
        fetchBestDeals()
        fetchBestProd()
    }


    fun fetchSpecialProd(){
        viewModelScope.launch {
            _specialprod.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereEqualTo("category","Special Products").get()
            .addOnSuccessListener {result->
                val specialProdList=result.toObjects(Products::class.java)
                viewModelScope.launch {
                    _specialprod.emit(Resource.Success(specialProdList))
                }
            }
            .addOnFailureListener{
                viewModelScope.launch {
                    _specialprod.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestDeals(){
        viewModelScope.launch {
            _bestDealProd.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereGreaterThan("offerPercentage",0).get()
            .addOnSuccessListener {result->
                val bestDealsProd=result.toObjects(Products::class.java)
                viewModelScope.launch {
                    _bestDealProd.emit(Resource.Success(bestDealsProd))
                }

            }.addOnFailureListener{
                viewModelScope.launch {
                    _bestDealProd.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestProd(){
        if (!pagingInfo.isPagingEnd){
        viewModelScope.launch {
            _bestProd.emit(Resource.Loading())
        }
        firestore.collection("Products").get()

            .addOnSuccessListener {result->
                val bestProd=result.toObjects(Products::class.java)
                viewModelScope.launch {
                    _bestProd.emit(Resource.Success(bestProd))
                }
                pagingInfo.bestprodpage++
            }.addOnFailureListener{
                viewModelScope.launch {
                    _bestProd.emit(Resource.Error(it.message.toString()))
                }
            }
    }
    }

internal data class PagingInfo(
    var bestprodpage: Long=1,
    var oldBestprod:List<Products> = emptyList(),
    var isPagingEnd:Boolean=false
)

}
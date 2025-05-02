package com.akj.popcart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akj.popcart.data.CartProd
import com.akj.popcart.firebase.FirebaseCommon
import com.akj.popcart.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) :ViewModel(){

    private val _addtCart=MutableStateFlow<Resource<CartProd>>(Resource.Unspecified())
    val addtCart=_addtCart.asStateFlow()

    fun addUpdateProdinCart(cartProd: CartProd){
        viewModelScope.launch { _addtCart.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart").whereEqualTo("products.id",cartProd.product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if(it.isEmpty()){
                        addNewProd(cartProd)
                    }
                    else{
                        val product=it.first().toObject(cartProd::class.java)
                        if(product==cartProd){
                            val documentId=it.first().id
                            increasQuantity(documentId,cartProd)
                        }else{

                        }
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch { _addtCart.emit(Resource.Error(it.message.toString())) }
            }
    }
    private fun addNewProd(cartProd: CartProd){
        firebaseCommon.addProdToCart(cartProd){addedProd,e->
            viewModelScope.launch {
                if (e == null)
                    _addtCart.emit(Resource.Success(addedProd!!))
                else
                    _addtCart.emit(Resource.Error(e.message.toString()))

            }
        }
    }

    private fun increasQuantity(documentId:String,cartProd: CartProd){
        firebaseCommon.increaseQuantity(documentId){_,e->
            viewModelScope.launch {
                if (e == null)
                    _addtCart.emit(Resource.Success(cartProd))
                else
                    _addtCart.emit(Resource.Error(e.message.toString()))

            }
        }

    }


}
package com.akj.popcart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akj.popcart.data.CartProd
import com.akj.popcart.firebase.FirebaseCommon
import com.akj.popcart.helper.getProductPrice
import com.akj.popcart.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth:FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) :ViewModel(){

    private val _cartProd = MutableStateFlow<Resource<List<CartProd>>>(Resource.Unspecified())
    val cartProds=_cartProd.asStateFlow()


    private var cartProdDoc= emptyList<DocumentSnapshot>()


    val productPrice = cartProds.map {
        when(it){
            is Resource.Success ->{
                calculatedPrice(it.data!!)
            }
            else -> null
        }
    }



    private val _deleteDialog= MutableSharedFlow<CartProd>()
    val deleteDialog=_deleteDialog.asSharedFlow()

    fun deleteCartProd(cartProd: CartProd) {
        val index = cartProds.value.data?.indexOf(cartProd)
        if (index != null && index != -1){
        val documentId = cartProdDoc[index].id
        firestore.collection("user").document(auth.uid!!).collection("cart").document(documentId).delete()
    }
    }

    private fun calculatedPrice(data: List<CartProd>): Float {

        return data.sumByDouble { cartProd ->
            (cartProd.product.offerPercentage.getProductPrice(cartProd.product.price) * cartProd.quantity).toDouble()

        }.toFloat()

    }



    init {
        getcartProd()
    }

    private fun getcartProd(){
        viewModelScope.launch { _cartProd.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .addSnapshotListener{value , error->
                if (error!= null || value == null){
                    viewModelScope.launch { _cartProd.emit(Resource.Error(error?.message.toString())) }
                }else{
                    cartProdDoc= value.documents
                    val cartProd=value.toObjects(CartProd::class.java)
                    viewModelScope.launch { _cartProd.emit(Resource.Success(cartProd)) }
                }
            }
    }

    fun changeQuantity(
        cartProd: CartProd,
        quantityChanging: FirebaseCommon.QuantityChanging
    ){


        val index = cartProds.value.data?.indexOf(cartProd)



        if(index != null && index != -1) {
            val documentId = cartProdDoc[index].id
            when(quantityChanging){
                FirebaseCommon.QuantityChanging.INCREASE ->{
                    viewModelScope.launch { _cartProd.emit(Resource.Loading()) }
                    increaseQuantity(documentId)
                }
                FirebaseCommon.QuantityChanging.DECREASE ->{
                    if(cartProd.quantity==1){
                        viewModelScope.launch{
                            _deleteDialog.emit(cartProd)
                        }
                        return
                    }
                    viewModelScope.launch { _cartProd.emit(Resource.Loading()) }
                    decreasQuantity(documentId)
                }
            }
        }
    }

    private fun decreasQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId){result,exception ->
            if (exception != null)
                viewModelScope.launch { _cartProd.emit(Resource.Error(exception.message.toString())) }
        }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId){result,exception ->
            if (exception != null)
                viewModelScope.launch { _cartProd.emit(Resource.Error(exception.message.toString())) }
        }
    }

}
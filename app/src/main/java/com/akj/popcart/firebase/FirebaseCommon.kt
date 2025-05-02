package com.akj.popcart.firebase

import com.akj.popcart.data.CartProd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseCommon(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val cartCollection=firestore.collection("user").document(auth.uid!!).collection("cart")


    fun addProdToCart(cartProd: CartProd,onResult: (CartProd?,Exception?)->Unit){
        cartCollection.document().set(cartProd)
            .addOnSuccessListener {
                onResult(cartProd,null)
            }
            .addOnFailureListener {
                onResult(null,it)
            }
    }

    fun increaseQuantity(documentId: String,onResult: (String?, Exception?) -> Unit){
        firestore.runTransaction{transition->
            val documentRef=cartCollection.document(documentId)
            val document=transition.get(documentRef)
            val productObject=document.toObject(CartProd::class.java)
            productObject?.let { cartProd ->
                val newQuantity = cartProd.quantity + 1
                val newProdObject=cartProd.copy(quantity = newQuantity)
                transition.set(documentRef,newProdObject)
            }
        }.addOnSuccessListener {
            onResult(documentId,null)
        }.addOnFailureListener {
            onResult(null,it)
        }
    }


    fun decreaseQuantity(documentId: String,onResult: (String?, Exception?) -> Unit){
        firestore.runTransaction{transition->
            val documentRef=cartCollection.document(documentId)
            val document=transition.get(documentRef)
            val productObject=document.toObject(CartProd::class.java)
            productObject?.let { cartProd ->
                val newQuantity = cartProd.quantity - 1
                val newProdObject=cartProd.copy(quantity = newQuantity)
                transition.set(documentRef,newProdObject)
            }
        }.addOnSuccessListener {
            onResult(documentId,null)
        }.addOnFailureListener {
            onResult(null,it)
        }
    }

    enum class QuantityChanging{
        INCREASE,DECREASE
    }



}
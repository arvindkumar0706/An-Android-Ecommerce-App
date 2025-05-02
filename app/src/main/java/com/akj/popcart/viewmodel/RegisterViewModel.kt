package com.akj.popcart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akj.popcart.data.User
import com.akj.popcart.util.Constants.USER_COLLECTION
import com.akj.popcart.util.RegisterFieldsState
import com.akj.popcart.util.RegisterValidation
import com.akj.popcart.util.Resource
import com.akj.popcart.util.validateEmail
import com.akj.popcart.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Loading())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    fun createAccwEmailandPass(user: User, password: String) {
        if (checkValidation(user, password)) {
            viewModelScope.launch {
                _register.value = Resource.Loading()

                try {
                    firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                        .addOnSuccessListener { result ->
                            result.user?.let { firebaseUser ->
                                saveUserInfo(firebaseUser.uid, user)
                            }
                        }
                        .addOnFailureListener { exception ->
                            _register.value = Resource.Error(exception.message ?: "Registration failed")
                        }
                } catch (e: Exception) {
                    _register.value = Resource.Error(e.message ?: "An unexpected error occurred")
                }
            }
        } else {
            val registerFieldsState = RegisterFieldsState(
                validateEmail(user.email), validatePassword(password)
            )
            viewModelScope.launch {
                _validation.send(registerFieldsState)
            }
        }
    }


    private fun saveUserInfo(userUid: String, user: User) {
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }
            .addOnFailureListener { exception ->
                _register.value = Resource.Error(exception.message ?: "Failed to save user info")
            }
    }


    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        return emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
    }
}
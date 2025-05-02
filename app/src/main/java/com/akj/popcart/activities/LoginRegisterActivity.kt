package com.akj.popcart.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.akj.popcart.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)
    }
}
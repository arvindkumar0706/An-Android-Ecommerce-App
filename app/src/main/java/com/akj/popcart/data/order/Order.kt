package com.akj.popcart.data.order

import android.os.Parcelable
import com.akj.popcart.data.Address
import com.akj.popcart.data.CartProd
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random.Default.nextLong


@Parcelize
data class Order(
    val orderStatus:String="",
    val totalPrice:Float =0f,
    val products:List<CartProd> = emptyList(),
    val address:Address = Address(),
    val paymentMethod: String,
    val date :String=SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId:Long = nextLong(0,100_000_000_000)+ totalPrice.toLong()
):Parcelable{
    constructor() : this("", 0f, emptyList(), Address(), "")
}
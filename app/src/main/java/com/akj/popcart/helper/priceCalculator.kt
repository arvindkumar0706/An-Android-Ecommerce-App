package com.akj.popcart.helper


fun Float?.getProductPrice(price : Float):Float{
    if(this == null)
        return price


    val priceafteroffer = price * (1 - this / 100f)

    return  priceafteroffer

}
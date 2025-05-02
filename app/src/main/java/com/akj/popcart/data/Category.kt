package com.akj.popcart.data

sealed class Category(val category: String) {

    object Fashion : Category("Fashion")
    object Mobiles : Category("Mobiles")
    object Electronics : Category("Electronics")
    object Footwears : Category("Grooming")
    object Computer : Category("Computer")
    object Furniture : Category("Furniture")
    object Sports : Category("Sports")




}
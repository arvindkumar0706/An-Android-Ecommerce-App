package com.akj.popcart.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.data.CartProd
import com.akj.popcart.databinding.CartProductItemBinding
import com.akj.popcart.helper.getProductPrice
import com.bumptech.glide.Glide

class cartProdAdapter : RecyclerView.Adapter<cartProdAdapter.cartProdViewHolder>() {

    inner class cartProdViewHolder( val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartProd: CartProd) {
            binding.apply {
                Glide.with(itemView).load(cartProd.product.images[0]).into(imageCartProduct)
                tvProductCartName.text=cartProd.product.name
                tvCartProductQuantity.text=cartProd.quantity.toString()

                val priceafteroffer = cartProd.product.offerPercentage.getProductPrice(cartProd.product.price)
                tvProductCartPrice.text="₹ ${String.format("%.2f",priceafteroffer)}"

                imageCartProductColor.setImageDrawable(ColorDrawable(cartProd.selectedColor?: Color.TRANSPARENT))
                tvCartProductSize.text=cartProd.selectedSize?:"".also { imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT)) }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProd>() {
        override fun areItemsTheSame(oldItem: CartProd, newItem: CartProd): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProd, newItem: CartProd): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): cartProdViewHolder {
        return cartProdViewHolder( CartProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: cartProdViewHolder, position: Int) {
        val cartProd = differ.currentList[position]
        holder.bind(cartProd)

        holder.itemView.setOnClickListener {
            onProdClick?.invoke(cartProd)
        }

        holder.binding.imagePlus.setOnClickListener {
            onPlusClick?.invoke(cartProd)
        }

        holder.binding.imageMinus.setOnClickListener {
            onMinusClick?.invoke(cartProd)
        }

    }



    var onProdClick:((CartProd)->Unit)? = null
    var onPlusClick:((CartProd)->Unit)? = null
    var onMinusClick:((CartProd)->Unit)? = null
}

package com.akj.popcart.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.akj.popcart.data.CartProd
import com.akj.popcart.databinding.BillingProductsRvItemBinding
import com.akj.popcart.helper.getProductPrice
import com.bumptech.glide.Glide

class BillingAdapter :Adapter<BillingAdapter.BillingViewHolder>() {

    inner class BillingViewHolder(val binding:BillingProductsRvItemBinding):ViewHolder(binding.root){
        fun bind(billingProduct: CartProd) {

            binding.apply {
                Glide.with(itemView).load(billingProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text=billingProduct.product.name
                tvBillingProductQuantity.text=billingProduct.quantity.toString()
                val priceafteroffer = billingProduct.product.offerPercentage.getProductPrice(billingProduct.product.price)
                tvProductCartPrice.text="₹ ${String.format("%.2f",priceafteroffer)}"

                imageCartProductColor.setImageDrawable(ColorDrawable(billingProduct.selectedColor?: Color.TRANSPARENT))
                tvCartProductSize.text=billingProduct.selectedSize?:"".also { imageCartProductSize.setImageDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                ) }
            }

        }

    }

    private val diffUtil=object :DiffUtil.ItemCallback<CartProd>(){
        override fun areItemsTheSame(oldItem: CartProd, newItem: CartProd): Boolean {
            return oldItem.product==newItem.product
        }

        override fun areContentsTheSame(oldItem: CartProd, newItem: CartProd): Boolean {
            return oldItem == newItem
        }

    }

    val differ=AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        return BillingViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]
        
        holder.bind(billingProduct)
        
    }

}
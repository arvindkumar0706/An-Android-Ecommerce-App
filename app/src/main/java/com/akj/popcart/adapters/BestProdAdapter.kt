package com.akj.popcart.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.data.Products
import com.akj.popcart.databinding.ProductRvItemBinding
import com.bumptech.glide.Glide

class BestProdAdapter : RecyclerView.Adapter<BestProdAdapter.BestProdViewHolder>(){
    inner class BestProdViewHolder(private val binding : ProductRvItemBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(product: Products){
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgProduct)
                product.offerPercentage?.let {

                    val priceafteroffer = product.price * (1 - it / 100f)
                    tvNewPrice.text="₹ ${String.format("%.2f",priceafteroffer)}"
                    tvPrice.paintFlags=Paint.STRIKE_THRU_TEXT_FLAG
                }
                if(product.offerPercentage== null){
                    tvNewPrice.visibility=View.INVISIBLE
                }
                tvPrice.text="₹ ${product.price}"
                tvName.text=product.name
            }
        }
    }
    private val diffCallback = object : DiffUtil.ItemCallback<Products>(){
        override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
            return oldItem==newItem
        }
    }

    val differ= AsyncListDiffer(this,diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestProdViewHolder {
        return BestProdViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestProdViewHolder, position: Int) {
        val product=differ.currentList[position]
        holder.bind(product)
        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick:((Products)->Unit)? = null
}
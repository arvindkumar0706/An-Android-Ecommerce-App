package com.akj.popcart.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.data.Products
import com.akj.popcart.databinding.SpecialRvItemBinding
import com.bumptech.glide.Glide

class SpecialProdAdapter : RecyclerView.Adapter<SpecialProdAdapter.SpecialProdViewHolder>() {

    inner class SpecialProdViewHolder(private val binding: SpecialRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Products) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imageSpecialRvItem)
                tvSpecialProdName.text=product.name
                tvSpecialProdPrice.text="₹ ${String.format("%.2f",product.price)}"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Products>() {
        override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
            return oldItem == newItem
        }
    }

     val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialProdViewHolder {
       return SpecialProdViewHolder( SpecialRvItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
       )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SpecialProdViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }

    }

    fun submitList(list: List<Products>) {
        differ.submitList(list)
    }

    var onClick:((Products)->Unit)? = null
}

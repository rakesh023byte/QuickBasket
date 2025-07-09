package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemProductBinding
import com.example.myapplication.models.Product

class ProductAdapter(
    private val context: Context,
    private val productList: List<Product>,
    private val loadingSet: MutableSet<String> = mutableSetOf(),
    private val listener: OnAddToCartClickListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    interface OnAddToCartClickListener {
        fun onAddToCartClicked(product: Product)
    }

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        val isLoading = loadingSet.contains(product.productId)

        holder.binding.apply {
            productName.text = product.name
            productPrice.text = "₹${product.price}"

            if (product.imageUrl.startsWith("http")) {
                Glide.with(context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(productImage)
            } else {
                val imageResId = context.resources.getIdentifier(product.imageUrl, "drawable", context.packageName)
                Glide.with(context)
                    .load(imageResId)
                    .into(productImage)
            }

            btnAddToCart.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            addToCartProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            quantityLayout.visibility = View.GONE

            btnAddToCart.setOnClickListener {
                loadingSet.add(product.productId ?: product.name)
                notifyItemChanged(position)
                listener.onAddToCartClicked(product)
            }
        }
    }

    override fun getItemCount() = productList.size

    fun stopLoading(productId: String) {
        loadingSet.remove(productId)
        notifyDataSetChanged()
    }
}

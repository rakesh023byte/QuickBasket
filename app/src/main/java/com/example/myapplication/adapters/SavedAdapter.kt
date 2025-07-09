package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.CartItem
import com.google.android.material.button.MaterialButton

class SavedAdapter(
    private val context: Context,
    private val savedItems: MutableList<CartItem>,
    private val onMoveToCart: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit,
    private val onSavedUpdated: () -> Unit
) : RecyclerView.Adapter<SavedAdapter.SavedViewHolder>() {

    inner class SavedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnMoveToCart: MaterialButton = itemView.findViewById(R.id.btnMoveToCart)
        val btnRemove: MaterialButton = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_saved, parent, false)
        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        val item = savedItems[position]

        holder.tvTitle.text = item.name
        holder.tvPrice.text = "₹${item.pricePerUnit}"
        Glide.with(context).load(item.imageUrl).into(holder.productImage)

        holder.btnMoveToCart.setOnClickListener {
            onMoveToCart(item)
            onSavedUpdated()
            Toast.makeText(context, "Moved to cart", Toast.LENGTH_SHORT).show()
        }

        holder.btnRemove.setOnClickListener {
            onRemove(item)
            onSavedUpdated()
            Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = savedItems.size
}

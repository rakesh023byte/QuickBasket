package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.CartItem
import com.google.android.material.button.MaterialButton

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<CartItem>,
    private val onCartAction: (CartItem, Action) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    enum class Action {
        INCREMENT, DECREMENT, REMOVE, SAVE_FOR_LATER
    }

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnIncrement: ImageButton = view.findViewById(R.id.btnIncrement)
        val btnDecrement: ImageButton = view.findViewById(R.id.btnDecrement)
        val btnRemove: MaterialButton = view.findViewById(R.id.btnRemove)
        val btnSaveForLater: MaterialButton = view.findViewById(R.id.btnSaveForLater)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.tvTitle.text = item.name
        holder.tvPrice.text = "₹${item.pricePerUnit * item.quantity}"
        holder.tvQuantity.text = item.quantity.toString()

        Glide.with(context).load(item.imageUrl).into(holder.productImage)

        holder.btnIncrement.setOnClickListener {
            onCartAction(item, Action.INCREMENT)
        }

        holder.btnDecrement.setOnClickListener {
            onCartAction(item, Action.DECREMENT)
        }

        holder.btnRemove.setOnClickListener {
            onCartAction(item, Action.REMOVE)
        }

        holder.btnSaveForLater.setOnClickListener {
            onCartAction(item, Action.SAVE_FOR_LATER)
            Toast.makeText(context, "Saved for later", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = cartItems.size
}

package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemOrderBinding
import com.example.myapplication.models.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            // Order ID (last 6 characters)
            binding.tvOrderId.text = "Order ID: #${order.orderId.takeLast(6)}"

            // Format timestamp to readable date
            binding.tvOrderDate.text = formatDate(order.timestamp)

            // Status and Total Price
            binding.tvOrderStatus.text = "Status: ${order.status}"
            binding.tvTotalAmount.text = "Total: ₹%.2f".format(order.totalAmount)

            // Generate item summary string
            val itemNames = order.items.map { it.name }

            val summary = when {
                itemNames.size > 2 -> {
                    val first = itemNames[0]
                    val second = itemNames[1]
                    val remaining = itemNames.size - 2
                    "$first, $second +$remaining more"
                }
                itemNames.size == 2 -> "${itemNames[0]}, ${itemNames[1]}"
                itemNames.size == 1 -> itemNames[0]
                else -> "No products"
            }

            binding.tvOrderItemsSummary.text = summary
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size
}

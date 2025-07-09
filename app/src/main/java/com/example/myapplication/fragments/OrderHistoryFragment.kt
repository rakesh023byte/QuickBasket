package com.example.myapplication.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.OrderAdapter
import com.example.myapplication.databinding.FragmentOrderHistoryBinding
import com.example.myapplication.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter
    private val orders = mutableListOf<Order>()

    private val auth = FirebaseAuth.getInstance()
    private val dbRef by lazy {
        FirebaseDatabase.getInstance().reference
            .child("orders")
            .child(auth.currentUser?.uid ?: "")
    }

    // Handler for periodic status update
    private val statusHandler = Handler(Looper.getMainLooper())
    private val statusSequence = listOf("Pending", "Shipped", "Out for Delivery", "Delivered")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadOrders()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(orders)
        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewOrders.adapter = adapter
    }

    private fun loadOrders() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                orders.clear()
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(Order::class.java)
                    if (order != null) {
                        orders.add(order)
                    }
                }

                // Sort orders by timestamp in descending order
                orders.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()
                updateEmptyState()

                // Start auto status update
                startAutoStatusUpdater()
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmptyState() {
        if (!isAdded || _binding == null) return

        if (orders.isEmpty()) {
            binding.recyclerViewOrders.visibility = View.GONE
            binding.tvNoOrders.visibility = View.VISIBLE
        } else {
            binding.recyclerViewOrders.visibility = View.VISIBLE
            binding.tvNoOrders.visibility = View.GONE
        }
    }

    private fun startAutoStatusUpdater() {
        statusHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!isAdded || _binding == null) return

                for (order in orders) {
                    if (order.status != "Delivered") {
                        val currentIndex = statusSequence.indexOf(order.status)
                        if (currentIndex in 0 until statusSequence.lastIndex) {
                            val newStatus = statusSequence[currentIndex + 1]
                            updateOrderStatusInFirebase(order.orderId, newStatus)
                        }
                    }
                }

                statusHandler.postDelayed(this, 5000) // Repeat every 5 seconds
            }
        }, 5000)
    }

    private fun updateOrderStatusInFirebase(orderId: String, newStatus: String) {
        val orderRef = dbRef.child(orderId)
        orderRef.child("status").setValue(newStatus)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statusHandler.removeCallbacksAndMessages(null)
        _binding = null
    }
}

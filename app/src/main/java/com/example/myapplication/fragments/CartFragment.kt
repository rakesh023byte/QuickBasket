package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.CartAdapter
import com.example.myapplication.adapters.SavedAdapter
import com.example.myapplication.animation.OrderSuccessFragment
import com.example.myapplication.databinding.FragmentCartBinding
import com.example.myapplication.models.CartItem
import com.example.myapplication.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartItems = mutableListOf<CartItem>()
    private val savedItems = mutableListOf<CartItem>()

    private val auth = FirebaseAuth.getInstance()
    private val dbRef by lazy {
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(auth.currentUser?.uid ?: "")
    }

    private lateinit var cartAdapter: CartAdapter
    private lateinit var savedAdapter: SavedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        setupAdapters()
        loadCartItems()
        loadSavedItems()

        binding.btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val orderId = UUID.randomUUID().toString().replace("-", "").take(12)
            val total = cartItems.sumOf { it.pricePerUnit * it.quantity }

            val order = Order(
                orderId = orderId,
                userId = userId,
                items = cartItems,
                totalAmount = total,
                timestamp = System.currentTimeMillis(),
                status = "Pending"
            )

            FirebaseDatabase.getInstance().reference
                .child("orders")
                .child(userId)
                .child(orderId)
                .setValue(order)
                .addOnSuccessListener {
                    dbRef.child("cart").removeValue()
                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(id, OrderSuccessFragment()) // use `id` of current fragment container
                .addToBackStack(null)
                .commit()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to place order!", Toast.LENGTH_SHORT).show()
                }
        }

        return binding.root
    }

    private fun setupAdapters() {
        cartAdapter = CartAdapter(requireContext(), cartItems) { item, action ->
            handleCartAction(item, action)
        }

        savedAdapter = SavedAdapter(
            requireContext(),
            savedItems,
            onMoveToCart = { moveToCart(it) },
            onRemove = { removeFromSaved(it) },
            onSavedUpdated = { updateSavedVisibility() }
        )

        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter

        binding.savedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.savedRecyclerView.adapter = savedAdapter
    }

    private fun loadCartItems() {
        dbRef.child("cart").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear()
                for (itemSnap in snapshot.children) {
                    itemSnap.getValue(CartItem::class.java)?.let { cartItems.add(it) }
                }
                cartAdapter.notifyDataSetChanged()
                updateTotalPrice()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadSavedItems() {
        dbRef.child("saved").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savedItems.clear()
                for (itemSnap in snapshot.children) {
                    itemSnap.getValue(CartItem::class.java)?.let { savedItems.add(it) }
                }
                savedAdapter.notifyDataSetChanged()
                updateSavedVisibility()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun handleCartAction(item: CartItem, action: CartAdapter.Action) {
        val cartRef = dbRef.child("cart").child(item.productId)
        val savedRef = dbRef.child("saved").child(item.productId)

        when (action) {
            CartAdapter.Action.INCREMENT -> {
                cartRef.child("quantity").setValue(item.quantity + 1)
            }

            CartAdapter.Action.DECREMENT -> {
                if (item.quantity > 1) {
                    cartRef.child("quantity").setValue(item.quantity - 1)
                } else {
                    cartRef.removeValue()
                }
            }

            CartAdapter.Action.REMOVE -> {
                cartRef.removeValue()
            }

            CartAdapter.Action.SAVE_FOR_LATER -> {
                savedRef.setValue(item).addOnSuccessListener {
                    cartRef.removeValue()
                }
            }
        }
    }

    private fun moveToCart(item: CartItem) {
        val cartRef = dbRef.child("cart").child(item.productId)
        val savedRef = dbRef.child("saved").child(item.productId)

        cartRef.setValue(item).addOnSuccessListener {
            savedRef.removeValue()
        }
    }

    private fun removeFromSaved(item: CartItem) {
        dbRef.child("saved").child(item.productId).removeValue()
    }

    private fun updateTotalPrice() {
        if (!isAdded || _binding == null) return
        val total = cartItems.sumOf { it.pricePerUnit * it.quantity }
        binding.tvTotalPrice.text = "Total: ₹%.2f".format(total)
    }

    private fun updateSavedVisibility() {
        if (!isAdded || _binding == null) return
        binding.savedRecyclerView.visibility = if (savedItems.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

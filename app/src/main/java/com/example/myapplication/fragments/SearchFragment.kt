package com.example.myapplication.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.adapters.SearchAdapter
import com.example.myapplication.databinding.FragmentSearchBinding
import com.example.myapplication.models.CartItem
import com.example.myapplication.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter
    private val allProducts = mutableListOf<Product>()

    private val dbRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("products")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchAllProducts()
        setupSearchListener()

        return binding.root
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(
            requireContext(),
            mutableListOf(),
            onProductClick = { product ->
                Toast.makeText(requireContext(), "Clicked: ${product.name}", Toast.LENGTH_SHORT).show()
            },
            onAddToCartClick = { product ->
                addToCart(product)
            }
        )

        binding.recyclerViewSearch.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerViewSearch.adapter = searchAdapter
    }

    private fun fetchAllProducts() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProducts.clear()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(Product::class.java)
                    if (product != null) {
                        allProducts.add(product)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                if (query.isEmpty()) {
                    searchAdapter.updateList(emptyList())
                    binding.recyclerViewSearch.visibility = View.GONE
                    return
                }

                val filtered = allProducts.filter {
                    it.name.lowercase().contains(query)
                }

                searchAdapter.updateList(filtered)
                binding.recyclerViewSearch.visibility = View.VISIBLE
            }
        })
    }

    private fun addToCart(product: Product) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login to add items.", Toast.LENGTH_SHORT).show()
            return
        }

        val cartRef = FirebaseDatabase.getInstance().getReference("users/$userId/cart")

        cartRef.child(product.name).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val existingItem = snapshot.getValue(CartItem::class.java)
                val newQuantity = (existingItem?.quantity ?: 0) + 1

                val cartItem = CartItem(
                    productId = product.name,
                    name = product.name,
                    quantity = newQuantity,
                    pricePerUnit = product.price,
                    imageUrl = product.imageUrl
                )

                cartRef.child(product.name).setValue(cartItem)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to add to cart.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

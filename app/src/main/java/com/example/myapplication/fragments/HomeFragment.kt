package com.example.myapplication.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.adapters.ProductAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.models.CartItem
import com.example.myapplication.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class HomeFragment : Fragment(), ProductAdapter.OnAddToCartClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter

    private val sampleProducts = listOf(
        Product("Apple", 99.0, "ic_apple"),
        Product("Banana", 90.0, "ic_banana"),
        Product("Milk", 99.0, "ic_milk"),
        Product("Bread", 100.0, "ic_bread"),
        Product("Blueberry", 1000.0, "ic_blueberry"),
        Product("Dragon Fruit", 1000.0, "ic_dragon_fruit"),
        Product("Coconut Water", 150.0, "ic_coconut_water"),
        Product("Sprite", 60.0, "ic_sprite"),
        Product("Rolex", 60000.0, "ic_rolex"),
        Product("Cologne", 6000.0, "ic_cologne"),
        Product("ToothBrush", 8000.0, "ic_toothbrush"),
        Product("FaceWash", 5000.0, "ic_facewash"),
        Product("Golden Apple", 500000.0, "ic_golden_apple"),
        Product("Charizard", 5000000.0, "ic_charizard")
    )

    private val dbRef by lazy { FirebaseDatabase.getInstance().getReference("products") }
    private val storageRef by lazy { FirebaseStorage.getInstance().reference.child("product_images") }
    private val configRef by lazy { FirebaseDatabase.getInstance().getReference("config") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.progressBar.visibility = View.VISIBLE
        checkAndUploadProductsThenLoad()
        return binding.root
    }

    private fun checkAndUploadProductsThenLoad() {
        configRef.child("products_initialized").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alreadyUploaded = snapshot.getValue(Boolean::class.java) == true
                if (alreadyUploaded) {
                    loadProductsIntoRecycler()
                } else {
                    uploadProductsToFirebase {
                        configRef.child("products_initialized").setValue(true)
                        loadProductsIntoRecycler()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadProductsToFirebase(onComplete: () -> Unit) {
        var uploadedCount = 0
        sampleProducts.forEachIndexed { index, product ->
            val fileName = "${product.name.lowercase().replace(" ", "_")}.png"
            val imageRef = storageRef.child(fileName)

            val drawableId = resources.getIdentifier(product.imageUrl, "drawable", requireContext().packageName)
            val bitmap = BitmapFactory.decodeResource(resources, drawableId)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageData = baos.toByteArray()

            imageRef.putBytes(imageData).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val productId = "product_${index + 1}"
                    val productWithUrl = product.copy(productId = productId, imageUrl = uri.toString())

                    dbRef.child(productId).setValue(productWithUrl).addOnSuccessListener {
                        uploadedCount++
                        if (uploadedCount == sampleProducts.size) {
                            onComplete()
                        }
                    }
                }
            }.addOnFailureListener { ex ->
                Log.e("UploadError", "Failed to upload $fileName: ${ex.message}")
            }
        }
    }

    private fun loadProductsIntoRecycler() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val productList = mutableListOf<Product>()
                for (snap in snapshot.children) {
                    val product = snap.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }

                productAdapter = ProductAdapter(requireContext(), productList.toList(), mutableSetOf(), this@HomeFragment)
                binding.productRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.productRecyclerView.adapter = productAdapter
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Failed to load products.", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onAddToCartClicked(product: Product) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val productKey = product.productId ?: product.name

        val cartRef = FirebaseDatabase.getInstance().getReference("users/$userId/cart")
        cartRef.child(productKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val existingItem = snapshot.getValue(CartItem::class.java)
                val newQuantity = (existingItem?.quantity ?: 0) + 1

                val cartItem = CartItem(
                    productId = productKey,
                    name = product.name,
                    quantity = newQuantity,
                    pricePerUnit = product.price,
                    imageUrl = product.imageUrl
                )

                cartRef.child(productKey).setValue(cartItem)
                    .addOnSuccessListener {
                        if (!isAdded) return@addOnSuccessListener
                        Toast.makeText(requireContext(), "${product.name} added to cart.", Toast.LENGTH_SHORT).show()
                        productAdapter.stopLoading(product.productId ?: product.name)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Failed to add to cart.", Toast.LENGTH_SHORT).show()
                productAdapter.stopLoading(product.productId ?: product.name)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

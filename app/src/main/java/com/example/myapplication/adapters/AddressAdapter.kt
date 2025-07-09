package com.example.myapplication.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activity.AddAddressActivity
import com.example.myapplication.models.Address
import com.google.firebase.firestore.FirebaseFirestore

class AddressAdapter(
    private val context: Context,
    private var addressList: MutableList<Address>,
    private val userId: String
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun getItemCount(): Int = addressList.size

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addressList[position]
        holder.tvAddressDetails.text = "${address.fullName}, ${address.phone}, ${address.addressLine}, ${address.city}, ${address.pincode}"
        holder.checkboxDefault.isChecked = address.isDefault

        // Handle default checkbox logic
        holder.checkboxDefault.setOnCheckedChangeListener(null)
        holder.checkboxDefault.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateDefaultAddress(address.id)
            }
        }

        // Delete address
        holder.btnDelete.setOnClickListener {
            db.collection("users").document(userId)
                .collection("addresses").document(address.id).delete()
                .addOnSuccessListener {
                    addressList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, addressList.size)
                }
        }

        // Edit address logic (to be implemented in your Edit flow)
        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, AddAddressActivity::class.java).apply {
                putExtra("addressId", address.id)
                putExtra("fullName", address.fullName)
                putExtra("phone", address.phone)
                putExtra("addressLine", address.addressLine)
                putExtra("city", address.city)
                putExtra("pincode", address.pincode)
            }
            context.startActivity(intent)
        }

    }

    private fun updateDefaultAddress(newDefaultId: String) {
        val batch = db.batch()
        val addressesRef = db.collection("users").document(userId).collection("addresses")

        for (addr in addressList) {
            val ref = addressesRef.document(addr.id)
            batch.update(ref, "isDefault", addr.id == newDefaultId)
        }

        batch.commit().addOnSuccessListener {
            // Rearrange list to keep default at top
            for (i in addressList.indices) {
                addressList[i] = addressList[i].copy(isDefault = addressList[i].id == newDefaultId)
            }
            addressList = addressList.sortedByDescending { it.isDefault }.toMutableList()
            notifyDataSetChanged()
        }
    }

    fun updateList(newList: List<Address>) {
        addressList = newList.sortedByDescending { it.isDefault }.toMutableList()
        notifyDataSetChanged()
    }

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAddressDetails: TextView = itemView.findViewById(R.id.tvAddress)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val checkboxDefault: CheckBox = itemView.findViewById(R.id.cbDefault)
    }
}

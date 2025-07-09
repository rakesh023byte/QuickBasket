package com.example.myapplication.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activity.AddAddressActivity
import com.example.myapplication.models.Address
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class AddressBottomSheet(
    private val context: Context,
    private val userId: String
) : BottomSheetDialog(context) {

    private lateinit var rvAddresses: RecyclerView
    private lateinit var btnAdd: Button
    private val db = FirebaseFirestore.getInstance()
    private var addressList = mutableListOf<Address>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_sheet_address)

        rvAddresses = findViewById(R.id.rvAddresses)!!
        btnAdd = findViewById(R.id.btnAddAddress)!!
        rvAddresses.layoutManager = LinearLayoutManager(context)
        fetchAddresses()

        btnAdd.setOnClickListener {
            dismiss()
            context.startActivity(Intent(context, AddAddressActivity::class.java))
        }
    }

    private fun fetchAddresses() {
        db.collection("users").document(userId).collection("addresses")
            .get()
            .addOnSuccessListener { result ->
                addressList = result.map { doc ->
                    Address(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: "",
                        phone = doc.getString("phone") ?: "",
                        addressLine = doc.getString("addressLine") ?: "",
                        city = doc.getString("city") ?: "",
                        pincode = doc.getString("pincode") ?: "",
                        isDefault = (doc.get("isDefault") as? Boolean) ?: false
                    )
                }.sortedByDescending { it.isDefault }.toMutableList()

                rvAddresses.adapter = AddressAdapter(addressList)
            }
    }

    inner class AddressAdapter(private val items: MutableList<Address>) :
        RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

        inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
            val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
            val cbDefault: CheckBox = itemView.findViewById(R.id.cbDefault)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_address, parent, false)
            return AddressViewHolder(view)
        }

        override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
            val address = items[position]
            holder.tvAddress.text = """
                ${address.fullName}
                ${address.phone}
                ${address.addressLine}, ${address.city}
                ${address.pincode}
            """.trimIndent()
            holder.cbDefault.isChecked = address.isDefault

            holder.cbDefault.setOnClickListener {
                if (!address.isDefault) updateDefault(address)
            }

            holder.btnEdit.setOnClickListener {
                val intent = Intent(context, AddAddressActivity::class.java).apply {
                    putExtra("mode", "edit")
                    putExtra("id", address.id)
                    putExtra("fullName", address.fullName)
                    putExtra("phone", address.phone)
                    putExtra("addressLine", address.addressLine)
                    putExtra("city", address.city)
                    putExtra("pincode", address.pincode)
                }
                context.startActivity(intent)
                dismiss()
            }

            holder.btnDelete.setOnClickListener {
                db.collection("users").document(userId)
                    .collection("addresses").document(address.id).delete()
                    .addOnSuccessListener {
                        items.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, items.size)
                        Toast.makeText(context, "Address deleted", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        override fun getItemCount(): Int = items.size

        private fun updateDefault(newDefault: Address) {
            val batch = db.batch()
            val addressesRef = db.collection("users").document(userId).collection("addresses")
            items.filter { it.isDefault }.forEach {
                batch.update(addressesRef.document(it.id), "isDefault", false)
                it.isDefault = false
            }
            batch.update(addressesRef.document(newDefault.id), "isDefault", true)
            newDefault.isDefault = true

            batch.commit().addOnSuccessListener {
                items.sortByDescending { it.isDefault }
                notifyDataSetChanged()
            }
        }
    }
}

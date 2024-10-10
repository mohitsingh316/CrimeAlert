package com.example.crimealert.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class ContactsAdapter(
    private val context: Context,
    private val contacts: MutableList<Map<String, String>>
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactProfileImg: CircleImageView = itemView.findViewById(R.id.contact_profile_img)
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val contactNumber: TextView = itemView.findViewById(R.id.contactNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contactName.text = contacts[position]["name"]
        holder.contactNumber.text = contacts[position]["phone"]

        holder.itemView.setOnLongClickListener {
            showDeleteConfirmationDialog(position)
            true
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val contactName = contacts[position]["name"]
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete $contactName?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteContactFromDatabase(position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun deleteContactFromDatabase(position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val contactToDelete = contacts[position]

        // Remove the contact from the Firestore using FieldValue.arrayRemove
        firestore.collection("users")
            .document(userId)
            .update("contacts", FieldValue.arrayRemove(contactToDelete))
            .addOnSuccessListener {
                Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()

                // Update the local contacts list and notify the adapter
                contacts.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, contacts.size) // Optionally notify the range changed
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error deleting contact: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

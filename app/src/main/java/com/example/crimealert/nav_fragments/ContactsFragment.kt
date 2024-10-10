package com.example.crimealert.nav_fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.example.crimealert.adapter.ContactsAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ContactsFragment : Fragment() {

    private lateinit var fabAddContact: FloatingActionButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

        fabAddContact = view.findViewById(R.id.fab_add_contact)

        fabAddContact.setOnClickListener {
            showAddContactDialog(view)
        }
        showContacts(view)
        return view
    }

    private fun showAddContactDialog(view: View) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Contact")

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_contact_dialogbox, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.Contact_Name)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.Contact_Number)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        builder.setView(dialogView)
        val dialog = builder.create()

        // Adjust the dialog's window size
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            if (name.isNotEmpty() && phone.isNotEmpty()) {
                saveContactToFirestore(view,name, phone, dialog)  // Pass the dialog instance
            } else {
                Toast.makeText(requireContext(), "Please enter both name and phone number", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun saveContactToFirestore(view: View,name: String, phone: String, dialog: AlertDialog) {
        val userId = auth.currentUser?.uid ?: return
        val contact = mapOf("name" to name, "phone" to phone)

        firestore.collection("users").document(userId)
            .update("contacts", FieldValue.arrayUnion(contact))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Contact added successfully", Toast.LENGTH_SHORT).show()
                Log.d("ContactsFragment", "Contact added: Name=$name, Phone=$phone")
                showContacts(view)
                dialog.dismiss()  // Close the dialog after successful addition
            }
            .addOnFailureListener { e ->
                Log.w("ContactsFragment", "Error adding contact", e)
                firestore.collection("users").document(userId)
                    .set(mapOf("contacts" to listOf(contact)))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Contact added successfully", Toast.LENGTH_SHORT).show()
                        Log.d("ContactsFragment", "Contact added: Name=$name, Phone=$phone")
                        dialog.dismiss()  // Close the dialog after successful addition
                    }
                    .addOnFailureListener { e ->
                        Log.w("ContactsFragment", "Error creating contacts array", e)
                        Toast.makeText(requireContext(), "Failed to add contact", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun showContacts(view: View) {
        val userId = auth.currentUser?.uid ?: return
        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Safely cast the contacts field to a MutableList of Maps
                    val contacts = document.get("contacts") as? MutableList<Map<String, String>> ?: mutableListOf()

                    // Pass the MutableList to the RecyclerView setup function
                    setupRecyclerView(view, contacts)
                } else {
                    Log.d("MainActivity", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error fetching contacts: ${exception.message}")
            }
    }

    private fun setupRecyclerView(view: View, contacts: MutableList<Map<String, String>>) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.contactsRecyclerView)
        val adapter = ContactsAdapter(requireContext(), contacts)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


}

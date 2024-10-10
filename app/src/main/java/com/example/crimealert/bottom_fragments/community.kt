package com.example.crimealert.bottom_fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.example.crimealert.activity.CommunityChat
import com.example.crimealert.adapter.CommunityAdapter
import com.example.crimealert.model.community_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class community : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var communityAdapter: CommunityAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var communityList = mutableListOf<community_model>()
    private lateinit var createCommunityButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        searchView = view.findViewById(R.id.community_search_view)
        recyclerView = view.findViewById(R.id.community_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        createCommunityButton = view.findViewById(R.id.community_add_btn) // Reference to ImageButton
        createCommunityButton.setOnClickListener {
            showCreateCommunityDialog() // Show dialog to create a community
        }

        communityList = mutableListOf()

        communityAdapter = CommunityAdapter(communityList, { community ->
            checkIfUserJoinedCommunityOnClick(community)
        }, { community ->
            confirmUnjoinCommunity(community)
        })
        recyclerView.adapter = communityAdapter

        setupSearchView()
        loadJoinedCommunities()

        return view
    }

    private fun showCreateCommunityDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_community, null)
        val communityNameInput = dialogView.findViewById<EditText>(R.id.community_name_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Create Community")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val communityName = communityNameInput.text.toString().trim()
                if (communityName.isNotEmpty()) {
                    createCommunity(communityName)
                } else {
                    Toast.makeText(context, "Please enter a community name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun createCommunity(communityName: String) {
        val userEmail = auth.currentUser?.email ?: return
        val newCommunity = hashMapOf(
            "name" to communityName,
            "members" to listOf(userEmail),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("communities")
            .add(newCommunity)
            .addOnSuccessListener {
                Toast.makeText(context, "Community created successfully!", Toast.LENGTH_SHORT).show()
                loadJoinedCommunities()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error creating community: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfUserJoinedCommunityOnClick(community: community_model) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    val joinedCommunities = document.get("joined_community") as? List<String> ?: emptyList()
                    if (joinedCommunities.contains(community.name)) {
                        // User has already joined, navigate to chat
                        val intent = Intent(requireContext(), CommunityChat::class.java).apply {
                            putExtra("COMMUNITY_ID", community.id)
                            putExtra("COMMUNITY_NAME", community.name)
                        }
                        startActivity(intent)
                    } else {
                        // User has not joined, allow them to join
                        joinCommunity(community.name, it.email ?: "")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error checking user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun joinCommunity(communityName: String, userEmail: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(it.uid)
                .update("joined_community", FieldValue.arrayUnion(communityName))
                .addOnSuccessListener {
                    db.collection("communities").whereEqualTo("name", communityName).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("communities").document(document.id)
                                    .update("members", FieldValue.arrayUnion(userEmail))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Successfully joined $communityName", Toast.LENGTH_SHORT).show()
                                        loadJoinedCommunities()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Error adding user to community: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error retrieving community: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error joining community: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun confirmUnjoinCommunity(community: community_model) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unjoin Community")
            .setMessage("Are you sure you want to unjoin ${community.name}?")
            .setPositiveButton("Yes") { _, _ ->
                unjoinCommunity(community.name, auth.currentUser?.email ?: "")
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun unjoinCommunity(communityName: String, userEmail: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(it.uid)
                .update("joined_community", FieldValue.arrayRemove(communityName))
                .addOnSuccessListener {
                    db.collection("communities").whereEqualTo("name", communityName).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("communities").document(document.id)
                                    .update("members", FieldValue.arrayRemove(userEmail))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Successfully unjoined $communityName", Toast.LENGTH_SHORT).show()
                                        loadJoinedCommunities()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Error removing user from community: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error retrieving community: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error unjoining community: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val communityName = query ?: ""
                if (communityName.isNotEmpty()) {
                    searchForCommunity(communityName)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val communityName = newText ?: ""
                if (communityName.isNotEmpty()) {
                    searchForCommunity(communityName)
                } else {
                    loadJoinedCommunities()
                }
                return false
            }
        })
    }

    private fun searchForCommunity(communityName: String) {
        db.collection("communities")
            .orderBy("name")
            .startAt(communityName)
            .endAt(communityName + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    communityList.clear()
                    for (document in documents) {
                        val community = document.toObject(community_model::class.java)
                        communityList.add(community)
                    }
                    communityAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "No communities found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error searching for community: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadJoinedCommunities() {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(it.uid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(context, "Error fetching user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val joinedCommunities = it.get("joined_community") as? List<String> ?: emptyList()
                        fetchCommunities(joinedCommunities)
                    }
                }
        }
    }

    private fun fetchCommunities(joinedCommunities: List<String>) {
        if (joinedCommunities.isEmpty()) {
            Toast.makeText(context, "No joined communities to display", Toast.LENGTH_SHORT).show()
            communityList.clear()
            communityAdapter.notifyDataSetChanged()
            return
        }

        db.collection("communities").whereIn("name", joinedCommunities).addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                Toast.makeText(context, "Error fetching communities: ${exception.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            snapshots?.let {
                communityList.clear()
                for (document in it) {
                    val community = document.toObject(community_model::class.java)
                    communityList.add(community)
                }
                communityAdapter.notifyDataSetChanged()
            }
        }
    }
}

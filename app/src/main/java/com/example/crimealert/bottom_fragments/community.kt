package com.example.crimealert.bottom_fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.example.crimealert.activity.CommunityChat
import com.example.crimealert.adapter.CommunityAdapter
import com.example.crimealert.model.community_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [community.newInstance] factory method to
 * create an instance of this fragment.
 */
class community : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var searchButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var communityAdapter: CommunityAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var communityList = mutableListOf<community_model>()

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
        searchButton = view.findViewById(R.id.community_search_btn)
        searchEditText = view.findViewById(R.id.community_search_edittext)
        recyclerView = view.findViewById(R.id.community_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        communityList = mutableListOf()
        communityAdapter = CommunityAdapter(communityList){ community ->
            // Handle item click and navigate to CommunityChat activity
            val intent = Intent(requireContext(), CommunityChat::class.java).apply {
                putExtra("COMMUNITY_ID", community.id)
                putExtra("COMMUNITY_NAME", community.name)
            }
            startActivity(intent)}
        recyclerView.adapter = communityAdapter

        searchButton.setOnClickListener {
            val communityName = searchEditText.text.toString()
            if (communityName.isNotEmpty()) {
                searchForCommunity(communityName)
            } else {
                Toast.makeText(context, "Please enter a community name", Toast.LENGTH_SHORT).show()
            }
        }

        loadJoinedCommunities()

        return view
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

    private fun searchForCommunity(communityName: String) {
        db.collection("communities").whereEqualTo("name", communityName).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    checkIfUserJoinedCommunity(communityName)
                } else {
                    Toast.makeText(context, "Community not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error searching for community: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfUserJoinedCommunity(communityName: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    val joinedCommunities = document.get("joined_community") as? List<String> ?: emptyList()
                    if (joinedCommunities.contains(communityName)) {
                        Toast.makeText(context, "Already joined", Toast.LENGTH_SHORT).show()
                    } else {
                        joinCommunity(communityName, it.email ?: "")
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
                    addUserToCommunityMembers(communityName, userEmail)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error joining community: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addUserToCommunityMembers(communityName: String, userEmail: String) {
        db.collection("communities").whereEqualTo("name", communityName).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("communities").document(document.id)
                        .update("members", FieldValue.arrayUnion(userEmail))
                        .addOnSuccessListener {
                            Toast.makeText(context, "Successfully joined", Toast.LENGTH_SHORT).show()
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


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            community().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
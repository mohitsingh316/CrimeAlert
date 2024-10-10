package com.example.crimealert.bottom_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crimealert.adapter.PostsAdapter
import com.example.crimealert.interfaces.PostInteractionListener
import com.example.crimealert.model.Post
import com.example.crimealert.activity.CreatePostActivity
import com.example.crimealert.databinding.FragmentUpdatesBinding
import com.example.crimealert.repository.PostRepository
import com.example.crimealert.repository.UserRepository
import com.google.firebase.database.*

class updates : Fragment(), PostInteractionListener {

    private lateinit var binding: FragmentUpdatesBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var postsAdapter: PostsAdapter
    private var posts: ArrayList<Post> = ArrayList()
    private var postsReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("posts")
    private lateinit var postsListener: ValueEventListener

    // Repositories
    private val userRepository = UserRepository()
    private val postRepository = PostRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdatesBinding.inflate(inflater, container, false)

        binding.updatesRecyclerView.layoutManager = LinearLayoutManager(context)

        setupPostsListener()

        binding.fab.setOnClickListener {
            startActivity(Intent(activity, CreatePostActivity::class.java))
        }

        postsAdapter = PostsAdapter(posts, requireContext(), userRepository, postRepository, this)
        binding.updatesRecyclerView.adapter = postsAdapter

        return binding.root
    }

    // Method to set up the listener for post updates
    private fun setupPostsListener() {
        postsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                posts.clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }
                postsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load posts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        postsReference.addValueEventListener(postsListener)
    }

    // Remove the posts listener when the fragment is destroyed
    override fun onDestroy() {
        super.onDestroy()
        postsReference.removeEventListener(postsListener)
    }

    // Method to handle like button click
    override fun onLikeClick(post: Post) {
        Toast.makeText(context, "Liked post by ${post.userName}", Toast.LENGTH_SHORT).show()
    }

    // Method to handle comment button click
    override fun onCommentClick(post: Post) {
        Toast.makeText(context, "Comment on post by ${post.userName}", Toast.LENGTH_SHORT).show()
    }

    // Method to handle share button click
    override fun onShareClick(post: Post) {
        Toast.makeText(context, "Shared post by ${post.userName}", Toast.LENGTH_SHORT).show()
    }
}

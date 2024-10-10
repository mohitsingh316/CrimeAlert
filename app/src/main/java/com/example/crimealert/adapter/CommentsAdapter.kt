package com.example.crimealert.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.databinding.ItemCommentBinding
import com.example.crimealert.model.Comment
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsAdapter(
    private val comments: MutableList<Comment>, // Changed to MutableList for potential updates
    private val usersMap: Map<String, String>,
    private val context: Context
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            val userName = usersMap[comment.userId] ?: "Unknown"
            binding.commentUserName.text = userName
            binding.commentText.text = comment.text

            // Optional: Set the comment time if your Comment model has a timestamp
            val formattedTime = formatTimestamp(comment.timestamp)
            binding.commentTime.text = formattedTime
        }

        // Function to format timestamp into a readable format
        private fun formatTimestamp(timestamp: Long?): String {
            return timestamp?.let {
                val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                sdf.format(java.util.Date(it))
            } ?: "Unknown time"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    // Function to update the comments list and notify the adapter
    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}

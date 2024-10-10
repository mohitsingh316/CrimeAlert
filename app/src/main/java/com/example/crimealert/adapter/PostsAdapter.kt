package com.example.crimealert.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crimealert.R
import com.example.crimealert.activity.CommentsActivity
import com.example.crimealert.databinding.ItemPostBinding
import com.example.crimealert.interfaces.PostInteractionListener
import com.example.crimealert.model.Post
import com.example.crimealert.repository.PostRepository
import com.example.crimealert.repository.UserRepository
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PostsAdapter(
    private val posts: ArrayList<Post>,
    private val context: Context,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val postInteractionListener: PostInteractionListener
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private val activeHolders: MutableList<PostViewHolder> = mutableListOf()
    private var currentlyPlayingHolder: PostViewHolder? = null

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var exoPlayer: ExoPlayer? = null

        init {
            binding.updateLikeBtn.setOnClickListener {
                val post = posts[adapterPosition]
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val wasLiked = post.likesMap?.containsKey(userId) == true
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val updatedPost = postRepository.toggleLike(post.postId ?: "", userId)
                            withContext(Dispatchers.Main) {
                                if (updatedPost != null) {
                                    updatePostItem(adapterPosition, updatedPost)
                                    postInteractionListener.onLikeClick(updatedPost)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Revert UI update if the server-side update fails
                            withContext(Dispatchers.Main) {
                                binding.UpdateLikeCountTv.text = "${post.likeCount} Likes"
                                binding.updateLikeBtn.setImageResource(
                                    if (wasLiked) R.drawable.baseline_thumb_up_24
                                    else R.drawable.baseline_thumb_up_off_alt_24
                                )
                            }
                        }
                    }
                }
            }

            binding.updateCommentBtn.setOnClickListener {
                val post = posts[adapterPosition]
                val intent = Intent(context, CommentsActivity::class.java).apply {
                    putExtra("postId", post.postId)
                }
                context.startActivity(intent)
            }

            binding.updateShareBtn.setOnClickListener {
                val post = posts[adapterPosition]
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this post: ${post.postImage ?: post.postVideo}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }

        fun bind(post: Post) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userName = userRepository.getUserName(post.userId ?: "")
                    val userImage = userRepository.getUserImage(post.userId ?: "")
                    withContext(Dispatchers.Main) {
                        binding.updateUserNameTV.text = userName
                        Glide.with(context).load(userImage).into(binding.updateUsersImage)
                        binding.updatePostTextTV.text = post.postText ?: ""
                        updatePostContent(post)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun updatePostContent(post: Post) {
            when {
                !post.postImage.isNullOrEmpty() -> {
                    binding.updateFeedIV.visibility = View.VISIBLE
                    binding.updateFeedVideoView.visibility = View.GONE
                    Glide.with(context).load(post.postImage).into(binding.updateFeedIV)
                    adjustConstraintsForImage()
                }
                !post.postVideo.isNullOrEmpty() -> {
                    binding.updateFeedIV.visibility = View.GONE
                    binding.updateFeedVideoView.visibility = View.VISIBLE
                    exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                        binding.updateFeedVideoView.player = player
                        val mediaItem = MediaItem.fromUri(Uri.parse(post.postVideo))
                        player.setMediaItem(mediaItem)
                        player.prepare()
                    }
                    adjustConstraintsForVideo()
                }
                else -> {
                    binding.updateFeedIV.visibility = View.GONE
                    binding.updateFeedVideoView.visibility = View.GONE
                    resetConstraints()
                }
            }

            post.postTime?.let {
                val formattedTime = formatTimestamp(it)
                binding.updateTimeTV.text = formattedTime
            }

            binding.UpdateLikeCountTv.text = "${post.likeCount} Likes"
            updateLikeButton(post)
        }

        private fun adjustConstraintsForImage() {
            (binding.root as ConstraintLayout).apply {
                val params = binding.linearLayout.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.updateFeedIV.id
                binding.linearLayout.layoutParams = params
            }
        }

        private fun adjustConstraintsForVideo() {
            (binding.root as ConstraintLayout).apply {
                val params = binding.linearLayout.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.updateFeedVideoView.id
                binding.linearLayout.layoutParams = params
            }
        }

        private fun resetConstraints() {
            (binding.root as ConstraintLayout).apply {
                val params = binding.linearLayout.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.updateFeedIV.id
                binding.linearLayout.layoutParams = params
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(timestamp))
        }

        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isLiked = post.likesMap?.containsKey(currentUserId) == true
            binding.updateLikeBtn.setImageResource(
                if (isLiked) R.drawable.baseline_thumb_up_24
                else R.drawable.baseline_thumb_up_off_alt_24
            )
        }

        fun releasePlayer() {
            exoPlayer?.release()
            exoPlayer = null
        }

        fun checkAndPlayVideoIfVisible() {
            val rect = Rect()
            binding.root.getGlobalVisibleRect(rect)
            val visibleHeight = rect.height()
            val totalHeight = binding.root.height

            if (visibleHeight == totalHeight) {
                if (currentlyPlayingHolder != this) {
                    currentlyPlayingHolder?.exoPlayer?.pause()
                    currentlyPlayingHolder = this
                    exoPlayer?.seekTo(0) // Start video from the beginning
                    exoPlayer?.play()
                }
            } else {
                exoPlayer?.pause()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
        holder.checkAndPlayVideoIfVisible()
        activeHolders.add(holder)
    }

    override fun getItemCount(): Int = posts.size

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
        activeHolders.remove(holder)
        if (currentlyPlayingHolder == holder) {
            currentlyPlayingHolder = null
        }
    }

    override fun onViewAttachedToWindow(holder: PostViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.checkAndPlayVideoIfVisible()
    }

    override fun onViewDetachedFromWindow(holder: PostViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.exoPlayer?.pause()
        if (currentlyPlayingHolder == holder) {
            currentlyPlayingHolder = null
        }
    }

    fun releaseAllPlayers() {
        for (holder in activeHolders) {
            holder.releasePlayer()
        }
        activeHolders.clear()
        currentlyPlayingHolder = null
    }

    fun handleScroll() {
        for (holder in activeHolders) {
            holder.checkAndPlayVideoIfVisible()
        }
    }

    private fun updatePostItem(position: Int, updatedPost: Post?) {
        updatedPost?.let {
            posts[position] = it
            notifyItemChanged(position)
        }
    }

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}

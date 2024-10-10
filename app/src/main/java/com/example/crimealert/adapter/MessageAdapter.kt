package com.example.crimealert.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crimealert.databinding.ItemImageMessageBinding
import com.example.crimealert.databinding.ItemTextMessageBinding
import com.example.crimealert.databinding.ItemVideoMessageBinding
import com.example.crimealert.model.Message
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class MessageAdapter(
    private val messages: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when (message.mediaType) {
            "image" -> TYPE_IMAGE
            "video" -> TYPE_VIDEO
            else -> TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_IMAGE -> {
                val binding = ItemImageMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ImageMessageViewHolder(binding)
            }
            TYPE_VIDEO -> {
                val binding = ItemVideoMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                VideoMessageViewHolder(binding, parent.context)
            }
            else -> {
                val binding = ItemTextMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                TextMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is ImageMessageViewHolder -> holder.bind(message)
            is VideoMessageViewHolder -> holder.bind(message)
            is TextMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // ViewHolder for text messages
    inner class TextMessageViewHolder(private val binding: ItemTextMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.senderTextView.text = message.name
            binding.messageTextView.text = message.text
            binding.dateTextView.text = formatDate(message.timestamp)
        }
    }

    // ViewHolder for image messages
    inner class ImageMessageViewHolder(private val binding: ItemImageMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.senderTextView.text = message.name
            binding.dateTextView.text = formatDate(message.timestamp)

            // Use Glide to load the image
            Glide.with(binding.imageView.context)
                .load(message.mediaUrl)
                .into(binding.imageView)
        }
    }

    // ViewHolder for video messages
    inner class VideoMessageViewHolder(
        private val binding: ItemVideoMessageBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private var exoPlayer: ExoPlayer? = null

        fun bind(message: Message) {
            binding.senderTextView.text = message.name
            binding.dateTextView.text = formatDate(message.timestamp)

            // Initialize and play video using ExoPlayer
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build()
                binding.videoPlayer.player = exoPlayer
            }

            val mediaItem = MediaItem.fromUri(Uri.parse(message.mediaUrl))
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = false // Set to false to prevent auto-play
        }

        fun releasePlayer() {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    private fun formatDate(timestamp: Long): String {
        // Convert the timestamp to a readable format (optional)
        return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}

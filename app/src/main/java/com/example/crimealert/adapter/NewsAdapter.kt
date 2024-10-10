package com.example.crimealert.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.example.crimealert.model.NewsItem
import com.bumptech.glide.Glide

class NewsAdapter(
    private val newsList: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val newsTitleTextView: TextView = view.findViewById(R.id.newsTitleTextView)
        val newsDescriptionTextView: TextView = view.findViewById(R.id.newsDescriptionTextView)
        val newsImageView: ImageView = view.findViewById(R.id.newsImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.newsTitleTextView.text = newsItem.title
        holder.newsDescriptionTextView.text = newsItem.description

        // Load the image using Glide
        Glide.with(holder.itemView.context)
            .load(newsItem.imageUrl)
            .placeholder(R.drawable.baseline_newspaper_24) // Placeholder in case of an error
            .into(holder.newsImageView)

        // Set click listener
        holder.itemView.setOnClickListener { onItemClick(newsItem) }
    }

    override fun getItemCount() = newsList.size
}

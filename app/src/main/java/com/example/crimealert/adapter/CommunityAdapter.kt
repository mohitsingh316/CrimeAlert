package com.example.crimealert.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.databinding.CommunityItemBinding

import com.example.crimealert.model.community_model

class CommunityAdapter(
    private val communities: List<community_model>,
    private val onItemClick: (community_model) -> Unit,
    private val onItemLongClick: (community_model) -> Unit // Add a long click listener
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = CommunityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communities[position]
        holder.bind(community)
    }

    override fun getItemCount(): Int = communities.size

    inner class CommunityViewHolder(private val binding: CommunityItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(community: community_model) {
            binding.communityNameTxt.text = community.name
            binding.root.setOnClickListener { onItemClick(community) }

            // Handle long click for unjoining the community
            binding.root.setOnLongClickListener {
                onItemLongClick(community)
                true
            }
        }
    }
}


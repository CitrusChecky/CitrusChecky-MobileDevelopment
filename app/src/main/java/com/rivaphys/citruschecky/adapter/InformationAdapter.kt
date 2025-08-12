package com.rivaphys.citruschecky.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rivaphys.citruschecky.data.InformationItem
import com.rivaphys.citruschecky.databinding.ItemInformationBinding

class InformationAdapter : ListAdapter<InformationItem, InformationAdapter.InformationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationViewHolder {
        val binding = ItemInformationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InformationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InformationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class InformationViewHolder(private val binding: ItemInformationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InformationItem) {
            binding.apply {
                tvItemInformation.text = item.title
                tvDescInformation.text = item.description
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<InformationItem>() {
            override fun areItemsTheSame(oldItem: InformationItem, newItem: InformationItem): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: InformationItem, newItem: InformationItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
package com.rivaphys.citruschecky.adapter

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rivaphys.citruschecky.data.DetectionResult
import com.rivaphys.citruschecky.databinding.ItemHasilDeteksiBinding

class DetectionResultAdapter(
    private val detectionResults: List<DetectionResult>
) : RecyclerView.Adapter<DetectionResultAdapter.DetectionViewHolder>() {

    class DetectionViewHolder(private val binding: ItemHasilDeteksiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: DetectionResult, position: Int) {
            binding.tvTandaItemHasilDeteksi.text = "${position + 1}."
            binding.tvHasilDeteksiItemDeteksi.text = result.className
            binding.tvConfidenceItemDeteksi.text = "${(result.confidence * 100).toInt()}%"

            if (!result.capturedImagePath.isNullOrEmpty()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(result.capturedImagePath)
                    Log.d("DetectionAdapter", "Captured image available for ${result.className}")
                } catch (e: Exception) {
                    Log.e("DetectionAdapter", "Error loading captured image", e)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val binding = ItemHasilDeteksiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DetectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        holder.bind(detectionResults[position], position)
    }

    override fun getItemCount(): Int = detectionResults.size
}
package com.example.qrscanner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.data.ScanRecord
import com.example.qrscanner.databinding.ItemScanBinding

class ScanAdapter : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {

    private val items = mutableListOf<ScanRecord>()

    fun updateItems(newItems: List<ScanRecord>) {
        items.clear()
        items.addAll(newItems.takeLast(10).reversed())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val binding = ItemScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount() = items.size

    class ScanViewHolder(private val binding: ItemScanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ScanRecord, number: Int) {
            binding.tvNumber.text = number.toString()
            binding.tvCellId.text = record.cellId
            binding.tvTimestamp.text = record.getFormattedTime()
        }
    }
}

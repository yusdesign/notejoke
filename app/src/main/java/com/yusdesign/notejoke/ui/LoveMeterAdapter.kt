package com.yusdesign.notejoke.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yusdesign.notejoke.data.LoveMeter
import com.yusdesign.notejoke.databinding.ItemLoveMeterCardBinding
import java.text.SimpleDateFormat
import java.util.*

class LoveMeterAdapter(private var entries: List<LoveMeter> = emptyList()) :
    RecyclerView.Adapter<LoveMeterAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK)

    class ViewHolder(val binding: ItemLoveMeterCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoveMeterCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.binding.apply {
            tvStatus.text = entry.status
            tvChance.text = "${entry.chance}%"
            tvTimestamp.text = dateFormat.format(entry.timestamp)
            tvId.text = entry.id
        }
    }

    override fun getItemCount() = entries.size

    fun updateData(newEntries: List<LoveMeter>) {
        entries = newEntries.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}

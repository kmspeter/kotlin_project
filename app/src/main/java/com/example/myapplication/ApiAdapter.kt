package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.TeamScheduleItemBinding

class ApiAdapter : RecyclerView.Adapter<ApiAdapter.ViewHolder>() {

    var data: List<MatchSchedule> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(private val binding: TeamScheduleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(matchSchedule: MatchSchedule) {
            binding.matchInfo.text =
                "${matchSchedule.team1} vs ${matchSchedule.team2} (${matchSchedule.date} ${matchSchedule.time})"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TeamScheduleItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val matchSchedule = data[position]
        holder.bind(matchSchedule)
    }

    override fun getItemCount(): Int = data.size
}
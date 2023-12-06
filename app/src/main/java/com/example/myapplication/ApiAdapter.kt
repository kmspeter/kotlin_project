package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.TeamScheduleItemBinding

class ApiAdapter : RecyclerView.Adapter<ApiAdapter.ViewHolder>() {

    // 일정 데이터를 저장하는 리스트
    var data: List<MatchSchedule> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // ViewHolder 정의
    inner class ViewHolder(private val binding: TeamScheduleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(matchSchedule: MatchSchedule) {
            binding.matchInfo.text =
                "${matchSchedule.team1} vs ${matchSchedule.team2} (${matchSchedule.date} ${matchSchedule.time})"
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TeamScheduleItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val matchSchedule = data[position]
        holder.bind(matchSchedule)
    }

    // 데이터 수 반환
    override fun getItemCount(): Int = data.size
}
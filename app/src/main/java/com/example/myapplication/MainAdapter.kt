package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ScheduleItemBinding

class MainAdapter : ListAdapter<Schedule, MainAdapter.ViewHolder>(ScheduleDiffCallback()) {

    // ViewHolder 정의
    class ViewHolder(private val binding: ScheduleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule: Schedule) {
            binding.contentTextView.text = schedule.content
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
    }

    // DiffUtil을 사용하여 일정 변경 여부 확인
    private class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }
}
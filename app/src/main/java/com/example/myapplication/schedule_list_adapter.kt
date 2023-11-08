package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ScheduleListFormBinding

class schedule_list_adapter(val schedule_data_form: Array<Schedule_data>): RecyclerView.Adapter<schedule_list_adapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ScheduleListFormBinding.inflate(LayoutInflater.from(parent.context))
        return Holder(binding)
    }


    override fun getItemCount() = schedule_data_form.size


    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(schedule_data_form[position])
    }

    class Holder(private val binding: ScheduleListFormBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(schedule_data: Schedule_data){
            binding.imageView.setImageResource(R.drawable.calendar)
            binding.txtschedule.text = schedule_data.schedule
            binding.txtday.text = schedule_data.day
        }
    }

}
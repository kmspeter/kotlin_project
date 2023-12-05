package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.database.*

data class Schedule(val date: String = "", val content: String = "")
class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val scheduleList = mutableListOf<Schedule>()
    private val adapter = ScheduleAdapter()

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val api = Intent(this, ApiActivity::class.java)
        binding.apiButton.setOnClickListener { startActivity(api) }

        val search_view = Intent(this, SearchViewActivity::class.java)
        binding.searchButton.setOnClickListener { startActivity(search_view) }

        val calendarView = binding.calendarView
        val simpleView = binding.simpleView
        val listButton = binding.listButton

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = formatDateWithZeroPadding("$year-${month + 1}-$dayOfMonth")
            // 특정 날짜에 일정이 없는 경우 scheduleList를 비우도록 수정
            scheduleList.clear()
            loadScheduleFromFirebase(selectedDate)
        }
    }

    private fun loadScheduleFromFirebase(date: String) {
        val formattedDate = formatDateWithZeroPadding(date)
        val scheduleRef = database.reference.child("schedules").child(formattedDate)

        scheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scheduleList.clear() // 기존 목록 비우기

                val content = dataSnapshot.child("content").value?.toString()
                Log.d("FirebaseData", "Content from Firebase: $content")

                if (content != null) {
                    val schedule = Schedule(formattedDate, content)
                    scheduleList.add(schedule)

                    // 데이터 갱신
                    adapter.notifyDataSetChanged()
                    binding.simpleView.text = "" // 리사이클러뷰에 데이터가 있으면 텍스트 뷰 내용 비우기
                } else {
                    // 일정이 없는 경우 처리
                    scheduleList.clear() // 리사이클러뷰에 표시된 데이터 제거
                    adapter.notifyDataSetChanged()
                    binding.simpleView.text = "일정이 없습니다.\n여기를 눌러서 일정을 추가해주세요"
                    binding.simpleView.setOnClickListener {
                        showAddScheduleDialog(formattedDate)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
                Toast.makeText(this@MainActivity, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddScheduleDialog(date: String) {
        val editText = EditText(this)
        editText.hint = "일정을 입력하세요."

        val dialog = AlertDialog.Builder(this)
            .setTitle("일정 추가")
            .setView(editText)
            .setPositiveButton("추가") { _, _ ->
                val content = editText.text.toString().trim()

                if (content.isNotEmpty()) {
                    // 0을 붙이도록 수정
                    val formattedDate = formatDateWithZeroPadding(date)
                    val scheduleRef = database.reference.child("schedules").child(formattedDate)
                    val newSchedule = Schedule(formattedDate, content)
                    scheduleRef.setValue(newSchedule)

                    Toast.makeText(this@MainActivity, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    private fun clearSchedule() {
        binding.simpleView.text = "일정이 없습니다."
    }

    private fun formatDateWithZeroPadding(date: String): String {
        // 날짜를 0을 붙이도록 변환하는 함수
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1].padStart(2, '0') // 0을 붙이도록 처리
        val day = parts[2].padStart(2, '0') // 0을 붙이도록 처리
        return "$year-$month-$day"
    }

    inner class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val schedule = scheduleList[position]
            Log.d("ScheduleAdapter", "onBindViewHolder: position $position, content: ${schedule.content}")
            holder.contentTextView.text = schedule.content
        }

        override fun getItemCount(): Int {
            return scheduleList.size
        }
    }
}
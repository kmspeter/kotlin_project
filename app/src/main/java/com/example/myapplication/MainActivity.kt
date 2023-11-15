package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

data class Schedule(val date: String, val content: String)
class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
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

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            loadScheduleFromFirebase(selectedDate)
        }
    }

    private fun loadScheduleFromFirebase(date: String) {
        val scheduleRef = database.reference.child("schedules").child(date)

        scheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val contentValue = dataSnapshot.child("content").value
                val content = contentValue?.toString()

                if (content != null) {
                    // 달력에 일정 표시
                    binding.simpleView.text = content

                } else {
                    // 일정이 없는 경우 처리
                    binding.simpleView.text = "일정이 없습니다.\n여기를 눌러서 일정을 추가해주세요"
                    binding.simpleView.setOnClickListener {
                        showAddScheduleDialog(date)
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
                    val scheduleRef = database.reference.child("schedules").child(date)
                    val newSchedule = Schedule(date, content)
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
}
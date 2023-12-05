package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

data class Schedule(val date: String = "", val content: String = "")

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private val adapter = MainAdapter()

    private lateinit var binding: ActivityMainBinding // View Binding 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // View Binding 초기화
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = DateZero.formatDateWithZeroPadding("$year-${month + 1}-$dayOfMonth")
            viewModel.loadSchedule(selectedDate)
        }

        viewModel.schedule.observe(this) { schedule ->
            if (schedule != null) {
                binding.simpleView.text = ""
                adapter.submitList(listOf(schedule))
            } else {
                binding.simpleView.text = "일정이 없습니다.\n여기를 눌러서 일정을 추가해주세요"
                binding.simpleView.setOnClickListener {
                    showAddScheduleDialog(binding.calendarView.date) // 수정된 부분
                }
                adapter.submitList(emptyList())
            }
        }

        val api = Intent(this, ApiActivity::class.java)
        binding.apiButton.setOnClickListener { startActivity(api) }

        val searchView = Intent(this, SearchViewActivity::class.java)
        binding.searchButton.setOnClickListener { startActivity(searchView) }
    }

    private fun showAddScheduleDialog(date: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        val editText = EditText(this)
        editText.hint = "일정을 입력하세요."

        val dialog = AlertDialog.Builder(this)
            .setTitle("일정 추가")
            .setView(editText)
            .setPositiveButton("추가") { _, _ ->
                val content = editText.text.toString().trim()

                if (content.isNotEmpty()) {
                    viewModel.addSchedule(formattedDate, content)
                    Toast.makeText(this@MainActivity, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }
}
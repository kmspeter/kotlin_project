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

    // ViewModel 및 RecyclerView 어댑터 초기화
    private lateinit var viewModel: MainViewModel
    private val adapter = MainAdapter()

    // View Binding 추가
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // RecyclerView 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // CalendarView의 선택된 날짜 변환후 일정 로드
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = DateZero.formatDateWithZeroPadding("$year-${month + 1}-$dayOfMonth")
            viewModel.loadSchedule(selectedDate)
        }

        // Schedule 데이터가 변경되면 UI 갱신
        viewModel.schedule.observe(this) { schedule ->
            if (schedule != null) {
                binding.simpleView.text = ""
                adapter.submitList(listOf(schedule))
            } else {
                // 일정이 없으면 메시지 표시 및 클릭시 창 열기
                binding.simpleView.text = "일정이 없습니다.\n여기를 눌러서 일정을 추가해주세요"
                binding.simpleView.setOnClickListener {
                    showAddScheduleDialog(binding.calendarView.date)
                }
                adapter.submitList(emptyList())
            }
        }

        // API 및 검색 화면으로 이동
        val api = Intent(this, ApiActivity::class.java)
        binding.apiButton.setOnClickListener { startActivity(api) }

        val searchView = Intent(this, SearchViewActivity::class.java)
        binding.searchButton.setOnClickListener { startActivity(searchView) }
    }

    // 일정 추가 창 표시
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
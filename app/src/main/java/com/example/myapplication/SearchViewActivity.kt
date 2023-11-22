package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityScheduleListBinding
import com.example.myapplication.databinding.ActivitySearchViewBinding

class SearchViewActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchViewBinding

    val schedule_datas = arrayOf(
        Schedule_data("도르트문트vs뉴캐슬","9월 29일"),
        Schedule_data("샤흐타르vs바르셀로나","10월 3일"),
        Schedule_data("라치오vs페예노르트","10월 9일"),
        Schedule_data("AC밀란vs파리 생제르맹","12월 25일")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recschedule.layoutManager = LinearLayoutManager(this)
        binding.recschedule.adapter = schedule_list_adapter(schedule_datas)

        val main = Intent(this, MainActivity::class.java)
        binding.searchBack.setOnClickListener { startActivity(main) }
    }

    private fun initSearchView() {
        // init SearchView
        binding.search.isSubmitButtonEnabled = true
        binding.search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // @TODO
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // @TODO
                return true
            }
        })
    }
}
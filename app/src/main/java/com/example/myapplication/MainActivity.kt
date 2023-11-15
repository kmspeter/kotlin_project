package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase


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

        val schedule = Intent(this, ScheduleListActivity::class.java)
        binding.addButton.setOnClickListener { startActivity(schedule) }
    }
}
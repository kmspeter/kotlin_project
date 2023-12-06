package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityApiBinding
import androidx.lifecycle.ViewModelProvider

data class MatchSchedule(
    val date: String = "",
    val time: String = "",
    val team1: String = "",
    val team2: String = ""
)

data class Team(
    val name: String = ""
)


class ApiActivity : AppCompatActivity() {

    private val binding by lazy { ActivityApiBinding.inflate(layoutInflater) }
    private lateinit var apiViewModel: ApiViewModel
    private val adapter = ApiAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = Intent(this, MainActivity::class.java)
        binding.apiBack.setOnClickListener { startActivity(intent) }

        // Initialize ViewModel
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        binding.apiSelect.setOnClickListener {
            showTeamSelectionDialog()
        }

        binding.apiLoad.setOnClickListener {
            val selectedTeam = binding.team.text.toString()
            if (selectedTeam.isNotEmpty()) {
                // Load matches using ViewModel
                apiViewModel.loadMatches(selectedTeam, object : ApiViewModel.ScheduleCallback {
                    override fun onMatchesLoaded(matches: List<MatchSchedule>, error: String?) {
                        if (error == null) {
                            showSelectedTeamScheduleDialog(selectedTeam, matches)
                        } else {
                            Toast.makeText(this@ApiActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } else {
                Toast.makeText(this, "팀을 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.teamRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.teamRecyclerView.adapter = adapter
    }

    private fun showTeamSelectionDialog() {
        apiViewModel.loadTeams(object : ApiViewModel.TeamCallback {
            override fun onTeamsLoaded(teams: List<String>, error: String?) {
                if (error == null) {
                    showTeamSelectionDialog(teams)
                } else {
                    Toast.makeText(this@ApiActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showTeamSelectionDialog(teams: List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("팀 선택")
            .setItems(teams.toTypedArray()) { _, which ->
                val selectedTeam = teams[which]
                binding.team.text = selectedTeam
                apiViewModel.loadMatches(selectedTeam, object : ApiViewModel.ScheduleCallback {
                    override fun onMatchesLoaded(matches: List<MatchSchedule>, error: String?) {
                        if (error == null) {
                            showSelectedTeamSchedule(selectedTeam, matches)
                        } else {
                            Toast.makeText(this@ApiActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        builder.show()
    }
    private fun showSelectedTeamSchedule(selectedTeam: String, schedules: List<MatchSchedule>) {
        adapter.data = schedules
    }
    private fun showSelectedTeamScheduleDialog(selectedTeam: String, schedules: List<MatchSchedule>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$selectedTeam 일정")

        if (schedules.isNotEmpty()) {
            builder.setMessage(schedules.joinToString("\n") {
                "${it.team1} vs ${it.team2} (${it.date} ${it.time})"
            })
        } else {
            builder.setMessage("일정이 없습니다.")
        }

        builder.setPositiveButton("확인") { _, _ ->
            apiViewModel.saveSelectedTeamSchedule(schedules) { success ->
                if (success) {
                    Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "일정 추가 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("취소", null)
        builder.show()

        adapter.data = schedules
    }
}
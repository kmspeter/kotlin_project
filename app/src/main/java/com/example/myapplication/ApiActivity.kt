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

    // View Binding 사용
    private val binding by lazy { ActivityApiBinding.inflate(layoutInflater) }

    // ViewModel 및 RecyclerView 어댑터 초기화
    private lateinit var apiViewModel: ApiViewModel
    private val adapter = ApiAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // MainActivity로 이동
        val intent = Intent(this, MainActivity::class.java)
        binding.apiBack.setOnClickListener { startActivity(intent) }

        // ViewModel 초기화
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        // apiSelect 버튼 클릭 시
        binding.apiSelect.setOnClickListener {
            showTeamSelectionDialog()
        }

        // apiLoad 버튼 클릭 시
        binding.apiLoad.setOnClickListener {
            val selectedTeam = binding.team.text.toString()
            if (selectedTeam.isNotEmpty()) {
                // ViewModel을 사용하여 일정 불러오기
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

        // RecyclerView 설정
        binding.teamRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.teamRecyclerView.adapter = adapter
    }

    // 팀 목록 로드
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

    // 로드된 팀 선택
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

    // 선택된 팀의 일정을 RecyclerView에 표시
    private fun showSelectedTeamSchedule(selectedTeam: String, schedules: List<MatchSchedule>) {
        adapter.data = schedules
    }

    // 선택된 팀의 일정을 창으로 표시 및 일정 추가
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
    }
}
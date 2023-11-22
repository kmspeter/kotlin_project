package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityApiBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale

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
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val teamsRef: DatabaseReference = database.getReference("teams")
    private val matchesRef: DatabaseReference = database.getReference("matches")
    private val schedulesRef: DatabaseReference = database.getReference("schedules")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = Intent(this, MainActivity::class.java)
        binding.apiBack.setOnClickListener { startActivity(intent) }

        binding.apiSelect.setOnClickListener {
            showTeamSelectionDialog()
        }

        binding.apiLoad.setOnClickListener {
            val selectedTeam = binding.team.text.toString()
            if (selectedTeam.isNotEmpty()) {
                showSelectedTeamScheduleDialog(selectedTeam)
            } else {
                // Handle the case where no team is selected
                Toast.makeText(this, "팀을 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTeamSelectionDialog() {
        teamsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val teams = dataSnapshot.children.mapNotNull { it.getValue(Team::class.java) }
                    .map { it.name }
                    .toTypedArray()

                showTeamSelectionDialog(teams)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.e("TeamSelection", "Error fetching teams: ${databaseError.message}")
            }
        })
    }

    private fun showTeamSelectionDialog(teams: Array<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("팀 선택")
            .setItems(teams) { _, which ->
                val selectedTeam = teams[which]
                binding.team.text = selectedTeam
                showMatchSchedule(selectedTeam)
            }
        builder.show()
    }

    private fun showMatchSchedule(selectedTeam: String) {
        matchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allMatches = mutableListOf<MatchSchedule>()

                for (roundSnapshot in dataSnapshot.children) {
                    for (matchSnapshot in roundSnapshot.children) {
                        val match = matchSnapshot.getValue(MatchSchedule::class.java)
                        if (match != null) {
                            allMatches.add(match)
                        }
                    }
                }

                Log.d("MatchSchedule", "All Matches: $allMatches")

                val team1Matches = allMatches.filter { it.team1 == selectedTeam }
                val team2Matches = allMatches.filter { it.team2 == selectedTeam }

                Log.d("TeamSchedule", "Team1 Matches: $team1Matches")
                Log.d("TeamSchedule", "Team2 Matches: $team2Matches")

                saveSelectedTeamSchedule(team1Matches + team2Matches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MatchSchedule", "Error fetching matches: ${databaseError.message}")
            }
        })
    }

    private fun saveSelectedTeamSchedule(matches: List<MatchSchedule>) {
        val selectedTeam = binding.team.text.toString()

        if (selectedTeam.isNotEmpty() && matches.isNotEmpty()) {
            val scheduleMap = mutableMapOf<String, Map<String, String>>()

            for (match in matches) {
                val formattedDate = formatDate(match.date, match.time)
                val content = "${match.team1} vs ${match.team2} ($formattedDate)"

                val scheduleItem = mapOf(
                    "content" to content,
                    "date" to formattedDate.split(" ")[0] // 형식 변환된 날짜 문자열에서 날짜 추출
                )

                val dateWithoutTime = formattedDate.split(" ")[0]
                val dateWithZeroPadding = formatDateWithZeroPadding(dateWithoutTime)
                scheduleMap[dateWithZeroPadding] = scheduleItem
            }

            binding.matches.text = scheduleMap.values.joinToString("\n") { it["content"].toString() }

            binding.apiLoad.setOnClickListener {
                schedulesRef.setValue(scheduleMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "일정 추가 실패", Toast.LENGTH_SHORT).show()
                        Log.e("SaveSchedule", "일정 저장 오류: $it")
                    }
            }
        }
    }

    private fun formatDate(date: String, time: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = originalFormat.parse(date)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(parsedDate)

        return "$formattedDate ($time)"
    }

    private fun formatDateWithZeroPadding(date: String): String {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1].padStart(2, '0')
        val day = parts[2].padStart(2, '0')
        return "$year-$month-$day"
    }

    private fun showSelectedTeamScheduleDialog(selectedTeam: String) {
        matchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allMatches = mutableListOf<MatchSchedule>()

                for (roundSnapshot in dataSnapshot.children) {
                    for (matchSnapshot in roundSnapshot.children) {
                        val match = matchSnapshot.getValue(MatchSchedule::class.java)
                        if (match != null) {
                            allMatches.add(match)
                        }
                    }
                }

                Log.d("MatchSchedule", "모든 매치: $allMatches")

                val team1Matches = allMatches.filter { it.team1 == selectedTeam }
                val team2Matches = allMatches.filter { it.team2 == selectedTeam }

                Log.d("TeamSchedule", "팀 1 매치: $team1Matches")
                Log.d("TeamSchedule", "팀 2 매치: $team2Matches")

                saveSelectedTeamSchedule(team1Matches + team2Matches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TeamSchedule", "데이터 검색 오류: ${databaseError.message}")
            }
        })
    }
}
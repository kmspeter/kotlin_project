package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityApiBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.TeamScheduleItemBinding

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

    private val scheduleList = mutableListOf<MatchSchedule>()
    private val adapter = MatchScheduleAdapter()

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

        binding.teamRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.teamRecyclerView.adapter = adapter // adapter 설정
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

                // 로그로 확인
                Log.d("MatchSchedule", "All Matches: $allMatches")

                // 선택한 팀과 관련된 일정을 표시
                val team1Matches = allMatches.filter { it.team1 == selectedTeam }
                val team2Matches = allMatches.filter { it.team2 == selectedTeam }

                // 로그로 확인
                Log.d("TeamSchedule", "Team1 Matches: $team1Matches")
                Log.d("TeamSchedule", "Team2 Matches: $team2Matches")

                // saveSelectedTeamSchedule 함수 호출
                saveSelectedTeamSchedule(team1Matches + team2Matches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.e("MatchSchedule", "Error fetching matches: ${databaseError.message}")
            }
        })
    }

    private fun saveSelectedTeamSchedule(matches: List<MatchSchedule>) {
        val selectedTeam = binding.team.text.toString()

        if (selectedTeam.isNotEmpty() && matches.isNotEmpty()) {
            val scheduleMap = mutableMapOf<String, Map<String, String>>()

            // MatchSchedule 객체를 원하는 구조로 변환
            for (match in matches) {
                val formattedDate = formatDate(match.date, match.time)
                val content = "${match.team1} vs ${match.team2} ($formattedDate)"

                val scheduleItem = mapOf(
                    "content" to content,
                    "date" to formattedDate.split(" ")[0] // 형식 변환된 날짜 문자열에서 날짜 추출
                )

                // 기존 시간 정보를 제거하고 "2023-11-26"과 같은 형식으로 저장
                val dateWithoutTime = formattedDate.split(" ")[0]
                // 0을 붙이도록 수정
                val dateWithZeroPadding = formatDateWithZeroPadding(dateWithoutTime)
                scheduleMap[dateWithZeroPadding] = scheduleItem
            }

            // "확인" 버튼을 눌렀을 때만 Firebase에 변환된 scheduleMap 저장
            binding.apiLoad.setOnClickListener {
                val updates = mutableMapOf<String, Any>()
                updates.putAll(scheduleMap) // 새로운 데이터를 추가
                schedulesRef.updateChildren(updates)
                    .addOnSuccessListener {
                        // 성공 시 처리
                        Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // 실패 시 처리
                        Toast.makeText(this, "일정 추가 실패", Toast.LENGTH_SHORT).show()
                        Log.e("SaveSchedule", "일정 저장 오류: $it")
                    }
            }

            scheduleList.clear()
            scheduleList.addAll(matches)
            adapter.notifyDataSetChanged()
        }
    }

    private fun formatDate(date: String, time: String): String {
        // 날짜와 시간을 특정한 형식으로 변환하는 함수
        // 여기에서는 "yyyy-MM-dd HH:mm" 형식으로 변환하였습니다.
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = originalFormat.parse(date)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(parsedDate)

        return "$formattedDate ($time)"
    }

    private fun formatDateWithZeroPadding(date: String): String {
        // 날짜를 0을 붙이도록 변환하는 함수
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1].padStart(2, '0') // 0을 붙이도록 처리
        val day = parts[2].padStart(2, '0') // 0을 붙이도록 처리
        return "$year-$month-$day"
    }

    private fun showSelectedTeamScheduleDialog(selectedTeam: String) {
        matchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allMatches = mutableListOf<MatchSchedule>()
                scheduleList.clear()

                for (roundSnapshot in dataSnapshot.children) {
                    for (matchSnapshot in roundSnapshot.children) {
                        val match = matchSnapshot.getValue(MatchSchedule::class.java)
                        if (match != null) {
                            allMatches.add(match)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MatchSchedule", "Error fetching matches: ${databaseError.message}")
            }
        })
    }

    inner class MatchScheduleAdapter : RecyclerView.Adapter<MatchScheduleAdapter.ViewHolder>() {

        inner class ViewHolder(private val binding: TeamScheduleItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(matchSchedule: MatchSchedule) {
                binding.matchInfo.text = "${matchSchedule.team1} vs ${matchSchedule.team2} (${formatDate(matchSchedule.date, matchSchedule.time)})"
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = TeamScheduleItemBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val matchSchedule = scheduleList[position]
            holder.bind(matchSchedule)
        }

        override fun getItemCount(): Int = scheduleList.size
    }
}
package com.example.myapplication

import com.google.firebase.database.*

class ApiRepository {

    // 경기 일정 인터페이스
    interface MatchesCallback {
        fun onMatchesLoaded(matches: List<MatchSchedule>)
        fun onError(error: String)
    }

    // 모든 팀 인터페이스
    interface TeamsCallback {
        fun onTeamsLoaded(teams: List<String>)
        fun onError(error: String)
    }

    // Firebase에서 경기 일정 로드
    fun loadMatches(selectedTeam: String, callback: MatchesCallback) {
        val matchesRef = FirebaseDatabase.getInstance().reference.child("matches")

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

                // 선택된 팀과 관련된 경기 일정만 필터링
                val teamMatches = allMatches.filter { it.team1 == selectedTeam || it.team2 == selectedTeam }

                callback.onMatchesLoaded(teamMatches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError.message)
            }
        })
    }

    // Firebase에서 팀 목록 로드
    fun loadTeams(callback: TeamsCallback) {
        val teamsRef = FirebaseDatabase.getInstance().reference.child("teams")

        teamsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val teams = dataSnapshot.children.mapNotNull {
                    it.child("name").getValue(String::class.java)
                }

                callback.onTeamsLoaded(teams)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError.message)
            }
        })
    }

    // 팀 일정 저장
    fun saveSelectedTeamSchedule(matches: List<MatchSchedule>, callback: (Boolean) -> Unit) {
        val schedulesRef = FirebaseDatabase.getInstance().reference.child("schedules")

        // MatchSchedule 객체를 일정에 맞는 구조로 변환
        val scheduleMap = mutableMapOf<String, Any>()

        for (match in matches) {
            val formattedDate = DateZero.formatDate(match.date, match.time)
            val content = "${match.team1} vs ${match.team2} ($formattedDate)"

            val scheduleItem = mapOf(
                "content" to content,
                "date" to formattedDate.split(" ")[0]
            )

            scheduleMap[scheduleItem["date"] as String] = scheduleItem
        }

        // Firebase를 업데이트하여 새로운 일정 데이터를 추가
        schedulesRef.updateChildren(scheduleMap)
            .addOnSuccessListener {
                // 성공
                callback(true)
            }
            .addOnFailureListener {
                // 실패
                callback(false)
            }
    }
}
package com.example.myapplication

import com.google.firebase.database.*

class ApiRepository {

    interface MatchesCallback {
        fun onMatchesLoaded(matches: List<MatchSchedule>)
        fun onError(error: String)
    }

    interface TeamsCallback {
        fun onTeamsLoaded(teams: List<String>)
        fun onError(error: String)
    }

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

                // Filter matches for the selected team
                val teamMatches = allMatches.filter { it.team1 == selectedTeam || it.team2 == selectedTeam }

                callback.onMatchesLoaded(teamMatches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError.message)
            }
        })
    }

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

    fun saveSelectedTeamSchedule(matches: List<MatchSchedule>, callback: (Boolean) -> Unit) {
        val schedulesRef = FirebaseDatabase.getInstance().reference.child("schedules")

        // Transform MatchSchedule objects into the desired structure
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

        // Update Firebase with the new schedule data without overwriting existing data
        schedulesRef.updateChildren(scheduleMap)
            .addOnSuccessListener {
                // Success callback
                callback(true)
            }
            .addOnFailureListener {
                // Failure callback
                callback(false)
            }
    }
}
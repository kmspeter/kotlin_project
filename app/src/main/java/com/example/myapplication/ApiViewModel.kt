package com.example.myapplication

import androidx.lifecycle.ViewModel

class ApiViewModel(private val repository: ApiRepository) : ViewModel() {

    interface TeamCallback {
        fun onTeamsLoaded(teams: List<String>, error: String?)
    }

    interface ScheduleCallback {
        fun onMatchesLoaded(matches: List<MatchSchedule>, error: String?)
    }

    fun loadTeams(callback: TeamCallback) {
        repository.loadTeams(object : ApiRepository.TeamsCallback {
            override fun onTeamsLoaded(teams: List<String>) {
                callback.onTeamsLoaded(teams, null)
            }

            override fun onError(error: String) {
                callback.onTeamsLoaded(emptyList(), error)
            }
        })
    }

    fun loadMatches(selectedTeam: String, callback: ScheduleCallback) {
        repository.loadMatches(selectedTeam, object : ApiRepository.MatchesCallback {
            override fun onMatchesLoaded(matches: List<MatchSchedule>) {
                callback.onMatchesLoaded(matches, null)
            }

            override fun onError(error: String) {
                callback.onMatchesLoaded(emptyList(), error)
            }
        })
    }

    fun saveSelectedTeamSchedule(matches: List<MatchSchedule>, callback: (Boolean) -> Unit) {
        repository.saveSelectedTeamSchedule(matches) { success ->
            callback(success)
        }
    }

    constructor() : this(ApiRepository())
}
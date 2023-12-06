package com.example.myapplication

import androidx.lifecycle.ViewModel

class ApiViewModel(private val repository: ApiRepository) : ViewModel() {

    // 팀 인터페이스
    interface TeamCallback {
        fun onTeamsLoaded(teams: List<String>, error: String?)
    }

    // 일정 인터페이스
    interface ScheduleCallback {
        fun onMatchesLoaded(matches: List<MatchSchedule>, error: String?)
    }

    // 팀 로드
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

    // 일정 로드
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

    // 선택된 팀의 일정 저장
    fun saveSelectedTeamSchedule(matches: List<MatchSchedule>, callback: (Boolean) -> Unit) {
        repository.saveSelectedTeamSchedule(matches) { success ->
            callback(success)
        }
    }

    //기본 생성자
    constructor() : this(ApiRepository())
}
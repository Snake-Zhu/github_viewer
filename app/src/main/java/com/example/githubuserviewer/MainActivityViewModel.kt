package com.example.githubuserviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubuserviewer.usecase.MainActivityUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {
    private val _state = MutableStateFlow(MainActivityState(null, emptyList()))
    val state = _state.asStateFlow()
    private val useCase = MainActivityUseCase()

    private fun currentState() = _state.value
    private fun updateState(newState: () -> MainActivityState) {
        _state.value = newState()
    }

    fun changeQueryUserName(userName: String?) {
        viewModelScope.launch {
            updateState {
                val currentState = currentState()
                MainActivityState(userName, currentState.userList, currentState.nextLink)
            }
        }
    }

    fun queryUser() {
        viewModelScope.launch(Dispatchers.Default) {
            currentState().queryName?.let { queryUserName ->
                updateState {
                    val userListReturnType = useCase.getUserList(queryUserName)
                    MainActivityState(queryUserName, userListReturnType.items, userListReturnType.nextPage)
                }
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch(Dispatchers.Default) {
            currentState().nextLink?.let { nextLink ->
                updateState {
                    val newList = currentState().userList.toMutableList()
                    val userListReturnType = useCase.loadNextPage(nextLink)
                    newList.addAll(userListReturnType.items)
                    val queryUserName = currentState().queryName
                    MainActivityState(queryUserName, newList, userListReturnType.nextPage)
                }
            }
        }
    }
}
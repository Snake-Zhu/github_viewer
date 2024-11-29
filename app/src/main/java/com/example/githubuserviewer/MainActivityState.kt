package com.example.githubuserviewer

import com.example.githubuserviewer.usecase.User

data class MainActivityState(
    val queryName: String? = null,
    val userList: List<User>,
    val nextLink: String? = null
)
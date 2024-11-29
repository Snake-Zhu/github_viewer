package com.example.githubuserviewer.screen

data class UserDetailState(
    val userDetail: UserDetail,
    val repos: List<Repository>
)
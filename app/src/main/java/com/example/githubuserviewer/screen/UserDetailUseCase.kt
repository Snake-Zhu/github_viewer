package com.example.githubuserviewer.screen

import com.example.githubuserviewer.Constants
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.OkHttpClient
import okhttp3.Request

class UserDetailUseCase {
    private val client = OkHttpClient()

    private fun getRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("Authorization", "Bearer ${Constants.BEARER}")
            .build()
    }

    fun getUserDetail(url: String): UserDetail {
        val request = getRequest(url)

        client.newCall(request).execute().use { response ->
            response.body?.string()?.let {
                val json = Json {
                    isLenient = true  // 不正なJSONをある程度許容する
                    ignoreUnknownKeys = true  // 不要なフィールドを無視する
                    namingStrategy = JsonNamingStrategy.SnakeCase  // スネークケース→キャメルケース
                }
                val userDetail: UserDetail = json.decodeFromString(it)
                return userDetail
            }
            return UserDetail("Error", "", "", 0, 0)
        }
    }

    fun getUserRepos(url: String): List<Repository> {
        val request = getRequest(url)

        client.newCall(request).execute().use { response ->
            response.body?.string()?.let {
                val json = Json {
                    isLenient = true  // 不正なJSONをある程度許容する
                    ignoreUnknownKeys = true  // 不要なフィールドを無視する
                    namingStrategy = JsonNamingStrategy.SnakeCase  // スネークケース→キャメルケース
                }
                val repos: List<Repository> = json.decodeFromString(it)
                return repos
            }
            return emptyList()
        }
    }
}

@Serializable
data class UserDetail(
    val login: String,
    val avatarUrl: String,
    val name: String?,
    val followers: Int,
    val following: Int
)

@Serializable
data class Repository(
    val id: Int,
    val fullName: String,
    val fork: Boolean,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val description: String?
)

@Serializable
data class RepositoryResponseType(
    val list: List<Repository>
)
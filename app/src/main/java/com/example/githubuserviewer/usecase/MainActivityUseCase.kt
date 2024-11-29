package com.example.githubuserviewer.usecase

import android.util.Log
import com.example.githubuserviewer.Constants
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class MainActivityUseCase {
    private val client = OkHttpClient()

    private fun getRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("Authorization", "Bearer ${Constants.BEARER}")
            .build()
    }

    fun getUserList(name: String): UserListReturnType {
        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
        val request = getRequest("https://api.github.com/search/users?q=$encodedName")

        client.newCall(request).execute().use { response ->
            response.body?.string()?.let {
                val json = Json {
                    isLenient = true  // 不正なJSONをある程度許容する
                    ignoreUnknownKeys = true  // 不要なフィールドを無視する
                    namingStrategy = JsonNamingStrategy.SnakeCase  // スネークケース→キャメルケース
                }
                val userQueryResponse: UserQueryResponse = json.decodeFromString(it)
                Log.d("getUserListResponse", "count: " + userQueryResponse.totalCount)
                val nextPage = response.header("link")?.let { link ->
                    // 正規表現でHeader中のnextページのリンクを取得する
                    val regex = """<([^>]+)>; rel="next"""".toRegex()
                    val matchResult = regex.find(link)
                    matchResult?.groups?.get(1)?.value
                }
                return UserListReturnType(userQueryResponse.items, nextPage)
            }
            return UserListReturnType(emptyList())
        }
    }

    fun loadNextPage(link: String): UserListReturnType {
        val request = getRequest(link)
        client.newCall(request).execute().use { response ->
            response.body?.string()?.let {
                val json = Json {
                    isLenient = true  // 不正なJSONをある程度許容する
                    ignoreUnknownKeys = true  // 不要なフィールドを無視する
                    namingStrategy = JsonNamingStrategy.SnakeCase  // スネークケース→キャメルケース
                }
                val userQueryResponse: UserQueryResponse = json.decodeFromString(it)
                Log.d("getUserListResponse", "count: " + userQueryResponse.totalCount)
                val nextPage = response.header("link")?.let { link ->
                    // 正規表現でHeader中のnextページのリンクを取得する
                    val regex = """<([^>]+)>; rel="next"""".toRegex()
                    val matchResult = regex.find(link)
                    matchResult?.groups?.get(1)?.value
                }
                return UserListReturnType(userQueryResponse.items, nextPage)
            }
            return UserListReturnType(emptyList())
        }
    }
}

@Serializable
data class UserQueryResponse(
    val totalCount: Int,
    val items: List<User>
)

@Serializable
data class User(
    val id: Int,
    val login: String,
    val avatarUrl: String,
    val url: String,
    val reposUrl: String
)

data class UserListReturnType(
    val items: List<User>,
    val nextPage: String? = null
)
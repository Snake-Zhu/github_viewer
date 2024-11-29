package com.example.githubuserviewer

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.githubuserviewer.screen.UserDetailState
import com.example.githubuserviewer.screen.UserDetailViewModel
import com.example.githubuserviewer.ui.theme.GitHubUserViewerTheme
import com.example.githubuserviewer.usecase.User

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var userDetailViewModel: UserDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = MainActivityViewModel()
        userDetailViewModel = UserDetailViewModel()
        setContent {
            GitHubUserViewerTheme {
                NavigationSetup(viewModel, userDetailViewModel)
            }
        }
    }
}

@Composable
fun UserListView(state: MainActivityState,
                 onClickHandler: () -> Unit,
                 onNextPageHandler: () -> Unit,
                 onChangeQueryName: (String?) -> Unit,
                 navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier.weight(3f),
                    value = state.queryName ?: "",
                    onValueChange = { onChangeQueryName(it) },
                )
                Button(
                    modifier = Modifier.weight(1.5f),
                    onClick = onClickHandler,
                ) {
                    Text("Search")
                }
            }
        }

        items(state.userList) {
            UserItem(it, navController)
        }
        state.nextLink?.let {
            item {
                Button(
                    onNextPageHandler
                ) {
                    Text("NextPage")
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate("user_detail/${user.id}")
            }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarUrl) // URLを指定
                .crossfade(true) // クロスフェードエフェクトを有効にする
                .build()
        )
        Image(
            painter = painter,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp) // アイコンのサイズ
                .clip(CircleShape) // 丸くクリップ
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = user.login, style = MaterialTheme.typography.titleLarge)

    }
}

@Composable
fun NavigationSetup(viewModel: MainActivityViewModel, userDetailViewModel: UserDetailViewModel) {
    val state by viewModel.state.collectAsState()

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "user_list") {
        composable("user_list") {
            UserListView(
                state,
                { viewModel.queryUser() },
                { viewModel.loadNextPage() },
                { name: String? -> viewModel.changeQueryUserName(name) },
                navController
            )
        }
        composable("user_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val user = state.userList.first { it.id == userId.toIntOrNull() }
            val userDetailState by userDetailViewModel.state.collectAsState()

            UserDetailScreen(userDetailState, navController)
            userDetailViewModel.loadUserDetail(user.url)
            userDetailViewModel.loadUserRepos(user.reposUrl)
        }
        composable(
            route = "webview?url={url}",
            arguments = listOf(navArgument("url") {
                type = NavType.StringType
                defaultValue = "https://www.google.com"
            })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: "https://www.google.com"
            WebViewScreen(url)
        }
    }
}

@Composable
fun UserDetailScreen(
    state: UserDetailState,
    navController: NavController
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(state.userDetail.avatarUrl) // URLを指定
                        .crossfade(true) // クロスフェードエフェクトを有効にする
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("User ID: ${state.userDetail.login}")
            }

            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "User Name: ${state.userDetail.name ?: "Empty"} ")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Follower: ${state.userDetail.followers}")
                Spacer(modifier = Modifier.width(16.dp))
                Text("Following: ${state.userDetail.following}")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                items(state.repos) { repo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(2.dp, Color.Blue, shape = RoundedCornerShape(8.dp))
                            .clickable { navController.navigate("webview?url=${repo.htmlUrl}") }
                    ) {
                        Column {
                            Text(
                                modifier = Modifier
                                    .padding(8.dp),
                                text = repo.fullName
                            )
                            Text(modifier = Modifier.padding(horizontal = 8.dp), text = "Language: ${repo.language}")
                            Text(modifier = Modifier.padding(horizontal = 8.dp), text = "Stars: ${repo.stargazersCount}")
                            Text(modifier = Modifier.padding(horizontal = 8.dp), text = "Description: ${repo.description}", maxLines = 3)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true // JavaScriptを有効化
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GitHubUserViewerTheme {
        UserListView(
            MainActivityState(null, emptyList()),
            {},
            {},
            { abc: String? -> },
            rememberNavController()
        )
    }
}
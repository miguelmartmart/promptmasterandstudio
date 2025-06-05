package com.promptmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.promptmaster.data.PromptRoomDatabase
import com.promptmaster.data.PromptRepository
import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.PromptViewModelFactory
import com.promptmaster.ui.home.HomeScreen
import com.promptmaster.ui.editprompt.EditPromptScreen
import com.promptmaster.ui.home.PromptItem
import com.promptmaster.ui.settings.SettingsScreen
import com.promptmaster.ui.theme.PromptMasterTheme
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptDao
import java.util.Locale
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.promptmaster.utils.rememberBooleanPreference // Import the helper function
import androidx.preference.PreferenceManager // Import PreferenceManager
import com.promptmaster.utils.setLocale // Import the setLocale extension function

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(newBase!!)
        val language = preferences.getString("appLanguage", Locale.getDefault().language) ?: "en"
        val context = newBase.setLocale(language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val database = PromptRoomDatabase.getDatabase(applicationContext)
        val repository = PromptRepository(database.promptDao())
        val viewModelFactory = PromptViewModelFactory(repository)

        setContent {
            val isDarkModeEnabled by rememberBooleanPreference(
                key = "darkModeEnabled",
                defaultValue = isSystemInDarkTheme()
            )

            PromptMasterTheme(darkTheme = isDarkModeEnabled) {
                PromptMasterApp(
                    viewModelFactory = viewModelFactory,
                    onThemeChange = { recreate() }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector? = null, val resourceId: Int? = null) {
    data object Home : Screen("home", Icons.Filled.Home, R.string.menu_home)
    data object Favorites : Screen("favorites", Icons.Filled.Favorite, R.string.menu_favorites)
    data object Settings : Screen("settings", Icons.Filled.Settings, R.string.menu_settings)
    data object EditPrompt : Screen("editprompt/{promptId}", null, null) {
        fun createRoute(promptId: Int? = null) = "editprompt/${promptId ?: 0}"
    }
    // TODO: Add other screens like Legal, Affiliate Ads
}

@Composable
fun PromptMasterApp(
    viewModelFactory: PromptViewModelFactory,
    onThemeChange: () -> Unit // Add the callback parameter
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onPromptClick = { promptId -> navController.navigate(Screen.EditPrompt.createRoute(promptId)) },
                    onAddPromptClick = { navController.navigate(Screen.EditPrompt.createRoute()) }
                )
            }
            composable(Screen.Favorites.route) {
                val promptViewModel: PromptViewModel = viewModel(factory = viewModelFactory)
                FavoritesScreenContent(
                    viewModel = promptViewModel,
                    onPromptClick = { promptId -> navController.navigate(Screen.EditPrompt.createRoute(promptId)) },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onThemeChange = onThemeChange) // Pass the callback to SettingsScreen
            }
            composable(
                route = Screen.EditPrompt.route,
                arguments = listOf(navArgument("promptId") { type = NavType.IntType; defaultValue = 0 })
            ) { backStackEntry ->
                val promptId = backStackEntry.arguments?.getInt("promptId")
                EditPromptScreen(
                    navController = navController,
                    promptId = if (promptId == 0) null else promptId,
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
            // TODO: Add other screen composables
        }
    }
}

@Composable
fun FavoritesScreenContent(
    viewModel: PromptViewModel,
    onPromptClick: (Int) -> Unit,
) {
    val favoritePrompts by viewModel.favoritePrompts.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(text = stringResource(R.string.favorite_prompts_title))

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(favoritePrompts) { prompt ->
                PromptItem( // Reuse the PromptItem composable from HomeScreen
                    prompt = prompt,
                    onPromptClick = onPromptClick,
                    onDeleteClick = { viewModel.delete(it) },
                    onFavoriteClick = { viewModel.update(it.copy(isFavorite = !it.isFavorite)) },
                    onDuplicateClick = { viewModel.duplicatePrompt(it) }
                )
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.Settings
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            // Only show items with icons and resource IDs in the bottom bar
            if (screen.icon != null && screen.resourceId != null) {
                NavigationBarItem(
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PromptMasterTheme {
        val mockPromptDao = object : PromptDao {
            override suspend fun insert(prompt: Prompt): Long = 0L
            override suspend fun update(prompt: Prompt) {}
            override suspend fun delete(prompt: Prompt) {}
            override fun getPrompt(id: Int): Flow<Prompt> = flowOf()
            override fun getAllPrompts(): Flow<List<Prompt>> = flowOf(emptyList())
            override fun searchPrompts(
                searchQuery: String?,
                category: String?,
                recommendedModel: String?,
                tag: String?
            ): Flow<List<Prompt>> = flowOf(emptyList())
            override fun getFavoritePrompts(): Flow<List<Prompt>> = flowOf(emptyList())
        }
        val mockPromptRepository = PromptRepository(mockPromptDao)
        PromptMasterApp(
            viewModelFactory = PromptViewModelFactory(mockPromptRepository),
            onThemeChange = {} // Provide a dummy lambda for the preview
        )
    }
}

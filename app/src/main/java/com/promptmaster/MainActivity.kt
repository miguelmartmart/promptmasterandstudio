package com.promptmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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

import com.promptmaster.data.PromptViewModelFactory // Import PromptViewModelFactory
import com.promptmaster.ui.FavoritesViewModel // New import
import com.promptmaster.ui.FavoritesViewModelFactory // Import FavoritesViewModelFactory
import com.promptmaster.ui.home.HomeScreen
import com.promptmaster.ui.editprompt.EditPromptScreen
import com.promptmaster.ui.settings.SettingsScreen
import com.promptmaster.ui.theme.PromptMasterTheme
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptDao
import java.util.Locale
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.promptmaster.utils.rememberBooleanPreference // Import the helper function
import androidx.preference.PreferenceManager // Import PreferenceManager
import com.promptmaster.data.PromptBackupManager
import com.promptmaster.ui.PromptOperationsViewModel // Import PromptOperationsViewModel
import com.promptmaster.ui.components.PromptListScreen // Import PromptListScreen
import com.promptmaster.utils.setLocale // Import the setLocale extension function
import androidx.compose.runtime.remember // Import remember
import kotlinx.coroutines.launch // Import launch
import androidx.compose.runtime.rememberCoroutineScope // Import rememberCoroutineScope
import com.promptmaster.ui.CombinedViewModelFactory // New import
import kotlinx.coroutines.CoroutineScope // Import CoroutineScope
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.launch // Import launch


class MainActivity : ComponentActivity() {

    private lateinit var repository: PromptRepository
    private lateinit var promptBackupManager: PromptBackupManager

    override fun attachBaseContext(newBase: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(newBase!!)
        val language = preferences.getString("appLanguage", Locale.getDefault().language) ?: "en"
        val context = newBase.setLocale(language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize repository and backup manager
        val promptDao = PromptRoomDatabase.getDatabase(applicationContext) {
            // This lambda is called when the database is populated
            // We need to launch a coroutine because setDatabaseReady is a suspend function
            CoroutineScope(Dispatchers.IO).launch {
                repository.setDatabaseReady()
            }
        }.promptDao()

        repository = PromptRepository(promptDao, application)
        promptBackupManager = PromptBackupManager(promptDao, applicationContext)

        val viewModelFactory = PromptViewModelFactory(
            repository,
            this.application,
            promptBackupManager
        )

        setContent {
            val isDarkModeEnabled by rememberBooleanPreference(
                key = "darkModeEnabled",
                defaultValue = isSystemInDarkTheme()
            )

            PromptMasterTheme(darkTheme = isDarkModeEnabled) {
                PromptMasterApp(
                    repository = repository, // Pass repository
                    application = application, // Pass application
                    promptBackupManager = promptBackupManager, // Pass promptBackupManager
                    viewModelFactory = viewModelFactory,
                    onThemeChange = { recreate() }
                )
            }
        }
    }

    // Create a mock Application class for previews
    class MockApplication : android.app.Application()
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
    repository: PromptRepository, // Add repository
    application: android.app.Application, // Change type to android.app.Application
    promptBackupManager: PromptBackupManager, // Add promptBackupManager
    viewModelFactory: PromptViewModelFactory,
    onThemeChange: () -> Unit // Add the callback parameter
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != Screen.EditPrompt.route) {
                FloatingActionButton(onClick = { navController.navigate(Screen.EditPrompt.createRoute()) }) {
                    Icon(Icons.Filled.Add, stringResource(R.string.add_new_prompt))
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { backStackEntry ->
                val promptViewModel: PromptViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = viewModelFactory
                )

                // Trigger refresh when the Home screen is composed or re-composed due to navigation
                LaunchedEffect(backStackEntry) {
                    promptViewModel.refresh()
                }

                HomeScreen(
                    viewModelFactory = viewModelFactory, // Pass the factory
                    onPromptClick = { promptId -> navController.navigate(Screen.EditPrompt.createRoute(promptId)) }
                )
            }
            composable(Screen.Favorites.route) { backStackEntry ->
                val favoritesViewModel: FavoritesViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = FavoritesViewModelFactory(repository, application, promptBackupManager, repository.databaseReadyEvent)
                )
                val coroutineScope = rememberCoroutineScope() // Move coroutineScope outside lambda

                // Trigger refresh when the Favorites screen is composed or re-composed due to navigation
                LaunchedEffect(backStackEntry) {
                    android.util.Log.d("MainActivity", "Favorites tab LaunchedEffect triggered. Refreshing data.")
                    favoritesViewModel.refresh()
                }

                PromptListScreen(
                    viewModel = favoritesViewModel,
                    onPromptClick = { promptId -> navController.navigate(Screen.EditPrompt.createRoute(promptId)) },
                    onCopyDescriptionClick = { promptId, description ->
                        coroutineScope.launch {
                            // Only mark as used, as copyToClipboard is in PromptViewModel's PromptUtils
                            favoritesViewModel.markPromptAsUsed(promptId)
                        }
                    }
                )
            }
            composable(
                route = Screen.EditPrompt.route,
                arguments = listOf(navArgument("promptId") { type = NavType.IntType; defaultValue = 0 })
            ) { backStackEntry ->
                val promptId = backStackEntry.arguments?.getInt("promptId")
                val combinedViewModelFactory = remember {
                    CombinedViewModelFactory(
                        promptViewModelFactory = viewModelFactory,
                        favoritesViewModelFactory = FavoritesViewModelFactory(repository, application, promptBackupManager, repository.databaseReadyEvent)
                    )
                }
                val promptOperationsViewModel: PromptOperationsViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = combinedViewModelFactory
                ) as PromptOperationsViewModel

                EditPromptScreen(
                    promptId = promptId ?: 0,
                    promptViewModel = promptOperationsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) { backStackEntry ->
                val promptViewModel: PromptViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = viewModelFactory
                )
                SettingsScreen(
                    viewModel = promptViewModel,
                    onThemeChange = onThemeChange
                )
            }
            // TODO: Add other screen composables
        }
    }
}

/*
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
                    onPromptClick = { onPromptClick(prompt.id) }, // Fix: Pass prompt.id
                    onDeleteClick = { viewModel.delete(prompt) }, // Fix: Use prompt instead of it
                    onFavoriteClick = { viewModel.update(prompt.copy(isFavorite = !prompt.isFavorite)) }, // Fix: Use prompt instead of it
                    onDuplicateClick = { viewModel.duplicatePrompt(prompt) } // Fix: Use prompt instead of it
                )
            }
        }
    }
}
*/

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
            override fun getAllCategories(): Flow<List<String>> = flowOf(emptyList()) // Add dummy implementation
            override fun getPromptsFiltered(category: String?): Flow<List<Prompt>> = flowOf(emptyList())
            override fun getAllSubcategories(category: String?): Flow<List<String>> = flowOf(emptyList()) // Add dummy implementation
            override suspend fun updateLastUsed(id: Int, timestamp: Long) {}
            override fun getFilteredAndSortedPrompts(
                searchQuery: String?,
                category: String?,
                subcategory: String?,
                showFavoritesOnly: Boolean
            ): Flow<List<Prompt>> = flowOf(emptyList())

            override suspend fun deleteAllPrompts() {}
            override suspend fun getAllPromptsList(): List<Prompt> = emptyList()
            override suspend fun getPromptCountByContent(title: String, description: String): Int = 0
            override fun getPaginatedPrompts(limit: Int, offset: Int): Flow<List<Prompt>> = flowOf(emptyList())
            override suspend fun getPaginatedFilteredAndSortedPrompts(
                searchQuery: String?,
                category: String?,
                subcategory: String?,
                showFavoritesOnly: Boolean,
                limit: Int,
                offset: Int
            ): List<Prompt> = emptyList() // Corrected return type to List<Prompt>
            override suspend fun getAllFilteredAndSortedPromptsList(
                searchQuery: String?,
                category: String?,
                subcategory: String?,
                showFavoritesOnly: Boolean
            ): List<Prompt> = emptyList() // Added missing implementation

            override suspend fun getPaginatedFilteredAndSortedPromptsList(
                searchQuery: String?,
                category: String?,
                subcategory: String?,
                showFavoritesOnly: Boolean,
                limit: Int,
                offset: Int
            ): List<Prompt> = emptyList() // Corrected return type to List<Prompt>
        }
        val mockApplication = com.promptmaster.MainActivity.MockApplication() // Create mock Application
        val mockPromptBackupManager = PromptBackupManager(mockPromptDao, mockApplication) // Create mock PromptBackupManager
        val mockPromptRepository = PromptRepository(mockPromptDao, mockApplication) // Pass mockApplication to mockPromptRepository
        PromptMasterApp(
            repository = mockPromptRepository, // Pass mock repository
            application = mockApplication, // Pass mock application
            promptBackupManager = mockPromptBackupManager, // Pass mock promptBackupManager
            viewModelFactory = PromptViewModelFactory(
                mockPromptRepository,
                application = mockApplication,
                mockPromptBackupManager
            ), // Provide mock repository, mock Application, and mock PromptBackupManager
            onThemeChange = {} // Provide a dummy lambda for the preview
        )
    }
}

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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.promptmaster.data.PromptRepository
import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.home.HomeScreen
import com.promptmaster.ui.editprompt.EditPromptScreen
import com.promptmaster.ui.settings.SettingsScreen
import com.promptmaster.ui.theme.PromptMasterTheme
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale
import androidx.core.view.WindowCompat
import com.promptmaster.utils.rememberBooleanPreference // Import the helper function
import androidx.preference.PreferenceManager // Import PreferenceManager
import com.promptmaster.data.PromptBackupManager
import com.promptmaster.ui.components.PromptListScreen // Import PromptListScreen
import com.promptmaster.ui.components.ZoomableContent // Import ZoomableContent
import com.promptmaster.utils.setLocale // Import the setLocale extension function
import kotlinx.coroutines.launch // Import launch
import androidx.compose.runtime.rememberCoroutineScope // Import rememberCoroutineScope
import dagger.hilt.android.AndroidEntryPoint // Import AndroidEntryPoint
import javax.inject.Inject // Import Inject
import androidx.hilt.navigation.compose.hiltViewModel // Import hiltViewModel
import com.promptmaster.ui.FavoritesViewModel // Import FavoritesViewModel
import androidx.compose.foundation.layout.Arrangement // Import Arrangement
import androidx.compose.ui.Alignment // Import Alignment
import androidx.compose.foundation.layout.fillMaxHeight // Import fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp // Import sp for font sizes
// Removed AdMob imports: import androidx.compose.ui.viewinterop.AndroidView, import com.google.android.gms.ads.AdRequest, import com.google.android.gms.ads.AdSize, import com.google.android.gms.ads.AdView

import com.promptmaster.ui.ads.AdViewModel // Import AdViewModel
import com.promptmaster.ui.components.ads.AdView // Import AdView
import androidx.compose.runtime.collectAsState // Import collectAsState
import kotlinx.coroutines.delay // Import delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration // Import LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.dimensionResource // Import dimensionResource

@AndroidEntryPoint // Add AndroidEntryPoint annotation
class MainActivity : ComponentActivity() { // Define MainActivity as a class

    @Inject lateinit var repository: PromptRepository
    @Inject lateinit var promptBackupManager: PromptBackupManager

    override fun attachBaseContext(newBase: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(newBase!!)
        val language = preferences.getString("appLanguage", Locale.getDefault().language) ?: "en"
        val context = newBase.setLocale(language)
        applyOverrideConfiguration(context.resources.configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val isDarkModeEnabled by rememberBooleanPreference(
                key = "darkModeEnabled",
                defaultValue = isSystemInDarkTheme()
            )

            PromptMasterTheme(darkTheme = isDarkModeEnabled) {
                PromptMasterApp(
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
    onThemeChange: () -> Unit // Add the callback parameter
) {
    val navController = rememberNavController()
    val adViewModel: AdViewModel = hiltViewModel() // Get AdViewModel instance
    val currentAd by adViewModel.currentAd.collectAsState() // Collect current ad state
    val screenWidth = LocalConfiguration.current.screenWidthDp // Get screen width for adaptive padding
    val horizontalPadding = 16.dp // Set a fixed padding based on user preference for Favorites tab

    // LaunchedEffect to alternate ads every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Wait for 10 seconds
            adViewModel.showNextAd()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) { // Use Column to stack Scaffold and AdView
        Scaffold(
            modifier = Modifier.weight(1f), // Scaffold takes all remaining vertical space
            bottomBar = {
                BottomNavigationBar(navController = navController) // Removed metrics
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
            ZoomableContent(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                ) {
                    composable(Screen.Home.route) {
                        val promptViewModel: PromptViewModel = hiltViewModel()

                        // Trigger refresh when the Home screen is composed or re-composed due to navigation
                        LaunchedEffect(Unit) { // Use Unit as key for LaunchedEffect to run once
                            delay(300) // evitar lock en cold boot
                            promptViewModel.refresh()
                        }

                        HomeScreen(
                            onPromptClick = { promptId -> navController.navigate(Screen.EditPrompt.createRoute(promptId)) },
                            horizontalPadding = horizontalPadding // Pass horizontalPadding to HomeScreen
                        )
                    }
                    composable(Screen.Favorites.route) {
                        val favoritesViewModel: FavoritesViewModel = hiltViewModel()
                        val coroutineScope = rememberCoroutineScope()

                        // Trigger refresh when the Favorites screen is composed or re-composed due to navigation
                        LaunchedEffect(Unit) { // Use Unit as key for LaunchedEffect to run once
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
                            },
                            horizontalPadding = horizontalPadding // Pass horizontalPadding to PromptListScreen
                        )
                    }
                    composable(
                        route = Screen.EditPrompt.route,
                        arguments = listOf(navArgument("promptId") { type = NavType.IntType; defaultValue = 0 })
                    ) { backStackEntry ->
                        val promptId = backStackEntry.arguments?.getInt("promptId")
                        val promptOperationsViewModel: PromptViewModel = hiltViewModel()

                        EditPromptScreen(
                            promptId = promptId ?: 0,
                            promptViewModel = promptOperationsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Settings.route) {
                        val promptViewModel: PromptViewModel = hiltViewModel()
                        SettingsScreen(
                            viewModel = promptViewModel,
                            onThemeChange = onThemeChange
                        )
                    }
                    // TODO: Add other screen composables
                }
            }
        }
        // Custom Affiliate Ad View
        currentAd?.let { ad ->
            AdView(
                ad = ad,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.ad_view_horizontal_padding)) // Apply horizontal padding from dimens
                    .heightIn(min = dimensionResource(R.dimen.ad_view_min_height), max = dimensionResource(R.dimen.ad_view_max_height)) // Set adaptive height range from dimens
            )
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
fun BottomNavigationBar(navController: NavHostController) { // Removed metrics parameter
    val items = listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.Settings
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp, max = 80.dp), // Increased height range for more vertical space
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            if (screen.icon != null && screen.resourceId != null) {
                NavigationBarItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(screen.resourceId),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis // Ensure text doesn't overflow if too long
                            )
                        }
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    alwaysShowLabel = true
                )
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PromptMasterTheme {
        val mockPromptDao = object : PromptDao {
            override suspend fun insert(prompt: Prompt): Long = 0L
            override suspend fun insertAll(prompts: List<Prompt>) {}
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
        PromptMasterApp(
            onThemeChange = {} // Provide a dummy lambda for the preview
        )
    }
}
*/

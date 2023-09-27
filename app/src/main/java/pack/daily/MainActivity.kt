package pack.daily

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pack.daily.ui.theme.DailyTheme
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import pack.daily.ui.theme.Addpage
import pack.daily.ui.theme.Homepage


const val DB_HAS_TO_BE_DELETED = false

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appStartupActions(applicationContext)

        setContent {
            DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxWidth()) {
                        NavHost(navController, startDestination = "home") {
                            composable("home") { Homepage().HomeScreen(applicationContext) }
                            composable("search") { SearchScreen() }
                            composable("add") { Addpage().AddScreen(applicationContext) }
                        }
                        BottomNavigationBar(navController, Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

fun appStartupActions(applicationContext: Context) {
    log("APP STARTED")

    val helper = DBHelper(applicationContext)

    if (DB_HAS_TO_BE_DELETED)
        helper.deleteDB()

    helper.deleteOldTasks()
    helper.addRepeatablesToTodaysTasks()

    log("Task names: " + helper.getTaskNames().toString())
    log("Repeatable names: " + helper.getRepNames().toString())
}

@Composable
fun SearchScreen() {
    // TODO: Add your search screen UI components here.
}

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier) {
    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home),
        BottomNavItem("Add", Icons.Filled.Add),
        BottomNavItem("Search", Icons.Filled.Search)
    )

    BottomNavigation(modifier = modifier) { // Apply the modifier here
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String = label.lowercase()
)

fun log(msg: String) {
    Log.d("MainActivity", msg)
}

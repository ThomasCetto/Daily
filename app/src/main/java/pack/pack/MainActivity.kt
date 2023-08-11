package pack.pack

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pack.pack.ui.theme.DailyTheme
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost


class MainActivity : ComponentActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("APP STARTED")

        // deletes and it will re-create
        DBHelper(applicationContext).deleteDB(applicationContext)

        setContent {
            DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Box {
                        NavHost(navController, startDestination = "home") {
                            composable("home") { HomeScreen(applicationContext) }
                            composable("search") { SearchScreen() }
                            composable("profile") { ProfileScreen() }
                        }
                        BottomNavigationBar(navController, Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(appCont: Context) {
    val defMod = Modifier.fillMaxWidth().padding(8.dp)
    Column(
        modifier = defMod
    ) {
        Row(
            modifier = defMod,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Dates().getTodaysCoolDate() + "\n\n TODO LIST",
                style = TextStyle(fontSize = 24.sp),
                textAlign = TextAlign.Center, // Center the text horizontally
                modifier = Modifier.fillMaxWidth() // Expand the width to the full available width
            )
        }
        Row(
            modifier = defMod,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckBoxList(appCont)
        }
    }
}

@Composable
fun SearchScreen() {
    // TODO: Add your search screen UI components here.
}

@Composable
fun ProfileScreen() {
    // TODO: Add your profile screen UI components here.
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


data class BottomNavItem(val label: String, val icon: ImageVector, val route: String = label.lowercase())

fun log(msg: String){
    Log.d("MainActivity", msg)
}

@Composable
fun CheckBoxList(appCont: Context){
    val dbHelper = DBHelper(appCont)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val rows: ArrayList<String> = dbHelper.getTodaysTaskNames()
        for (row in rows) {
            var isChecked by remember {
                mutableStateOf(false)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { newChecked ->
                        isChecked = newChecked
                    })
                Text(text = row)
            }
        }
    }
}

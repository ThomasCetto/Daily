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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pack.pack.ui.theme.DailyTheme


class MainActivity : ComponentActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("APP STARTED")

        deleteDB(applicationContext)
        val dbHelper = DBHelper(applicationContext)
        val db = dbHelper.writableDatabase


        setContent {
            DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    AddTaskButton()
                    val defMod = Modifier.fillMaxWidth().padding(8.dp)
                    Column(
                        modifier = defMod
                    ) {
                        Row(
                            modifier = defMod,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Dates().getCoolTodaysDate() + "\n\n TODO LIST")

                        }
                        Row(
                            modifier = defMod,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CheckBoxList(applicationContext)
                        }
                    }
                }
            }
        }
    }
}


fun deleteDB(appCont: Context){
    val dbHelper = DBHelper(appCont)
    dbHelper.close()

    // Delete the database file
    val dbFile = appCont.getDatabasePath("daily.db")
    if (dbFile.exists()) {
        dbFile.delete()
        log("Database deleted")
    } else {
        log("Database file not found, and not deleted")
    }
}

fun log(msg: String){
    Log.d("MainActivity", msg)
}

@Preview
@Composable
fun AddTaskButton(){

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)  // Add padding to create some spacing
    ) {
        Button(
            onClick = { /* Handle button click */ },
            modifier = Modifier
                .size(75.dp)  // Adjust the button size as needed
                .align(Alignment.BottomEnd), // Align to the bottom right corner
            colors = ButtonDefaults.buttonColors(contentColor = Color.Green)
        ) {
            Text(
                text = "+",
                style = TextStyle(fontSize = 30.sp),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }


}

@Composable
fun CheckBoxList(appCont: Context){
    val dbHelper = DBHelper(appCont)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val rows: ArrayList<String> = dbHelper.getTodaysTaskNames(appCont)
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

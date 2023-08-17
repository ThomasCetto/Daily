package pack.pack

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import androidx.compose.ui.platform.LocalContext



var taskName: String = ""
var taskDay: String = ""
var taskImportance: Int = 0

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
                            composable("add") { AddScreen(applicationContext) }
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
    val defMod = Modifier
        .fillMaxWidth()
        .padding(8.dp)
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
fun AddScreen(appCont: Context) {
    val defMod = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    Column(
        modifier = defMod
    ) {
        val verticalAlignmentMod = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(Modifier.verticalScroll(rememberScrollState())) // Add vertical scrolling if needed

        RowWithVerticalAlignment(verticalAlignmentMod) {
            Text(
                text = "AGGIUNGI TASK",
                style = TextStyle(fontSize = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            log("AGGIUNGI TASK")
        }

        RowWithVerticalAlignment(verticalAlignmentMod) {
            NameField()
        }

        RowWithVerticalAlignment(verticalAlignmentMod) {
            DateSelector()
        }

        RowWithVerticalAlignment(verticalAlignmentMod) {
            ImportanceSelector()
        }

        RowWithVerticalAlignment(verticalAlignmentMod) {
            ConfirmationButton(appCont)
        }
    }
}

@Composable
fun RowWithVerticalAlignment(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
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
fun CheckBoxList(appCont: Context) {
    val dbHelper = DBHelper(appCont)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val rows: ArrayList<HashMap<String, String>> = dbHelper.getTodaysTaskInfo()
        for (row in rows) {

            var isChecked by remember {
                mutableStateOf(false)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(if (row["checked"] == "1") Color.Green else (if (row["important"] == "1") Color.Red else Color.Transparent))
                ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { newChecked ->
                        isChecked = newChecked
                    },
                    modifier = Modifier
                        .background(if (row["checked"] == "1") Color.Green else (if (row["important"] == "1") Color.Red else Color.Transparent))

                )
                Text(text = row["name"] ?: "Errore")
            }
        }
    }
}


@Composable
fun DateSelector() {
    val mContext = LocalContext.current
    val mCalendar = Calendar.getInstance()

    // Fetching current year, month and day
    val mYear = mCalendar.get(Calendar.YEAR)
    val mMonth = mCalendar.get(Calendar.MONTH)
    val mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    mCalendar.time = Date()

    // Declaring a string value to
    // store date in string format
    val mDate = remember { mutableStateOf("") }

    // Declaring DatePickerDialog and setting
    // initial values as current values (present year, month and day)
    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            mDate.value = "$mDayOfMonth/${mMonth + 1}/$mYear"
        }, mYear, mMonth, mDay
    )

    Text(text="Giorno", textAlign=TextAlign.Center)
    // Creating a button that on
    // click displays/shows the DatePickerDialog
    Button(onClick = {
        mDatePickerDialog.show()
    },
    modifier= Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .height(56.dp)
        .wrapContentSize(Alignment.Center)) {
        Text(text = "Scegli", color = Color.White)
    }
    taskDay = mDate.value
    log("Data scelta: ${mDate.value}")
}

@Composable
fun NameField(){
    Text(text="Nome task")
    var textState by remember { mutableStateOf(TextFieldValue()) }
    BasicTextField(
        value = textState,
        onValueChange = { textState = it },
        textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
            .wrapContentSize(Alignment.CenterStart) // Center vertically, align text to start (left)
    )

    taskName = textState.text
    log("You entered: ${textState.text}")
}

@Composable
fun ImportanceSelector(){
    var isChecked by remember { mutableStateOf(false) }
    Text(text="Importante")
    Checkbox(
        checked = isChecked,
        onCheckedChange = { isChecked = it }
    )
    taskImportance = if (isChecked) 1 else 0
    log("Is checked; $isChecked" )
}

@Composable
fun ConfirmationButton(context: Context){
    Button(onClick = {
        val helper = DBHelper(context)
        try {
            helper.addTask(taskName, taskDay, taskImportance)
            log("La task è stata aggiunta con successo")
        }catch (ex: Exception){
            log("La task non è stata aggiunta a causa di un errore: " + ex.message)
        }
    }) {
        Text("Conferma")
    }
}
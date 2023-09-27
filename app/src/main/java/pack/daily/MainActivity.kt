package pack.daily

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
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
import pack.daily.ui.theme.DailyTheme
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import java.util.Calendar
import java.util.Date
import androidx.compose.ui.platform.LocalContext
import pack.daily.ui.theme.Homepage


var taskNameState by mutableStateOf(TextFieldValue())
var taskName: String by mutableStateOf("")
var taskDay: String by mutableStateOf("")
var taskImportance: Int by mutableIntStateOf(0)
var taskRepetition: Boolean by mutableStateOf(false)
var isDayOfWeek: Boolean by mutableStateOf(false)
var daysOfWeekChosen: List<Boolean> by mutableStateOf(List(7) { false })
var dayOfMonthChosen: Int by mutableIntStateOf(-1)

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
                            composable("add") { AddScreen(applicationContext) }
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
fun AddScreen(appCont: Context) {
    val defMod = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    Column(
        modifier = defMod.verticalScroll(ScrollState(0))
    ) {
        Text(
            text = "AGGIUNGI TASK",
            style = TextStyle(fontSize = 24.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        NameField()

        // every time that the page is loaded or a switch gets pressed it resets the unnecessary data
        resetInsertionData(taskRepet = taskRepetition, isDoW = isDayOfWeek, deleteName = false)

        // type of task scheduling chooser
        TaskSchedulingChooser() // repetition or not switch
        if (taskRepetition) {
            DayOfWeekOrMonth() // day of month or week switch
            if (isDayOfWeek) DayOfWeekSelector() else DayOfMonthSelector()
        } else {
            DateSelector()
            ImportanceSelector()
        }
        ConfirmationButton(appCont)
    }
}

@Composable
fun DayOfMonthSelector() {
    RowWithVerticalAlignment {
        var selectedValue by remember { mutableIntStateOf(1) }

        Text("Giorno: $selectedValue")
        NumberPicker(
            onValueChange = { newValue ->
                selectedValue = newValue
            },
            minValue = 1,
            maxValue = 31
        )
    }
}

@Composable
fun NumberPicker(onValueChange: (Int) -> Unit, minValue: Int, maxValue: Int) {
    Slider(
        value = dayOfMonthChosen.toFloat(),
        onValueChange = { newValue ->
            dayOfMonthChosen = newValue.toInt()
            onValueChange(newValue.toInt())
        },
        valueRange = minValue.toFloat()..maxValue.toFloat(),
        steps = maxValue - minValue
    )
}

@Composable
fun DayOfWeekSelector() {
        var checkedStates by remember { mutableStateOf(List(7) { false }) }
        val daysOfWeek = arrayOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")

        Column {
            for (index in 0 until 7) {
                RowWithVerticalAlignment {
                    Text(text = daysOfWeek[index])
                    Checkbox(
                        checked = checkedStates[index],
                        onCheckedChange = { isChecked ->
                            checkedStates = checkedStates.toMutableList().also {
                                it[index] = isChecked
                            }
                        }
                    )
                }
            }
        }
        daysOfWeekChosen = checkedStates
}

@Composable
fun DayOfWeekOrMonth() {
    RowWithVerticalAlignment {
        Text(text = "gg mese     ")
        var checked by remember { mutableStateOf(false) }

        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
            }
        )
        Text(text = "     gg settimana")
        isDayOfWeek = checked
    }
}

@Composable
fun TaskSchedulingChooser() {
    RowWithVerticalAlignment {
        Text(text = "Task a ripetizione   ")
        var checked by remember { mutableStateOf(false) }

        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
            }
        )
        taskRepetition = checked
    }
}

@Composable
fun RowWithVerticalAlignment(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String = label.lowercase()
)

fun log(msg: String) {
    Log.d("MainActivity", msg)
}

@Composable
fun DateSelector() {
    RowWithVerticalAlignment {
        val mContext = LocalContext.current
        val mCalendar = Calendar.getInstance()

        // Fetching current year, month and day
        val year = mCalendar.get(Calendar.YEAR)
        val month = mCalendar.get(Calendar.MONTH)
        val day = mCalendar.get(Calendar.DAY_OF_MONTH)

        mCalendar.time = Date()

        // Date in string format
        val mDate = remember { mutableStateOf(Dates().getTodaysDate()) }

        // Creating dialog
        val mDatePickerDialog = DatePickerDialog(
            mContext,
            { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
                mDate.value = "$mYear-${mMonth + 1}-$mDayOfMonth"
            }, year, month, day
        )

        Text(text = "Giorno      " + Dates().convertDate(mDate.value, "dd/MM/yyyy"))
        // Button that opens the dialog
        Button(
            onClick = {
                mDatePickerDialog.show()
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp)
                .wrapContentSize(Alignment.Center)
        ) {
            Text(text = "Scegli", color = Color.White)
        }
        taskDay = mDate.value
    }
}

@Composable
fun NameField() {
    RowWithVerticalAlignment {
        Text(text = "Nome task")
        BasicTextField(
            value = taskNameState,
            onValueChange = { taskNameState = it },
            textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
        )

        taskName = taskNameState.text
    }
}

@Composable
fun ImportanceSelector() {
    RowWithVerticalAlignment {
        var isChecked by remember { mutableStateOf(false) }
        Text(text = "Importante")
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it }
        )
        taskImportance = if (isChecked) 1 else 0
    }
}

@Composable
fun ConfirmationButton(context: Context) {
        Button(enabled = taskName.isNotBlank(), // if it has a name, it's enabled
            onClick = {
                try {
                    if (taskName == "" || taskName == " ")
                        throw Exception("la task non ha un nome e non verrà inserita\n")

                    if (taskRepetition) {
                        DBHelper(context).insertRepeatable(
                            taskName,
                            dayOfMonthChosen,
                            daysOfWeekChosen
                        )
                    } else {
                        DBHelper(context).insertTask(taskName, taskDay, taskImportance)
                    }

                    /*reset everything so the user understands that the previous one was confirmed
                        successfully and that he can insert another one*/
                    resetInsertionData(taskRepet = false, isDoW = false, deleteName = true)

                } catch (ex: Exception) {
                    log("La task non è stata aggiunta a causa di un errore: " + ex.message + ex.stackTraceToString())
                }
            }) {
            Text("Conferma")
        }
}

fun resetInsertionData(taskRepet: Boolean, isDoW: Boolean? = null, deleteName: Boolean = false) {
    taskDay = Dates().getTodaysDateSlash()
    taskImportance = 0
    taskRepetition = taskRepet
    isDayOfWeek = isDoW ?: isDayOfWeek  // if null it stays the same
    daysOfWeekChosen = List(7) { false }
    dayOfMonthChosen = -1
    if (deleteName) {
        taskNameState = TextFieldValue("")
        taskName = ""
    }
}
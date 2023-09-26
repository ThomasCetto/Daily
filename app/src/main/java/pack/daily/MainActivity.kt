package pack.daily

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
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
import pack.daily.ui.theme.DailyTheme
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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


var taskNameState by mutableStateOf(TextFieldValue())
var taskName: String by mutableStateOf("")
var taskDay: String by mutableStateOf("")
var taskImportance: Int by mutableIntStateOf(0)
var taskRepetition: Boolean by mutableStateOf(false)
var isDayOfWeek: Boolean by mutableStateOf(false)
var daysOfWeekChosen: List<Boolean> by mutableStateOf(List(7) { false })
var dayOfMonthChosen: Int by mutableIntStateOf(-1)

class MainActivity : ComponentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("APP STARTED")

        val helper = DBHelper(applicationContext)

        // deletes and it will re-create the database
        //helper.deleteDB()

        helper.deleteOldTasks()
        helper.addRepeatables() // of the day

        log("Task names: " + helper.getTaskNames().toString())
        log("Repeatable names: " + helper.getRepNames().toString())


        setContent {
            DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                text = Dates().getTodaysCoolDate() + "\n\n TO-DO LIST",
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
        modifier = defMod.verticalScroll(ScrollState(0))
    ) {
        RowWithVerticalAlignment {
            Text(
                text = "AGGIUNGI TASK",
                style = TextStyle(fontSize = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        RowWithVerticalAlignment { NameField() }

        // type of task scheduling chooser
        resetInsertionData(taskRepet = taskRepetition, isDoW = isDayOfWeek, deleteName = false)

        RowWithVerticalAlignment { TaskSchedulingChooser() } // repetition or not switch
        if (taskRepetition) {
            RowWithVerticalAlignment { DayOfWeekOrMonth() } // day of month or week switch

            RowWithVerticalAlignment { if (isDayOfWeek) DayOfWeekSelector() else DayOfMonthSelector() }
        } else {
            RowWithVerticalAlignment { DateSelector() }
            RowWithVerticalAlignment { ImportanceSelector() }
        }
        RowWithVerticalAlignment { ConfirmationButton(appCont) }
    }
}

@Composable
fun DayOfMonthSelector() {
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

@Composable
fun NumberPicker(
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int
) {
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
    val daysOfWeek =
        arrayOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")

    Column {
        for (index in 0 until 7) {
            Row {
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
    log("isDayOfWeek changed to $isDayOfWeek")
}

@Composable
fun TaskSchedulingChooser() {
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

@Composable
fun RowWithVerticalAlignment(
    content: @Composable RowScope.() -> Unit
) {
    val verticalAlignmentMod = Modifier
        .fillMaxWidth()
        .padding(8.dp)

    Row(
        modifier = verticalAlignmentMod,
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
fun CheckBoxList(appCont: Context) {
    val dbHelper = DBHelper(appCont)
    val rows: ArrayList<HashMap<String, String>> = dbHelper.getTodaysTaskInfo()

    // lazyColumn makes scrolling possible
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(items = rows, itemContent = { row ->
            var isChecked by remember { mutableStateOf(row["checked"].equals("1")) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .background(if (isChecked) Color.Green else (if (row["important"] == "1") Color.Red else Color.Transparent)),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { newChecked ->
                        try {
                            isChecked = newChecked
                            dbHelper.changeCheckID(
                                Integer.parseInt(row["id"] ?: "-1"),
                                if (newChecked) 1 else 0
                            )
                        } catch (e: Exception) {
                            log(e.message ?: "errore nell errore")
                        }
                    },
                    modifier = Modifier
                        .background(if (isChecked) Color.Green else (if (row["important"] == "1") Color.Red else Color.Transparent))
                )
                Text(text = row["name"] ?: "Errore")
            }

        })
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

    // Date in string format
    val mDate = remember { mutableStateOf(Dates().getTodaysDate()) }

    // Creating dialog
    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            mDate.value = "$mYear-${mMonth + 1}-$mDayOfMonth"
        }, mYear, mMonth, mDay
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

@Composable
fun NameField() {
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

@Composable
fun ImportanceSelector() {
    var isChecked by remember { mutableStateOf(false) }
    Text(text = "Importante")
    Checkbox(
        checked = isChecked,
        onCheckedChange = { isChecked = it }
    )
    taskImportance = if (isChecked) 1 else 0
}

@Composable
fun ConfirmationButton(context: Context) {
    Button(enabled = !taskName.isNullOrBlank(), // if it has a name it's enabled
        onClick = {
        try {
            if (taskName == "" || taskName == " ")
                throw Exception("la task non ha un nome e non verrà inserita\n")

            if (taskRepetition){
                DBHelper(context).insertRepeatable(taskName, dayOfMonthChosen, daysOfWeekChosen)
            }else {
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

fun resetInsertionData(taskRepet: Boolean, isDoW: Boolean? = null, deleteName: Boolean = false){
    taskDay = Dates().getTodaysDateSlash()
    taskImportance = 0
    taskRepetition = taskRepet
    isDayOfWeek = isDoW ?: isDayOfWeek  // if null it stays the same
    daysOfWeekChosen = List(7){false}
    dayOfMonthChosen = -1
    if(deleteName){
        taskNameState = TextFieldValue("")
        taskName = ""
        log("Name deleted");
    }
}
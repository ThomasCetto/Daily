package pack.daily.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pack.daily.DBHelper
import pack.daily.Dates
import java.util.Calendar
import java.util.Date

class Addpage {

    private var taskNameState by mutableStateOf(TextFieldValue())
    private var taskName: String by mutableStateOf("")
    private var taskDay: String by mutableStateOf("")
    private var taskImportance: Int by mutableIntStateOf(0)
    private var taskRepetition: Boolean by mutableStateOf(false)
    private var isDayOfWeek: Boolean by mutableStateOf(false)
    private var daysOfWeekChosen: List<Boolean> by mutableStateOf(List(7) { false })
    private var dayOfMonthChosen: Int by mutableIntStateOf(-1)

    @Composable
    fun AddScreen(appCont: Context) {
        val defMod = Modifier.fillMaxWidth().padding(8.dp)

        Column(modifier = defMod.verticalScroll(ScrollState(0))) {
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
    fun DayOfWeekSelector() {
        var checkedStates by remember { mutableStateOf(List(7) { false }) }
        val daysOfWeek = arrayOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")

        Column {
            for (index in 0 until 7) {
                RowWithVerticalAlignment(modifier = Modifier.fillMaxWidth().padding(0.dp)) {
                    Checkbox(
                        checked = checkedStates[index],
                        onCheckedChange = { isChecked ->
                            checkedStates = checkedStates.toMutableList().also {
                                it[index] = isChecked
                            }
                        }
                    )
                    Text(text = daysOfWeek[index])
                }
            }
        }
        daysOfWeekChosen = checkedStates
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

    private fun resetInsertionData(taskRepet: Boolean, isDoW: Boolean? = null, deleteName: Boolean = false) {
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

    @Composable
    fun RowWithVerticalAlignment(modifier: Modifier = Modifier.fillMaxWidth().padding(8.dp),
                                 content: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }

    private fun log(msg: String) {
        Log.d("MainActivity", msg)
    }
}
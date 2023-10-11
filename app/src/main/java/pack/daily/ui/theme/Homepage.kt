package pack.daily.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pack.daily.DBHelper
import pack.daily.Dates

class Homepage {

    @Composable
    fun HomeScreen(appCont: Context) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = Dates().getTodaysCoolDate() + "\n\n TO-DO LIST",
                style = TextStyle(fontSize = 24.sp),
                textAlign = TextAlign.Center, // Center the text horizontally
                modifier = Modifier.fillMaxWidth() // Expand the width to the full available width
            )
            CheckBoxList(appCont)
        }
    }

    @Composable
    fun CheckBoxList(appCont: Context) {
        val dbHelper = DBHelper(appCont)
        val rows: ArrayList<HashMap<String, String>> = dbHelper.getTodaysTaskInfo()

        // lazyColumn makes scrolling possible
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(fraction = 0.85F)) {
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
                    Text(text = row["name"] ?: "Errore")  // shows the task name
                }
            })
        }
    }

    private fun log(msg: String) {
        Log.d("MainActivity", msg)
    }
}
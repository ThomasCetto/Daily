package pack.daily.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pack.daily.DBHelper
import androidx.compose.runtime.*

class Searchpage {

    @Composable
    fun SearchScreen(appContext: Context) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = "\n TASKS \n",
                style = TextStyle(fontSize = 24.sp),
                textAlign = TextAlign.Center, // Center the text horizontally
                modifier = Modifier.fillMaxWidth() // Expand the width to the full available width
            )
            Text(
                text = "A ripetizione: ",
                style = TextStyle(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth() // Expand the width to the full available width
            )

            Text(
                text = "Task normali: ",
                style = TextStyle(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth() // Expand the width to the full available width
            )

            ListOfTasks(appContext)

        }
    }

    @Composable
    fun ListOfRepeatables(appCont: Context){


    }

    @Composable
    fun ListOfTasks(appCont: Context){
        val dbHelper = DBHelper(appCont)
        var tasks by remember { mutableStateOf(dbHelper.getTasksData()) }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(items = tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(if (task["important"] == "1") Color.Red else Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = task["name"] ?: "Errore")  // shows the task name

                    // TODO: make the important task colored red, but not the complete row

                    Spacer(Modifier.width(15.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            // Remove the task from the list and database
                            dbHelper.deleteTask(Integer.parseInt(task["id"] ?: "-1"))
                            tasks = tasks.filterNot { it == task }
                        }
                    ) {
                        Text("Elimina")
                    }
                }
            }
        }



    }



    fun log(msg: String) {
        Log.d("MainActivity", msg)
    }

}
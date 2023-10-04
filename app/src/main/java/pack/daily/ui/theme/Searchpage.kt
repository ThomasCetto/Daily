package pack.daily.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource

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

            ListOfRepeatables(appContext)

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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${task["name"]}",
                        modifier = Modifier.weight(1f)  //This will align the first Text to the left
                    )


                    Text(
                        text = "${task["day"]}",
                        modifier = Modifier.weight(1f)  // This will center the second Text
                    )

                    Spacer(modifier = Modifier.padding(end = 16.dp))

                    Button(
                        modifier = Modifier
                            .size(40.dp) // You can adjust the size as needed
                            .padding(4.dp) // Add some padding to the Button
                            .requiredWidthIn(min = 40.dp), // Set a minimum width for the Button
                        contentPadding = PaddingValues(8.dp), // Add content padding for the text
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            // Remove the task from the list and database
                            dbHelper.deleteTask(Integer.parseInt(task["id"] ?: "-1"))
                            tasks = tasks.filterNot { it == task }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

            }
        }



    }



    fun log(msg: String) {
        Log.d("MainActivity", msg)
    }

}
package pack.daily

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext: Context = context.applicationContext

    companion object {
        private const val DATABASE_NAME = "daily.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Check if the table exists before creating it
        val tableExistsQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='my_table'"
        val cursor = db.rawQuery(tableExistsQuery, null)

        if (cursor != null && cursor.count > 0) {
            cursor.close()
            return  // Table already exists
        }

        try {
            val queries = readQueriesFromFile(appContext, R.raw.creationqueries)
            for (query in queries) {
                db.execSQL(query)
            }
        } catch (e: SQLException) {
            log("Error creating tables: ${e.message}")
        }
        log("THE DATABASE WAS CREATED")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database schema upgrades if needed
    }

    private fun readQueriesFromFile(context: Context, resourceId: Int): List<String> {
        val queries = mutableListOf<String>()
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            val stringBuilder = StringBuilder()
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                if (line?.trim()?.endsWith(";") == true) {
                    queries.add(stringBuilder.toString())
                    stringBuilder.clear()
                }
            }
            reader.close()
        } catch (e: IOException) {
            log("Error reading queries: ${e.message}")

        }
        return queries
    }

    private fun getAllRows(): Cursor {
        val db = readableDatabase
        return db.query("task", null, null, null, null, null, null)
    }

    private fun getAllRepRows(): Cursor {
        val db = readableDatabase
        return db.query("repetitive", null, null, null, null, null, null)
    }
    private fun log(msg: String){
        Log.d("MainActivity", msg)
    }

    fun getRowStrings(): ArrayList<String>{
        val rows = ArrayList<String>()

        try {
            val cursor = getAllRows()
            val columnNames = cursor.columnNames  // Get column names dynamically

            while (cursor.moveToNext()) {
                val rowInfo = columnNames.joinToString(", ") { columnName ->
                    val value = cursor.getString(cursor.getColumnIndexOrThrow(columnName))
                    "$columnName: $value"
                }
                rows.add(rowInfo)
            }
        } catch (e: Exception) {
            log("Error in obtaining the rows " + e.message.toString())
        }
        return rows
    }

    private fun getRepRowStrings(): ArrayList<String>{
        val rows = ArrayList<String>()

        try {
            val cursor = getAllRepRows()
            val columnNames = cursor.columnNames  // Get column names dynamically

            while (cursor.moveToNext()) {
                val rowInfo = columnNames.joinToString(", ") { columnName ->
                    val value = cursor.getString(cursor.getColumnIndexOrThrow(columnName))
                    "$columnName: $value"
                }
                rows.add(rowInfo)
            }
        } catch (e: Exception) {
            log("Error in obtaining the rows " + e.message.toString())
        }
        return rows
    }

    fun getTaskNames(): ArrayList<String>{
        val strings: ArrayList<String> = getRowStrings()
        val outList = ArrayList<String>()
        for (string in strings){
            outList.add(string.split("name: ")[1].split(", day: ")[0])
        }
        return outList
    }

    fun getRepNames(): ArrayList<String>{
        val strings: ArrayList<String> = getRepRowStrings()
        val outList = ArrayList<String>()

        for (string in strings)
            outList.add((string.split("name: ")[1].split(", dayOfWeek: "))[0])

        return outList
    }

    private fun getAllRowsForToday(): Cursor {
        val today = Dates().getTodaysDate()

        val db = readableDatabase
        return db.query("task", null, "day = ?", arrayOf(today), null, null, null)
    }


    fun getTodaysTaskInfo(): ArrayList<HashMap<String,String>> {
        val info = ArrayList<HashMap<String, String>>()

        val cursor: Cursor = getAllRowsForToday()

        val idIndex = cursor.getColumnIndex("id")
        val nameIndex = cursor.getColumnIndex("name")
        val importanceIndex = cursor.getColumnIndex("important")
        val checkedIndex = cursor.getColumnIndex("checked")

        if (nameIndex >= 0 && importanceIndex >= 0 && checkedIndex >= 0) {
            while (cursor.moveToNext()) {
                val rowInfo = HashMap<String, String>()

                rowInfo["id"] = cursor.getInt(idIndex).toString()
                rowInfo["name"] = cursor.getString(nameIndex)
                rowInfo["important"] = cursor.getInt(importanceIndex).toString()
                rowInfo["checked"] = cursor.getInt(checkedIndex).toString()

                info.add(rowInfo)
            }
        }

        cursor.close()

        return info
    }

    fun changeCheckID(id: Int, value: Int){
        val db = writableDatabase
        val updateQuery = "UPDATE task " +
                "SET checked = $value " +
                "WHERE id = $id"
        db.execSQL(updateQuery)
        db.close()
    }

    fun deleteDB(){
        close()

        // Delete the database file
        val dbFile = appContext.getDatabasePath("daily.db")
        if (dbFile.exists()) {
            dbFile.delete()
            log("DATABASE DELETED")
        } else {
            log("Database file not found, and not deleted")
        }
    }

    fun insertRepeatable(name: String, dayOfMonth: Int = -1, daysOfWeek: List<Boolean> = emptyList()){
        var idx = 0

        for(day in daysOfWeek){
            idx += 1 //day of week indexes start from 1
            if (!day) continue

            val values = ContentValues().apply{
                put("name", name)
                put("dayOfWeek", idx)
                put("dayOfMonth", -1)
            }
            writableDatabase.insert("repetitive", null, values)
            log("Added repetitive task with name: $name, in day of the week: $idx")
        }

        if (dayOfMonth != -1){
            val values = ContentValues().apply{
                put("name", name)
                put("dayOfWeek", -1)
                put("dayOfMonth", dayOfMonth)
            }
            writableDatabase.insert("repetitive", null, values)
            log("Added repetitive task with name: $name, in day of the month: $dayOfMonth")
        }

        addRepeatablesToTodaysTasks() // adds the task right now if it's scheduled also for today
    }

    fun insertTask(name: String, day: String, importance: Int){
        val newDate = Dates().convertDate(day)

        // if the same name is already scheduled for the same day, it does not insert another one
        if(isTaskAlreadyScheduled(name, newDate)){
            log("The task was already scheduled for the same day, and it hadn't been added.")
            return
        }

        val values = ContentValues().apply {
            put("name", name)
            put("day", newDate)
            put("important", importance)
            put("checked", 0)
        }

        writableDatabase.insert("task", null, values)

        log("Added task: $name")
    }

    fun deleteOldTasks(){
        // if the task has already been display for that day
        writableDatabase.delete("task", "day < ?", arrayOf(Dates().getTodaysDate()))
    }

    fun addRepeatablesToTodaysTasks(){
        // Searches for repetitiveTasks that are scheduled for the current day of week or month.
        // Then inserts new tasks for each of these tasks for today's day

        val date = Dates()
        val idxInWeek = date.getDayOfWeekIndex()
        val idxInMonth = date.getDayOfMonth()

        val data = readableDatabase.query(
            "repetitive", null,
            "dayOfWeek = $idxInWeek OR dayOfMonth = $idxInMonth",
            null, null, null, null
        )
        val nameIdx =  if (data.getColumnIndex("name") >= 0) data.getColumnIndex("name") else 0

        // for each row adds a new task
        while(data.moveToNext()){
            val taskName = data.getString(nameIdx)
            insertTask(taskName, date.getTodaysDate(), 0)
        }

        data.close()
    }

    private fun isTaskAlreadyScheduled(name: String, day: String): Boolean{
        val countOfTasks = DatabaseUtils.queryNumEntries(readableDatabase, "task", "name = ? AND day = ?", arrayOf(name, day.replace("/", "-")))

        log("This task was already scheduled $countOfTasks times")

        return countOfTasks > 0
    }

    @SuppressLint("Range")
    fun getTasksData(): List<HashMap<String, String>> {
        val taskList = ArrayList<HashMap<String, String>>()
        val db = readableDatabase

        val query = "SELECT * FROM task ORDER BY day"
        val cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val taskMap = HashMap<String, String>()
                taskMap["id"] = cursor.getString(cursor.getColumnIndex("id"))
                taskMap["name"] = cursor.getString(cursor.getColumnIndex("name"))
                taskMap["day"] = cursor.getString(cursor.getColumnIndex("day"))
                taskMap["important"] = cursor.getString(cursor.getColumnIndex("important"))
                taskMap["checked"] = cursor.getString(cursor.getColumnIndex("checked"))
                taskList.add(taskMap)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return taskList
    }



    fun deleteTask(id: Int){
        if (id == -1) return
        writableDatabase.delete("task", "id = ?", arrayOf(id.toString()))
        log("Deleted task with id: $id")
    }

    fun deleteRepetitive(id: Int){
        if (id == -1) return
        writableDatabase.delete("repetitive", "id = ?", arrayOf(id.toString()))
        log("Deleted repeatable with id: $id")
    }


}

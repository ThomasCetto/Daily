package pack.pack

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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

        log("STARTING onCreate")
        // Check if the table exists before creating it
        val tableExistsQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='my_table'"
        val cursor = db.rawQuery(tableExistsQuery, null)

        if (cursor != null && cursor.count > 0) {
            cursor.close()
            log("DATABASE ALREADY EXIST")
            return  // Table already exists
        }

        log("TRYING TO CREATE THE TABLES")
        try {
            val queries = readQueriesFromFile(appContext, R.raw.creationqueries)
            for (query in queries) {
                Log.d("MainActivity", "Query: $query")
                db.execSQL(query)
            }
            log("TABLES CREATED SUCCESSFULLY")
        } catch (e: SQLException) {
            log("Error creating tables: ${e.message}")
        }


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
        log("Read queries: $queries")
        return queries
    }

    private fun getAllRows(): Cursor {
        val db = readableDatabase
        return db.query("task", null, null, null, null, null, null)
    }

    private fun log(msg: String){
        Log.d("MainActivity", msg)
    }

    private fun getRowStrings(): ArrayList<String>{
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

    fun getTaskNames(): ArrayList<String>{
        val strings: ArrayList<String> = getRowStrings()
        val outList = ArrayList<String>()
        for (string in strings){
            outList.add(string.split("name: ")[1].split(", day: ")[0])
        }
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

    fun changeCheckID(id: Int){
        val db = writableDatabase
        val updateQuery = "UPDATE task " +
                "SET checked = 1 - checked " +
                "WHERE id = $id"
        db.execSQL(updateQuery)
        db.close()
    }

    fun deleteDB(appCont: Context){
        close()

        // Delete the database file
        val dbFile = appCont.getDatabasePath("daily.db")
        if (dbFile.exists()) {
            dbFile.delete()
            log("Database deleted")
        } else {
            log("Database file not found, and not deleted")
        }
    }

    fun addTask(name: String, day: String, importance: Int): Long{
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("day", Dates().convertDate(day))
            put("important", importance)
            put("checked", 0)
        }

        log("trying to add task with day: $name, day: $day, importance: $importance")
        log("DB now: " + getRowStrings())

        return writableDatabase.insert("task", null, values)
    }
}

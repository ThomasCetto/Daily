package pack.pack

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDate
import java.util.Calendar

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

    fun readQueriesFromFile(context: Context, resourceId: Int): List<String> {
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

    fun getAllRows(tableName: String): Cursor {
        val db = readableDatabase
        return db.query(tableName, null, null, null, null, null, null)
    }

    private fun log(msg: String){
        Log.d("MainActivity", msg)
    }

    fun getRowStrings(appCont: Context, tableName: String): ArrayList<String>{
        val rows = ArrayList<String>()

        try {
            val cursor = getAllRows(tableName)
            val columnNames = cursor.columnNames  // Get column names dynamically

            while (cursor.moveToNext()) {
                val rowInfo = columnNames.joinToString(", ") { columnName ->
                    val value = cursor.getString(cursor.getColumnIndexOrThrow(columnName))
                    "$columnName: $value"
                }
                rows.add(rowInfo)
            }
        } catch (e: Exception) {
            log("Errore nell'ottenere le righe " + e.message.toString())
        }
        return rows
    }

    fun getTaskNames(appCont: Context): ArrayList<String>{
        val strings: ArrayList<String> = getRowStrings(appCont, "task")
        val outList = ArrayList<String>()
        for (string in strings){
            outList.add(string.split("name: ")[1].split(", day: ")[0])
        }
        return outList
    }

    fun getAllRowsForToday(): Cursor {
        val today = Dates().getTodaysDate()

        val db = readableDatabase
        return db.query("task", null, "day = ?", arrayOf(today), null, null, null)
    }


    fun getTodaysTaskNames(appCont: Context): ArrayList<String>{
        val cursor: Cursor = getAllRowsForToday()
        val out = ArrayList<String>()
        val nameIndex = cursor.getColumnIndex("name")

        if (nameIndex >= 0) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                out.add(name)
            }
            cursor.close()
        }
        return out
    }




}
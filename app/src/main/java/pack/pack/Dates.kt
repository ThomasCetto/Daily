package pack.pack

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Dates {

    fun getTodaysDate(): String{
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Months are zero-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return String.format("%04d-%02d-%02d", year, month, day)
    }

    fun getTodaysCoolDate(): String{
        val locale = Locale("it", "IT") // Italian locale
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        return dateFormat.format(Date())
    }
}
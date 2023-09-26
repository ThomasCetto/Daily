package pack.daily

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    fun getTodaysDateSlash(): String{
        val date = getTodaysDate()
        return date.replace("-", "/")
    }

    fun getTodaysCoolDate(): String{
        val locale = Locale("it", "IT") // Italian locale
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        return dateFormat.format(Date())
    }

    fun convertDate(inputDate: String, outFormat: String = "yyyy-MM-dd"): String {
        // from dd-MM-yyyy, dd/MM/yyyy, yyyy/MM/dd, etc. format to the specified output format



        val divider = if(inputDate.contains("/")) "/" else "-"
        val split = inputDate.split(divider)

        val val1 = if(split[0].length == 1) "0" + split[0] else split[0]
        val val2 = if(split[1].length == 1) "0" + split[1] else split[1]
        val val3 = if(split[2].length == 1) "0" + split[2] else split[2]


        val modDate = val1 + divider + val2 + divider + val3
        val inFormat = if(split[0].length == 4)
            "yyyy" + divider + "MM" + divider + "dd" else
            "dd" + divider + "MM" + divider + "yyyy"

        val inputFormat = DateTimeFormatter.ofPattern(inFormat)
        val outputFormat = DateTimeFormatter.ofPattern(outFormat)

        val date: LocalDate = LocalDate.parse(modDate, inputFormat)
        return date.format(outputFormat)
    }

    fun getDayOfWeekIndex(): Int{
        return LocalDate.now().dayOfWeek.value
    }

    fun getDayOfMonth(): Int{
        return LocalDate.now().dayOfMonth
    }

    fun dateIsTodaysDate(date: String): Boolean{
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateToCheck = LocalDate.parse(date, formatter)
        val currentDate = LocalDate.now()
        return dateToCheck == currentDate
    }
}
import java.text.SimpleDateFormat
import java.util.Locale

object DateZero {
    fun formatDateWithZeroPadding(date: String): String {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1].padStart(2, '0')
        val day = parts[2].padStart(2, '0')
        return "$year-$month-$day"
    }

    fun formatDate(date: String, time: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = originalFormat.parse(date)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(parsedDate)

        return "$formattedDate ($time)"
    }
}
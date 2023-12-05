object DateZero {
    fun formatDateWithZeroPadding(date: String): String {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1].padStart(2, '0')
        val day = parts[2].padStart(2, '0')
        return "$year-$month-$day"
    }
}
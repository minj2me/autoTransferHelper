package project.kotlin.johnnyzhao.com.autotransferhelper.utils

class TimeUtils {
    companion object {
        fun getTimeDiffInMins(ms: Long): Long {
            val ss = 1000
            val mi = ss * 60
            val hh = mi * 60
            val dd = hh * 24
            val diff = System.currentTimeMillis() - ms

            val day = diff / dd
            val hour = (diff - day * dd) / hh
            val minute = (diff - day * dd - hour * hh) / mi

            return Math.abs(minute)
        }
    }
}
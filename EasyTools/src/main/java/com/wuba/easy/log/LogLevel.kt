package com.wuba.easy.log

/**
 *
 * Created by wswenyue on 2021/12/17.
 */
enum class LogLevel(val value: Int, val shortName: String) {
    /**
     * Log.i
     */
    VERBOSE(1, "V"),

    /**
     * Log.d.
     */
    DEBUG(2, "D"),

    /**
     * Log.w.
     */
    WARN(3, "W"),

    /**
     * Log.e.
     */
    ERROR(4, "E");

    companion object {
        fun buildWithValue(value: Int): LogLevel {
            return when (value) {
                1 -> VERBOSE
                2 -> DEBUG
                3 -> WARN
                4 -> ERROR
                else -> ERROR
            }
        }

        fun buildWithShortName(name: String): LogLevel {
            return when (name) {
                "V" -> VERBOSE
                "D" -> DEBUG
                "W" -> WARN
                "E" -> ERROR
                else -> ERROR
            }
        }
    }
}
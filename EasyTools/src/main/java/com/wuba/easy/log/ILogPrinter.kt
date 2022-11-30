package com.wuba.easy.log

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
interface ILogPrinter {

    /**
     * log printer
     * @param msg Any
     * @param tag String?
     * @param level LogLevel
     * @param isOnlyPrintMsg Boolean
     */
    fun print(
        msg: Any,
        tag: String? = null,
        level: LogLevel = LogLevel.VERBOSE,
        isOnlyPrintMsg: Boolean = true
    )

}
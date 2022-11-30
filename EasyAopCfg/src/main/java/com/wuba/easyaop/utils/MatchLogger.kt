package com.wuba.easyaop.utils

import com.wuba.easy.log.Log
import com.wuba.easy.log.debug

/**
 *
 * Created by wswenyue on 2022/9/15.
 */

private const val aopSkipRecordLog = "match_record.txt"

fun Log.matchOnly(pattern: String, match: String, ext: String? = null, isJar: Boolean = false) {
    val msg = "[only:${if (isJar) "Jar" else "Source"}]${pattern}=>${match}<=${ext}"
    debug { msg }
    getFileLogger(aopSkipRecordLog).print(msg, isOnlyPrintMsg = true)
}

fun Log.matchSkip(pattern: String, match: String, ext: String? = null, isJar: Boolean = false) {
    val msg = "[skip:${if (isJar) "Jar" else "Source"}]${pattern}=>${match}<=${ext}"
    debug { msg }
    getFileLogger(aopSkipRecordLog).print(msg, isOnlyPrintMsg = true)
}
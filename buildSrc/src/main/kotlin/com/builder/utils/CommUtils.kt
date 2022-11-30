package com.builder.utils

import groovy.util.Node
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 *
 * Created by wswenyue on 2022/2/16.
 */
object CommUtils {
    /**
     * 判断字符串是否为空
     */
    fun <T : CharSequence> isEmpty(cs: T?): Boolean {
        if (cs == null) {
            return true
        }
        if (cs.isEmpty()) {
            return true
        }
        if (cs.isBlank()) {
            return true
        }
        return false
    }

    fun <T : CharSequence> isNotEmpty(cs: T?): Boolean {
        return !isEmpty(cs)
    }
}


fun List<String>.cmd(workingDir: File? = null): ProcessBuilder {
    return ProcessBuilder(this).apply {
        workingDir?.let {
            directory(it)
        }
        redirectOutput(ProcessBuilder.Redirect.PIPE)
        redirectError(ProcessBuilder.Redirect.PIPE)
    }
}

fun List<String>.runCommand(workingDir: File? = null): String? {
    return try {
        val proc = this.cmd(workingDir).start()
        proc.waitFor(60, TimeUnit.MINUTES)
        proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


fun Node.addChildIfValid(name: String?, value: String?): Node? {
    if (CommUtils.isEmpty(name) || CommUtils.isEmpty(value)) {
        return null
    }
    return appendNode(name, value)
}
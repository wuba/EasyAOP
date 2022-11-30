package com.wuba.easy.utils

import com.wuba.easy.log.Log
import com.wuba.easy.log.debug
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Manifest

/**
 *
 * Created by wswenyue on 2021/11/22.
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


    fun getDateTimeNowStr(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    fun getSystemEnv(key: String): String? {
        return System.getenv(key)
    }

    fun durationTimeFormat(durationMS: Long): String {
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        val day = durationMS / dd
        val hour = (durationMS - day * dd) / hh
        val minute = (durationMS - day * dd - hour * hh) / mi
        val second = (durationMS - day * dd - hour * hh - minute * mi) / ss
        val milliSecond = durationMS - day * dd - hour * hh - minute * mi - second * ss
        val sb = StringBuilder()
        if (day > 0) {
            sb.append(day).append("天")
        }
        if (hour > 0) {
            sb.append(hour).append("小时")
        }
        if (minute > 0) {
            sb.append(minute).append("分")
        }
        if (second > 0) {
            sb.append(second).append("秒")
        }
        if (milliSecond > 0) {
            sb.append(milliSecond).append("毫秒")
        }
        return sb.toString()
    }


    fun deleteFileOrDir(file: File?) {
        if (file == null || !file.exists()) {
            return
        }
        if (file.isFile) {
            file.delete()
        } else {
            file.deleteRecursively()
        }
        if (file.exists()) {
            file.delete()
        }
    }

    val availableCPUCount = Runtime.getRuntime().availableProcessors()

    fun getJarManifestAttribute(
        name: String,
        loader: ClassLoader? = null
    ): String? {
        val clsLoader: ClassLoader =
            loader ?: javaClass.classLoader
        Log.debug { "getJarManifestAttribute clsLoader:${clsLoader}" }
        return clsLoader
            .getResource("META-INF/MANIFEST.MF").also {
                Log.debug { "URL==>${it}" }
            }?.openStream().also {
                Log.debug { "inputStream is not null==>${it != null}" }
            }?.use {
                Manifest(it).mainAttributes.getValue(name)
            }
    }
}
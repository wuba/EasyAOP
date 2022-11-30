package com.wuba.easy.log

import com.wuba.easy.log.impl.ConsoleLogger
import com.wuba.easy.log.impl.FileLogger
import com.wuba.easy.utils.CommUtils
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
object Log : ILog, ILogPrinter {
    private const val nameDefFileLogger = "console.log"
    private val mDef: ILogPrinter = ConsoleLogger(nameDefFileLogger)
    var baseTag: String = ""

    var isLogEnable: Boolean = true
    var minLogLevel: LogLevel = LogLevel.DEBUG

    private val mFileLoggerMap: MutableMap<String, FileLogger> by lazy {
        Collections.synchronizedMap(
            HashMap<String, FileLogger>()
        )
    }

    private fun getDefFileLogger(): FileLogger? {
        if (!isInitialized()) {
            return null
        }
        return getFileLogger(nameDefFileLogger)
    }

    private var mLogRootDir: File? = null

    fun init(logRootDir: File) {
        mLogRootDir = logRootDir
    }

    private fun isInitialized(): Boolean {
        return mLogRootDir != null
    }

    private fun getFileLoggerSaveFile(name: String): File {
        if (!isInitialized()) {
            throw RuntimeException("Log not Initialized!!! Please initialize(call init method) before use .")
        }
        val fileName = if (name.endsWith(".txt") || name.endsWith(".log")) name else "$name.txt"
        return File(mLogRootDir, fileName)
    }

    /**
     * 获取FileLogger，如果不存在则进行创建，如果存在直接返回缓存
     * @param name String
     * @param isClean Boolean 该值只有在FileLogger第一次创建的时候生效
     * @return FileLogger
     */
    fun getFileLogger(name: String, isClean: Boolean = true): FileLogger {
        if (mFileLoggerMap.containsKey(name)) {
            return mFileLoggerMap[name] as FileLogger
        }
        return FileLogger(getFileLoggerSaveFile(name), isClean).apply {
            mFileLoggerMap[name] = this
        }
    }


    fun getTag(tag: String?): String {
        return if (CommUtils.isEmpty(tag)) {
            baseTag
        } else "${baseTag}(${tag})"
    }

    override fun print(
        msg: Any, tag: String?, level: LogLevel, isOnlyPrintMsg: Boolean
    ) {
        getDefFileLogger()?.print(msg, tag, level, isOnlyPrintMsg)
        // 控制台输出控制
        if (!isLogEnable) {
            return
        }
        if (minLogLevel.value > level.value) {
            return
        }
        mDef.print(msg, tag, level, isOnlyPrintMsg)
    }


    @JvmStatic
    @Synchronized
    fun closeResWaitIoFinished() {
        val size = mFileLoggerMap.size
        val latch = CountDownLatch(size)
        println("Log closeResWaitIoFinished======begin====size:${size}======".yellow())
        val iterator = mFileLoggerMap.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            println("Log closeResWaitIoFinished===closeWaitIoFinish==${next.key}==".yellow())
            next.value.closeWaitIoFinish(latch)
            iterator.remove()
        }
        try {
            latch.await(20, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            println("Log closeResWaitIoFinished=====end=====InterruptedException====".red())
        }
        println("Log closeResWaitIoFinished========end============".yellow())
    }

    override fun i(msg: Any, tag: String?) {
        print(msg, tag, LogLevel.VERBOSE, isOnlyPrintMsg = false)
    }

    override fun d(msg: Any, tag: String?) {
        print(msg, tag, LogLevel.DEBUG, isOnlyPrintMsg = false)
    }

    override fun w(msg: Any, tag: String?) {
        print(msg, tag, LogLevel.WARN, isOnlyPrintMsg = false)
    }

    override fun e(msg: Any, tag: String?) {
        print(msg, tag, LogLevel.ERROR, isOnlyPrintMsg = false)
    }
}


inline fun Log.debug(tag: String? = null, msg: () -> Any) {
    d(msg(), tag)
}

inline fun Log.warn(tag: String? = null, msg: () -> Any) {
    w(msg(), tag)
}

inline fun Log.info(tag: String? = null, msg: () -> Any) {
    i(msg(), tag)
}

inline fun Log.error(tag: String? = null, msg: () -> Any) {
    e(msg(), tag)
}
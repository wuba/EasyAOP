package com.wuba.easy.log.impl

import com.wuba.easy.utils.touch
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
class FileLogger(private val logFile: File, private val isClean: Boolean = true) :
    BaseLogPrinter(logFile.name) {
    private val mWriter: FileWriter by lazy {
        if (isClean && logFile.exists()) {
//            println("del log file=>${logFile.absolutePath}".red())
            logFile.delete()
        }
        FileWriter(logFile)
    }

    override fun closeRes() {
        mWriter.close()
    }

    override fun handleMsg(msg: String) {
        mWriter.writeLine(msg)
    }

    /**
     * 提供了文件的写入，避免日志丢失。log太多，文件太大都会导致日志丢失，
     * @constructor
     */
    private class FileWriter(private val baseFile: File) {
        private val size: AtomicInteger = AtomicInteger(0)
        private var index = 0
        private var mBuf: BufferedSink? = null;

        companion object {
            /**
             * 一个文件最多写2000次，一次最多10条，那么文件最大就是2W行
             */
            const val maxLength = 2000
        }


        @Synchronized
        private fun getWriter(): BufferedSink {
            if (size.get() >= maxLength) {
                size.set(0)
                mBuf?.flush()
                mBuf?.close()
                index += 1
                //new
                val name =
                    "${baseFile.name.substringBeforeLast(".", "")}${index}.${baseFile.extension}"
                mBuf = File(baseFile.parentFile, name).touch().sink().buffer()
            } else {
                if (mBuf == null) {
                    mBuf = baseFile.touch().sink().buffer()
                }
            }
            return mBuf as BufferedSink
        }

        @Synchronized
        fun writeLine(msg: String) {
            size.incrementAndGet()
            getWriter().writeUtf8(msg + "\n")
        }

        fun close() {
            mBuf?.flush()
            mBuf?.close()
        }
    }
}


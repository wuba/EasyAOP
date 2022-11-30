package com.wuba.easy.log.impl

import com.wuba.easy.log.*
import com.wuba.easy.utils.CommUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
abstract class BaseLogPrinter(private val logName: String) : ILogPrinter {


    companion object {
        @JvmStatic
        protected fun curTime(): String {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS")
            return current.format(formatter)
        }


        private fun formatColorString(level: LogLevel, tag: String?, msg: Any): String {
            val base = when (level) {
                LogLevel.VERBOSE -> ColorStr.White
                LogLevel.DEBUG -> ColorStr.Green
                LogLevel.WARN -> ColorStr.Yellow
                LogLevel.ERROR -> ColorStr.Red
            }
            val labels = ArrayList<IAnsiLabel>()
            labels.add(
                base.build(
                    "[${level.shortName}][${curTime()}][t:${Thread.currentThread().id}][${
                        Log.getTag(
                            tag
                        )
                    }]["
                )
            )
            when (msg) {
                is IAnsiLabel -> {
                    labels.addAll(msg.labelFlatten(base))
                }
                else -> {
                    labels.add(base.build(msg.toString()))
                }
            }
            labels.add(base.build("]"))
            return AnsiGroupLabel(labels).toString()
        }

        private fun format(level: LogLevel, tag: String?, msg: Any): String {
            val realMsg = if (msg is IAnsiLabel) {
                msg.originSource()
            } else {
                msg.toString()
            }
            return "[${level.shortName}][${curTime()}][t:${Thread.currentThread().id}][${
                Log.getTag(
                    tag
                )
            }][$realMsg]"
        }
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)


    private val mQueue: LinkedBlockingQueue<String> by lazy {
        LinkedBlockingQueue<String>().apply {
            Thread({
                dealMsg()
            }, logName).start()
        }
    }

    private fun fetchData(headMsg: String, max: Int = 10): List<String> {
        val list: ArrayList<String> = ArrayList()
        var msg: String? = headMsg
        var size: Int = 0
        while (msg != null) {
            list.add(msg)
            size++
            if (size >= max) {
                break
            }
            msg = mQueue.poll()
        }
        return list.asReversed()
    }

    private fun dealMsg() {
        try {
            handleMsg("*****************begin*****************")
            while (true) {
                val msg = mQueue.poll()
                if (msg == null) {
                    if (isClosed.get()) {
                        break
                    }
                    try {
                        Thread.sleep(10)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    handleMsg(fetchData(msg).joinToString("\n"))
                }
            }
            handleMsg("*****************close*****************")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            doClean()
        }
    }

    private fun doClean() {
        println("===${logName}======doClean================")
        try {
            closeRes()
            println("===${logName}======doClean==countDown==========".yellow())
            mLatch?.countDown()
            mLatch = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun isColorPrint(): Boolean {
        return false
    }

    protected abstract fun closeRes()

    protected abstract fun handleMsg(msg: String)


    override fun print(
        msg: Any, tag: String?, level: LogLevel, isOnlyPrintMsg: Boolean
    ) {
        if (isClosed.get()) {
            println("$tag printer already close !!!".red())
            return
        }
        val content: String = if (isOnlyPrintMsg) {
            msg.toString()
        } else {
            if (isColorPrint()) {
                formatColorString(level, tag, msg)
            } else {
                format(level, tag, msg)
            }
        }
        if (CommUtils.isEmpty(content)) {
            return
        }
        mQueue.offer(content.trim())
    }

    var mLatch: CountDownLatch? = null
    fun closeWaitIoFinish(latch: CountDownLatch) {
        if (isClosed.get()) {
            println("=====${logName}====closeWaitIoFinish====already close!!!===".red())
            latch.countDown()
            return
        }
        println("=====${logName}====closeWaitIoFinish=======")
        mLatch = latch
        isClosed.set(true)
    }
}
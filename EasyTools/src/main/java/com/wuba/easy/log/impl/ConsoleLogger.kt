package com.wuba.easy.log.impl

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
class ConsoleLogger(logName: String) : BaseLogPrinter(logName) {

    override fun handleMsg(msg: String) {
        println(msg)
    }

    override fun isColorPrint(): Boolean {
        return true
    }

    override fun closeRes() {
        //nothing
    }
}
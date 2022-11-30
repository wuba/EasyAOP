package com.wuba.easyaop.asm

import com.wuba.easy.log.Log
import com.wuba.easy.log.debug
import com.wuba.easyaop.asm.ASMConstant.aopResultLogName

/**
 *
 * Created by wswenyue on 2022/8/27.
 */


inline fun Log.result(msg: () -> Any) {
    debug { msg() }
    getFileLogger(aopResultLogName).print(msg(), isOnlyPrintMsg = true)
}
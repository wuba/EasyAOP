package com.wuba.easyaop.error

/**
 *
 * Created by wswenyue on 2022/9/13.
 */
class EasyAOPCfgError(errMsg: String) : RuntimeException(errMsg) {
    companion object {
        fun throwError(errMsg: String) {
            throw EasyAOPCfgError(errMsg)
        }
    }
}
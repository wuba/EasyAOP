package com.wuba.easy.log

/**
 *
 * Created by wswenyue on 2022/9/9.
 */
interface ILog {
    /**
     * for log VERBOSE or INFO
     * @param msg Any
     * @param tag String?
     */
    fun i(msg: Any, tag: String? = null)

    /**
     * for log DEBUG
     * @param msg Any
     * @param tag String?
     */
    fun d(msg: Any, tag: String? = null)

    /**
     * for log WARN
     * @param msg Any
     * @param tag String?
     */
    fun w(msg: Any, tag: String? = null)

    /**
     * for log ERROR
     * @param msg Any
     * @param tag String?
     */
    fun e(msg: Any, tag: String? = null)
}
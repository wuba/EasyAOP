package com.wuba.easyaop.asm.base

import com.wuba.easyaop.bean.ClassDesc

/**
 *
 * Created by wswenyue on 2021/12/19.
 */
interface IClassHandler {
    /**
     * @return String aopClassHandler name
     */
    fun handlerName(): String

    /**
     * handle class bytes
     * @param classDesc class info descriptor
     * @param classBytes class data bytes to be processed
     * @return ByteArray? process the finished result
     */
    fun doHandle(classDesc: ClassDesc, classBytes: ByteArray): ByteArray?
}
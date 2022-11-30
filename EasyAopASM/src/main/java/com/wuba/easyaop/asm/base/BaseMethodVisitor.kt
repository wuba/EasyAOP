package com.wuba.easyaop.asm.base

import com.wuba.easyaop.asm.ASMConstant.asmApiVersion
import org.objectweb.asm.MethodVisitor

/**
 *
 * Created by wswenyue on 2021/12/19.
 */
open class BaseMethodVisitor(mMv: MethodVisitor) :
    MethodVisitor(asmApiVersion, mMv) {
}
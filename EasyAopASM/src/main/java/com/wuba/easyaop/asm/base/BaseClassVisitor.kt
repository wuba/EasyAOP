package com.wuba.easyaop.asm.base

import com.wuba.easyaop.asm.ASMConstant.asmApiVersion
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 *
 * Created by wswenyue on 2021/12/19.
 */
abstract class BaseClassVisitor(private val mCv: ClassVisitor) :
    ClassVisitor(asmApiVersion, mCv) {
    /**
     * class changed
     */
    var classChanged: Boolean = false
    var isInterfaceClass: Boolean = false
        private set

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        isInterfaceClass = access and Opcodes.ACC_PROTECTED != 0
    }
}
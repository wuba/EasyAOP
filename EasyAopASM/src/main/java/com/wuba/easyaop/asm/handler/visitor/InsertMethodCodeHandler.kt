package com.wuba.easyaop.asm.handler.visitor

import com.wuba.easy.log.Log
import com.wuba.easyaop.asm.AOPUtils.packArgsArray
import com.wuba.easyaop.asm.base.BaseClassHandler
import com.wuba.easyaop.asm.base.BaseClassVisitor
import com.wuba.easyaop.asm.base.BaseMethodVisitor
import com.wuba.easyaop.asm.result
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import com.wuba.easyaop.cfg.warp.InsertItemWarp
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 在指定方法中插入代码
 * Created by wswenyue on 2021/12/19.
 */
class InsertMethodCodeHandler(cfg: EasyConfigWarp) : BaseClassHandler(cfg) {
    override fun onVisitorClass(classDesc: ClassDesc, visitor: ClassVisitor): BaseClassVisitor {
        return object : BaseClassVisitor(visitor) {
            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {

                val isAbstractMethod = access and Opcodes.ACC_ABSTRACT != 0
                val isNativeMethod = access and Opcodes.ACC_NATIVE != 0
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                if (isInterfaceClass || isAbstractMethod || isNativeMethod) {
                    return mv
                }
                val insertCfg = cfg.insertItemsCfg[classDesc] ?: return mv
                if (!insertCfg.isInsertInMethod(name, descriptor)) {
                    return mv
                }

                return InsertMethodVisitor(
                    handlerName(),
                    this,
                    mv,
                    classDesc,
                    insertCfg,
                    access,
                    name as String,
                    descriptor as String,
                    signature,
                    exceptions
                )
            }
        }
    }

    class InsertMethodVisitor(
        private val tag: String,
        private val classVisitor: BaseClassVisitor,
        mMv: MethodVisitor,
        val classDesc: ClassDesc,
        private val insertItemsCfg: InsertItemWarp,
        private val access: Int,
        private val methodName: String,
        private val methodDesc: String,
        val signature: String?,
        val exceptions: Array<out String>?
    ) : BaseMethodVisitor(mMv) {

        private val isStatic: Boolean by lazy {
            (access and Opcodes.ACC_STATIC) != 0
        }

        private val isInsertEnterCode: Boolean by lazy {
            insertItemsCfg.isInsertEnterCode(methodName, methodDesc)
        }

        private val isInsertExitCode: Boolean by lazy {
            insertItemsCfg.isInsertExitCode(methodName, methodDesc)
        }

        override fun visitEnd() {
            //设置一个较大的数，因为设置了自动计算，不会使用具体的数，设值只是为了触发自动计算。
            //如果开启了ASM字节码检查，这块需要设置一个较大的值，低于正常值或<=0会报错;暂设置20，一般情况下都可以
            mv.visitMaxs(20, 20)
            super.visitEnd()
        }

        override fun visitCode() {
            super.visitCode()
            if (isInsertEnterCode) {
                insertCodeEnterOrExit(true)
            }
        }

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN || opcode == Opcodes.ATHROW) {
                if (isInsertExitCode) {
                    insertCodeEnterOrExit(false)
                }
            }
            super.visitInsn(opcode)
        }

        private fun insertCodeEnterOrExit(isEnter: Boolean) {
            mv.visitInsn(if (isStatic) Opcodes.ICONST_1 else Opcodes.ICONST_0)
            mv.visitLdcInsn(classDesc.classFullName)
            mv.visitLdcInsn(methodName)
            mv.visitLdcInsn(methodDesc)
            packArgsArray(isStatic, methodDesc, mv)
            val insertClass: String
            val insertMethodName: String
            val insertMethodDesc =
                "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V"
            if (isEnter) {
                insertClass = insertItemsCfg.enterClass?.classPath as String
                insertMethodName = insertItemsCfg.enterMethodInfo?.methodName as String
            } else {
                insertClass = insertItemsCfg.exitClass?.classPath as String
                insertMethodName = insertItemsCfg.exitMethodInfo?.methodName as String
            }
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                insertClass,
                insertMethodName,
                insertMethodDesc,
                false
            )

            val logDesc = if (isEnter) "_Insert_Enter" else "_Insert_Exit"
            Log.result {
                "${tag}${logDesc}\t:${classDesc.className}.${methodName}()#${methodDesc}\t${classDesc.sourceRes}" + "\n"
                "${tag}${logDesc}\t\t:==>${insertClass}.${insertMethodName}()#${insertMethodDesc}"
            }

            classVisitor.classChanged = true
        }
    }


}
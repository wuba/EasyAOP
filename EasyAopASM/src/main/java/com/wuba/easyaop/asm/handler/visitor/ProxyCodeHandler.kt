package com.wuba.easyaop.asm.handler.visitor

import com.wuba.easy.log.Log
import com.wuba.easy.log.debug
import com.wuba.easy.log.error
import com.wuba.easyaop.asm.base.BaseClassHandler
import com.wuba.easyaop.asm.base.BaseClassVisitor
import com.wuba.easyaop.asm.base.BaseMethodVisitor
import com.wuba.easyaop.asm.result
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.bean.MethodInfo
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * TODO：未做测试
 * Created by wswenyue on 2021/12/19.
 */
class ProxyCodeHandler(cfg: EasyConfigWarp) : BaseClassHandler(cfg) {
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
                return ProxyMethodVisitor(
                    mv, this, MethodInfo(
                        methodName = name!!,
                        methodDesc = descriptor,
                        classDesc = classDesc
                    ), cfg, handlerName()
                )
            }
        }
    }

    private class ProxyMethodVisitor(
        mMv: MethodVisitor,
        private val cV: BaseClassVisitor,
        private val visitMethodInfo: MethodInfo,
        val cfg: EasyConfigWarp,
        val tag: String
    ) :
        BaseMethodVisitor(mMv) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?
        ) {
            visitMethodInsn(opcode, owner, name, descriptor, false)
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            if (name == null || owner == null || descriptor == null) {
                Log.error(tag) { "name == null || owner == null || descriptor == null !!!" }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                return
            }
            if (!(opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.INVOKEVIRTUAL)) {
                //INVOKESPECIAL: 调用需要特殊处理的实例方法，包括实例初始化方法，私有方法和父类方法
                //INVOKEINTERFACE: 调用接口方法，在运行时搜索一个实现这个接口方法的对象，找出合适的方法进行调用
                //INVOKESTATIC: 调用类方法(static)
                //INVOKEVIRTUAL: 调用对象的实例方法，根据对象的实际类型进行分派(虚拟机分派)
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
//                Log.info(tag) { "unSupport opcode==>${opcode};;=>owner:${owner};;name:${name};;descriptor:${descriptor}" }
                return
            }
            val invokeClassDesc = ClassDesc.buildWithUnknownPath(owner)
            val proxyCfg = cfg.proxyItemsCfg[invokeClassDesc]

            if (proxyCfg == null) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                return
            }
            //匹配到调用类
            val invokeMethodInfo =
                MethodInfo(name, descriptor, invokeClassDesc)
            val proxyMethodInfo = proxyCfg.foundProxyMethodInfo(invokeMethodInfo)
            if (proxyMethodInfo == null) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                return
            }
            if (proxyMethodInfo.classDesc == null) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                Log.error(tag) { "proxyMethodInfo not config classPath!!!=>${proxyMethodInfo}" }
                return
            }
            Log.debug(tag) {
                "begin proxy method ====>" +
                        "\n\t${invokeMethodInfo}" +
                        "\n\t${proxyMethodInfo}" +
                        "\n<===="
            }

            //proxy method
            when (opcode) {
                Opcodes.INVOKESTATIC -> {
                    //static 方法直接替换
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        proxyMethodInfo.classDesc?.classPath,
                        proxyMethodInfo.methodName,
                        descriptor,
                        isInterface
                    )
                    record(invokeMethodInfo, proxyMethodInfo, descriptor)

                }
                Opcodes.INVOKEVIRTUAL -> {
                    //对象方法，需要将实例对象消耗掉
                    if (!descriptor.startsWith("(")) {
                        throw RuntimeException("descriptor err!!!==>${invokeMethodInfo}")
                    }
                    val desc =
                        "(L${invokeMethodInfo.classDesc?.classPath};" + descriptor.substring(1)

                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        proxyMethodInfo.classDesc?.classPath,
                        proxyMethodInfo.methodName,
                        desc,
                        isInterface
                    )
                    record(invokeMethodInfo, proxyMethodInfo, desc)
                }
                else -> {
                    throw RuntimeException("UnSupport opcode:${opcode}==>${invokeMethodInfo}")
                }
            }
            cV.classChanged = true


        }

        private fun record(
            invokeMethodInfo: MethodInfo,
            proxyMethodInfo: MethodInfo,
            proxyMethodDesc: String
        ) {
            //class 方法
            //原始调用=》Hooker调用
            Log.result { "${tag}_Proxy\t:${visitMethodInfo}\t${visitMethodInfo.classDesc?.sourceRes}" }
            Log.result {
                "${tag}_Proxy\t\t:" +
                        "${invokeMethodInfo.classDesc?.className}.${invokeMethodInfo.methodName}()##${invokeMethodInfo.methodDesc}" +
                        "==>" +
                        "${proxyMethodInfo.classDesc?.className}.${proxyMethodInfo.methodName}()##${proxyMethodDesc}"
            }
        }
    }
}
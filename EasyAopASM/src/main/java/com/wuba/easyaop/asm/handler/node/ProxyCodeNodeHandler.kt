package com.wuba.easyaop.asm.handler.node

import com.wuba.easy.log.Log
import com.wuba.easy.log.debug
import com.wuba.easy.log.error
import com.wuba.easy.log.info
import com.wuba.easyaop.asm.base.BaseClassNodeHandler
import com.wuba.easyaop.asm.result
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.bean.MethodInfo
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

/**
 * Created by wswenyue on 2022/1/7.
 */
class ProxyCodeNodeHandler(cfg: EasyConfigWarp) : BaseClassNodeHandler(cfg) {

    private fun foundProxyInfo(
        visitClassDesc: ClassDesc,
        node: AbstractInsnNode
    ): Pair<MethodInfo, MethodInfo>? {
        if (node !is MethodInsnNode) {
            return null
        }
        val targetClassDesc = ClassDesc.buildWithUnknownPath(node.owner) //原调用方法类描述
        val proxyCfg = cfg.proxyItemsCfg[targetClassDesc] ?: return null

        val targetMethodInfo =
            MethodInfo(node.name, node.desc, targetClassDesc)

        //匹配到调用类
        val proxyMethodInfo = proxyCfg.foundProxyMethodInfo(targetMethodInfo) ?: return null

        if (!proxyCfg.isMatchCfg(visitClassDesc)) {
            Log.debug(handlerName()) { "no match ==${proxyCfg.targetClassDesc}==>${visitClassDesc}" }
            return null
        }

        if (proxyMethodInfo.classDesc == null) {
            Log.error(handlerName()) { "proxyMethodInfo not config classPath!!!=>${proxyMethodInfo}" }
            return null
        }
        return Pair(targetMethodInfo, proxyMethodInfo)
    }

    private fun record(
        visitMethodInfo: MethodInfo,
        invokeMethodInfo: MethodInfo,
        proxyMethodInfo: MethodInfo,
        proxyMethodDesc: String
    ) {
        //class 方法
        //原始调用=》Hooker调用
        Log.result {
            "${handlerName()}_Proxy\t:${visitMethodInfo}\t${visitMethodInfo.classDesc?.sourceRes}" +
                    "\n" +
                    "${handlerName()}_Proxy\t\t:" +
                    "${invokeMethodInfo.classDesc?.className}.${invokeMethodInfo.methodName}()##${invokeMethodInfo.methodDesc}" +
                    "==>" +
                    "${proxyMethodInfo.classDesc?.className}.${proxyMethodInfo.methodName}()##${proxyMethodDesc}"
        }
    }

    override fun doHandleNode(nodeVisitor: ClassNode, visitClassDesc: ClassDesc): Boolean {
        var hasChanged = false
        nodeVisitor.methods.forEach { method ->

            val scanMethod = MethodInfo(
                methodName = method.name,
                methodDesc = method.desc,
                classDesc = visitClassDesc
            )
//            Log.info(handlerName()) { "scan method::>${scanMethod}" }

            /**
             * opcode
             * INVOKESPECIAL: 调用需要特殊处理的实例方法，包括实例初始化方法，私有方法和父类方法
             * INVOKEINTERFACE: 调用接口方法，在运行时搜索一个实现这个接口方法的对象，找出合适的方法进行调用
             * INVOKESTATIC: 调用类方法(static)
             * INVOKEVIRTUAL: 调用对象的实例方法，根据对象的实际类型进行分派(虚拟机分派)
             */
            //instruction
            method.instructions?.iterator()?.forEach {
                when (it.opcode) {
                    Opcodes.INVOKESTATIC,
                    Opcodes.INVOKEVIRTUAL -> {
                        foundProxyInfo(visitClassDesc, it)?.run {
                            val target = it as MethodInsnNode
                            val invokeMethod = first
                            val proxyMethod = second
                            Log.debug(handlerName()) {
                                "do handle proxy aop:" +
                                        "\n::>scan method:${scanMethod}" +
                                        "\n\t::>target method:${invokeMethod}" +
                                        "\n\t::>proxy method:${proxyMethod}"
                            }

                            val desc = if (target.opcode == Opcodes.INVOKESTATIC) {
                                target.desc
                            } else {
                                //对象方法，需要添加上当前对象
                                if (!target.desc.startsWith("(")) {
                                    throw RuntimeException("descriptor err!!!==>${invokeMethod}")
                                }
                                "(L${invokeMethod.classDesc?.classPath};" + target.desc.substring(1)
                            }

                            target.opcode = Opcodes.INVOKESTATIC
                            target.owner = proxyMethod.classDesc!!.classPath
                            target.name = proxyMethod.methodName
                            target.desc = desc
                            hasChanged = true

                            record(
                                scanMethod,
                                invokeMethod,
                                proxyMethod,
                                desc
                            )
                        }
                    }
                }
            }
        }
        return hasChanged
    }
}
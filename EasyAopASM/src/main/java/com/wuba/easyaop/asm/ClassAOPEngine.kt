package com.wuba.easyaop.asm

import com.wuba.easy.log.*
import com.wuba.easyaop.asm.base.IClassHandler
import com.wuba.easyaop.asm.handler.node.EmptyCodeNodeHandler
import com.wuba.easyaop.asm.handler.node.ProxyCodeNodeHandler
import com.wuba.easyaop.asm.handler.visitor.InsertMethodCodeHandler
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.cfg.warp.EasyConfigWarp

/**
 * class aop Engine
 * Created by wswenyue on 2021/12/19.
 */
class ClassAOPEngine(private val cfg: EasyConfigWarp) : IClassHandler {

    /**
     * 给定一个 Class 返回对应的Class Code Handler Chain
     * 以下的调用链顺序，请斟酌后修改添加。Hander链同时修改一个class，先后顺序会对最终结果有影响。
     * @param classDesc ClassDesc
     * @return ArrayList<IClassHandler>
     */
    private fun getHandleChain(classDesc: ClassDesc): ArrayList<IClassHandler>? {
        if (!cfg.checkMatchRootClazz(classDesc)) {
            return null
        }
        val chain = ArrayList<IClassHandler>()

        if (cfg.isNeedEmptyHandle(classDesc)) {
            //need Empty Handler
            chain.add(EmptyCodeNodeHandler(cfg))
        }
        if (cfg.isNeedInsertHandle(classDesc)) {
            //need Insert Handler
            chain.add(InsertMethodCodeHandler(cfg))
        }

        // proxy Handler 判断需要分析整个class调用较耗性能，暂全部添加
        if (cfg.isNeedProxyHandle(classDesc)) {
            chain.add(ProxyCodeNodeHandler(cfg))
        }

        return chain
    }

    override fun handlerName(): String {
        return "ClassAOPEngine"
    }


    /**
     * handle class bytes
     * @param classDesc ClassDesc
     * @param classBytes ByteArray
     * @return ByteArray?
     */
    override fun doHandle(classDesc: ClassDesc, classBytes: ByteArray): ByteArray? {
        var targetBuf: ByteArray? = null
        getHandleChain(classDesc)?.onEach { handler ->
            val buf = handler.doHandle(
                classDesc,
                if (targetBuf == null) classBytes else (targetBuf as ByteArray)
            )
            if (buf != null) {
                Log.debug { "AOP Handle Succeed==>${classDesc}[${handler.handlerName()}]" }
                targetBuf = buf
            }
        }
        if (targetBuf == null) {
//            Log.info {
//                listOf(
//                    "AOP Modify finished ==>[changed:",
//                    "No".yellowLabel(),
//                    "] $classDesc"
//                ).toAnsiLabels()
//            }
        } else {
            Log.debug {
                listOf(
                    "AOP Modify finished ==>[changed:",
                    "Yes".redLabel(),
                    "] $classDesc"
                ).toAnsiLabels()
            }
        }

        return targetBuf
    }
}





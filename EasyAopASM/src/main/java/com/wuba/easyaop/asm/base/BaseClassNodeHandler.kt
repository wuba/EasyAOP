package com.wuba.easyaop.asm.base

import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter

/**
 *
 * Created by wswenyue on 2021/12/19.
 */
abstract class BaseClassNodeHandler(val cfg: EasyConfigWarp) : IClassHandler {
    override fun handlerName(): String {
        return javaClass.simpleName
    }

    override fun doHandle(classDesc: ClassDesc, classBytes: ByteArray): ByteArray? {
        val cr = ClassReader(classBytes)
        val node = ClassNode()
        cr.accept(node, ClassReader.SKIP_DEBUG)

        if (!doHandleNode(node, classDesc)) {
            return null
        }
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        node.accept(
            if (cfg.isNeedAopCheck)
                CheckClassAdapter(
                    cw,
                    true
                ) else cw
        )
        return cw.toByteArray()
    }

    protected abstract fun doHandleNode(nodeVisitor: ClassNode, visitClassDesc: ClassDesc): Boolean
}
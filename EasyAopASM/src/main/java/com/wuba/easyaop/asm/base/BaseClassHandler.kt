package com.wuba.easyaop.asm.base

import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.util.CheckClassAdapter

/**
 *
 * Created by wswenyue on 2021/12/19.
 */
abstract class BaseClassHandler(val cfg: EasyConfigWarp) : IClassHandler {
    var mDesc: ClassDesc? = null
    override fun handlerName(): String {
        return javaClass.simpleName
    }


    override fun doHandle(classDesc: ClassDesc, classBytes: ByteArray): ByteArray? {
        mDesc = classDesc
        val cr = ClassReader(classBytes)
        //ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        //COMPUTE_MAXS: 自动计算本地变量和操作数栈的大小，10%性能损耗
        //COMPUTE_FRAMES: 自动计算本地变量、操作数栈大小和所需的栈映射帧，50%性能损耗
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        val visitor: BaseClassVisitor = if (cfg.isNeedAopCheck) {
            onVisitorClass(classDesc, CheckClassAdapter(cw))
        } else {
            onVisitorClass(classDesc, cw)
        }
        cr.accept(visitor, ClassReader.SKIP_DEBUG)
        return if (!visitor.classChanged) {
            null
        } else cw.toByteArray()
    }

    /**
     *
     * @param classDesc ClassDesc
     * @param visitor ClassVisitor
     * @return BaseClassVisitor
     */
    protected abstract fun onVisitorClass(
        classDesc: ClassDesc,
        visitor: ClassVisitor
    ): BaseClassVisitor
}
package com.wuba.easyaop.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 *
 * Created by wswenyue on 2021/12/26.
 */
object AOPUtils {
    /**
     * 对非对象（基础数据）进行封箱操作
     *
     * @param mv   MethodVisitor
     * @param sort 类型
     */
    private fun warpBox(mv: MethodVisitor, sort: Int) {
        when (sort) {
            Type.BOOLEAN -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Boolean",
                "valueOf",
                "(Z)Ljava/lang/Boolean;",
                false
            )
            Type.CHAR -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Character",
                "valueOf",
                "(C)Ljava/lang/Character;",
                false
            )
            Type.BYTE -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Byte",
                "valueOf",
                "(B)Ljava/lang/Byte;",
                false
            )
            Type.SHORT -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Short",
                "valueOf",
                "(S)Ljava/lang/Short;",
                false
            )
            Type.INT -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Integer",
                "valueOf",
                "(I)Ljava/lang/Integer;",
                false
            )
            Type.FLOAT -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Float",
                "valueOf",
                "(F)Ljava/lang/Float;",
                false
            )
            Type.LONG -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Long",
                "valueOf",
                "(J)Ljava/lang/Long;",
                false
            )
            Type.DOUBLE -> mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Double",
                "valueOf",
                "(D)Ljava/lang/Double;",
                false
            )
            Type.OBJECT -> {
                //nothing
            }
            Type.ARRAY -> {
                //nothing
            }
            else -> {
                throw RuntimeException("Unsupported type!!!")
            }
        }
    }

    /**
     * 访问int类型的数据，如果数据在6以下（不包含6）是有系统定义的常量来使用。
     *
     * @param mv         MethodVisitor
     * @param visitIndex 访问数据
     */
    fun visitInsn(mv: MethodVisitor, visitIndex: Int) {
        if (visitIndex >= 6) {
            mv.visitIntInsn(Opcodes.BIPUSH, visitIndex)
        } else {
            when (visitIndex) {
                1 -> mv.visitInsn(Opcodes.ICONST_1)
                2 -> mv.visitInsn(Opcodes.ICONST_2)
                3 -> mv.visitInsn(Opcodes.ICONST_3)
                4 -> mv.visitInsn(Opcodes.ICONST_4)
                5 -> mv.visitInsn(Opcodes.ICONST_5)
                else -> mv.visitInsn(Opcodes.ICONST_0)
            }
        }
    }

    /**
     * 将入参封装到数组中
     * 注意：对象方法，数据第一个位置会放置该对象实例
     *
     * @param isStatic   是否是静态方法
     * @param methodDesc 方法描述
     * @param mv         methodVisitor
     */
    fun packArgsArray(isStatic: Boolean, methodDesc: String, mv: MethodVisitor) {
        var slotIndex = 0
        val methodType = Type.getMethodType(methodDesc)
        val argumentTypes: ArrayList<Type> = ArrayList()
        argumentTypes.addAll(methodType.argumentTypes)
        if (!isStatic) {
            argumentTypes.add(0, Type.getType(Object::class.java))
        }
        visitInsn(mv, argumentTypes.size) //初始化数组长度
        //mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getDescriptor(Object.class));//使用Type.getDescriptor会有问题
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object") //创建array
        for (i in argumentTypes.indices) {
            val t = argumentTypes[i]
            val sort = t.sort
            val size = t.size
            //String descriptor = t.getDescriptor();
            val opcode = t.getOpcode(Opcodes.ILOAD)
            mv.visitInsn(Opcodes.DUP)
            visitInsn(mv, i) //数组下标操作
            mv.visitVarInsn(opcode, slotIndex) //获取入参
            warpBox(mv, sort) //封箱
            mv.visitInsn(Opcodes.AASTORE) //save
            slotIndex += size
        }
    }
}
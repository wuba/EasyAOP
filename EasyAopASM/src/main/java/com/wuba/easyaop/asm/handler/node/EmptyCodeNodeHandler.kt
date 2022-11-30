package com.wuba.easyaop.asm.handler.node

import com.wuba.easy.log.Log
import com.wuba.easyaop.asm.base.BaseClassNodeHandler
import com.wuba.easyaop.asm.result
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 *
 * Created by wswenyue on 2021/12/19.
 */
class EmptyCodeNodeHandler(cfg: EasyConfigWarp) : BaseClassNodeHandler(cfg) {
    override fun doHandleNode(nodeVisitor: ClassNode, visitClassDesc: ClassDesc): Boolean {
        var classIsChanged = false
        nodeVisitor.methods?.filter { methodIsNeedHandle(visitClassDesc, it) }?.onEach {
            it.instructions = EmptyMethodReturnHelper.handle(it.desc)
            record(visitClassDesc, it)
            classIsChanged = true
        }
        return classIsChanged
    }


    private fun record(desc: ClassDesc, method: MethodNode) {
        //class 方法
        Log.result {
            "${handlerName()}_Empty\t:${desc.className}##${method.name}##${method.desc}\t${desc.sourceRes}"
        }
    }

    private fun methodIsNeedHandle(desc: ClassDesc, method: MethodNode?): Boolean {
        if (method == null) {
            return false
        }
        val item = cfg.emptyItemsCfg[desc] ?: return false

        return item.isNeedEmpty(method.name, method.desc)
    }

    /**
     * invokestatic：该指令用于调用静态方法，即使用 static 关键字修饰的方法；
     * invokespecial：该指令用于三种场景：调用实例构造方法，调用私有方法（即private关键字修饰的方法）和父类方法（即super关键字调用的方法）
     * invokeinterface：该指令用于调用接口方法，在运行时再确定一个实现此接口的对象；
     * invokevirtual：该指令用于调用虚方法（就是除了上述三种情况之外的方法）；
     * invokedynamic：在运行时动态解析出调用点限定符所引用的方法之后，调用该方法；在JDK1.7中推出，主要用于支持JVM上的动态脚本语言（如Groovy，Jython等）。
     */
    private object EmptyMethodReturnHelper {
        fun handle(desc: String): InsnList {
            return if (desc.endsWith(")V")) {
                returnVoid()
            } else if (desc.endsWith(")I")) {
                returnInt()
            } else if (desc.endsWith(")J")) {
                returnLong()
            } else if (desc.endsWith(")F")) {
                returnFloat()
            } else if (desc.endsWith(")D")) {
                returnDouble()
            } else if (desc.endsWith(")Z")) {
                returnBoolean()
            } else if (desc.endsWith(")Ljava/lang/String;")) {
                returnString()
            } else if (desc.endsWith(")Ljava/util/List;") || desc.endsWith(")Ljava/util/Collection;")) {
                returnList()
            } else if (desc.endsWith(")Ljava/util/Set;")) {
                returnSet()
            } else if (desc.endsWith(")Ljava/util/Map;")) {
                returnMap()
            } else if (desc.endsWith(")B")) {
                returnByte()
            } else if (desc.endsWith(")S")) {
                returnShort()
            } else if (desc.endsWith(")C")) {
                returnChar()
            } else {
                returnNull()
            }
        }

        private fun returnMap(): InsnList {
            val insnList = InsnList()
            insnList.add(TypeInsnNode(Opcodes.NEW, "java/util/HashMap"))
            insnList.add(InsnNode(Opcodes.DUP))
            insnList.add(
                MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    "java/util/HashMap",
                    "<init>",
                    "()V",
                    false
                )
            )
            insnList.add(InsnNode(Opcodes.ARETURN))
            return insnList
        }

        private fun returnList(): InsnList {
            val insnList = InsnList()
            insnList.add(
                MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "java/util/Collections",
                    "emptyList",
                    "()Ljava/util/List;",
                    false
                )
            )
            insnList.add(InsnNode(Opcodes.ARETURN))
            return insnList
        }

        private fun returnSet(): InsnList {
            val insnList = InsnList()
            insnList.add(
                MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "java/util/Collections",
                    "emptySet",
                    "()Ljava/util/Set;",
                    false
                )
            )
            insnList.add(InsnNode(Opcodes.ARETURN))
            return insnList
        }

        private fun returnString(): InsnList {
            val insnList = InsnList()
            insnList.add(LdcInsnNode(""))
            insnList.add(InsnNode(Opcodes.ARETURN))
            return insnList
        }

        private fun returnVoid(): InsnList {
            val insnList = InsnList()
            insnList.add(InsnNode(Opcodes.RETURN))
            return insnList
        }

        private fun returnBoolean(): InsnList {
            val insnList = InsnList()
            // insnList.add(new InsnNode(Opcodes.ICONST_1));//true
            insnList.add(InsnNode(Opcodes.ICONST_0)) //false
            insnList.add(InsnNode(Opcodes.IRETURN))
            return insnList
        }

        private fun returnDouble(): InsnList {
            val insnList = InsnList()
            insnList.add(LdcInsnNode(0.toDouble()))
            insnList.add(InsnNode(Opcodes.DRETURN))
            return insnList
        }

        private fun returnFloat(): InsnList {
            val insnList = InsnList()
            insnList.add(LdcInsnNode(0.toFloat()))
            insnList.add(InsnNode(Opcodes.FRETURN))
            return insnList
        }

        private fun returnLong(): InsnList {
            val insnList = InsnList()
            insnList.add(LdcInsnNode(0.toLong()))
            insnList.add(InsnNode(Opcodes.LRETURN))
            return insnList
        }

        private fun returnInt(): InsnList {
            val insnList = InsnList()
            insnList.add(IntInsnNode(Opcodes.BIPUSH, 0))
            insnList.add(InsnNode(Opcodes.IRETURN))
            return insnList
        }

        private fun returnChar(): InsnList {
            val insnList = InsnList()
            insnList.add(
                IntInsnNode(
                    Opcodes.SIPUSH,
                    0
                )
            )
            insnList.add(InsnNode(Opcodes.IRETURN))
            return insnList
        }

        private fun returnShort(): InsnList {
            val insnList = InsnList()
            insnList.add(
                IntInsnNode(
                    Opcodes.BIPUSH,
                    0
                )
            )
            insnList.add(InsnNode(Opcodes.IRETURN))
            return insnList
        }

        private fun returnByte(): InsnList {
            val insnList = InsnList()
            insnList.add(LdcInsnNode("0".toByte()))
            insnList.add(InsnNode(Opcodes.IRETURN))
            return insnList
        }

        private fun returnNull(): InsnList {
            val insnList = InsnList()
            insnList.clear()
            insnList.add(InsnNode(Opcodes.ACONST_NULL))
            insnList.add(InsnNode(Opcodes.ARETURN))
            return insnList
        }
    }
}
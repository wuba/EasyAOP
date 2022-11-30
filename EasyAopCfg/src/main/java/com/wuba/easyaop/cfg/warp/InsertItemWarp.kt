package com.wuba.easyaop.cfg.warp

import com.wuba.easy.utils.CommUtils
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.bean.MethodInfo
import com.wuba.easyaop.cfg.InsertItem
import com.wuba.easyaop.utils.CfgItemUtils
import com.wuba.easyaop.utils.CfgItemUtils.methodInfoListToMap

/**
 *
 * Created by wswenyue on 2021/12/20.
 */
class InsertItemWarp(val targetClassDesc: ClassDesc, private val source: InsertItem) {
    val enterClass: ClassDesc? by lazy {
        if (CommUtils.isEmpty(source.insertEnterClass)) {
            null
        } else {
            ClassDesc.buildWithUnknownPath(source.insertEnterClass)
        }
    }

    val enterMethodInfo: MethodInfo? by lazy {
        if (CommUtils.isEmpty(source.insertEnterMethodName)) {
            null
        } else {
            MethodInfo.build(source.insertEnterMethodName)
        }
    }

    val enterTargetMethodMap: HashMap<String, ArrayList<MethodInfo>> by lazy {
        methodInfoListToMap(source.targetEnterMethodList)
    }

    val exitClass: ClassDesc? by lazy {
        if (CommUtils.isEmpty(source.insertExitClass)) {
            null
        } else {
            ClassDesc.buildWithUnknownPath(source.insertExitClass)
        }
    }

    val exitMethodInfo: MethodInfo? by lazy {
        if (CommUtils.isEmpty(source.insertExitMethodName)) {
            null
        } else {
            MethodInfo.build(source.insertExitMethodName)
        }
    }

    private val exitTargetMethodMap: HashMap<String, ArrayList<MethodInfo>> by lazy {
        methodInfoListToMap(source.targetExitMethodList)
    }


    fun isInsertExitCode(methodName: String?, methodDesc: String?): Boolean {
        if (exitClass == null || exitMethodInfo == null) {
            return false
        }
        return CfgItemUtils.getMatchedMethodInfo(
            exitTargetMethodMap,
            methodName,
            methodDesc
        ) != null
    }

    fun isInsertEnterCode(methodName: String?, methodDesc: String?): Boolean {
        if (enterClass == null || enterMethodInfo == null) {
            return false
        }
        return CfgItemUtils.getMatchedMethodInfo(
            exitTargetMethodMap,
            methodName,
            methodDesc
        ) != null
    }

    /**
     * 是否在方法中插入code
     * @param methodName String
     * @param methodDesc String
     * @return Boolean
     */
    fun isInsertInMethod(methodName: String?, methodDesc: String?): Boolean {
        return isInsertEnterCode(methodName, methodDesc)
                || isInsertExitCode(methodName, methodDesc)
    }

}
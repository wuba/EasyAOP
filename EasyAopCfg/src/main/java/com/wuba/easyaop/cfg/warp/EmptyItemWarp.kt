package com.wuba.easyaop.cfg.warp

import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.bean.MethodInfo
import com.wuba.easyaop.utils.CfgItemUtils.getMatchedMethodInfo
import com.wuba.easyaop.utils.CfgItemUtils.methodInfoListToMap
import com.wuba.easyaop.cfg.EmptyItem

/**
 *
 * Created by wswenyue on 2021/12/20.
 */
class EmptyItemWarp(val targetClassDesc: ClassDesc, private val source: EmptyItem) {
    private val emptyMethodsMap: HashMap<String, ArrayList<MethodInfo>> by lazy {
        methodInfoListToMap(source.methodList)
    }

    fun isNeedEmpty(methodName: String, methodDesc: String): Boolean {
        return getMatchedMethodInfo(emptyMethodsMap, methodName, methodDesc) != null
    }

}
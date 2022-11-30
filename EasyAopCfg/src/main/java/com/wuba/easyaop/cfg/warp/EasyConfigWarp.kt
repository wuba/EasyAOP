package com.wuba.easyaop.cfg.warp

import com.wuba.easy.log.Log
import com.wuba.easy.log.LogLevel
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.bean.IMatchable
import com.wuba.easyaop.bean.MatchableHelper
import com.wuba.easyaop.cfg.EasyConfig
import com.wuba.easyaop.error.EasyAOPCfgError
import com.wuba.easyaop.utils.matchOnly
import com.wuba.easyaop.utils.matchSkip
import java.io.File
import java.util.*

/**
 *
 * Created by wswenyue on 2021/12/17.
 */
class EasyConfigWarp(
    private val cfg: EasyConfig
) {
    val isLogEnable: Boolean by lazy {
        cfg.logEnabled
    }
    val isNeedAopCheck: Boolean by lazy {
        cfg.needAopCheck
    }

    val isNeedDexCheck: Boolean by lazy {
        cfg.needDexCheck
    }
    val minLogLevel: LogLevel by lazy {
        LogLevel.buildWithValue(cfg.logLevel)
    }
    private val itemsCfgMatcher: CfgMatcher by lazy {
        CfgMatcher(this).apply { check() }
    }

    /**
     * Root 层 jar包匹配逻辑
     * @param jarFile File
     * @return Boolean
     */
    fun checkMatchRootJars(jarFile: File): Boolean {
        return itemsCfgMatcher.matchRootJars(jarFile)
    }

    /**
     * Root 层 class 的匹配逻辑
     * @param scanClass ClassDesc
     * @return Boolean
     */
    fun checkMatchRootClazz(scanClass: ClassDesc): Boolean {
        return itemsCfgMatcher.matchRootClazz(scanClass)
    }

    fun isNeedEmptyHandle(classDesc: ClassDesc): Boolean {
        return emptyItemsCfg.containsKey(classDesc)
    }

    fun isNeedInsertHandle(classDesc: ClassDesc): Boolean {
        return insertItemsCfg.containsKey(classDesc)
    }

    fun isNeedProxyHandle(classDesc: ClassDesc): Boolean {
        if (proxyItemsCfg.isEmpty()) {
            //没有配置proxy
            return false
        }
        if (itemsCfgMatcher.proxyItemsProxyClassSet.contains(classDesc)) {
            //跳过proxy类的处理，防止循环调用
            Log.matchSkip("proxyClass", classDesc.classFullName, ext = "ProxyClass")
            return false
        }

        return true
    }

    val emptyItemsCfg: HashMap<ClassDesc, EmptyItemWarp> by lazy {
        HashMap<ClassDesc, EmptyItemWarp>().apply {
            cfg.emptyItemsCfg?.filter {
                if (it.value == null) false else it.value.enabled
            }?.entries?.onEach {
                val classDesc = ClassDesc.buildWithUnknownPath(it.key)
                val item = it.value
                put(classDesc, EmptyItemWarp(classDesc, item))
            }
        }
    }

    /**
     * map<目标class,对应目标class的相关代理数据>
     */
    val proxyItemsCfg: HashMap<ClassDesc, ProxyItemWarp> by lazy {
        HashMap<ClassDesc, ProxyItemWarp>().apply {
            cfg.proxyItemsCfg?.filter {
                if (it.value == null) false else it.value.enabled
            }?.entries?.onEach {
                val targetClass = ClassDesc.buildWithUnknownPath(it.key)
                val item = it.value
                put(targetClass, ProxyItemWarp(targetClass, item))
            }
        }
    }


    val insertItemsCfg: HashMap<ClassDesc, InsertItemWarp> by lazy {
        HashMap<ClassDesc, InsertItemWarp>().apply {
            cfg.insertItemsCfg?.filter {
                if (it.value == null) false else it.value.enabled
            }?.entries?.onEach {
                val classDesc = ClassDesc.buildWithUnknownPath(it.key)
                val item = it.value
                put(classDesc, InsertItemWarp(classDesc, item))
            }
        }
    }

    /**
     * 职责：用于处理配置数据中的白名单和黑名单逻辑
     * 黑名单: skip...
     * 白名单: only...
     * 同级别黑白名单存在互斥性，白名单优先黑名单
     */
    private class CfgMatcher(easy: EasyConfigWarp) {
        private val rootSkipJars: Set<IMatchable> by lazy {
            HashSet<IMatchable>().apply {
                easy.cfg.skipJars?.onEach { skipPath ->
                    add(MatchableHelper.create(skipPath))
                }
            }
        }
        private val rootOnlyJars: Set<IMatchable> by lazy {
            HashSet<IMatchable>().apply {
                easy.cfg.onlyJars?.onEach { onlyPath ->
                    add(MatchableHelper.create(onlyPath))
                }
            }
        }
        private val rootSkipClazz: Set<IMatchable> by lazy {
            HashSet<IMatchable>().apply {
                easy.cfg.skipClazz?.onEach { skipPath ->
                    add(MatchableHelper.create(skipPath))
                }
            }
        }
        private val rootOnlyClazz: Set<IMatchable> by lazy {
            HashSet<IMatchable>().apply {
                easy.cfg.onlyClazz?.onEach { onlyPath ->
                    add(MatchableHelper.create(onlyPath))
                }
            }
        }

        /**
         * 所有代理类的集合，使用该集合用于跳过对设置代理类的遍历和修改，以防止重复循环
         */
        val proxyItemsProxyClassSet: HashSet<ClassDesc> by lazy {
            HashSet<ClassDesc>().apply {
                easy.proxyItemsCfg.values.onEach {
                    add(it.proxyClassDesc)
                }
            }
        }

        /**
         * check 配置黑白名单写的是否有问题
         */
        fun check() {
            //同级别黑白名单存在互斥
            if (rootOnlyJars.isNotEmpty() && rootSkipJars.isNotEmpty()) {
                //配置了白名单，黑名单失效
                EasyAOPCfgError.throwError("Config Root配置了OnlyJars，SkipJars失效。请将SkipJars移除或者注释!!!")
                return
            }

            if (rootOnlyClazz.isNotEmpty() && rootSkipClazz.isNotEmpty()) {
                //配置了白名单，黑名单失效
                EasyAOPCfgError.throwError("Config Root配置了OnlyClazz，SkipClazz失效。请将SkipClazz移除或者注释!!!")
                return
            }

        }

        /**
         * 处理Jar包的匹配逻辑
         * @param jarFile File
         * @return Boolean true：接受，进行后续aop处理；false：不接受，跳过后续aop处理
         */
        fun matchRootJars(jarFile: File): Boolean {
            if (!(jarFile.isFile && jarFile.extension.toLowerCase(Locale.getDefault()) == "jar")) {
                //只处理jar
                return false
            }
            val name = jarFile.name
            return if (rootOnlyJars.isNotEmpty()) {
                //如果设置了白名单，只有白名单中的jar才放行
                rootOnlyJars.find { match -> match.onMatch(name) }.let {
                    if (it != null) {
                        Log.matchOnly(it.pattern, name, isJar = true, ext = "Root")
                        true
                    } else {
                        false
                    }
                }
            } else {
                //如果没有设置白名单，只有不在黑名单中的jar才放行
                rootSkipJars.find { match -> match.onMatch(name) }.let {
                    if (it == null) {
                        //不在黑名单中，放行
                        true
                    } else {
                        Log.matchSkip(it.pattern, name, isJar = true, ext = "Root")
                        false
                    }
                }

            }
        }

        /**
         * 处理Root Clazz的匹配逻辑
         * 2. 判断root白名单
         * 3. 判断root黑名单
         * @param visitorClazz ClassDesc
         * @return Boolean true：接受，进行后续aop处理；false：不接受，跳过后续aop处理
         */
        fun matchRootClazz(visitorClazz: ClassDesc): Boolean {
            return if (rootOnlyClazz.isNotEmpty()) {
                //如果设置了白名单，只有白名单中的才放行
                rootOnlyClazz.find { match -> match.onMatch(visitorClazz.classFullName) }.let {
                    if (it != null) {
                        Log.matchOnly(it.pattern, visitorClazz.classFullName, ext = "Root")
                        true
                    } else {
                        false
                    }
                }
            } else {
                //如果没有设置白名单，只有不在黑名单中的才放行
                rootSkipClazz.find { match -> match.onMatch(visitorClazz.classFullName) }.let {
                    if (it == null) {
                        true
                    } else {
                        Log.matchSkip(it.pattern, visitorClazz.classFullName, ext = "Root")
                        false
                    }
                }
            }
        }

    }

}
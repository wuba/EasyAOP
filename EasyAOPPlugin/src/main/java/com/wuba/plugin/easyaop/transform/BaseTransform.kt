package com.wuba.plugin.easyaop.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project


/**
 *
 * Created by wswenyue on 2021/12/18.
 */
abstract class BaseTransform(
    val project: Project,
    val projectType: ProjectType
) : Transform() {
    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        onPreTransform(transformInvocation)
        doTransform(transformInvocation)
        onPostTransform(transformInvocation)
    }

    protected abstract fun doTransform(transformInvocation: TransformInvocation?)

    protected open fun onPostTransform(transformInvocation: TransformInvocation?) {

    }

    protected open fun onPreTransform(transformInvocation: TransformInvocation?) {

    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        // 配置 Transform 的输入类型为 Class
        return TransformManager.CONTENT_CLASS
    }


    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        // Library 配置作用域范围为工程
        if (projectType == ProjectType.Library) {
            return TransformManager.PROJECT_ONLY
        }
        // 其他情况 配置 Transform 的作用域为全工程
        return TransformManager.SCOPE_FULL_PROJECT
    }
}
package com.builder.publish

import com.builder.utils.CommUtils
import com.builder.utils.addChildIfValid
import com.builder.utils.runCommand
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Project
import org.gradle.api.XmlProvider

/**
 *
 * Created by wswenyue on 2022/2/16.
 */
class PomSetting(
    private val xml: XmlProvider,
    private val project: Project,
    private val packaging: String? = null
) {

    fun doSetting() {
        val root = xml.asNode() as Node
        checkPom(root)
        addPackaging(root)
        addDevelopers(root, project)
        addLicenses(root)
        addSCM(root)
    }

    private fun addPackaging(root: Node) {
        if (CommUtils.isNotEmpty(packaging)) {
            root.addChildIfValid("packaging", packaging)
        }
    }

    private fun findChildByName(parentNode: Node, childName: String): Node? {
        return parentNode.get(childName).let { childList ->
            if (childList is NodeList) {
                if (childList.isEmpty()) {
                    null
                } else {
                    childList.first() as Node
                }
            } else {
                null
            }
        }
    }

    //    <dependency>
//      <groupId>EasyAOPDemo</groupId>
//      <artifactId>EasyAopCfg</artifactId>
//      <version>unspecified</version>
//      <scope>runtime</scope>
//    </dependency>
    private fun checkPom(root: Node) {
        findChildByName(root, "dependencies")?.children()?.onEach { dev ->
            val dependencyNode = dev as Node
            val groupId = findChildByName(dependencyNode, "groupId")?.value()
            val artifactId = findChildByName(dependencyNode, "artifactId")?.value()
            val version = findChildByName(dependencyNode, "version")?.value()
            val scope = findChildByName(dependencyNode, "scope")?.value()

            if ("unspecified" == version) {
                throw RuntimeException("publish check Pom dependencies. The artifact(${groupId}.$artifactId:$version:$scope) unSupport!!!")
            }
        }
    }

    private fun addDevelopers(root: Node, project: Project) {
        //  <developers>
        //    <developer>
        //      <id>facebook</id>
        //      <name>facebook</name>
        //    </developer>
        //  </developers>
        val developersNode = root.appendNode("developers")
        listOf("git", "log", "--format=%aN %aE").runCommand(project.rootDir)
            ?.lines()?.filter { line -> CommUtils.isNotEmpty(line) }?.toSortedSet()
            ?.onEach { line: String ->
                println("--->$line")
                val sp = line.lastIndexOf(' ')
                if (sp > 0) {
                    val id = line.substring(0, sp).trim()
                    val email = line.substring(sp + 1).trim()
                    println("$id=>=$email")
                    developersNode.appendNode("developer").run {
                        addChildIfValid("id", id)
                        addChildIfValid("email", email)
                    }
                }
            }

    }

    private fun addLicenses(root: Node) {
        //  <licenses>
        //    <license>
        //      <name>MIT</name>
        //      <url>https://github.com/facebook/fresco/blob/main/LICENSE</url>
        //      <distribution>repo</distribution>
        //      <comments></comments>
        //    </license>
        //  </licenses>
        val licensesNode = root.appendNode("licenses")
        licensesNode.appendNode("license").run {
            addChildIfValid("name", "Apache License")
            addChildIfValid("url", "https://www.apache.org/licenses/LICENSE-2.0")
        }
    }


    private fun addSCM(root: Node) {
        //  <scm>
        //    <connection>scm:git:https://github.com/facebook/fresco.git</connection>
        //    <developerConnection>scm:git:git@github.com:facebook/fresco.git</developerConnection>
        //    <url>https://github.com/facebook/fresco.git</url>
        //  </scm>
        root.appendNode("scm").run {
            addChildIfValid("connection", "scm:git:git://github.com/wuba/EasyAOP.git")
            addChildIfValid("developerConnection", "scm:git:git@github.com:wuba/EasyAOP.git")
            addChildIfValid("url", "https://github.com/wuba/EasyAOP")
//            addChildIfValid("tag", "tag")
        }
    }
}
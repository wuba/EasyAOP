package com.wuba.easyaop.bean

import com.wuba.easy.utils.CommUtils.isEmpty
import com.wuba.easyaop.bean.IMatchable.Companion.REGEX_PRE

/**
 *
 * Created by wswenyue on 2022/8/24.
 */
abstract class IMatchable(val pattern: String) {
    abstract fun onMatch(target: String): Boolean

    companion object {
        const val REGEX_PRE = "_regex_"
    }
}


class RegexMatcher(pattern: String) : IMatchable(pattern) {
    private val _regex by lazy {
        Regex(pattern)
    }

    override fun onMatch(target: String): Boolean {
        return _regex.matches(target)
    }


    override fun toString(): String {
        return "RegexMatcher[$pattern]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegexMatcher

        if (pattern != other.pattern) return false

        return true
    }

    override fun hashCode(): Int {
        return pattern.hashCode()
    }

}

class StringMatcher(private val theContent: String) : IMatchable(theContent) {
    override fun onMatch(target: String): Boolean {
        if (isEmpty(theContent) || isEmpty(target)) {
            return false
        }
        return theContent == target
    }

    override fun toString(): String {
        return "StringMatcher[$theContent']"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringMatcher

        if (theContent != other.theContent) return false

        return true
    }

    override fun hashCode(): Int {
        return theContent.hashCode()
    }


}


object MatchableHelper {
    fun create(content: String): IMatchable {
        if (content.startsWith(REGEX_PRE)) {
            return RegexMatcher(content.substring(REGEX_PRE.length))
        }
        return StringMatcher(content)
    }
}

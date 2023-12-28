package com.xiaojinzi.aspectj.plugin

class PathMatcher(
    private val enableAdvancedMatch: Boolean,
    val includePackagePrefixFormatSet: Set<String>,
    val excludePackagePrefixFormatSet: Set<String>,
    val includePackagePatternFormatSet: Set<String>,
    val excludePackagePatternFormatSet: Set<String>,
) {

    /**
     * @param pattern com/xiaomi/**/miui
     * @param path com/xiaomi/miui
     */
    private fun pathMatch(pattern: String, path: String, pathLastIgnoreSize: Int = 0): Boolean {
        val patternParts = pattern.split("/")
        val pathParts = path.split("/")
        if (pathLastIgnoreSize < 0 || pathLastIgnoreSize > pathParts.size) {
            throw IllegalArgumentException("pathLastIgnoreSize must be in [0, ${pathParts.size}]")
        }
        if (patternParts.isEmpty()) {
            return false
        }
        // 如果两者 size 不同, 还得检查是否有 ** 在匹配多段的情况
        var indexForPattern = 0
        var indexForPath = 0
        var patternPart: String? = patternParts[indexForPattern]
        var nextPatternPartIndex = -1
        var nextPatternPart: String? = null
        while (indexForPath < (pathParts.size - pathLastIgnoreSize)) {
            val pathPart = pathParts[indexForPath]
            if (patternPart == "*") {
                indexForPattern += 1
                indexForPath += 1
                patternPart = patternParts.getOrNull(indexForPattern)
            } else if (patternPart == "**") {
                // 如果 ** 是最后一个, 那么就直接返回 true
                if (indexForPattern == patternParts.lastIndex) {
                    return true
                }
                if (nextPatternPartIndex == -1) { // 说明没找过
                    nextPatternPartIndex = indexForPattern + 1
                    var temp: String? = null
                    while (nextPatternPartIndex < patternParts.size) {
                        temp = patternParts[nextPatternPartIndex]
                        if (temp != "*" && temp != "**") {
                            nextPatternPart = temp
                            break
                        }
                        nextPatternPartIndex++
                    }
                }
                // 不可能是 "*" 或者 "**"
                if (nextPatternPart == null) {
                    return true
                }
                // 如果 ** 下一个匹配上了,
                if (nextPatternPart == pathPart) {
                    indexForPattern = nextPatternPartIndex + 1
                    indexForPath += 1
                    patternPart = patternParts.getOrNull(indexForPattern)
                    nextPatternPartIndex = -1
                    nextPatternPart = null
                } else {
                    indexForPath += 1
                }
            } else if (patternPart == pathPart) {
                indexForPattern += 1
                indexForPath += 1
                patternPart = patternParts.getOrNull(indexForPattern)
            } else {
                return false
            }
        }
        while (indexForPattern < patternParts.size) {
            patternPart = patternParts[indexForPattern]
            if (patternPart == "**") {
                indexForPattern++
                continue
            } else {
                return false
            }
        }
        return true
    }

    fun isMatch(path: String): Boolean {
        // 不支持 R.class
        /*if (path.endsWith(suffix = "/R.class")) {
            return false
        }*/
        // 默认不支持内部类
        /*if (path.indexOf(char = '$') > 0) {
            return false
        }*/
        return if (enableAdvancedMatch) {
            includePackagePatternFormatSet.any {
                pathMatch(
                    pattern = it,
                    path = path,
                    pathLastIgnoreSize = 1,
                )
            } && !excludePackagePatternFormatSet.any {
                pathMatch(
                    pattern = it,
                    path = path,
                    pathLastIgnoreSize = 1,
                )
            }
        } else {
            includePackagePrefixFormatSet.any {
                path.startsWith(prefix = it)
            } && !excludePackagePrefixFormatSet.any {
                path.startsWith(prefix = it)
            }
        }
    }

}
package jiachian.ws4j.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object OverlapFinder {
    private const val MARKER = "###"

    fun getOverlaps(gloss1: String, gloss2: String): Overlaps {
        val overlaps = Overlaps()
        var words0 = gloss1.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var words1 = gloss2.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        words0 = StopWordRemover.instance.removeStopWords(words0)
        words1 = StopWordRemover.instance.removeStopWords(words1)
        overlaps.length1 = words0.size
        overlaps.length2 = words1.size
        val overlapsLengths = mutableMapOf<Int, Int>()
        var matchStartIndex = 0
        var currIndex = -1
        while (currIndex < words0.size - 1) {
            currIndex++
            if (!contains(words1, words0, matchStartIndex, currIndex)) {
                overlapsLengths[matchStartIndex] = currIndex - matchStartIndex
                if ((overlapsLengths[matchStartIndex] ?: 0) > 0) {
                    currIndex--
                }
                matchStartIndex++
            }
        }
        for (i in matchStartIndex..currIndex) {
            overlapsLengths[i] = currIndex - i + 1
        }
        var longestOverlap = -1
        for (length in overlapsLengths.values) {
            if (longestOverlap < length) {
                longestOverlap = length
            }
        }
        overlaps.overlapsHash = ConcurrentHashMap(overlapsLengths.size)
        while (longestOverlap > 0) {
            for (i in 0 until overlapsLengths.size) {
                if (requireNotNull(overlapsLengths[i]) < longestOverlap) continue
                val stringEnd = i + longestOverlap - 1
                if (containsReplace(words1, words0, i, stringEnd)) {
                    val words0Sub = mutableListOf<String>()
                    words0Sub.addAll(words0.slice(i..stringEnd))
                    val temp = words0Sub.joinToString(" ")
                    synchronized(requireNotNull(overlaps.overlapsHash)) {
                        val v = overlaps.overlapsHash?.get(temp) ?: 0
                        overlaps.overlapsHash?.put(temp, v + 1)
                    }
                    for (j in i until i + longestOverlap) {
                        overlapsLengths[j] = 0
                    }
                    for (j in i - 1 downTo 0) {
                        if (requireNotNull(overlapsLengths[j]) <= i - j) break
                        overlapsLengths[j] = i - j
                    }
                } else {
                    var k = longestOverlap - 1
                    while (k > 0) {
                        val stringEndNew = i + k - 1
                        if (contains(words1, words0, i, stringEndNew)) break
                        k--
                    }
                    overlapsLengths[i] = k
                }
            }
            longestOverlap = -1
            for (length in overlapsLengths.values) {
                if (longestOverlap < length) {
                    longestOverlap = length
                }
            }
        }
        return overlaps
    }

    private fun containsReplace(
        words1: Array<String>,
        words2: Array<String>,
        begin: Int,
        end: Int
    ): Boolean {
        return contains(words1, words2, begin, end, true)
    }

    private fun contains(
        words1: Array<String>,
        words2: Array<String>,
        begin: Int,
        end: Int,
        doReplace: Boolean = false
    ): Boolean {
        val words2Sub = arrayOfNulls<String>(end - begin + 1)
        System.arraycopy(words2, begin, words2Sub, 0, end - begin + 1)
        if (words1.size < words2Sub.size) return false
        for (j in 0..words1.size - words2Sub.size) {
            if (words1[j] == MARKER) continue
            if (words2Sub[0] == words1[j]) {
                var match = true
                for (i in 1 until words2Sub.size) {
                    if (words1[j + i] == MARKER || words2Sub[i] != words1[j + i]) {
                        match = false
                        break
                    }
                }
                if (match) {
                    if (doReplace) {
                        for (k in j until j + words2Sub.size) {
                            words1[k] = MARKER
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    data class Overlaps(
        var overlapsHash: ConcurrentMap<String, Int>? = null,
        var length1: Int = 0,
        var length2: Int = 0,
    )
}
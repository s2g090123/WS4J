package jiachian.ws4j.util

import opennlp.tools.stemmer.PorterStemmer

class Morpha private constructor() {
    companion object {
        val instance = Morpha()
        private val stemmer = PorterStemmer()

        fun stemSentence(sentence: String): String {
            val stem = StringBuilder()
            sentence
                .split("\\s+".toRegex())
                .dropLastWhile { it.isEmpty() }
                .forEach { token ->
                    if (token.isNotEmpty()) {
                        stem.append(stemmer.stem(token)).append(" ")
                    }
                }
            return stem.toString()
        }
    }
}
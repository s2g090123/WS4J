package jiachian.lexical_db

import edu.mit.jwi.IRAMDictionary
import edu.mit.jwi.RAMDictionary
import edu.mit.jwi.data.ILoadPolicy
import edu.mit.jwi.item.ISynsetID
import edu.mit.jwi.item.Pointer
import edu.mit.jwi.item.SynsetID
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.Link
import jiachian.lexical_db.item.POS
import jiachian.ws4j.util.Morpha
import jiachian.ws4j.util.WS4JConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class MITWordNet : ILexicalDatabase {
    var dictionary: IRAMDictionary? = null
    private var glosses: ConcurrentMap<String, List<String>>? = null

    constructor(dict: IRAMDictionary) {
        dictionary = dict
        if (WS4JConfiguration.getInstance().useCache()) {
            glosses = ConcurrentHashMap()
        }
    }

    constructor() {
        try {
            loadWordNet()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (WS4JConfiguration.getInstance().useCache()) {
            glosses = ConcurrentHashMap()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun loadWordNet() {
        if (WS4JConfiguration.getInstance().useMemoryDB()) {
            LOGGER.info("Loading WordNet into memory")
            val t = System.currentTimeMillis()
            dictionary = RAMDictionary(
                requireNotNull(MITWordNet::class.java.classLoader.getResource(WORDNET_FILE)),
                ILoadPolicy.IMMEDIATE_LOAD
            ).apply {
                open()
            }
            LOGGER.info("WordNet loaded into memory in {} sec.", (System.currentTimeMillis() - t) / 1000L)
        } else {
            dictionary = RAMDictionary(
                requireNotNull(MITWordNet::class.java.classLoader.getResource(WORDNET_FILE)),
                ILoadPolicy.NO_LOAD
            ).apply {
                open()
            }
        }
    }

    override fun getConcept(
        lemma: String,
        pos: POS,
        sense: Int
    ): Concept? {
        val indexWord = dictionary?.getIndexWord(lemma, edu.mit.jwi.item.POS.getPartOfSpeech(pos.tag))
        return indexWord?.let {
            Concept(
                indexWord.wordIDs[sense - 1].synsetID.toString(),
                pos,
                lemma
            )
        }
    }

    override fun getAllConcepts(
        lemma: String,
        pos: POS
    ): List<Concept> {
        val indexWord = dictionary?.getIndexWord(lemma, edu.mit.jwi.item.POS.getPartOfSpeech(pos.tag))
        return indexWord?.wordIDs?.map { wordID ->
            Concept(
                wordID.synsetID.toString(),
                pos,
                lemma
            )
        } ?: emptyList()
    }

    override fun getLinkedSynsets(
        concept: Concept,
        link: Link?
    ): List<Concept> {
        val linkedSynsets = mutableListOf<Concept>()
        if (link == null || link == Link.SYNSET) {
            linkedSynsets.add(concept)
        } else {
            val synsetID = SynsetID.parseSynsetID(concept.synsetID)
            when (link) {
                Link.MERONYM -> {
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.MERONYM_MEMBER))
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.MERONYM_SUBSTANCE))
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.MERONYM_PART))
                }

                Link.HOLONYM -> {
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.HOLONYM_MEMBER))
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.HOLONYM_SUBSTANCE))
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.HOLONYM_PART))
                }

                else -> {
                    linkedSynsets.addAll(getRelatedSynsets(synsetID, link))
                }
            }
        }
        return linkedSynsets
    }

    private fun getRelatedSynsets(
        synsetID: ISynsetID,
        link: Link
    ): List<Concept> {
        return dictionary
            ?.getSynset(synsetID)
            ?.getRelatedSynsets(Pointer.getPointerType(link.symbol, null))
            ?.map { synset -> Concept(synset.toString()) }
            ?: emptyList()
    }

    override fun getWords(concept: Concept): List<String> {
        return dictionary
            ?.getSynset(SynsetID.parseSynsetID(concept.synsetID))
            ?.words
            ?.map { obj -> obj.lemma }
            ?: emptyList()
    }

    override fun getGloss(
        concept: Concept,
        link: Link?
    ): List<String> {
        val key = "$concept $link"
        if (WS4JConfiguration.getInstance().useCache()) {
            glosses?.get(key)?.let {
                return it
            }
        }
        val linkedSynsets = getLinkedSynsets(concept, link)
        val glosses = mutableListOf<String>()
        for (linkedSynset in linkedSynsets) {
            var gloss = if (Link.SYNSET == link) {
                concept.name
            } else {
                dictionary
                    ?.getSynset(SynsetID.parseSynsetID(linkedSynset.synsetID))
                    ?.gloss
                    ?.replaceFirst("; \".+".toRegex(), "")
            } ?: continue
            gloss = gloss
                .replace("[.;:,?!(){}\"`$%@<>]".toRegex(), " ")
                .replace("&".toRegex(), " and ")
                .replace("_".toRegex(), " ")
                .replace(" +".toRegex(), " ")
                .replace("(?<!\\w)'".toRegex(), " ")
                .replace("'(?!\\w)".toRegex(), " ")
                .replace("--".toRegex(), " ")
                .lowercase(Locale.getDefault())
            if (WS4JConfiguration.getInstance().useStem()) {
                gloss = Morpha.stemSentence(gloss)
            }
            glosses.add(gloss)
        }
        if (WS4JConfiguration.getInstance().useCache()) {
            this.glosses?.put(key, glosses.toList())
        }
        return glosses
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MITWordNet::class.java)

        private const val WORDNET_FILE = "wn30.dict"
    }
}

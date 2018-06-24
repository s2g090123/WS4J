package edu.uniba.di.lacam.kdde.ws4j.util;

import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.lexical_db.item.POS;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final public class ICFinder {

	private static final ICFinder IC = new ICFinder();

	private ConcurrentMap<Integer, Integer> freqV;
	private ConcurrentMap<Integer, Integer> freqN;

	private final static int rootFreqN = 128767; // sum of all root freq of n in IC-semcor.dat
	private final static int rootFreqV = 95935;  // sum of all root freq of v in IC-semcor.dat

	private ICFinder(){
		try {
			loadIC();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ICFinder getIC(){
		return ICFinder.IC;
	}
	
	private synchronized void loadIC() throws IOException {
		String ICFileName = WS4JConfiguration.getInstance().getInfoContent();
		freqV = new ConcurrentHashMap<>();
		freqN = new ConcurrentHashMap<>();
		InputStream stream = ICFinder.class.getResourceAsStream("/"+ ICFileName);
		InputStreamReader isr = new InputStreamReader(stream);
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			String[] elements = line.split(" ");
			if (elements.length >= 2) {
				String e = elements[0];
				POS pos = POS.getPOS(e.charAt(e.length()-1));
				int id = Integer.parseInt(e.substring(0, e.length()-1));
				int freq = Integer.parseInt(elements[1]);
				if (pos.equals(POS.NOUN)) freqN.put(id, freq);
				else if (pos.equals(POS.VERB)) freqV.put(id, freq);
			}
		}
		br.close();
		isr.close();
	}
	
	public List<PathFinder.Subsumer> getLCSbyIC(PathFinder pathFinder, Concept synset1, Concept synset2,
                                                StringBuilder tracer) {
		List<PathFinder.Subsumer> paths = pathFinder.getAllPaths(synset1, synset2, tracer);
		if (paths == null || paths.size() == 0) return null;
		for (PathFinder.Subsumer path : paths) path.ic = IC(pathFinder, path.subsumer);
		paths.sort((s1, s2) -> Double.compare(s2.ic, s1.ic));
		List<PathFinder.Subsumer> results = new ArrayList<>(paths.size());
		for (PathFinder.Subsumer path : paths) {
			if (path.ic == paths.get(0).ic) results.add(path);
		}
		return results;
	}

	public double IC(PathFinder pathFinder, Concept synset) {
		POS pos = synset.getPOS();
		if (pos.equals(POS.NOUN) || pos.equals(POS.VERB)) {
			double prob = probability(pathFinder, synset);
			return prob > 0.0D ? - Math.log(prob) : 0.0D;
		} else return 0.0D;
	}

	private double probability(PathFinder pathFinder, Concept synset) {
		Concept rootSynset = pathFinder.getRoot(synset.getSynsetID());
		int rootFreq = 0;
		if (RelatednessCalculator.useRootNode) {
			if (synset.getPOS().equals(POS.NOUN)) rootFreq = rootFreqN;
			else if (synset.getPOS().equals(POS.VERB)) rootFreq = rootFreqV;
		} else rootFreq = getFrequency(rootSynset);
		int offFreq = getFrequency(synset);
		if (offFreq <= rootFreq) return (double) offFreq / (double) rootFreq;
		return 0.0D;
	}
	
	public int getFrequency(Concept synset) {
		if (synset.getSynsetID().equals("0")) {
			if (synset.getPOS().equals(POS.NOUN)) return rootFreqN;
			else if (synset.getPOS().equals(POS.VERB)) return rootFreqV;
		}
		int synsetID = Integer.parseInt(synset.getSynsetID().replaceAll("[^\\d]", ""));
		int freq = 0;
		if (synset.getPOS().equals(POS.NOUN)) {
			Integer freqObj = freqN.get(synsetID);
			freq = freqObj != null ? freqObj : 0;
		} else if (synset.getPOS().equals(POS.VERB)) {
			Integer freqObj = freqV.get(synsetID);
			freq = freqObj != null ? freqObj : 0;
		}
		return freq;
	}
}

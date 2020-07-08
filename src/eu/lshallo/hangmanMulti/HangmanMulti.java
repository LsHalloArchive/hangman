package eu.lshallo.hangmanMulti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class HangmanMulti {
	private static int NUM_THREADS = 3;
	private static int simulationsPerWord = 1000000;
	private static ConcurrentHashMap<String, Integer> resultsMap = new ConcurrentHashMap<String, Integer>(1900000, 1, NUM_THREADS);
	//private static HashMap<String, Integer> resultsMap = new HashMap<String, Integer>();
	private static String[] words = {"Jazz", "Baum", "Straße", "Zug", "Haus"};

	
	
	public static void main(String[] args) {
		ArrayList<String> readWordList = new ArrayList<String>();
		File wordListFile = new File("germanShort.dic");
		try {
			BufferedReader br = new BufferedReader(new FileReader(wordListFile));
			String line = "";
			while((line = br.readLine()) != null) {
				readWordList.add(line);
			}
			System.out.println("Read " + readWordList.size() + " words.\nTotal iterations: " + (readWordList.size() * simulationsPerWord));
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		words = new String[readWordList.size()];
		for(int i = 0; i < readWordList.size(); i++) {
			words[i] = readWordList.get(i);
		}
		
		for(int i = 0; i < words.length; i++) {
			resultsMap.put(words[i], 0);
		}
		
		HangmanThread[] threads = new HangmanThread[NUM_THREADS];
		int wordsPerThread = (int) Math.floor((double)words.length / NUM_THREADS);
		for(int i = 0; i < NUM_THREADS; i++) {
			if(i < NUM_THREADS - 1) {
				System.out.println("Initializing thread " + i + " from index " + i * wordsPerThread + " to " + ((i + 1) * wordsPerThread - 1));
				threads[i] = new HangmanThread(i + "", Arrays.copyOfRange(words, i * wordsPerThread, (i + 1) * wordsPerThread - 1), simulationsPerWord);
			} else {
				System.out.println("Initializing thread " + i + " from index " + i * wordsPerThread + " to " + words.length);
				threads[i] = new HangmanThread(i + "", Arrays.copyOfRange(words, i * wordsPerThread, words.length), simulationsPerWord);
			}
		}
		
		long pre = System.currentTimeMillis();
		for(int i = 0; i < NUM_THREADS; i++) {
			threads[i].start();
		}
		
		for(int i = 0; i < NUM_THREADS; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long post = System.currentTimeMillis();
		
		printResults(resultsMap);
		System.out.println("Total runtime: " + (post-pre) + "ms");
	}
	
	private static void printResults(Map<String, Integer> mp) {
		Iterator<Entry<String, Integer>> it = mp.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, Integer> pair = (Entry<String, Integer>) it.next();
			System.out.println(pair.getKey() + ": " + pair.getValue());
		}
	}
	
	public static void incrementResult(String word) {
		resultsMap.put(word, resultsMap.get(word) + 1);
	}
}

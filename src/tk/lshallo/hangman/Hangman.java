package tk.lshallo.hangman;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Hangman {
	
	static final String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ẞ"};
	static String[] wordList = {"Jazz", "Asphalt", "Baum", "Straßenreinigungsdienst", "Drehmomentschlüssel", "Zebra", "Opera"};
	static final int MAX_ERRORS = 10;
	static int ITERATIONS = 1000000;
	static long GAMES = wordList.length * ITERATIONS;
	static final double[] LETTER_PROBABILITY = {0.0651, 0.0189, 0.0306, 0.0508, 0.174, 0.0166, 0.0301, 0.0476, 0.0755, 0.0027, 0.0121, 0.0344, 0.0253, 0.0978, 0.0251, 0.0079, 0.0002, 0.07, 0.0727, 0.0615, 0.0435, 0.067, 0.0189, 0.0003, 0.0004, 0.03, 0.0031};
	
	static HashMap<String, Integer> results = new HashMap<String, Integer>();
	static HashMap<String, Integer> played = new HashMap<String, Integer>();
	
	public static void main(String[] args) {
		
		ArrayList<String> readWordList = new ArrayList<String>();
		File wordListFile = new File("germanShort.dic");
		try {
			BufferedReader br = new BufferedReader(new FileReader(wordListFile));
			String line = "";
			while((line = br.readLine()) != null) {
				readWordList.add(line);
			}
			GAMES = readWordList.size() * ITERATIONS;
			
			System.out.println("Read " + readWordList.size() + " words.\nTotal iterations: " + GAMES);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		wordList = new String[readWordList.size()];
		for(int i = 0; i < readWordList.size(); i++) {
			wordList[i] = readWordList.get(i).replace("ü", "ue")
									 .replace("ö", "oe")
									 .replace("ä", "ae");
			results.put(wordList[i], 0);
			played.put(wordList[i], 0);
		}
		
		System.out.println("Replaced malicious chars and set hashmaps");
		
		long totalPre = System.currentTimeMillis();
		long pre = System.currentTimeMillis();
		for(long i = 0; i < GAMES; i++) {
			if(i % 500000 == 0) {
				long post = System.currentTimeMillis();
				System.out.println("Iteration " + i + " of " + GAMES + " (" + ((double)i/(double)GAMES * 100) + "%)");
				System.out.println("Estimated time left: " + ((GAMES - i) / 500000. * (post-pre)) / 60000. + "min");
				pre = System.currentTimeMillis();
			}
			String word = wordList[(int) (i % wordList.length)];
			boolean result = playGame(word, alphabet, LETTER_PROBABILITY, MAX_ERRORS);
			if(result) {
				results.put(word, results.get(word) + 1);
			}
			played.put(word, played.get(word) + 1);
		}
		long totalPost = System.currentTimeMillis();
		
		File out = new File("out.txt");
		boolean file = true;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(out));
		} catch (IOException e) {
			e.printStackTrace();
			file = false;
		}
		results = results.entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(100000).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		Set<String> it = results.keySet();
		for(String s : it) {
			System.out.println(s + ": " + results.get(s) + "/" + played.get(s));
			if(file) {
				try {
					bw.write(s + ": " + results.get(s) + "/" + played.get(s) + System.lineSeparator());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total runtime: " + (totalPost - totalPre) + "ms");
	}
	
	public static boolean playGame(String word, String[] alphabet, double[] letterProbabilty, int errors) {
		word = word.toUpperCase();
		HashMap<Character, Boolean> wordStatus = new HashMap<Character, Boolean>();
		
		char[] wordChars = word.toCharArray();
		for(int i = 0; i < wordChars.length; i++) {
			wordStatus.put(wordChars[i], false);
		}
		
		double sumOfWeights = 0;
		for(int i = 0; i < letterProbabilty.length; i++) {
			sumOfWeights += letterProbabilty[i];
		}
		
		int mistakes = 0;
		while(mistakes < errors) {
			int letterGuess = generateRandomWeightedInt(letterProbabilty, sumOfWeights);
			if(word.contains(alphabet[letterGuess])) {
				wordStatus.put(alphabet[letterGuess].toCharArray()[0], true);
			} else {
				mistakes++;
			}
			
			boolean right = true;
			Set<Character> it = wordStatus.keySet();
			for(char c : it) {
				if(!wordStatus.get(c)) {
					right = false;
				}
			}
			if(right) {
				return true;
			}
		}
		return false;
	}
	
	public static int generateRandomWeightedInt(double[] letterProbability, double sumOfWeights) {
		
		double random = Math.random() * sumOfWeights;
		for(int i = 0; i < letterProbability.length; i++) {
			if(random < letterProbability[i])
				return i;
			random -= letterProbability[i];
		}
		
		return 0;
	}
	
	public static int countLines(String filename) throws IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		}
	}

}

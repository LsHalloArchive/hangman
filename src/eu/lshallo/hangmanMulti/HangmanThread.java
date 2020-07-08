package eu.lshallo.hangmanMulti;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

public class HangmanThread extends Thread {

	private String name;
	private String[] words;
	private int simulationsPerWord;
	private String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ẞ"};
	private double[] LETTER_PROBABILTY = {0.0651, 0.0189, 0.0306, 0.0508, 0.174, 0.0166, 0.0301, 0.0476, 0.0755, 0.0027, 0.0121, 0.0344, 0.0253, 0.0978, 0.0251, 0.0079, 0.0002, 0.07, 0.0727, 0.0615, 0.0435, 0.067, 0.0189, 0.0003, 0.0004, 0.03, 0.0031};
	private int MAX_ERRORS = 10;
	private double sumOfWeightedProbability;
	
	public HangmanThread(String name, String[] words, int simulationsPerWord) {
		this.name = name;
		this.words = words;
		this.simulationsPerWord = simulationsPerWord;
		
		for(int i = 0; i < LETTER_PROBABILTY.length; i++) {
			sumOfWeightedProbability += LETTER_PROBABILTY[i];
		}
		System.out.println("Thread " + this.name + " with " + this.words.length + " words initialized.");
	}
	
	@Override
	public void run() {
		DecimalFormat df = new DecimalFormat("#.######min");
		for(int i = 0; i < this.words.length; i++) {
			long pre = System.currentTimeMillis();
			String unsanitizedWord = words[i];
			String word = sanitizeString(unsanitizedWord);
			HashMap<Character, Boolean> wordStatus = new HashMap<Character, Boolean>();
			
			char[] wordChars = word.toCharArray();
			for(int k = 0; k < this.simulationsPerWord; k++) {
				for(int l = 0; l < wordChars.length; l++) {
					wordStatus.put(wordChars[l], false);
				}
				
				int errors = 0;
				boolean guessed = false;
				while(errors <= this.MAX_ERRORS && !guessed) {
					String guess = this.alphabet[generateRandomWeightedInt(this.LETTER_PROBABILTY, this.sumOfWeightedProbability)];
					if(word.contains(guess)) {
						wordStatus.put(guess.charAt(0), true);
					} else {
						errors++;
					}
					boolean correct = true;
					Set<Character> it = wordStatus.keySet();
					for(char c : it) {
						if(!wordStatus.get(c)) {
							correct = false;
						}
					}
					if(correct) {
						guessed = true;
						HangmanMulti.incrementResult(unsanitizedWord);
					}
				}
				
			}
			long post = System.currentTimeMillis();
			if(i % Math.floor(this.words.length /  100) == 0) {
				System.out.println("Thread " + this.name + " finished a word in " + df.format((post - pre) / 60000.) + "\nTime remaining: " + df.format((this.words.length - i) * (post-pre) / 60000.));
			}
		}
		
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
	
	private String sanitizeString(String word) {
		word = word.replace("ö", "oe").replace("ü", "ue").replace("ä", "ae");
		return word.toUpperCase();
	}

}

package welchezukunft;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import welchezukunft.timeline;
import processing.core.PApplet;



public class WordCloudTimeline {

	public static timeline timeLine;
	public static List<String> taggedText;
	public static boolean running = false;

	static List<String> sentenceList;
	public static List<String> words;

	public WordCloudTimeline(){
		
	}
	
	public static void main(String[] args0){
		sentenceList = new ArrayList<String>();
		WordCloudTimeline mainSketch = new WordCloudTimeline();
		mainSketch.start();
		
	}
	
	public void start(){
		try {
			readNouns("./resources/text/nouns_verbs.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String[] processingArgs = {"timeline"};
		timeLine = new timeline();
		PApplet.runSketch(processingArgs, timeLine);
	}
	
	

	public void readNouns(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        
	        
	       String[] wordBuff = split(sb.toString());
	       words = Arrays.asList(wordBuff);
	    } finally {
	        br.close();
	    }
	}
	
	
	public String[] split(String text) {
	        String [] tokens = text.split("[\\W]");
	        return tokens;
	    }
	
}

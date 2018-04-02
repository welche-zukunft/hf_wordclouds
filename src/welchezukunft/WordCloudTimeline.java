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
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.StringUtils;


public class WordCloudTimeline {

	public static String input = "At the beginning of the second decade of the Twenty-First Century, global civilization faces a new breed of cataclysm.";
	public static String preprocess;
	
	public static Tagger tagWords;
	public static timeline timeLine;
	public static List<String> taggedText;
	public static boolean running = false;
	public static Pipeline pipe;
	
	public static List<List<HasWord>> sentences;
	static List<String> sentenceList;
	
	public static List<String> words;

	
	public WordCloudTimeline(){
		
	}
	
	public static void main(String[] args0){
		sentences = new ArrayList<List<HasWord>>();
		sentenceList = new ArrayList<String>();
		WordCloudTimeline mainSketch = new WordCloudTimeline();
		mainSketch.start();
		
	}
	
	public void start(){
		/*
		try {
			readFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//pipe = new Pipeline();
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
	
	
	public static void readFile() throws FileNotFoundException{	
	
           
			DocumentPreprocessor dp = new DocumentPreprocessor(new FileReader("./resources/text/manifest2.txt"));
		      for (List<HasWord> sentence : dp) {
		    	  sentences.add(sentence);
		    	  System.out.println(sentence.toString());
		      }
		     
	      List<CoreLabel> tokens = new ArrayList<CoreLabel>();
	      PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(input), new CoreLabelTokenFactory(), "");
	      while (tokenizer.hasNext()) {
	    	  tokens.add(tokenizer.next());
	      }
	      //// Split sentences from tokens
	      List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>().process(tokens);
	      //// Join back together
	      int end;
	      int start = 0;
	      sentenceList = new ArrayList<String>();
	      for (List<CoreLabel> sentence: sentences) {
	    	  end = sentence.get(sentence.size()-1).endPosition();
	    	  sentenceList.add(input.substring(start, end).trim());
	    	  start = end;
	      }
	      System.out.println(StringUtils.join(sentenceList, " _ "));
	      preprocess = StringUtils.join(sentenceList, " _ ");
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

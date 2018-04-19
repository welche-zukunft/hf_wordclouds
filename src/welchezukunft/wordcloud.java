package welchezukunft;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.swing.plaf.synth.SynthSpinnerUI;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;


public class wordcloud {
	
	int id;
	String name;
	
	int wordcount;
	float minY;
	float maxY;
	float lastposX = 0;
	
	int starttime;
	int endtime;
	
	private int r1, r2, g1, g2, b1, b2;
	private int fc1,fc2;
	
	boolean finished = false;
	
	List<wordObject> words;
	List<knotObject> knots;
	List<Integer> colors;
	List<sentence> wordCountperSentence;
	
	public int sizex = 10000;
	
	PVector lastObjectPosition = new PVector(0,0,0);

	PShape wordCloud; 
	PShape connections;
	PShape allCircles;
	PApplet parent;
		
	public wordcloud(PApplet parent,int id) {
		this.parent = parent;
		this.id = id;
		this.setColorGrad();
		//words = Collections.synchronizedList(new CopyOnWriteArrayList<wordObject>());
		words = Collections.synchronizedList(new ArrayList<wordObject>());
	    knots = Collections.synchronizedList(new ArrayList<knotObject>());
	    colors = new ArrayList<Integer>();
	    wordCountperSentence = new ArrayList<sentence>();
	
	    allCircles = parent.createShape(PConstants.GROUP);
		wordCloud = parent.createShape();
		connections = parent.createShape(PConstants.GROUP);
	    this.name = timeline.workshops.get(id-1).getName();
	}
	
	private void setColorGrad() {
		//System.out.println(this.id);
		this.r1 = timeline.colorGradients.get(this.id-1).getR1();
		this.r2 = timeline.colorGradients.get(this.id-1).getR2();
		this.g1 = timeline.colorGradients.get(this.id-1).getG1();
		this.g2 = timeline.colorGradients.get(this.id-1).getG2();
		this.b1 = timeline.colorGradients.get(this.id-1).getB1();
		this.b2 = timeline.colorGradients.get(this.id-1).getB2();
		this.fc1 = timeline.colorGradients.get(this.id-1).getFc1();
		this.fc2 = timeline.colorGradients.get(this.id-1).getFc2();		
	}
	
	
	
	private int getColor(float time) {
		int col;
		int R = (int)(this.r1 * time + this.r2 * (1 - time));
		int G = (int)(this.g1 * time + this.g2 * (1 - time));
		int B = (int)(this.b1 * time + this.b2 * (1 - time));
		col = parent.color(R,G,B);
		return col;
	}
	
	public void createBadge(String text, int time,int sentenceId, boolean fx) {
		boolean newWord = false;
		boolean overtime = false;
		int knotid = 0;
		
		//add count to wordcount per sentence
		//check if sentence is existing
		Optional<sentence> currentSentence = this.wordCountperSentence.stream()
		        .filter(sentence -> sentence.getSentence_id() == sentenceId)
		        .findFirst();
		//if existing add count
		if(currentSentence.isPresent()) {
			currentSentence.get().addCount();
		}
		//otherwise add sentence
		else {
			sentence newsentence = new sentence(sentenceId);
			wordCountperSentence.add(newsentence);
		}
		
		int countinsentence = wordCountperSentence.stream().filter(sentence -> sentence.getSentence_id() == sentenceId).findFirst().get().getCount();
		
		synchronized(words){
			//if first word set starttime
			if(this.words.size() == 0) {
				this.starttime = time;
			}
		}

		//check if word was already used
		synchronized(this.knots) {
			Optional<knotObject> currentKnot = this.knots.stream()
			        .filter(knotObject -> knotObject.word.equalsIgnoreCase(text))
			        .findFirst();
	
			// new word = new knot	
			if(currentKnot.isPresent() == false) {
				newWord = true;
				//running time in seconds
				float seconds = time - this.starttime;
				float tposition = (float)seconds/(float)(timeline.speaktime*60); 
				if(tposition > 1.f) {
					overtime = true;
					tposition = 1.f;
				}
				int color = this.getColor(tposition);
				this.colors.add(color);
				knotid = this.knots.size();
	
				//create word
				createWord(fx,text,time,knotid,countinsentence,overtime);
				//generate new knotObject at position
	
				this.knots.add(new knotObject(this.lastposX,this.knots.size(),text,this));		
			}
			
			// used word = no new knot
			else if(currentKnot.isPresent() == true) {
				knotid = currentKnot.get().id;
				float seconds = time - this.starttime;
				float tposition = (float)seconds/(float)(timeline.speaktime*60); 
				if(tposition > 1.f) {
					overtime = true;
				}
				//create word
				createWord(fx,text,time,knotid,countinsentence,overtime);
				//repos & recalc connection
				currentKnot.get().changeposition(this.lastposX);
				float newposy = currentKnot.get().position.y;
				this.minY = (newposy <= this.minY) ? newposy : this.minY;
				this.maxY = (newposy >= this.maxY) ? newposy : this.maxY;
			}
		}	
		
		//focus to new object
		synchronized(words){
			timeline.wordFocus = this.words.size()-1;
		}
		timeline.currentCloudid = timeline.clouds.indexOf(this);		
	}
	
	private void createWord(boolean fx, String text, int time, int knotid, int countinsentence,boolean overtime) {
		//create new badge
		float movex = ((float)(time - this.starttime) / (float)(timeline.speaktime*60))*this.sizex;
		float deltaY = (countinsentence -1) * (timeline.badgeSizeY * 1.2f);
		PVector pos = new PVector(movex,(float)deltaY,(float)0.);
		this.minY = (deltaY <= this.minY) ? deltaY : this.minY;
		this.maxY = (deltaY >= this.maxY) ? deltaY : this.maxY;
		float fxVal = 1.0f;
		if(fx == false) {
			fxVal = 0.0f;
		}
		wordObject new1 = new wordObject(text,pos,knotid,fxVal,time,overtime,this);
		synchronized(this.words){
			this.words.add(new1);
		}
		this.lastposX = movex;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getWsName() {
		return this.name;
	}
	
	public Stream<knotObject> getKnots() {
		synchronized(this.knots) {
		 return this.knots.stream().filter(k -> k.childs.size() > 1);
		}
	}

}

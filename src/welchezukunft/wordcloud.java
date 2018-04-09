package welchezukunft;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;


public class wordcloud {
	
	int id;
	
	int wordcount;
	float minY;
	float maxY;
	float lastposX = 0;
	
	int starttime;
	int endtime;
	
	int r1, r2, g1, g2, b1,b2;
	int fc;
	
	List<wordObject> words;
	List<knotObject> knots;
	List<Integer> colors;
	List<Integer> wordCount;
	
	PVector lastObjectPosition = new PVector(0,0,0);

	PShape wordCloud; 
	PShape connections;
	PShape allCircles;
	PApplet parent;
		
	public wordcloud(PApplet parent,int id) {
		this.parent = parent;
		setColorGrad();
		words = new ArrayList<wordObject>();
	    knots = new ArrayList<knotObject>();
	    colors = new ArrayList<Integer>();
	    wordCount = new ArrayList<Integer>();
	    
	    allCircles = parent.createShape(PConstants.GROUP);
		wordCloud = parent.createShape();
		connections = parent.createShape(PConstants.GROUP);
	    this.id = id;
	    //System.out.println("new cloud: " + this.id);

	}
	
	private void setColorGrad() {
		JSONArray jsonA = parent.loadJSONArray("./resources/data/colors.json");
		JSONObject json = jsonA.getJSONObject(0);
		this.r1 = json.getInt("redS");
		this.r2 = json.getInt("greenS");
		this.g1 = json.getInt("blueS");
		this.g2 = json.getInt("redE");
		this.b1 = json.getInt("greenE");
		this.b2 = json.getInt("blueE");
		this.fc = json.getInt("fontC");
	}
	
	public int getColor(float time) {
		int col;
		int R = (int)(r1 * time + r2 * (1 - time));
		int G = (int)(g1 * time + g2 * (1 - time));
		int B = (int)(b1 * time + b2 * (1 - time));
		col = parent.color(R,G,B);
		return col;
	}
	
	public void createBadge(String text, int time, boolean fx) {
		boolean newWord = false;
		int knotid = 0;
		
		//if first word set starttime
		if(this.words.size() == 0) {
			this.starttime = time;
		}
		
		//check if word was already used
		Optional<knotObject> currentKnot = this.knots.stream()
		        .filter(knotObject -> knotObject.word.equalsIgnoreCase(text))
		        .findFirst();
	
		// new word = new knot	
		if(currentKnot.isPresent() == false) {
			newWord = true;
			//running time in seconds
			float seconds = time - this.starttime;
			float tposition = (float)seconds/(float)(timeline.speaktime*60); 
			int color = this.getColor(tposition);
			this.colors.add(color);
			knotid = this.knots.size();
			//create word
			createWord(fx,text,knotid);
			//generate new knotObject at position
			int [] dir = {-1,1};
			float posy = parent.random((float)-1600.,(float)-1000.) * dir[(int)parent.random(2)];
			this.minY = (posy <= this.minY) ? posy : this.minY;
			this.maxY = (posy >= this.maxY) ? posy : this.maxY;
			this.knots.add(new knotObject(this.lastposX,posy,this.knots.size(),text,this));
			
		}
		
		// used word = no new knot
		else if(currentKnot.isPresent() == true) {
			knotid = currentKnot.get().id;
			//create word
			createWord(fx,text,knotid);
			//repos & recalc connection
			currentKnot.get().changeposition(this.lastposX);
			float newposy = currentKnot.get().position.y;
			this.minY = (newposy <= this.minY) ? newposy : this.minY;
			this.maxY = (newposy >= this.maxY) ? newposy : this.maxY;
		}
		
		//focus to new object
		timeline.wordFocus = this.words.size()-1;
		timeline.currentCloudid = timeline.clouds.indexOf(this);
	}
	
	private void createWord(boolean fx, String text, int knotid) {
		//create new badge
		float deltaX = parent.random((float)50.,(float)190.);
		float deltaY = parent.random((float)-400.,(float)400.);
		PVector pos = new PVector(this.lastposX + deltaX,(float)deltaY,(float)0.);
		this.minY = (deltaY <= this.minY) ? deltaY : this.minY;
		this.maxY = (deltaY >= this.maxY) ? deltaY : this.maxY;
		float fxVal = 1.0f;
		if(fx == false) {
			fxVal = 0.0f;
		}
		wordObject new1 = new wordObject(text,pos,knotid,fxVal,this);
		this.words.add(new1);
		this.lastposX += deltaX;
	}

}

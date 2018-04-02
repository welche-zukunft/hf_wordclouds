package welchezukunft;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.*;
import processing.opengl.PShader;


public class timeline extends PApplet{

	static List<wordObject> words;
	static List<knotObject> knots;
	static int[] colors;
	static List<Integer> wordCount;
	float lastposX = 0;
	float zPos = 400;
	PVector lookat = new PVector(0,0,400);
	PVector lastObjectPosition = new PVector(0,0,0);
	PFont menufont;
	static PShape wordCloud; 
	static PShape connections;
	static PShape circle;
	static PShape allCircles;
	static PImage timeline;
	static PShader corner;
	boolean useshader = false;
	static PGraphics mainOutput;
	int currentsentence = 0;
	boolean showall = false;
	float aspect;
	float maxY = 0;
	float minY = 50000;
	float fovy = PI/3;
	float widthBG, newwidthBG;
	
	Random r = new Random();
	
	public void settings(){
        size(1900, 800, P3D);
       
        widthBG = width;
        newwidthBG = widthBG;
        words = new ArrayList<wordObject>();
        knots = new ArrayList<knotObject>();
        aspect = (width/height);
        colors = new int[WordCloudTimeline.words.size()];
        wordCount = new ArrayList<Integer>();
       
        for(int k = 0; k < WordCloudTimeline.words.size(); k++) {
        	wordCount.add(0);
        }
    }
	
	public void setup(){
		mainOutput = createGraphics(1900, 800, P3D);
		menufont = createFont("Avenir LT 45 Book", 148,true);
		wordCloud = createShape();
		connections = createShape(GROUP);
		circle = createShape(ELLIPSE,0,0,10,10);
		circle.disableStyle();
		allCircles = createShape(GROUP);
		corner = loadShader("./resources/shader/corner.glsl");
		
		//create random colors
		for(int j = 0; j < WordCloudTimeline.words.size(); j++) {
			colors[j] = color(random(255),random(255),random(255));
		}
		
		//create auto Badges		
		int i = 0;
		while(i>0) {
			createBadge(1f);
			i--;
		}
		createTimelineTexture();
	}

    public void draw(){
    	clear();
    	mainOutput.beginDraw();
     	mainOutput.clear();
     	mainOutput.background(0);
     	mainOutput.hint(DISABLE_DEPTH_TEST); 
     	mainOutput.textMode(SHAPE);
     	mainOutput.textFont(menufont);
    	
    	//create timeline
     	mainOutput.noStroke();
    	if(words.size() > 0) {
    		newwidthBG = (widthBG < words.get(words.size()-1).pos.x) ? words.get(words.size()-1).pos.x : widthBG  ;
    	}
    	widthBG = lerp(widthBG,newwidthBG,0.15f);
    	mainOutput.image(timeline,-width/2,-400,width/2 + widthBG,800);
    	
    	//calculate last object position
		if(words.size() > 0){
			if(showall == false) {
				lastObjectPosition = words.get(words.size()-1).pos;
			}
			else if(showall == true) {
				float xStart = words.get(0).pos.x;
    			float xEnd = words.get(words.size()-1).pos.x;
	    		float posx = xStart + (float)0.5 * (xEnd - xStart);
	    		float yStart = minY;
    			float yEnd = maxY;
    			float posy = yStart + (float)0.5 * (yEnd - yStart);
				lastObjectPosition = new PVector(posx,posy,0);
			}
		}

		
		//generating camera view
    	lookat.x = lerp(lookat.x,lastObjectPosition.x,(float)0.15);
		lookat.y = lerp(lookat.y,lastObjectPosition.y,(float)0.15);
		lookat.z = lerp(lookat.z,zPos,(float)0.15);
		mainOutput.camera(lookat.x, lookat.y, lookat.z, lookat.x, lookat.y,0,0,1,0);  
		mainOutput.perspective(fovy, aspect,(float) 0.1, lookat.z*2);
		
		//draw connections and objects
		mainOutput.stroke(223,255,23,255);
		mainOutput.shape(connections);
		mainOutput.shape(wordCloud);
    	
    	//draw knots
    	for(knotObject k : knots) {
    		if(k.childs.size() > 1) {
    			mainOutput.pushMatrix();
    			mainOutput.translate(k.position.x,k.position.y,0);
    			mainOutput.noStroke();
    			mainOutput.fill(colors[k.id]);
	    		int count = wordCount.get(k.id);
	    		float sc = 3.f+count*10;
	    		mainOutput.scale(sc,sc,sc);
	    		mainOutput.shape(circle);
	    		mainOutput.popMatrix();
    		}
    	}
    	
    	float scl = 1f;
    	
    	//init FX
    	for(wordObject w : words){
			if(w.init > 0.) {
				mainOutput.pushMatrix();
				mainOutput.translate(w.pos.x,w.pos.y,0);
				mainOutput.fill(255,255*(w.init));
				mainOutput.textSize((scl / 1.777f) + 120f*(1.f - w.init));
				mainOutput.textAlign(CENTER, CENTER);
				mainOutput.text(w.word,0,0);
				mainOutput.popMatrix();
				w.init -= 0.01;
			}
    	}
		
    	/*
    	//draw text
		for(wordObject w : words){
		  pushMatrix();	
		  textSize(w.tweight);
		  textAlign(CENTER,CENTER);
		  fill(255);
		  translate(5,0,0);
		  text(w.word, w.pos.x, w.pos.y,(float)0.);
		  popMatrix();
		 }
		*/
    	mainOutput.endDraw();
    	
    	if(useshader == true) {
	    	corner.set("mouse",(float)mouseX,(float)mouseY);
	        corner.set("resolution", (float)width,(float)height);
	    	corner.set("amount", (float)mouseX);
	    	shader(corner);
    	}
    	
    	image(mainOutput,0,0);
		
    	if(useshader == true) {
    		resetShader();
    		
    	}
    	
    	surface.setTitle("fps: "+ frameRate + "//" + "wordcount = " + words.size());
    
    }
    
    
    public void createBadge(float amt) {
    	zPos = 400;
		showall = false;
		
		int pick = (int) ((int) r.nextInt(WordCloudTimeline.words.size()) * amt);
		
		//check if word was already used
		boolean newWord = false;
		if(wordCount.get(pick) == 0) {
			newWord = true;
		}
		wordCount.set(pick, wordCount.get(pick) + 1);
		
		
		//generate WordObject at position
		String text = WordCloudTimeline.words.get(pick);
		float deltaX = random((float)50.,(float)190.);
		float deltaY = random((float)-400.,(float)400.);
		minY = (deltaY <= minY) ? deltaY : minY;
		maxY = (deltaY >= maxY) ? deltaY : maxY;
		PVector pos = new PVector(lastposX + deltaX,(float)deltaY,(float)0.);
		float weigth = random(30,40);
		wordObject new1 = new wordObject(text,pos,weigth,pick,this);
		words.add(new1);
		lastposX += deltaX;
		
		if(newWord == true) {
			//generate new knotObject at position
			int [] dir = {-1,1};
			float posy = random((float)-1600.,(float)-1000.) * dir[(int)random(2)];
			minY = (posy <= minY) ? posy : minY;
			maxY = (posy >= maxY) ? posy : maxY;
			knots.add(new knotObject(lastposX,posy,pick,this));
		}
		else if(newWord == false) {
			Optional<knotObject> currentKnot = knots.stream()
		            .filter(knotObject -> knotObject.id == pick)
		            .findFirst();
			
			if(currentKnot.isPresent()) {
					currentKnot.get().changeposition(lastposX);
			}
		}	
    }
    
    void createTimelineTexture() {
    	  timeline = createImage(1,800,RGB);
    	  timeline.loadPixels();
    	  for (int i = 0; i < timeline.pixels.length; i++) {
    		  float delta = 1f - (0.5f * (cos(((float)i/(float)800)*TWO_PI) + 1f));
    		  timeline.pixels[i] = color(delta * 120f); 
    	  }
    	  timeline.updatePixels();
    	
    }
    
    public void keyPressed(){
    	//add new object
    	if (key == '1' && words.size() > 0) {
    		float deltaX = random((float)50.,(float)190.);
    		float deltaY = random((float)0.,height);
    		PVector pos = new PVector(lastposX + deltaX,(float)deltaY,(float)0.);
    		float weigth = random(30,40);
    		String text = WordCloudTimeline.taggedText.get(words.size());
    		wordObject new1 = new wordObject(text,pos,weigth,words.size(),this);
    		words.add(new1);
    		lastposX += deltaX;
    		
    	}
    	
    	else if(key == 'w') {
    		//show all
    		if(words.size() > 1) {
    			showall = true;
    			float xStart = words.get(0).pos.x;
    			float xEnd = words.get(words.size()-1).pos.x;
    			float yStart = minY;
    			float yEnd = maxY;		
    			
	    		float degAh = (float)0.5 * degrees((float) (PI/3.0));
	    		float degAv = (float)2.0 * atan(tan(((float)PI/(float)3.0) * (float)0.5) * aspect);
	    		
	    		float degCv = 180 - degrees(degAv) - 90;
	    		float lenav = (float)0.5 * (xEnd - xStart);
	    		
	    		float degCh = 180 -  degrees(degAh) - 90;
	    		float lenah = abs((float)0.5 * (yStart - yEnd));
	    		
	    		
	    		if(lenav >= lenah * aspect) {    		
	    			zPos = (lenav *sin(degCv) / sin(degAv)) * (float)-1.0;
	    			
	    		}
	    		else {
	    			zPos = aspect * (lenah *sin(degCh) / sin(degAh));
	    			
	    		}
	    		
    		
    		}
    	}
    	
    	else if(key == 'q') {
    		createBadge(1.0f);
    	}
    	
    	else if(key == 'a') {
    		createBadge(0.1f);
    	}   
    	else if(key == 's') {
    		useshader = !useshader;
    	}      	
    	else if(key == 'k') {
    		WordCloudTimeline.pipe.doSentenceTest(WordCloudTimeline.input);
    	}

    }
	
}

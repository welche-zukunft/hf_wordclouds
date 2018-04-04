package welchezukunft;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.*;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;
import static java.lang.Math.toIntExact;

public class timeline extends PApplet{

	static List<wordObject> words;
	static List<knotObject> knots;
	static List<Integer> colors;
	static List<Integer> wordCount;
	
	static List<colorGrad> gradients;

	PVector lookat = new PVector(0,0,400);
	PVector lastObjectPosition = new PVector(0,0,0);
	
	PFont menufont;
	
	static PShape wordCloud; 
	static PShape connections;
	static PShape circle;
	static PShape allCircles;
	
	static PImage timeline;
	static PShader corner;
	static PGraphics mainOutput;
	
	boolean useshader = false;
	boolean showall = false;
	boolean showtext = true;
	
	float lastposX = 0;
	float zPos = 400;	
	float aspect;
	float maxY = 0;
	float minY = 50000;
	float fovy = PI/3;
	float widthBG, newwidthBG;
	
	long millis;
	long millisrun;
	//9 minutes per talk
	int speaktime = 1;
	long totaltime = 1000 * 60 * speaktime;
	//time float between 0. - 1.
	float tposition;
	
	Random r = new Random();
	
	public void settings(){
        fullScreen(P3D, SPAN);
        smooth(8);
        
        widthBG = width;
        newwidthBG = widthBG;
        aspect = (3840/1080);
        
        words = new ArrayList<wordObject>();
        knots = new ArrayList<knotObject>();
        colors = new ArrayList<Integer>();
        wordCount = new ArrayList<Integer>();
       
        for(int k = 0; k < WordCloudTimeline.words.size(); k++) {
        	wordCount.add(0);
        }
    }
	
	public void setup(){
		
		frameRate(60);
		
		mainOutput = createGraphics(3840, 1080, P3D);
		
		menufont = createFont("Avenir LT 45 Book", 148,true);
		
		allCircles = createShape(GROUP);
		wordCloud = createShape();
		connections = createShape(GROUP);
		circle = createShape(ELLIPSE,0,0,10,10);
		circle.disableStyle();

		corner = loadShader("./resources/shader/corner.glsl");
		
		createTimelineTexture();
		
		loadGradients();
	}

    public void draw(){
    	clear();
    	mainOutput.beginDraw();
     	mainOutput.clear();
     	mainOutput.background(0);
     	mainOutput.hint(DISABLE_DEPTH_TEST); 
     	mainOutput.textFont(menufont);
    	
    	//draw timeline
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
		mainOutput.shape(allCircles);
		mainOutput.shape(wordCloud);
    	
		
    	//draw text over knots
    	for(knotObject k : knots) {
    		if(k.childs.size() > 1) {
    			 if(in_frustum(k.position) == true && showtext == true) {
    				 
    				 
	    			 mainOutput.pushMatrix();
	    			 mainOutput.translate(k.position.x,k.position.y,0);
	    			 mainOutput.fill(255);
	    			 mainOutput.textSize(60 + 20 * k.childs.size());
	    			 mainOutput.text(k.word,0,0); 
	    			 mainOutput.popMatrix();
	    		 }

    		}
    	}
    	
    	
    	float scl = 1f;
    	
    	//init FX
    	for(wordObject w : words){
			if(w.init > 0.) {
				mainOutput.pushMatrix();			
				mainOutput.translate(w.pos.x,w.pos.y,0);
				float scaleF = (float)1.+ 4f*(1.f - w.init);
				mainOutput.scale(scaleF);
				mainOutput.fill(255,255*(w.init));
				mainOutput.textAlign(CENTER, CENTER);
				mainOutput.text(w.word,0,0);
				mainOutput.popMatrix();
				w.init -= 0.01;
			}
    	}
		
    	
    	//draw text on visible object
		for(wordObject w : words){
		  if(in_frustum(w.pos) == true && showall == false && lookat.z < 401) {	
			  mainOutput.pushMatrix();	
			  mainOutput.textSize(18);
			  mainOutput.textAlign(CENTER,CENTER);
			  float col = 1 - (0.1f * (lookat.z - 400));
			  mainOutput.fill(255*col);
			  mainOutput.translate(w.pos.x, w.pos.y,0);
			  mainOutput.text(w.word, 0,0);
			  mainOutput.popMatrix();
		  }
		 }
		
    	mainOutput.endDraw();
    	
    	if(useshader == true) {
	    	corner.set("mouse",(float)mouseX,(float)mouseY);
	        corner.set("resolution", (float)width,(float)height);
	    	corner.set("amount", (float)mouseX);
	    	shader(corner);
    	}
    	
    	image(mainOutput,0,0,1920,540);
    	image(mainOutput,1920,0);
		
    	if(useshader == true) {
    		resetShader();
    	}
    	
    	textSize(13);
    	text("fps: " + frameRate,1720,1020);
    	text("floatposition: " + tposition,1720,1040);
    	millisrun = System.currentTimeMillis() - millis;
    	text("runningtime: " + millisrun,1720,1060);
    	//surface.setTitle("fps: "+ frameRate + "//" + "wordcount = " + words.size());
    
    }
    
    
    public void createBadge(float amt) {
    	//zoom near
    	zPos = 400;
		showall = false;
		
		int pick = (int) ((int) r.nextInt(WordCloudTimeline.words.size()) * amt);
		String text = WordCloudTimeline.words.get(pick);
		
		//check if word was already used
		boolean newWord = false;
		int knotid = 0;
		
		Optional<knotObject> currentKnot = knots.stream()
	        .filter(knotObject -> knotObject.word.equalsIgnoreCase(text))
	        .findFirst();
		
		// new word = new knot
		if(currentKnot.isPresent() == false) {
			newWord = true;
			
			//time in seconds
			float seconds = 100f* ((float)toIntExact(millisrun) / (float)toIntExact(totaltime));
			System.out.println(seconds + " / "  + millisrun + " / " + totaltime);
			tposition = (float)seconds/(float)(speaktime*60); 

			int color = gradients.get(0).getColor(tposition);

			colors.add(color);
			knotid = knots.size();
		}
		
		// used word = no new knot
		else if(currentKnot.isPresent() == true) {
			knotid = currentKnot.get().id;
		}
		
		
		//generate WordObject position
		float deltaX = random((float)50.,(float)190.);
		float deltaY = random((float)-400.,(float)400.);
		minY = (deltaY <= minY) ? deltaY : minY;
		maxY = (deltaY >= maxY) ? deltaY : maxY;
		PVector pos = new PVector(lastposX + deltaX,(float)deltaY,(float)0.);

		wordObject new1 = new wordObject(text,pos,knotid,this);
		words.add(new1);
		lastposX += deltaX;
		
		if(newWord == true) {
			//generate new knotObject at position
			int [] dir = {-1,1};
			float posy = random((float)-1600.,(float)-1000.) * dir[(int)random(2)];
			minY = (posy <= minY) ? posy : minY;
			maxY = (posy >= maxY) ? posy : maxY;
			knots.add(new knotObject(lastposX,posy,knots.size(),text,this));
		}
		else if(newWord == false) {
					currentKnot.get().changeposition(lastposX);
		}
		
	
    }
    
    void createTimelineTexture() {
    	  int heightTimelineTexture = 800;
    	  timeline = createImage(1,heightTimelineTexture,RGB);
    	  timeline.loadPixels();
    	  for (int i = 0; i < timeline.pixels.length; i++) {
    		  float delta = 1f - (0.5f * (cos(((float)i/(float)heightTimelineTexture)*TWO_PI) + 1f));
    		  timeline.pixels[i] = color(delta * 120f); 
    	  }
    	  timeline.updatePixels();
    	
    }
    
    public void keyPressed(){
    
    	
    	if(key == 'w') {
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
	    			zPos = abs((lenav *sin(degCv) / sin(degAv)) * (float)-1.0);
	    			
	    		}
	    		else {
	    			zPos = abs(aspect * (lenah *sin(degCh) / sin(degAh)));
	    			
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
    	else if(key == 't') {
    		showtext = !showtext;
    	}
    	else if(key == 'c') {
    		millis = System.currentTimeMillis();
    	}

    }
	
    private boolean in_frustum(PVector pos) {
        PMatrix3D MVP = ((PGraphics3D)mainOutput).projmodelview;
        float[] where = {pos.x,pos.y,pos.z-100,1.f};
        float[] Pclip = new float[4];
        MVP.mult(where, Pclip);
        return abs(Pclip[0]) < Pclip[3] && 
               abs(Pclip[1]) < Pclip[3] && 
               0 < Pclip[2] && 
               Pclip[2] < Pclip[3];
    }
    
    private void loadGradients() {
    	gradients = new ArrayList<colorGrad>();
    	colorGrad c1 = new colorGrad(230,97,107,44,44,112,this);
    	gradients.add(c1);
    }
}

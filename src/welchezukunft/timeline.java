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
	
	calibrationGui calibrationWin;
	requestSQL accessSQL;

	/*
	static List<wordObject> words;
	static List<knotObject> knots;
	static List<Integer> colors;
	static List<Integer> wordCount;
	static wordcloud cloud;
	*/
	
	static List<wordcloud> clouds;
	static wordcloud curCloud;
	
	PVector lookat = new PVector(0,0,400);
	//PVector lastObjectPosition = new PVector(0,0,0);
	
	PFont menufont;
	PImage calibration_img;
	static PShape circle;
	static PImage timeline;
	static PShader corner;
	static PGraphics mainOutput;
	
	
	/*
	static PShape wordCloud; 
	static PShape connections;
	static PShape allCircles;
	*/

	static boolean init = false;
	static boolean calibration = false;
	boolean useshader = false;
	boolean showall = false;
	boolean showtext = true;
	boolean cloudExist = false;
	
	//float lastposX = 0;
	float zPos = 400;	
	float aspect;
	//float maxY = 0;
	//float minY = 50000;
	float fovy = PI/3;
	float widthBG, newwidthBG;
	
	long millis;
	static long millisrun;
	//9 minutes per talk
	static int speaktime = 10;
	long totaltime = 1000 * 60 * speaktime;
	//time float between 0. - 1.
	float tposition;
	
	Random r = new Random();
	
	static int currentCloudid = 0;

	public void settings(){
		calibrationWin = new calibrationGui(this);
		accessSQL = new requestSQL(this);
        fullScreen(P3D, SPAN);
        smooth(8);
        
        widthBG = width;
        newwidthBG = widthBG;
        aspect = (3840/1080);
        
        clouds = new ArrayList<wordcloud>();
        
        /*
        words = new ArrayList<wordObject>();
        knots = new ArrayList<knotObject>();
        colors = new ArrayList<Integer>();
        wordCount = new ArrayList<Integer>();
        cloud = new wordcloud(this);
        for(int k = 0; k < WordCloudTimeline.words.size(); k++) {
        	wordCount.add(0);
        }
        */
        
        millis = System.currentTimeMillis();
    }
	
	public void setup(){
		frameRate(60);
		//create Processing stuff
		mainOutput = createGraphics(3840, 1080, P3D);
		/*
		allCircles = createShape(GROUP);
		wordCloud = createShape();
		connections = createShape(GROUP);
		*/
		circle = createShape(ELLIPSE,0,0,10,10);
		circle.disableStyle();
		
		//load externals
		menufont = createFont("Avenir LT 45 Book", 148,true);
		calibration_img = loadImage("./resources/pic/diagonal3.png");
		corner = loadShader("./resources/shader/mapping.glsl");
		
		//init internals
		createTimelineTexture();

		//load data from sql
		accessSQL.getWordsSetup();
		
		if(init == false){
			init = true; 
		}
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
	     	
	     if(init == true && clouds.size()>0) {
		    curCloud = clouds.get(currentCloudid);
		    
	    	if(curCloud.words.size() > 0) {
	    		newwidthBG = (widthBG < curCloud.words.get(curCloud.words.size()-1).pos.x) ? curCloud.words.get(curCloud.words.size()-1).pos.x : widthBG  ;
	    	}
	    	widthBG = lerp(widthBG,newwidthBG,0.15f);
	    	mainOutput.image(timeline,-width/2,-400,width/2 + widthBG,800);
	    	
	    	//calculate last object position
			if(curCloud.words.size() > 0){
				if(showall == false) {
					curCloud.lastObjectPosition = curCloud.words.get(curCloud.words.size()-1).pos;
				}
				else if(showall == true) {
					float xStart = curCloud.words.get(0).pos.x;
	    			float xEnd = curCloud.words.get(curCloud.words.size()-1).pos.x;
		    		float posx = xStart + (float)0.5 * (xEnd - xStart);
		    		float yStart = curCloud.minY;
	    			float yEnd = curCloud.maxY;
	    			float posy = yStart + (float)0.5 * (yEnd - yStart);
	    			curCloud.lastObjectPosition = new PVector(posx,posy,0);
				}
			}
	
			
			//generating camera view
	    	lookat.x = lerp(lookat.x,curCloud.lastObjectPosition.x,(float)0.15);
			lookat.y = lerp(lookat.y,curCloud.lastObjectPosition.y,(float)0.15);
			lookat.z = lerp(lookat.z,zPos,(float)0.15);
			mainOutput.camera(lookat.x, lookat.y, lookat.z, lookat.x, lookat.y,0,0,1,0);  
			mainOutput.perspective(fovy, aspect,(float) 0.1, lookat.z*2);
			
			//draw connections and objects
			mainOutput.stroke(223,255,23,255);
			mainOutput.shape(curCloud.connections);
			mainOutput.shape(curCloud.allCircles);
			mainOutput.shape(curCloud.wordCloud);
	    	
			
	    	//draw text over knots
	    	for(knotObject k : curCloud.knots) {
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
	    	
	    	//init FX - only in realtime modus / not on setup
	    	if(init == true) {
		    	for(wordObject w : curCloud.words){
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
	    	}
			
	    	
	    	//draw text on visible object
			for(wordObject w : curCloud.words){
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
	     }
	    
	    mainOutput.endDraw();
    	
    	
    	if(useshader == true) {
	        corner.set("resolution", (float)3840,(float)1080);
	        
	        corner.set("lu1",(float)-0.999,(float)0.9991);
	        corner.set("ru1",(float)0.799,(float)0.0001);
	        corner.set("ro1",(float)0.999,(float)0.999);
	        corner.set("lo1",(float)0.001,(float)0.999);
	        corner.set("lu2",(float)0.001,(float)0.0001);
	        corner.set("ru2",(float)0.999,(float)0.0001);
	        corner.set("ro2",(float)0.999,(float)0.999);
	        corner.set("lo2",(float)0.001,(float)0.999);
	        
	    	corner.set("rotateL", (float)0.);
	    	corner.set("rotateR", (float)0.);
	    	corner.set("nonlinear", (float)0.);
	    	corner.set("nonlinear2", (float)0.);
	    	
	    	shader(corner);
    	}
    	
    		// ---------------CALIBRATION ------------------
	    	if(calibration == true) {
	    		image(calibration_img,3840,0);
	    	}
    	
	    	// ---------------SHOW----------------------
	    	else {	
	    		image(mainOutput,1920,0,1920,540);
	    		image(mainOutput,3840,0);
	    	}
	    	
    	
    	
    	if(useshader == true) {
    		resetShader();
    	}
    	
    	pushMatrix();
    	translate(1920,0);
    	textSize(13);
    	text("fps: " + frameRate,1720,1020);
    	//text("floatposition: " + tposition,1720,1040);
    	//millisrun = System.currentTimeMillis() - millis;
    	//text("runningtime: " + millisrun,1720,1060);
    	//surface.setTitle("fps: "+ frameRate + "//" + "wordcount = " + words.size());
    	popMatrix();
    	if(frameCount % 60 == 0) {
    		accessSQL.updateWords();
    	}
    }
    
    
    public void createBadge(float amt) {
    	
    	//zoom near
    	zPos = 400;
		showall = false;
		/*
		int pick = (int) ((int) r.nextInt(WordCloudTimeline.words.size()) * amt);
		String text = WordCloudTimeline.words.get(pick);
		
		//check if word was already used
		boolean newWord = false;
		int knotid = 0;
		
		Optional<knotObject> currentKnot = curCloud.knots.stream()
	        .filter(knotObject -> knotObject.word.equalsIgnoreCase(text))
	        .findFirst();
		
		// new word = new knot
		if(currentKnot.isPresent() == false) {
			newWord = true;
			
			//time in seconds
			float seconds = 100f* ((float)toIntExact(millisrun) / (float)toIntExact(totaltime));
			tposition = (float)seconds/(float)(speaktime*60); 

			int color = curCloud.getColor(tposition);

			curCloud.colors.add(color);
			knotid = curCloud.knots.size();
		}
		
		// used word = no new knot
		else if(currentKnot.isPresent() == true) {
			knotid = currentKnot.get().id;
		}
		
		
		//generate WordObject position
		float deltaX = random((float)50.,(float)190.);
		float deltaY = random((float)-400.,(float)400.);
		PVector pos = new PVector(curCloud.lastposX + deltaX,(float)deltaY,(float)0.);
		curCloud.minY = (deltaY <= curCloud.minY) ? deltaY : curCloud.minY;
		curCloud.maxY = (deltaY >= curCloud.maxY) ? deltaY : curCloud.maxY;
		wordObject new1 = new wordObject(text,pos,knotid,this);
		curCloud.words.add(new1);
		curCloud.lastposX += deltaX;
		
		
		if(newWord == true) {
			//generate new knotObject at position
			int [] dir = {-1,1};
			float posy = random((float)-1600.,(float)-1000.) * dir[(int)random(2)];
			curCloud.minY = (posy <= curCloud.minY) ? posy : curCloud.minY;
			curCloud.maxY = (posy >= curCloud.maxY) ? posy : curCloud.maxY;
			curCloud.knots.add(new knotObject(curCloud.lastposX,posy,curCloud.knots.size(),text,this));
		}
		else if(newWord == false) {
			currentKnot.get().changeposition(curCloud.lastposX);
			float newposy = currentKnot.get().position.y;
			curCloud.minY = (newposy <= curCloud.minY) ? newposy : curCloud.minY;
			curCloud.maxY = (newposy >= curCloud.maxY) ? newposy : curCloud.maxY;
		}
		*/
	
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
    		if(curCloud.words.size() > 1) {
    			showall = true;
    			float xStart = curCloud.words.get(0).pos.x;
    			float xEnd = curCloud.words.get(curCloud.words.size()-1).pos.x;
    			float yStart = abs(curCloud.minY);
    			float yEnd = curCloud.maxY;	

    			float radAngle = radians(fovy);
    			float radHFOV = 2 * atan(tan(radAngle / 2) * aspect);
    			float fovx = degrees(radHFOV);
 			
    			float distancex = 1/(2f * tan((fovx/2)/(xEnd-xStart)));
    			float distancey = 1/(2f * tan((fovy/2)/(curCloud.maxY*1.05f-curCloud.minY*1.05f)));
    			zPos = Math.max(distancex, distancey);
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
    		currentCloudid = (currentCloudid+1)%clouds.size();
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
    
}

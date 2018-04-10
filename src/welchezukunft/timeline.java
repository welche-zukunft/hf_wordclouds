package welchezukunft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import processing.core.*;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

public class timeline extends PApplet{
	
	public static mode modus = mode.CRISIS;
	
	public static calibrationGui calibrationWin;
	public static requestSQL accessSQL;

	static List<wordcloud> clouds;
	static wordcloud curCloud;
	
	PVector lookat = new PVector(0,0,400);
	
	PFont menufont;
	PImage calibration_img;
	static PImage timeline;
	static PShader corner;
	static PGraphics mainOutput;
	static PGraphics caliOutput;
	
	PShape displayKW, displayCR, display2;

	static boolean init = false;
	static boolean initscale = false;
	static boolean calibration = true;
	boolean useshader = true;
	boolean showall = false;
	boolean showtext = true;
	boolean cloudExist = false;
	
	float zPos = 400;	
	float aspect;
	float fovy = PI/3f;
	float widthBG, newwidthBG;
	
	//9 minutes per talk
	static int speaktime = 10;
	long totaltime = 1000 * 60 * speaktime;
	//time float between 0. - 1.
	float tposition;
	
	Random r = new Random();
	
	static int currentCloudid = 0;
	static int wordFocus = 0;
	
	public static eventTimeline eventLine;
	
    static int dragposx;
    static int dragposy;
    
    static PVector mousePos;
    
	public void settings(){
		calibrationWin = new calibrationGui(this);
		accessSQL = new requestSQL(this);
        fullScreen(P3D, SPAN);
        smooth(8);
        
        widthBG = width;
        newwidthBG = widthBG;
        aspect = (3840/1080);
        
        clouds = new ArrayList<wordcloud>();
    }
	
	public void setup(){
		frameRate(60);
		//create Processing stuff
		mainOutput = createGraphics(3840, 1080, P3D);
		caliOutput = createGraphics(3840, 1080, P3D);

		//load externals
		menufont = createFont("Avenir LT 45 Book", 148,true);
		calibration_img = loadImage("./resources/pic/diagonal3.png");
		corner = loadShader("./resources/shader/corner.glsl");
		
		//load data from sql
		accessSQL.getWordsSetup();

		eventLine = new eventTimeline(3840,1080,this);	
		
		//create menu buttons
		eventLine.menu.createButtons();
		eventLine.menu.createButtonsEvent();

		//init internals
		createTimelineTexture();
		createDisplay();		
		
		if(init == false){
			init = true; 
		}
	}

    public void draw(){
    	
	    clear();

	    // ---------------CALIBRATION ------------------
    	if(calibration == true) {
    		caliOutput.beginDraw();
    		caliOutput.clear();
    		caliOutput.background(0);
    		caliOutput.image(calibration_img,0,0);
    		caliOutput.endDraw();
    	}
	
    	// ---------------SHOW----------------------
    	else if(calibration == false) {	
    		if(modus == mode.KEYWORDS) {
			     if(init == true && clouds.size()>0) {
			    	mainOutput.beginDraw();
			 	    mainOutput.clear();
			 	    mainOutput.background(0);
			 	    mainOutput.hint(DISABLE_DEPTH_TEST); 
			 	    mainOutput.textFont(menufont);
			    	 
				    curCloud = clouds.get(currentCloudid);
				    
				    //draw timeline
				    mainOutput.noStroke();
			    	if(curCloud.words.size() > 0) {
			    		newwidthBG = (widthBG < curCloud.words.get(curCloud.words.size()-1).pos.x) ? curCloud.words.get(curCloud.words.size()-1).pos.x : widthBG  ;
			    	}
			    	widthBG = lerp(widthBG,newwidthBG,0.15f);
			    	mainOutput.image(timeline,-width/2,-400,width/2 + widthBG,800);
			    	
			    	//calculate focus position 
			    	//center of wordcloud or focused word
					if(curCloud.words.size() > 0){
						if(wordFocus > curCloud.words.size() -1) {
							wordFocus = curCloud.words.size()-1;
						}
						if(showall == false) {
							curCloud.lastObjectPosition = curCloud.words.get(wordFocus).pos;
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
			    			 if(in_frustum(k.position,mainOutput) == true && showtext == true) {
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
					  if(in_frustum(w.pos,mainOutput) == true && showall == false && lookat.z < 405) {	
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
			    	//draw left screen
			    	image(mainOutput,1920,0,1920,540);
		    	}
    		
    		else if (modus == mode.CRISIS) {
    			eventLine.drawing();
    			}
    	}
	   
    	
    	if(useshader == true) {
	        corner.set("resolutionIn", (float)3840,(float)1080);     
	        corner.set("lu1",(float)calibrationWin.vals[6]/1000.f,(float)calibrationWin.vals[7]/1000.f);
	        corner.set("ru1",(float)calibrationWin.vals[4]/1000.f,(float)calibrationWin.vals[5]/1000.f);
	        corner.set("ro1",(float)calibrationWin.vals[2]/1000.f,(float)calibrationWin.vals[3]/1000.f);
	        corner.set("lo1",(float)calibrationWin.vals[0]/1000.f,(float)calibrationWin.vals[1]/1000.f);
	        corner.set("lu2",(float)calibrationWin.vals[14]/1000.f,(float)calibrationWin.vals[15]/1000.f);
	        corner.set("ru2",(float)calibrationWin.vals[12]/1000.f,(float)calibrationWin.vals[13]/1000.f);
	        corner.set("ro2",(float)calibrationWin.vals[10]/1000.f,(float)calibrationWin.vals[11]/1000.f);
	        corner.set("lo2",(float)calibrationWin.vals[8]/1000.f,(float)calibrationWin.vals[9]/1000.f);       
	    	corner.set("rotateL", 360.0f * (float)calibrationWin.vals2[0]/10000.f);
	    	corner.set("rotateR", 360.0f * (float)calibrationWin.vals2[6]/10000.f);
	    	corner.set("nonlinearL", (float)calibrationWin.vals2[5]/1000.f);
	    	corner.set("nonlinearR",(float)calibrationWin.vals2[11]/1000.f);
	    	shader(corner);
    	}
    	
    	//draw table
    	pushMatrix();
    	translate(3840,0,0);
    	if(calibration == true) {
    		shape(display2);
    	}
    	else if(calibration == false) {
    		if(modus == mode.KEYWORDS) {
    			shape(displayKW);
    		}
    		else if (modus == mode.CRISIS) {
    			shape(displayCR);
    		}
    	}
    	popMatrix();
    	    	
    	if(useshader == true) {
    		resetShader();
    	}
    	
    	
    	if (modus == mode.CRISIS) {
    		this.events(this.g);
    		eventLine.postdraw();
    	}
    	pushMatrix();
    	translate(1920,0);
    	textSize(13);
    	text("fps: " + frameRate,1720,1020);
    	//surface.setTitle("fps: "+ frameRate + "//" + "wordcount = " + words.size());
    	popMatrix();
    	
    	if(frameCount % 60 == 0) {
    		accessSQL.updateWords();
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
    	if(key == '+') {
    		zoomIn();
    	}
    	
    	else if(key == '-') {
    		zoomOut();
    	}

    	else if(key == 'w') {
    		wordFocus = wordFocus - 1;
    		if(wordFocus < 0) wordFocus = curCloud.words.size()-1;
    	}
    	else if(key == 'e') {
    		wordFocus = wordFocus + 1;
    		if(wordFocus > curCloud.words.size()-1) wordFocus = 0;
    	}

    	else if(key == 'r') {
    		currentCloudid = currentCloudid-1;
     		if(currentCloudid < 0) {
    			currentCloudid = clouds.size()-1;		
    		}
    	}
    	
    	else if(key == 't') {
    		currentCloudid = (currentCloudid+1);
    		if(currentCloudid > clouds.size()-1) {
    			currentCloudid = 0;
    		}
    	}
    	
    	else if (key == 'c'){
    		 	calibrationWin.mainPanel.setVisible(true);
    		 	calibrationWin.calframe.setVisible(true);
    	      } 
    	
     	else if(key == 's') {
     		useshader = !useshader;
     	}
   	
    }
	 
    public void zoomIn() {  	
    	//zoom near
    	zPos = 400;
		showall = false;
    }
    
    public void zoomOut() {
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
    
    static boolean in_frustum(PVector pos,PGraphics target) {
        PMatrix3D MVP = ((PGraphics3D)target).projmodelview;
        float[] where = {pos.x,pos.y,pos.z-100,1.f};
        float[] Pclip = new float[4];
        MVP.mult(where, Pclip);
        return abs(Pclip[0]) < Pclip[3] && 
               abs(Pclip[1]) < Pclip[3] && 
               0 < Pclip[2] && 
               Pclip[2] < Pclip[3];
    }
    
    // count chars in string
    public static int countMatches(String mainString, String whatToFind){
        String tempString = mainString.replaceAll(whatToFind, "");
        int times = (mainString.length()-tempString.length())/whatToFind.length();
        return times;
    }
    
    private void createDisplay() {
    	displayKW = createShape();
    	displayKW.beginShape();
    	displayKW.textureMode(NORMAL);
    	displayKW.texture(mainOutput);
    	displayKW.vertex(0, 0,0,0);
    	displayKW.vertex(3840, 0,1,0);
    	displayKW.vertex(3840, 1080,1,1);
    	displayKW.vertex(0, 1080,0,1);
    	displayKW.endShape();
 
       	displayCR = createShape();
    	displayCR.beginShape();
    	displayCR.textureMode(NORMAL);
    	displayCR.texture(eventLine.mainPlane);
    	displayCR.vertex(0, 0,0,0);
    	displayCR.vertex(3840, 0,1,0);
    	displayCR.vertex(3840, 1080,1,1);
    	displayCR.vertex(0, 1080,0,1);
    	displayCR.endShape();    	
    	
       	display2 = createShape();
    	display2.beginShape();
    	display2.textureMode(NORMAL);
    	display2.texture(caliOutput);
    	display2.vertex(0, 0,0,0);
    	display2.vertex(3840, 0,1,0);
    	display2.vertex(3840, 1080,1,1);
    	display2.vertex(0, 1080,0,1);
    	display2.endShape();   	
    	
    }
    
    private void events(PGraphics p) {
	    //draw timeline on monitor 1
	    p.image(eventLine.mainPlane,1920,720,1280,360);
	    
	    //drawZoom
	    if(eventLine.showzoommonitor == true){
	      p.pushMatrix();
	      eventLine.monitorzoom.set("tex", eventLine.mainPlane);
	      if(eventLine.zoommonitor == 1.){
	    	  eventLine.monitorzoom.set("mousedrag",map(dragposx,0,3840,0.f,-1.f),map(dragposy,0,1080,-1.f,0.f));
	      }
	      eventLine.zoomsmooth = lerp(eventLine.zoomsmooth,eventLine.zoommonitor,0.15f);
	      if(eventLine.zoomsmooth <= 0.001) eventLine.zoomsmooth = 0;
	      else if(eventLine.zoomsmooth >= 0.999) eventLine.zoomsmooth = 1.f;
	      eventLine.monitorzoom.set("zoom",eventLine.zoomsmooth);
	      //monitorzoom.set("zoom",map(zoomsmooth,0.,1.,0.5,1.));
	      p.shader(eventLine.monitorzoom);
	      p.shape(eventLine.monitor);
	      p.resetShader();
	      p.popMatrix();
	    }
	  
	   p.image(eventLine.connLabel.connector,1920,0);
    	
    }
   

    public void mousePressed(){
    	if (modus == mode.CRISIS) {
		      if((mouseX <= 1920+1280 && mouseX >=1920) && (mouseY >= 720) && mouseButton == LEFT){
		    	eventLine.oscAction = true;
		        eventLine.oscX = (int)(map(mouseX,1920,1920+1280,0,3840));
		        eventLine.oscY = (int)(map(mouseY,720,1080,0,1080));
		      }
		      if((mouseX <= 1920+1280 && mouseX >=1920) && mouseY >=720 && mouseButton == RIGHT){
		         PVector zoomat = eventLine.getUnProjectedPointOnFloor( map(mouseX,1920,1920+1280,0,3840),map(mouseY,720,1080,0,1080), eventLine.floorPos, eventLine.floorDir );
		         eventLine.cameraPosX = (int) (-1. * zoomat.x);
		         eventLine.cameraPosZ = (int) eventLine.minzoom;
		         eventLine.autoCamera = true;
		       }
    	}
    }

	public void mouseReleased(){
		if (modus == mode.CRISIS) {
		      if(mouseButton == LEFT){
		    	eventLine.oscAction = false;
		        eventLine.dragging = false;
		      }
		}
	  }


    public void mouseDragged() {
    if (modus == mode.CRISIS) {
       if(mouseButton == LEFT){
    	 eventLine.oscX = (int)(map(mouseX,1920,1280+1920,0,3840));
    	 eventLine.oscY = (int)(map(mouseY,720,1080,0,1080));
      }
    }
    }
    
}

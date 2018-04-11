package welchezukunft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;

public class timeline extends PApplet{
	
	public static mode modus = mode.CRISIS;
	
	public static calibrationGui calibrationWin;
	public static requestSQL accessSQL;

	static List<wordcloud> clouds;
	static List<workshopinfo> workshops;
	static List<colorGradient> colorGradients;
	static wordcloud curCloud;
	static logo logoWZ;
	
	
	PVector lookat = new PVector(0,0,400);
	
	PFont menufont;
	PImage calibration_img;
	static PImage timeline;
	static PShader corner;
	static PShader monitorzoom;
	static PShader gradientShader;
	static PGraphics mainOutput;
	static PGraphics caliOutput;
	static PGraphics wordcloudOutput;
	static PGraphics wordcloudstatus;
	
	PShape displayKW, displayCR, display2;
	PShape statusGradient;
	PShape monitor;
	
	public static float zoommonitor = 0;
	public static boolean showzoommonitor = true;
	public static float zoomsmooth = 0;
	
	static boolean init = false;
	static boolean initscale = false;
	static boolean calibration = false;
	boolean useshader = true;
	boolean showall = false;
	boolean showtext = true;
	boolean cloudExist = false;
	static boolean showlogo = false;
	
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
    
    static int sizeTableX = 3840;
    static int sizeTableY = 1080;
    
	public void settings(){
		calibrationWin = new calibrationGui(this);
		accessSQL = new requestSQL(this);
		loadWorkshopInfos();

        fullScreen(P3D, SPAN);
        smooth(16);
        
        widthBG = width;
        newwidthBG = widthBG;
        aspect = (this.sizeTableX/this.sizeTableY);
        
        clouds = new ArrayList<wordcloud>();
        colorGradients = new ArrayList<colorGradient>();
    }
	
	public void setup(){
		frameRate(60);
		//create Processing stuff
		mainOutput = createGraphics(this.sizeTableX, this.sizeTableY, P3D);
		wordcloudOutput = createGraphics(this.sizeTableX, this.sizeTableY, P3D);
		caliOutput = createGraphics(this.sizeTableX, this.sizeTableY, P3D);
		wordcloudstatus = createGraphics(this.sizeTableX,300,P3D);

		//load externals
		menufont = createFont("Avenir LT 45 Book", 148,true);
		calibration_img = loadImage("./resources/pic/diagonal3.png");
		corner = loadShader("./resources/shader/corner.glsl");

		gradientShader = loadShader("./resources/shader/gradient.glsl");
		
		//init EventTimeline
		eventLine = new eventTimeline(this.sizeTableX,this.sizeTableY,this);	
		//create menu buttons
		eventLine.menu.createButtons();
		eventLine.menu.createButtonsEvent();

		//init internals
		createTimelineTexture();
		createDisplay();	
		createMonitor();
		createStatusGradient();
		loadGradients();
		
		//load data from sql
		accessSQL.getWordsSetup();

		this.logoWZ = new logo(this);
		
		if(init == false){
			//for some reason we have to call postdraw once before drawing events (?)
			eventLine.postdraw();
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
				    //TODO: calculate correct position right
				    mainOutput.noStroke();
			    	if(curCloud.words.size() > 0) {
			    		//newwidthBG = (widthBG < curCloud.words.get(curCloud.words.size()-1).pos.x) ? curCloud.words.get(curCloud.words.size()-1).pos.x : widthBG  ;
			    		newwidthBG = curCloud.words.get(curCloud.words.size()-1).pos.x;
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
					
				  
					
					mainOutput.endDraw();
					wordcloudOutput.beginDraw();
					wordcloudOutput.image(mainOutput,0,0);
					if(this.showlogo == true){
					    	this.logoWZ.drawlogo();
					    	this.wordcloudOutput.image(logoWZ.logoPlane,0,0);
					   }
					wordcloudOutput.endDraw();
					
					
					//draw statusbar
					wordcloudstatus.beginDraw();
					wordcloudstatus.clear();
					int idC = clouds.get(currentCloudid).id -1;
					gradientShader.set("color1", colorGradients.get(idC).getR1()/255.f,colorGradients.get(idC).getG1()/255.f,colorGradients.get(idC).getB1()/255.f);
					gradientShader.set("color2", colorGradients.get(idC).getR2()/255.f,colorGradients.get(idC).getG2()/255.f,colorGradients.get(idC).getB2()/255.f);
					wordcloudstatus.shader(gradientShader);
					wordcloudstatus.shape(statusGradient);
					wordcloudstatus.resetShader();
					//TODO: show current postion
					wordcloudstatus.textFont(menufont);
					wordcloudstatus.textSize(40);
					wordcloudstatus.text(clouds.get(currentCloudid).name, 10, 110);
					wordcloudstatus.endDraw();
					image(wordcloudstatus,1920,720,wordcloudstatus.width,wordcloudstatus.height);
					
			     	}
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
    	
    	// post draw events
    	if (modus == mode.CRISIS && init == true && calibration == false) {
    		//this.events(this.g);
    		image(eventLine.mainPlane,1920,720,1280,360);
    		image(eventLine.connLabel.connector,1920,0);
    		eventLine.postdraw();
    	}
    	
    	//drawmonitor
	    if(this.showzoommonitor == true && init == true && calibration == false){
	    	  PGraphics output = this.wordcloudOutput;
	    	  if(modus == mode.CRISIS) {
	    		  output = eventLine.mainPlane;
	    	  }
		      pushMatrix();
		      this.monitorzoom.set("tex", output);
		      if(this.zoommonitor == 1.){
		    	  this.monitorzoom.set("mousedrag",map(dragposx,0,3840,0.f,-1.f),map(dragposy,0,1080,-1.f,0.f));
		      }
		      this.zoomsmooth = lerp(this.zoomsmooth,this.zoommonitor,0.15f);
		      if(this.zoomsmooth <= 0.001) this.zoomsmooth = 0;
		      else if(this.zoomsmooth >= 0.999) this.zoomsmooth = 1.f;
		      this.monitorzoom.set("zoom",this.zoomsmooth);
		      //monitorzoom.set("zoom",map(zoomsmooth,0.,1.,0.5,1.));
		      shader(this.monitorzoom);
		      shape(this.monitor);
		      resetShader();
		      popMatrix();
		    }

    	if (modus == mode.KEYWORDS) {
	    	if(frameCount % 60 == 0) {
	    		this.accessSQL.updateWords();
	    	}
    	}
    	
    	//Status
    	pushMatrix();
    	translate(1920,0);
    	textSize(13);
    	text("fps: " + frameRate,1720,1020);
    	if (modus == mode.KEYWORDS) {
    		text("cloudID: " + currentCloudid,1720,980);
    	}
    	//surface.setTitle("fps: "+ frameRate + "//" + "wordcount = " + words.size());
    	popMatrix();
    	
    }
    
    private void loadGradients() {
    	
    	JSONArray jsonA = loadJSONArray("./resources/data/colors.json");
    	for(int i = 0; i < jsonA.size(); i++) {
    		JSONObject json = jsonA.getJSONObject(i);
    		
    		String col1 = json.getString("color1");
    		String col2 = json.getString("color2");
    		int fc1 = json.getInt("fontC1");
    		int fc2 = json.getInt("fontC2");
    		
    		int r1 = Integer.valueOf( col1.substring( 1, 3 ), 16 );
            int g1 = Integer.valueOf( col1.substring( 3, 5 ), 16 );
            int b1 = Integer.valueOf( col1.substring( 5, 7 ), 16 );
    		
            int r2 = Integer.valueOf( col2.substring( 1, 3 ), 16 );
            int g2 = Integer.valueOf( col2.substring( 3, 5 ), 16 );
            int b2 = Integer.valueOf( col2.substring( 5, 7 ), 16 );
   
    		colorGradient colnew = new colorGradient(r1,r2,g1,g2,b1,b2,fc1,fc2); 
    		colorGradients.add(colnew);
    	}
    }
   
    private void createMonitor() {
		this.monitor = createShape();
		this.monitor.beginShape(PConstants.QUADS);
		this.monitor.textureMode(PConstants.NORMAL);
		this.monitor.vertex(1920,0,0.125f,1);
		this.monitor.vertex(3840,0,0.875f,1);
		this.monitor.vertex(3840,720,0.875f,0);
		this.monitor.vertex(1920,720,0.125f,0);
		this.monitor.texture(eventLine.mainPlane);
		this.monitor.endShape();

		this.monitorzoom = loadShader("./resources/shader/monitorzoom2_frag.glsl");
		this.monitorzoom.set("tex", eventLine.mainPlane);
		this.monitorzoom.set("zoom",zoommonitor);
		this.monitorzoom.set("mousedrag",0.f, 0.f);
    }
    
    void createTimelineTexture() {
    	  int heightTimelineTexture = 800;
    	  timeline = createImage(1,heightTimelineTexture,RGB);
    	  timeline.loadPixels();
    	  for (int i = 0; i < timeline.pixels.length; i++) {
    		  float delta = 1.f- (0.5f * (cos(((float)i/(float)heightTimelineTexture)*TWO_PI) + 1.f));
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
    	
    	else if (key == 'g'){
		 	eventLine.userGui.topPanel.setVisible(true);
		 	eventLine.userGui.guiframe.setVisible(true);
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
    	displayKW.texture(wordcloudOutput);
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
     
    private void createStatusGradient() {
    	statusGradient = createShape();
    	statusGradient.beginShape();
    	statusGradient.textureMode(NORMAL);
    	statusGradient.noStroke();
    	statusGradient.vertex(0,0,0,0);
    	statusGradient.vertex(1920,0,1,0);
    	statusGradient.vertex(1920,60,1,1);
    	statusGradient.vertex(0,60,0,1);
    	statusGradient.endShape();
    	
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
    
    private void loadWorkshopInfos() {
		workshops = new ArrayList<workshopinfo>();
		JSONArray jsonA = loadJSONArray("./resources/data/workshops.json");
		for(int i = 0; i < jsonA.size(); i++) {
			JSONObject json = jsonA.getJSONObject(i);
			workshopinfo newWs = new workshopinfo(json.getInt("value"),json.getString("name"));
			workshops.add(newWs);
		}
	}
    
}

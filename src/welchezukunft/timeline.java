package welchezukunft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	static List<Integer> workshopColors;
	static List<Integer> workshopColorsBG;
	static wordcloud curCloud;
	static logo logoWZ;
	
	
	PVector lookat = new PVector(0,0,400);
	
	PFont menufont;
	PImage calibration_img;
	static PImage timeline,timelineout;
	static PShader corner;
	static PShader monitorzoom;
	static PShader gradientShader;
	static PGraphics mainOutput;
	static PGraphics caliOutput;
	static PGraphics wordcloudOutput;
	static PGraphics wordcloudstatus;
	
	static PGraphics tester;
	
	PShape displayKW, displayCR, display2;
	PShape statusGradient;
	PShape monitor;
	PShape triangle;
	
	public static float zoommonitor = 0;
	public static boolean showzoommonitor = true;
	public static float zoomsmooth = 0;
	
	static boolean init = false;
	static boolean initscale = false;
	static boolean calibration = false;
	boolean useshader = true;
	static boolean showall = false;
	boolean showtext = true;
	boolean cloudExist = false;
	static boolean showlogo = false;
	
	static float zPos = 400;	
	static float aspect;
	static float fovy = PI/3f;
	float widthBG, newwidthBG, heightBG, newheightBG;
	
	//10 minutes per talk
	static int speaktime = 10;

	//time float between 0. - 1.
	float tposition;
	
	float posTRI = 0;
	
	Random r = new Random();

	public static int badgeSizeX = 150;
	public static int badgeSizeY = 100;
	
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
        aspect = ((float)this.sizeTableX/(float)this.sizeTableY);
        clouds = new ArrayList<wordcloud>();
        colorGradients = new ArrayList<colorGradient>();
        workshopColors = new ArrayList<Integer>();
        workshopColorsBG = new ArrayList<Integer>();
    }
	
	public void setup(){
		frameRate(60);
		//create Processing stuff
		mainOutput = createGraphics(this.sizeTableX, this.sizeTableY, P3D);
		wordcloudOutput = createGraphics(this.sizeTableX, this.sizeTableY, P3D);
		caliOutput = createGraphics(this.sizeTableX, this.sizeTableY, P3D);
		wordcloudstatus = createGraphics(1920,360,P3D);

		tester = createGraphics(1920,720,P2D);
		
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
		createTriangle();
		
		//load data from sql
		thread("loader");

		this.logoWZ = new logo(this);
		
		
		if(init == false){
			//for some reason we have to call postdraw once before drawing events (?)
			eventLine.postdraw();
			init = true; 
		}
	}
	
	public void loader() {
		accessSQL.getWordsSetup();
	}

    public void draw(){
    	
	    clear();
	    background(0);
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
			 	    mainOutput.background(255);
			 	    mainOutput.hint(DISABLE_DEPTH_TEST); 
			 	    mainOutput.textFont(menufont);
			    	 
				    curCloud = clouds.get(currentCloudid);
				    
				    //draw timeline
				    mainOutput.noStroke();
				    //near val = 0. :: far val = 1.
				    float val = (float)(lookat.z -400) / (float)10000;
				    val = (val > 1.f) ? 1.f : val;
				    
			    	if(curCloud.words.size() > 0) {
			    		newwidthBG = curCloud.words.get(curCloud.words.size()-1).pos.x;
			    		newheightBG = 500 + (val * 12000f); 
			    	}
			    	widthBG = lerp(widthBG,newwidthBG,0.15f);
			    	heightBG = lerp(heightBG,newheightBG,0.15f);
			    	mainOutput.pushMatrix();
			    	mainOutput.translate(0, heightBG/2f * -1.f);
			    	val = map(val,0.f,1.f,0.5f,1.f);
			    	mainOutput.tint(255, 120 * val);
			    	mainOutput.image(timeline,width * -1f,0f,width + widthBG,heightBG);
			    	mainOutput.image(timelineout,widthBG,0f,timelineout.width,heightBG);
			    	mainOutput.translate(0, (heightBG/2f) - 90);
			    	mainOutput.fill(0);
			    	mainOutput.noStroke();
			    	mainOutput.rect(width * -1f, -8, width + widthBG, 8);
			    	mainOutput.popMatrix();
			    	
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
					mainOutput.pushMatrix();
					mainOutput.stroke(223,255,23,255);
					mainOutput.shape(curCloud.connections);
					mainOutput.shape(curCloud.allCircles);
					mainOutput.shape(curCloud.wordCloud);
			    	mainOutput.popMatrix();
					
			    	//draw text over knots
			    	for(knotObject k : curCloud.knots) {
			    		if(k.childs.size() > 1) {
			    			 if(in_frustum(k.position,mainOutput) == true && showtext == true) {
				    			 mainOutput.pushMatrix();
				    			 mainOutput.translate(k.position.x,k.position.y,0);
				    			 mainOutput.fill(0);
				    			 mainOutput.textAlign(CENTER, CENTER);
				    			 float tsize = 60 + 20 * k.childs.size() + (400 * val);
				    			 mainOutput.textSize(tsize);
				    			 float gap = mainOutput.textDescent();
				    			 int wi = ceil(mainOutput.textWidth(k.word));
				    			 int hi = ceil(mainOutput.textAscent()+mainOutput.textDescent()+tsize);
				    			 if(k.sign == -1) gap = mainOutput.textAscent() * -1.f;
				    			 mainOutput.text(k.word,0,gap + 2.2f*(k.rad * k.sign)); 
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
								float scaleF = (float)1.+ 0.2f*(1.f - w.init);
								mainOutput.scale(scaleF);
								
								
								mainOutput.textAlign(CENTER, CENTER);
								mainOutput.textSize(60);
								
								float wi = mainOutput.textWidth(w.word);
								float hi = mainOutput.textAscent() + mainOutput.textDescent() + 60;
								mainOutput.fill(255,255*(w.init));
								mainOutput.noStroke();
								mainOutput.rect(-1 * wi/2, -1 * hi/2, wi, hi);
								
								mainOutput.fill(0,255*(w.init));
								mainOutput.text(w.word,0,0);

								mainOutput.popMatrix();
								w.init -= 0.008;
							}
				    	}
			    	}
					
			    	//draw text on visible object
					for(wordObject w : curCloud.words){
					  if(in_frustum(w.pos,mainOutput) == true && showall == false && lookat.z < 405) {	
							  mainOutput.pushMatrix();	
							  mainOutput.textSize(18);
							  mainOutput.textAlign(LEFT,TOP);
							  float alp = 1 - (0.1f * (lookat.z - 400));
							  int col = getComplimentColor(w.pos.x / curCloud.lastposX);
							  mainOutput.fill(col,255*alp);
							  mainOutput.translate(w.pos.x, w.pos.y,0);
							  mainOutput.text(w.word, -1.f * (this.badgeSizeX/2f), -1.f * (this.badgeSizeY/2f),badgeSizeX,badgeSizeY);
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
					wordcloudstatus.pushMatrix();
					wordcloudstatus.translate(50,0);
					int idC = clouds.get(currentCloudid).id -1;
					gradientShader.set("color1", colorGradients.get(idC).getR1()/255.f,colorGradients.get(idC).getG1()/255.f,colorGradients.get(idC).getB1()/255.f);
					gradientShader.set("color2", colorGradients.get(idC).getR2()/255.f,colorGradients.get(idC).getG2()/255.f,colorGradients.get(idC).getB2()/255.f);
					wordcloudstatus.shader(gradientShader);
					wordcloudstatus.shape(statusGradient);
					wordcloudstatus.resetShader();
					wordcloudstatus.popMatrix();
					//display WS-BAdge Background
					wordcloudstatus.textSize(40);
					float textwi = wordcloudstatus.textWidth(clouds.get(currentCloudid).name);
					wordcloudstatus.fill(workshopColorsBG.get(idC));
					wordcloudstatus.noStroke();
					wordcloudstatus.rect(0, 280, textwi + 100, 100);
					
					//display WS colButton
					wordcloudstatus.fill(workshopColors.get(idC));
					wordcloudstatus.noStroke();
					wordcloudstatus.rect(50, 290, 20, 40);
					//display WS title
					wordcloudstatus.textFont(menufont);
					wordcloudstatus.textSize(40);
					wordcloudstatus.textAlign(LEFT,TOP);
					wordcloudstatus.text(clouds.get(currentCloudid).name, 80, 290);
					
					//draw status of current keyword 
					if(showall == false) {
						//show current postion with TRIANGLE
						float newposTRI = 1;
						if(clouds.get(currentCloudid).words.get(wordFocus).overtime == false) {
							newposTRI = (float)clouds.get(currentCloudid).words.get(wordFocus).pos.x / (float)clouds.get(currentCloudid).sizex;
						}
						posTRI = lerp(posTRI,newposTRI,0.3f);
						float trianglePosition = 50 + (posTRI * (wordcloudstatus.width-100));
						if(clouds.get(currentCloudid).words.get(wordFocus).overtime == true) {
						//display overttime badge
						wordcloudstatus.noStroke();
						wordcloudstatus.fill(255,0,0);
						wordcloudstatus.rect(1870, 0, 50, 50);
						wordcloudstatus.fill(255);
						wordcloudstatus.textAlign(CENTER,CENTER);
						wordcloudstatus.textSize(20);
						wordcloudstatus.text("OT", 1895, 25);
						trianglePosition = 1895;
						}
						wordcloudstatus.pushMatrix();
						wordcloudstatus.translate(trianglePosition, 30);
						wordcloudstatus.shape(triangle);
						wordcloudstatus.popMatrix();
						//display current keyword
						wordcloudstatus.textSize(25);
						wordcloudstatus.fill(255);
						int timeinsec = clouds.get(currentCloudid).words.get(wordFocus).time - clouds.get(currentCloudid).starttime;
						int mins = floor(timeinsec / 60);
						int secs = timeinsec - (mins*60);
						String timeText = "time -> " + String.format("%02d", mins) + ":" + String.format("%02d", secs);
						if(clouds.get(currentCloudid).words.get(wordFocus).overtime == true) {
							timeText = timeText.concat(" (OT)");
						}
						wordcloudstatus.textAlign(LEFT, TOP);
						wordcloudstatus.text(clouds.get(currentCloudid).words.get(wordFocus).word, 50, 150);
						wordcloudstatus.text(timeText, 50, 190);
					}	
					
					//draw top3
					else if (showall == true) {
						//sort knots
						List<knotObject> sortedKnots = clouds.get(currentCloudid).knots.stream()
								.sorted(Comparator.comparing(knotObject -> knotObject.childs.size()))
								.collect(Collectors.toList());
						//check if less than 3 knots
						int top = 3;
						int maxiChilds = sortedKnots.get(sortedKnots.size()-1).getCount();
						top = (sortedKnots.size() > top) ? top : sortedKnots.size();
						//start draw top3-stat
						wordcloudstatus.pushMatrix();
						wordcloudstatus.translate(720,70);
						for(int i = 0; i < top; i++) {
							int pos = sortedKnots.size() - 1 - i;
							if(sortedKnots.get(pos).childs.size() > 1) {
								int count = sortedKnots.get(pos).getCount();
								//parent.colors.get(this.id
								int col = sortedKnots.get(pos).parent.colors.get(sortedKnots.get(pos).id);
								String word = sortedKnots.get(pos).word;
								wordcloudstatus.fill(col);
								wordcloudstatus.noStroke();
								float sizeCir = 200 * ((float)count / (float)maxiChilds);
								wordcloudstatus.ellipse(200,145,sizeCir,sizeCir);
								wordcloudstatus.textAlign(LEFT,TOP);
								wordcloudstatus.fill(255);
								wordcloudstatus.textSize(25);
								wordcloudstatus.text(word,0,0,400,115);
								wordcloudstatus.text(count + " x",280,220);
								wordcloudstatus.translate(400, 0);
							}
						}
						wordcloudstatus.popMatrix();
					
					}
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
    	
       	eventLine.userGui.guiframe.setTitle("fps: "+ frameRate);
    	
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
    		
    		int colorworkshop = color(r2,g2,b2);
    		workshopColors.add(colorworkshop);
    		
    		int colorworkshop2 = color(r1,g1,b1);
    		workshopColorsBG.add(colorworkshop2);
    		
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
    
    public void createTriangle() {
    	triangle = createShape();
    	triangle.beginShape();
    	triangle.noStroke();
    	triangle.fill(255,255);
    	triangle.vertex(0, 0);
    	triangle.vertex(10, 40);
    	triangle.vertex(-10, 40);
    	triangle.endShape();
    }
    
    void createTimelineTexture() {
    	  int heightTimelineTexture = 800;
    	  timeline = createImage(1,heightTimelineTexture,RGB);
    	  timeline.loadPixels();
    	  for (int i = 0; i < timeline.pixels.length; i++) {
    		  float delta = pow(0.5f * (cos(((float)i/(float)heightTimelineTexture)*TWO_PI) + 1.f),2);
    		  timeline.pixels[i] = color(delta * 255f); 
    	  }
    	  timeline.updatePixels();	
    	  
    	  timelineout = createImage(100,heightTimelineTexture,RGB);
    	  timelineout.loadPixels();
    	  for (int i = 0; i < timelineout.pixels.length; i++) {
    		  int row = ceil((float)i / 100f);		  
    		  float delta = pow(0.5f * (cos(((float)row/(float)heightTimelineTexture)*TWO_PI) + 1.f),2);
    		  float deltax = (float)(i%timelineout.width) / (float)timelineout.width;
    		  float col = lerp((delta * 255f),255,deltax);
    		  timelineout.pixels[i] = color(col); 
    	  }
    	  timelineout.updatePixels();	
    }
  
    
    public void keyPressed(){
    	if (key == 'c'){
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
	 
    public static void zoomIn() {
    	//zoom near
    	zPos = 400;
		showall = false;
    }
    
    public static void zoomOut() {
    	curCloud = clouds.get(currentCloudid);
	    //show all
		if(curCloud.words.size() > 1) {
			showall = true;
			
			float xStart = curCloud.words.get(0).pos.x;
			float xEnd = curCloud.words.get(curCloud.words.size()-1).pos.x;
			if(xStart == xEnd) xEnd += 100;
			
			float yStart = abs(curCloud.minY);
			float yEnd = curCloud.maxY;	
	
			float radAngle = radians(fovy);
			float radHFOV = 2f * atan(tan(radAngle / 2f) * aspect);
			float fovx = degrees(radHFOV);
			
			float distancex = 1f/(2f * (float)tan((fovx/2f)/(float)(xEnd-xStart)));
			float distancey = 1f/(2f * tan((fovy/2)/(curCloud.maxY*1.05f-curCloud.minY*1.05f)));
			
			zPos = Math.max(distancex * 1.7778f, distancey * 1.2f);
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
    
    private void createStatusGradient() {
    	statusGradient = createShape();
    	statusGradient.beginShape();
    	statusGradient.textureMode(NORMAL);
    	statusGradient.noStroke();
    	statusGradient.vertex(0,0,0,0);
    	statusGradient.vertex(1820,0,1,0);
    	statusGradient.vertex(1820,60,1,1);
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
    
    private int getComplimentColor(float time) {
    	int red = (int)(colorGradients.get(currentCloudid).getR1() * time + colorGradients.get(currentCloudid).getR2() * (1 - time));
    	int green = (int)(colorGradients.get(currentCloudid).getG1() * time + colorGradients.get(currentCloudid).getG2() * (1 - time));
    	int blue = (int)(colorGradients.get(currentCloudid).getB1() * time + colorGradients.get(currentCloudid).getB2() * (1 - time));
        // get existing colors
        int alpha = 255;
        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;
        return color(red, green, blue);
      }

    
    
}

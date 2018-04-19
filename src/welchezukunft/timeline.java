package welchezukunft;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
	
	public static physicsSum result;
	
	PVector lookat = new PVector(0,0,400);
	
	PFont menufont;
	PImage calibration_img;
	static PImage imagelogo;
	static PImage timeline,timelineout;
	static PShader corner;
	static PShader monitorzoom;
	static PShader gradientShader;
	static PGraphics mainOutput;
	static PGraphics caliOutput;
	static PGraphics wordcloudOutput;
	static PGraphics wordcloudstatus;
	
	PShape displayKW, displayCR, displayCRS, display2;
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
	boolean cloudExist = false;
	static boolean showlogo = false;
	static boolean automodus = true;
	
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
	public static int badgeSizeY = 70;
	
	static int currentCloudid = -1;
	static int wordFocus = 0;
	
	public static eventTimeline eventLine;
	
    static int dragposx;
    static int dragposy;
    
    static PVector mousePos;
    
    static int sizeTableX = 3840;
    static int sizeTableY = 1080;
    
    static int black;
	static int white;
	
	static int autoZoomTimer = 0;
    
	public void settings(){
		calibrationWin = new calibrationGui(this);
		accessSQL = new requestSQL(this);
		loadWorkshopInfos();
		
        fullScreen(P3D, SPAN);
        smooth(8);
        
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

		
		black = color(0);
		white = color(255);
		
		//load externals
		menufont = createFont("Avenir LT 45 Book", 148,true);
		calibration_img = loadImage("./resources/pic/diagonal3.png");
		corner = loadShader("./resources/shader/corner.glsl");
		imagelogo = loadImage("./resources/pic/wz_logo.png");
		gradientShader = loadShader("./resources/shader/gradient.glsl");
		
		//init EventTimeline
		eventLine = new eventTimeline(this.sizeTableX,this.sizeTableY,this);	
		//create menu buttons
		eventLine.menu.createButtons();
		eventLine.menu.createButtonsEvent();

		result = new physicsSum(this);
		
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
				    synchronized (curCloud.words) {
				    	if(curCloud.words.size() > 0) {
				    		newwidthBG = curCloud.words.get(curCloud.words.size()-1).pos.x;
				    		newheightBG = 500 + (val * 12000f); 
				    	}
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
			    	synchronized (curCloud.words) {
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
					
			    	//draw text on knots
			    	synchronized (curCloud.knots) {
				    	for(knotObject k : curCloud.knots) {
				    		if(k.childs.size() > 1) {
				    			 if(in_frustum(k.position,mainOutput) == true) {
					    			 mainOutput.pushMatrix();
					    			 PVector textPos = new PVector();
					    			 mainOutput.translate(k.position.x,k.position.y,0);
					    			 mainOutput.translate(textPos.x,textPos.y,0);
					    			 mainOutput.fill(0);
					    			 mainOutput.textAlign(LEFT, CENTER);
					    			 float tsize = 60 + 5 * k.childs.size() + (100 * val);	
					    			 mainOutput.textSize(tsize);
					    			 float gap = mainOutput.textDescent();
					    			 int wi = ceil(mainOutput.textWidth(k.word));
					    			 int hi = ceil(mainOutput.textAscent()+mainOutput.textDescent()+tsize);
					    			 if(k.sign == -1) gap = mainOutput.textAscent() * -1.f;
					    			 //mainOutput.translate(hi/2 * k.sign, 0);
					    			 mainOutput.rotateZ(radians(90) * k.sign);
					    			 mainOutput.translate(k.rad * 1.2f,0);
					    			 mainOutput.text(k.word,0,0);//gap + 1.2f*(k.rad * k.sign)); 
					    			 mainOutput.popMatrix();
					    		 }
				    		}	
				    	}
			    	}
			    	   	
			    	float scl = 1f;
			    	//init FX - only in realtime modus / not on setup
			    	if(init == true) {
			    		synchronized (curCloud.words) {
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
			    	}
					
			    	//draw text on visible object
			    	synchronized (curCloud.words) {
						for(wordObject w : curCloud.words){
						  if(in_frustum(w.pos,mainOutput) == true && showall == false && lookat.z < 405) {	
								  mainOutput.pushMatrix();	
								  mainOutput.textSize(18);

							      int chars = w.word.length();
								  if(chars > 20) chars = chars / 2;
								  float minTw = (float) ((18 / mainOutput.textWidth(w.word.substring(0,chars))) * (badgeSizeX * 0.9));
							      float minTh = (float) ((18 / (mainOutput.textAscent() + mainOutput.textDescent())) * (badgeSizeY*0.9));
								  mainOutput.textSize(min(minTw,minTh));
								  
								  mainOutput.textAlign(CENTER,CENTER);
								  float alp = 1 - (0.1f * (lookat.z - 400));
	
								  float timeX = (w.pos.x > 10000) ? 10000 : w.pos.x;
								  int colBG = curCloud.colors.get(w.id);  
								  //int col = getFontColor(timeX / 10000.f);
								  int col = getFontColorbasedonBG((int)red(colBG),(int)green(colBG),(int)blue(colBG));
								  mainOutput.fill(col,255*alp);
								  mainOutput.translate(w.pos.x, w.pos.y,0);
								  mainOutput.text(w.word, -1.f * (this.badgeSizeX/2f), -1.f * (this.badgeSizeY/2f),badgeSizeX,badgeSizeY);
								  mainOutput.popMatrix();
						  		}
						  if(wordFocus == curCloud.words.indexOf(w) && showall == false && automodus == false) {
							  mainOutput.pushMatrix();
							  mainOutput.translate(w.pos.x, w.pos.y,0);
							  mainOutput.translate(-1.2f * (this.badgeSizeX / 2f), 0);
							  mainOutput.rotate(radians(90));
							  mainOutput.fill(0);
							  mainOutput.scale((float) 0.6);
							  mainOutput.shape(triangle);
							  mainOutput.popMatrix();
						  }
						 }
			    	}
									
					mainOutput.endDraw();
					
					//create Output
					if(this.showlogo == true){
				    	this.logoWZ.drawlogo();
					}
					wordcloudOutput.beginDraw();
					wordcloudOutput.image(mainOutput,0,0);
					if(this.showlogo == true){
					    	this.wordcloudOutput.image(logoWZ.logoPlane,0,0,logoWZ.logoPlane.width,logoWZ.logoPlane.height);
					   }
					wordcloudOutput.endDraw();
								
					//draw statusbar
					wordcloudstatus.beginDraw();
					wordcloudstatus.clear();
					//gradient
					wordcloudstatus.pushMatrix();
					wordcloudstatus.translate(50,15);
					int idC = clouds.get(currentCloudid).id -1;
					gradientShader.set("color1", colorGradients.get(idC).getR1()/255.f,colorGradients.get(idC).getG1()/255.f,colorGradients.get(idC).getB1()/255.f);
					gradientShader.set("color2", colorGradients.get(idC).getR2()/255.f,colorGradients.get(idC).getG2()/255.f,colorGradients.get(idC).getB2()/255.f);
					wordcloudstatus.shader(gradientShader);
					wordcloudstatus.shape(statusGradient);
					wordcloudstatus.resetShader();
					wordcloudstatus.popMatrix();
					//scale
					if(showall == false) {
						wordcloudstatus.pushMatrix();
						wordcloudstatus.translate(50,0);
						wordcloudstatus.textSize(12);
						wordcloudstatus.fill(255);
						for(int i = 0; i < 6; i ++) {
							
							wordcloudstatus.text(i*2 + ":00", (1820/5) * i, 0);
							wordcloudstatus.stroke(255);
							wordcloudstatus.strokeWeight(2);
							wordcloudstatus.line((1820/5) * i, 15, (1820/5) * i, 45);
						}
						wordcloudstatus.noStroke();
						wordcloudstatus.popMatrix();
					}
					//display WS-BAdge Background
					wordcloudstatus.textSize(40);
					float textwi = wordcloudstatus.textWidth(clouds.get(currentCloudid).name);
					
					boolean twolines = false;
					if (textwi > 800) {
						textwi /= 2.;
						twolines = true;
					}
					wordcloudstatus.fill(workshopColorsBG.get(idC));
					wordcloudstatus.noStroke();
					wordcloudstatus.rect(0, 160, textwi + 120, 130);

					//display WS title
					wordcloudstatus.fill(workshopColors.get(idC));
					wordcloudstatus.textFont(menufont);
					wordcloudstatus.textSize(40);
					wordcloudstatus.textAlign(LEFT,TOP);
					wordcloudstatus.text(clouds.get(currentCloudid).name, 80, 170,textwi+100,120);
					
					//draw status of current keyword 
					if(showall == false) {
						//show current postion with TRIANGLE
						float newposTRI = 1;
						synchronized (clouds.get(currentCloudid).words) {
							if(clouds.get(currentCloudid).words.get(wordFocus).overtime == false) {
								newposTRI = (float)clouds.get(currentCloudid).words.get(wordFocus).pos.x / (float)clouds.get(currentCloudid).sizex;
							}
							posTRI = lerp(posTRI,newposTRI,0.3f);
							float trianglePosition = 50 + (posTRI * (wordcloudstatus.width-100));
							if(clouds.get(currentCloudid).words.get(wordFocus).overtime == true) {
								//display overttime badge
								wordcloudstatus.noStroke();
								wordcloudstatus.fill(255,0,0);
								wordcloudstatus.rect(1870, 15, 50, 35);
								wordcloudstatus.fill(255);
								wordcloudstatus.textAlign(CENTER,CENTER);
								wordcloudstatus.textSize(20);
								wordcloudstatus.text("OT", 1895, 25);
								trianglePosition = 1895;
							}
						wordcloudstatus.pushMatrix();
						wordcloudstatus.translate(trianglePosition, 30);
						wordcloudstatus.fill(255);
						wordcloudstatus.shape(triangle);
						wordcloudstatus.popMatrix();
						}
						//display current keyword
						wordcloudstatus.textSize(25);
						wordcloudstatus.fill(255);
						synchronized (clouds.get(currentCloudid).words) {
							int timeinsec = clouds.get(currentCloudid).words.get(wordFocus).time - clouds.get(currentCloudid).starttime;
							int mins = floor(timeinsec / 60);
							int secs = timeinsec - (mins*60);
							String timeText = "time -> " + String.format("%02d", mins) + ":" + String.format("%02d", secs);
							if(clouds.get(currentCloudid).words.get(wordFocus).overtime == true) {
								timeText = timeText.concat(" (OT)");
							}
							wordcloudstatus.textAlign(LEFT, TOP);
							wordcloudstatus.text(clouds.get(currentCloudid).words.get(wordFocus).word, 50, 80);
							wordcloudstatus.text(timeText, 50, 120);
						}
					}	
					
					//draw top3
					else if (showall == true) {
						//sort knots
						synchronized(clouds.get(currentCloudid).knots) {
							List<knotObject> sortedKnots = clouds.get(currentCloudid).knots.stream()
									.sorted(Comparator.comparing(knotObject -> knotObject.childs.size()))
									.collect(Collectors.toList());
							//check if less than 3 knots
							int top = 3;
							int maxiChilds = sortedKnots.get(sortedKnots.size()-1).getCount();
							top = (sortedKnots.size() > top) ? top : sortedKnots.size();
							//start draw top3-stat
							
							wordcloudstatus.pushMatrix();
							wordcloudstatus.translate(850,260);
							wordcloudstatus.rotate(radians(90)*-1.f);
							wordcloudstatus.fill(255);
							wordcloudstatus.textSize(40);
							wordcloudstatus.text("TOP 3", 0, 0);
							wordcloudstatus.popMatrix();
							
							wordcloudstatus.pushMatrix();
							wordcloudstatus.translate(1020,70);
							for(int i = 0; i < top; i++) {
								int pos = sortedKnots.size() - 1 - i;
								if(sortedKnots.get(pos).childs.size() > 1) {
									int count = sortedKnots.get(pos).getCount();
									//parent.colors.get(this.id
									int col = sortedKnots.get(pos).parent.colors.get(sortedKnots.get(pos).id);
									String word = sortedKnots.get(pos).word;
									wordcloudstatus.fill(col);
									wordcloudstatus.noStroke();
									float sizeCir = 150 * ((float)count / (float)maxiChilds);
									wordcloudstatus.ellipse(150,135,sizeCir,sizeCir);
									wordcloudstatus.textAlign(LEFT,TOP);
									wordcloudstatus.fill(255);
									wordcloudstatus.textSize(25);
									wordcloudstatus.text(word,0,20,300,105);
									float tw = wordcloudstatus.textWidth(word);
									
									wordcloudstatus.strokeWeight(3);
									wordcloudstatus.stroke(70);
									wordcloudstatus.line(tw*0.666f, 20 + 35, 150, 135);
									wordcloudstatus.ellipse(150,135,5,5);
									wordcloudstatus.text(count + " x",230,190);
									wordcloudstatus.translate(300, 0);
								}
							}
							wordcloudstatus.popMatrix();
						}
					}
					wordcloudstatus.endDraw();
					image(wordcloudstatus,1920,720,wordcloudstatus.width,wordcloudstatus.height);					
			     	}
		    	}
    		
    		else if (modus == mode.CRISIS) {
    			eventLine.drawing();
    			}
    		else if (modus == mode.CRISISSUM) {
    			result.drawPhysics();
    			image(result.legend,1920,720,result.legend.width,result.legend.height);
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
    		else if (modus == mode.CRISISSUM) {
    			shape(displayCRS);
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
	    	  else if(modus == mode.CRISISSUM) {
	    		  output = result.allplane;
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
 
      	displayCRS = createShape();
    	displayCRS.beginShape();
    	displayCRS.textureMode(NORMAL);
    	displayCRS.texture(result.allplane);
    	displayCRS.vertex(0, 0,0,0);
    	displayCRS.vertex(3840, 0,1,0);
    	displayCRS.vertex(3840, 1080,1,1);
    	displayCRS.vertex(0, 1080,0,1);
    	displayCRS.endShape(); 
    	
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
    	triangle.disableStyle();
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
    	if(currentCloudid >= 0) {
    		zPos = 400;
			showall = false;
    	}
    }
    
    public static void zoomOut() {
    	if(currentCloudid >= 0) {
	    	curCloud = clouds.get(currentCloudid);
		    //show all
	    	synchronized (curCloud.words) {
				if(curCloud.words.size() > 1) {
					showall = true;			
					float xStart = curCloud.words.get(0).pos.x;
					float xEnd = curCloud.words.get(curCloud.words.size()-1).pos.x;
					if(xStart == xEnd) xEnd += 100;
					
					float lenFac = 1.f - ((xEnd - xStart) / 10000.f);
					lenFac = (lenFac < 0.f) ? 0.f : lenFac;
					
					float radAngle = radians(fovy);
					float radHFOV = 2f * atan(tan(radAngle / 2f) * aspect);
					float fovx = degrees(radHFOV);
					
					float distancex = 1f/(2f * (float)tan((fovx/2f)/(float)(xEnd-xStart)));
					float distancey = 1f/(2f * tan((fovy/2)/(curCloud.maxY*1.05f-curCloud.minY*1.05f)));
	
					zPos = Math.max(distancex * 1.9f, distancey * (1.4f + lenFac));
				}
	    	}
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
    	statusGradient.vertex(1820,45,1,1);
    	statusGradient.vertex(0,45,0,1);
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
    

    private int getFontColor(float time) {
    	int R = (int)(colorGradients.get(currentCloudid).getR1() * time + colorGradients.get(currentCloudid).getR2() * (1 - time));
    	int G = (int)(colorGradients.get(currentCloudid).getG1() * time + colorGradients.get(currentCloudid).getG2() * (1 - time));
    	int B = (int)(colorGradients.get(currentCloudid).getB1() * time + colorGradients.get(currentCloudid).getB2() * (1 - time));
 	    int d = 0;
 	    // Counting the perceptive luminance - human eye favors green color... 
 	    double a = 1 - (0.299 * (float)R + 0.587 * (float)G + 0.114 * (float)B)/255;
 	    if (a < 0.186)
 	       d = 0; // bright colors - black font
 	    else
 	       d = 255; // dark colors - white font
    	return d;
    }
    
    private int getFontColorbasedonBG(int R, int G, int B) {

    	int d = 0;
 	    // Counting the perceptive luminance - human eye favors green color... 
 	    double a = 1 - (0.299 * (float)R + 0.587 * (float)G + 0.114 * (float)B)/255;
 	    if (a < 0.186)
 	       d = 0; // bright colors - black font
 	    else
 	       d = 255; // dark colors - white font
    	return d;
    }
    
}

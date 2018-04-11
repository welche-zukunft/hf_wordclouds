package welchezukunft;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

public class eventTimeline {

	PApplet parent;

	public static List<Event> eventList;
	public static PShape objectTimeline;
	public static PShape connections;
	
	
	//size of eventBadges
	public static int size = 7;
	
	String [] cols = {"dbe0ff","fcf197","fbb100","f87676","ea373d","b91a2e","cf3571","b8d8d3","72c2a9","4dac5b","add396","91b8df","0092c3","01559e","aaaaaa"};
	public static int cols_rgb [];
	
	PImage [] images;
	
	public static float minzoom = 200;
	public static float maxzoom = 6000;
	
	int windowwidth = 3840;
	int windowheight = 1080;
	
	// size of Text Areas
	float sizeTextAreaMin = 100;
	float sizeTextAreaMax = 200;
	
	// show timeline or deleted objects
	public static boolean showWSselection = false;
	//status new events
	public static boolean newpending = false;
	public static int currentWSselection = 0;
	public static boolean showtimeline = true;
	
	public static int cameraPosY, cameraPosZ,cameraPosX;
	public static float newcameraPosX,newcameraPosY,newcameraPosZ;
	public static boolean autoCamera;
	public static PVector upperLeftWorld;
	
	public static int state = 0; //ZOOM STATE 0 = NEAR, 1= MID1, 2 = MID2, 3 = FAR
	
	public static int moving = 0;
	

	public static eventBackground eventBack;
	public static eventTimelineMenu menu;
	public static connectionLabel connLabel;
	public static mainGui userGui;
	
	public static PFont menufont;
	public static PFont effectfont;
	
	public static PGraphics contentPlane,contentPlane0,mainPlane,accessPlane,statusPlane;
	
	public static float yearsTextWidth;
	public static float yearsTextHeight;
	
	public static int imgwidth, imgheight;
	
	public static float accesssize = 0;
	public static float accessinner = 0;
	public static boolean accesscirc = false;
	public static boolean accessclick = false;
	public static int accesradius = 400;
	public static float accessx,accessy;
	public static boolean access = false;
	public static boolean dragging = false;
	public static boolean autozoom = true;
	public static float oscX, oscY;
	public static boolean oscClick = false;
	public static boolean oscAction = false;
	public static boolean mouseaction = false;
	public static boolean plenum = true;
	
	public static boolean wPressed = false, sPressed = false,aPressed = false, dPressed = false, shiftPressed = false,qPressed = false,rPressed = false;
	
	int speed = 1;
	float speedscale = 0;
	requestSQL database;
	
	PVector floorPos = new PVector( 0, 0, 0 ); 
	PVector floorDir = new PVector( 0, 0, 1 ); 
	
	float HLcurrentTextSize = 0.f;
	
	public eventTimeline(int width, int height, PApplet parent){
		
		
		
		this.parent = parent;
		this.database = timeline.accessSQL;
		imgwidth = width;
		imgheight = height;
		//Fonts
		this.menufont = parent.createFont("Avenir LT 45 Book", 48,true);
		this.effectfont = parent.createFont("Avenir LT 95 Black Oblique", 96,true);
		
		//setup Badge Object && Colors
		this.objectTimeline = parent.createShape();
		this.connections = parent.createShape();
		this.cols_rgb = new int[cols.length];
		for(int i = 0; i < cols.length;i++){
		   this.cols_rgb[i] = parent.unhex("FF"+cols[i]);
		 }
		
		this.eventBack = new eventBackground(this);
		this.menu = new eventTimelineMenu(this);
		this.connLabel = new connectionLabel(this);
		this.userGui = new mainGui(this);
		
		
		 
		//Main Content
		this.contentPlane0 = parent.createGraphics(imgwidth, imgheight, PConstants.P3D);
		this.contentPlane = parent.createGraphics(imgwidth, imgheight, PConstants.P3D);	  
		this.mainPlane = parent.createGraphics(imgwidth, imgheight, PConstants.P3D);
		this.accessPlane = parent.createGraphics(accesradius,accesradius,PConstants.P3D);
		this.contentPlane.textFont(this.menufont);
		this.contentPlane.textSize(10);
		this.statusPlane = parent.createGraphics(1200, 50, PConstants.P3D);
	
		images = new PImage[57];
		  for(int i = 0; i < 57; i++){
		   images[i] = parent.requestImage("./resources/img/" + (i+1) + ".jpg"); 
		  }
		  
		 eventList = new ArrayList<Event>();
		 eventList.addAll(database.getNewEventsSetup());
		  
		 userGui.updateGUI();
		 this.cameraPosZ = 400;
	}
	
	public void drawing() {
		   	database.updateEvents();
		   	
		    eventBack.drawTimeline();
		    this.drawContent();
		    
		    if(this.access == true){
		      this.access(); 
		    } 
		    
		    if(this.menu.menu == true){
		      this.menu.drawMenu();
		      this.menu.drawMenuText();
		     }

		    if(this.menu.eventmenu== true){
		      this.menu.drawEventMenu();
		      this.menu.drawEventMenuText();
		    }
		    
		    if(this.showWSselection == true){
		        this.menu.drawstatus();
		    }
		    
		    if(timeline.showlogo == true){
		    	timeline.logoWZ.drawlogo();
		    } 
		     
		    this.mainPlane.beginDraw();
		    this.mainPlane.background(125);
		    //mainPlane.image(timelinePlane,0,0);
		    this.mainPlane.image(contentPlane0,0,0);
		    this.mainPlane.image(contentPlane,0,0);
		    
		    if(this.showWSselection == true){
			     this.mainPlane.pushMatrix();
			     this.mainPlane.translate(this.mainPlane.width - 550.f,this.mainPlane.height/2.f,0);
			     this.mainPlane.rotateZ(parent.radians(-90.0f));
			     this.mainPlane.image(this.statusPlane,-this.statusPlane.width/2.f,-this.statusPlane.height/2.f);
			     this.mainPlane.popMatrix();     
		    }
		    
		    if(this.menu.menu == true || this.menu.eventmenu == true){
		    	this.mainPlane.pushMatrix();
		    	this.mainPlane.translate(this.menu.menuposx,this.menu.menuposy,0);
		    	//rotate MENU around Axis to Place it visible for user 
		    	//this.mainPlane.rotateZ((int)Math.toRadians(-90));
		    	//this.mainPlane.rotateZ((float)Math.atan2((this.menu.menuposy)-this.windowheight/2,(float)parent.map((this.menu.menuposx),0,3840,1820,2020)-this.windowwidth/2.f));
		        //this.menu.menurotation = (float)Math.atan2(((this.menu.menuposy)-this.windowheight/2),(float)parent.map((this.menu.menuposx),0,3840,1820,2020)-this.windowwidth/2.f)+(int)Math.toRadians(-90);
		        this.mainPlane.image(this.menu.menuCanvas,-(this.menu.menusize/2.f),-(this.menu.menusize/2.f));
		        this.mainPlane.image(this.menu.menuText,-(this.menu.menusize/2.f),-(this.menu.menusize/2.f));
		      if(this.menu.showbasket == true || this.menu.shownewevents == true){
		    	this.mainPlane.translate(-150,-350,0);
		    	this.mainPlane.image(this.menu.subCanvas,0,0);
		    	this.mainPlane.image(this.menu.subText,0,0);
		      }
		      this.mainPlane.popMatrix();
		    }
		    
		    if(access == true){
		    	this.mainPlane.image(accessPlane, this.accessx-(this.accesradius/2),this.accessy-(this.accesradius/2));
		    }

		    if(timeline.showlogo == true){
		    	this.mainPlane.image(timeline.logoWZ.logoPlane,0,0);
		    } 

		    if(newpending == true){
		    	this.mainPlane.pushMatrix();
		    	this.mainPlane.translate(mainPlane.width/2.f,4,0);
		    	this.mainPlane.fill(0,255);
		    	this.mainPlane.rect(0,0,6,6);
		    	this.mainPlane.popMatrix();
		   }
		    this.mainPlane.endDraw();	    
		}
	
	
	void drawContent(){
		   parent.hint(PConstants.ENABLE_DEPTH_SORT);
		   this.contentPlane0.beginDraw();
		   this.contentPlane0.clear();
		   this.contentPlane0.background(255);
		   this.contentPlane0.camera(this.newcameraPosX, this.newcameraPosY, this.newcameraPosZ,this.newcameraPosX, this.newcameraPosY, 0.f,0.f,1.f,0.f);   
		   this.contentPlane0.pushMatrix();
		   this.contentPlane0.shape(this.eventBack.timeline);
		   this.contentPlane0.shape(objectTimeline); 
		   this.contentPlane0.popMatrix();
		   this.contentPlane0.endDraw();
		   
		   this.contentPlane.beginDraw();
		   this.contentPlane.clear();
		   this.contentPlane.textFont(this.menufont);
		   this.contentPlane.textMode(PConstants.MODEL);
		   this.contentPlane.camera(this.newcameraPosX, this.newcameraPosY, this.newcameraPosZ,this.newcameraPosX, this.newcameraPosY, 0.f,0.f,1.f,0.f);   
		   //draw years & month scale
		   int month = 0;
		   for(int i = 0; i <= this.eventBack.years; i++){
		        PVector v = this.eventBack.timeline.getVertex((i*4)+(month*4));
		        //draw only if in frustum
		        if(timeline.in_frustum(v,contentPlane)){
		          this.contentPlane.textAlign(PConstants.CENTER,PConstants.TOP);
		          this.contentPlane.textSize(25f + ((this.newcameraPosZ-this.minzoom)/(this.maxzoom-this.minzoom))*100f);
		          this.yearsTextHeight = this.contentPlane.textAscent() + this.contentPlane.textDescent();
		          this.yearsTextWidth = this.contentPlane.textWidth("_2000_");
		          this.contentPlane.pushMatrix();
		          this.contentPlane.stroke(255);
		          this.contentPlane.fill(255);
		          this.contentPlane.translate((this.eventBack.TLwidth*-1.f)+(this.eventBack.deltaY * i),-this.yearsTextHeight/2.f,0);
		          this.contentPlane.text(2007+i,0,0);
		          this.contentPlane.popMatrix();
		         } 
		     for(int j = 1; j < 13 && i < this.eventBack.years; j++){
		         PVector v2 = new PVector((this.eventBack.TLwidth*-1.f)+(this.eventBack.deltaY * i)+(this.eventBack.deltyYmonth * j)-((this.eventBack.deltyYmonth)/2.f),0.f,0.f);
		        //draw only if in frustum
		        if(timeline.in_frustum(v2,contentPlane) && (this.state==0 || this.state==1)){
		          contentPlane.textAlign(PConstants.CENTER,PConstants.TOP);
		          contentPlane.textSize(8.f + ((this.newcameraPosZ-this.minzoom)/(this.maxzoom-this.minzoom))*80);
		          this.menu.monthTextHeight = contentPlane.textAscent() + contentPlane.textDescent();
		          contentPlane.pushMatrix();
		          contentPlane.stroke(255);
		          contentPlane.fill(255);
		          contentPlane.translate((this.eventBack.TLwidth*-1.f)+(this.eventBack.deltaY * i)+(this.eventBack.deltyYmonth * j)-((this.eventBack.deltyYmonth)/2.f),-this.menu.monthTextHeight/2.f,0);
		          contentPlane.text(j + " / " + (2007+i),0,0);
		          contentPlane.popMatrix();
		         } 
		         month += 1;
		       }
		         
		   }
		   
		   this.newpending = false;
		   //draw text
		   for(Event event : this.eventList ){
		    if(this.newpending == false && event.getStatus() == Eventstatus.NEWEVENT) this.newpending = true;
		    PVector v = this.objectTimeline.getVertex((int)(this.eventList.indexOf(event)*8));
		    float z = 0;
		    int alfa =170;
		    int col = 240;
		    int fontcol = 50;
		    if(this.eventList.indexOf(event) == moving){
		     z = 0.01f; 
		     alfa = 200;
		     col = 190;
		     fontcol = 0;
		    }
		    this.contentPlane.fill(0);
		    this.contentPlane.stroke(0);
		    //draw only if in frustum
		    if(timeline.in_frustum(v,contentPlane)  && state != 3){
		      float influencecamfont = ((this.newcameraPosZ-this.minzoom)/(this.maxzoom-this.minzoom))*80;
		      //vertical text
		      if(this.state==2){
		       float HLheight = 26.f + influencecamfont ;
		       this.contentPlane.textSize(HLheight);
		       String HLtext = event.getHeadline();
		       float HLsize = contentPlane.textWidth(HLtext);
		       float widthtoborder = Math.abs(upperLeftWorld.y - (event.getYPos()*this.upperLeftWorld.y))+10; 
		       this.contentPlane.textAlign(PConstants.LEFT,PConstants.TOP);
		       if(event.getYPos() <= 0.) widthtoborder = (float) Math.abs((-1. * this.upperLeftWorld.y) - (event.getYPos()*this.upperLeftWorld.y));
		       int lines = (int) (Math.ceil((float)(Math.ceil(HLsize)) / widthtoborder) + 1);
		       
		       this.contentPlane.pushMatrix();
		       if(v.y < 0){
			       this.contentPlane.translate(v.x+(this.size * 3.f)-((lines*HLheight)*0.5f),(v.y - (this.size/2.f)),z);
			       this.contentPlane.rotateZ((float) Math.toRadians(-90));
		       }
		       else if (v.y > 0){
			       this.contentPlane.translate(v.x-(this.size)+((lines*HLheight)*0.5f),v.y + (2*this.size + this.size/2.f),z);
			       this.contentPlane.rotateZ((float) Math.toRadians(-90));
		       }     

		       this.contentPlane.text(HLtext,0,0,widthtoborder,lines*HLheight);
		     //contentPlane.text(HLtext,0,0,200+((newcameraPosZ-minzoom)/(maxzoom-minzoom))*400,150);
		     this.contentPlane.popMatrix();
		      }
		      //horizontal Text
		      else if(this.state==0 || this.state == 1){
		    	  this.contentPlane.pushMatrix();
		       if(v.y < 0){
		    	   this.contentPlane.translate(v.x+this.size*2.5f,v.y + (this.size/2.f),z);
		       }
		       else if (v.y > 0){
		    	   this.contentPlane.translate(v.x+this.size*2.5f,v.y,z);
		       }
		       this.contentPlane.fill(240,col,col,alfa);
		     this.HLcurrentTextSize = 10.f + influencecamfont;
		     this.contentPlane.textSize(HLcurrentTextSize);
		     String text = event.getHeadline() + " ";
		     float textwidth = contentPlane.textWidth(text);
		     float textheight = contentPlane.textAscent() + contentPlane.textDescent();
		     float currentTextWidth = textwidth;
		     if(textwidth < this.sizeTextAreaMin) currentTextWidth = this.sizeTextAreaMin;
		     else if(textwidth > this.sizeTextAreaMax) currentTextWidth = this.sizeTextAreaMax;
		     int headlineLines = (int) (Math.ceil(textwidth / currentTextWidth) + 1);
		     int ContentLines = 0;
		     float contentheight = 0.f;
		     //draw content text additional if zoom is near; only non-deleted objects
		     if(this.state == 0 && this.eventList.indexOf(event) == this.moving){//event.getStatus() != Status.DELETED){
		    	 this.contentPlane.textSize(7.f + influencecamfont);
		       String content = event.getContent();
		       int extralines = timeline.countMatches(content,"\n");
		       float contentwidth = this.contentPlane.textWidth(content);
		       contentheight = (float) ((this.contentPlane.textAscent() + this.contentPlane.textDescent())*1.5);
		       ContentLines = (int) (Math.ceil(contentwidth / currentTextWidth) + extralines);
		     }
		     this.contentPlane.noStroke();
		     this.contentPlane.rect(0,0,currentTextWidth+2,((textheight*headlineLines)+2) + (ContentLines*contentheight)+2 );
		     this.contentPlane.fill(fontcol);
		     this.contentPlane.textAlign(PConstants.LEFT,PConstants.TOP);
		     this.contentPlane.textSize(10.f + influencecamfont);
		     this.contentPlane.text(event.getHeadline(),1,1,currentTextWidth + 2,(textheight*headlineLines)+2);
		     
		     if(this.state == 0 && this.eventList.indexOf(event) == this.moving){
		       contentPlane.textSize(7.f + influencecamfont);
		       contentPlane.text(event.getContent(),1,(textheight*headlineLines)+2,currentTextWidth,(ContentLines*contentheight));
		       
		     }
		     
		     if(event.getImagePath() != null && event.getStatus() != Eventstatus.DELETED){
		       String [] idfromfilename = event.getImagePath().split(".",1);
		       int imgnum = Integer.parseInt(idfromfilename[0]);
		       float aspect = (float)(this.images[imgnum-1].width) / (float)(this.images[imgnum-1].height);
		       this.contentPlane.tint(255,255-fontcol);
		       this.contentPlane.image(images[imgnum-1],currentTextWidth+2,0,50*aspect,50);
		     }
		     
		     this.contentPlane.popMatrix();
		      }
		    }
		   }

		   
		   this.contentPlane.endDraw(); 
		   if(this.dragging == true){
		   // store mouse Position for Object picking must be done here
		   timeline.mousePos = getUnProjectedPointOnFloor( timeline.dragposx, timeline.dragposy, this.floorPos, this.floorDir );
		   }     

		}
	
		  
	void postdraw() {
		    this.upperLeftWorld = this.getUnProjectedPointOnFloor( 0, 20, this.floorPos, this.floorDir );
		    if(timeline.initscale == false){
			     this.scaleVertical(); 
			     this.eventBack.scaleYears();
			     timeline.initscale = true;
		    }
		    
		    this.processPosition();
		    
		    if(this.oscAction == true){
		       if(this.oscClick == false){
		           OSCPressed(this.oscX,this.oscY);
		           this.oscClick = true;
		         }
		         if(this.oscClick == true){
		          OSCDragged(this.oscX,this.oscY); 
		         }
		       }
		       if(this.oscAction == false && oscClick == true){
		    	   this.oscClick = false; 
		    	   this.OSCReleased();
		       }
		    
		  }
		  
		  


		void OSCPressed(float x,float y){
		  if(this.autozoom == true){ 
			  timeline.zoommonitor = 1.f;
		  }
		  if(this.access == false){
		    startaccess(x,y);
		  }
		    int clickposx = (int)x;
		    int clickposy = (int)y;
		    timeline.dragposx = (int)x;
		    timeline.dragposy = (int)y;
		     
		    int col = this.contentPlane0.get(clickposx, clickposy);
		    int r = col >> 020 & 0xFF;
		    int g = col >> 010 & 0xFF;
		    int b = col & 0xFF;
		
		   if((r == 255 && g == 255 && b == 255) || (r == 0 && g == 0 && b == 0)){
		      if(this.menu.menu == false){
		        this.menu.menuposx = clickposx;
		        this.menu.menuposy = clickposy;
		        //fill basket and news
		        this.menu.setupMenu();
		        //start menu
		        this.menu.menu = true;
		        }
		      else return;
		    }
		    
		    else if(dragging == false){
		     this.menu.menuposx = clickposx;
		     this.menu.menuposy = clickposy;
		     float idR = 0 + r;
		     float idG = 0 + g;
		     float idB = b - 1;
		     if(this.showtimeline == false){
		    	 this.cameraPosY = 0;
		    	 this.userGui.setMode(0);
		     }
		     this.moving = (int)((idR*100)+(idG*10)+idB);
		     if(this.moving <= this.objectTimeline.getVertexCount()/8){
		       this.menu.eventmenu = true;
		       this.dragging = true;
		     }
		    }
		  }
		  
		void OSCDragged(float x, float y) {
		  timeline.dragposx = (int)x;
		  timeline.dragposy = (int)y;
		  if(this.menu.menu == true){
		    float dragMenuX = parent.map((timeline.dragposx - this.menu.menuposx)+(this.menu.menusize/2.f),0,this.menu.menusize,-1.f,1.f);
		    float dragMenuY = parent.map((timeline.dragposy - this.menu.menuposy)+(this.menu.menusize/2.f),0,this.menu.menusize,-1.f,1.f);  
		    PVector menudrag = new PVector(dragMenuX,dragMenuY);
		    //menudrag.rotate(this.menu.menurotation * -1.f);
		    int col = this.menu.menuCanvas.get((int)(this.menu.menusize/2.)+(int)(menudrag.x*this.menu.menusize/2.),(int)(this.menu.menusize/2.)+(int)(menudrag.y*this.menu.menusize/2.));
		    int r = col >> 020 & 0xFF;
		    int g = col >> 010 & 0xFF;
		    if(r != 0 && g == 200){
		      r = r - 199;
		      if(r > 0 && r <= this.menu.menubutton.length && this.menu.currentButton != r){
		    	  this.menu.currentButton = r;
		    	  this.menu.buttonHit = true;
		      }
		      //change scroll speed based on position over button
		      if(this.menu.currentButton == 1 ){
		       this.menu.speedbuttonpos = 10.f * this.parent.map(menudrag.x,-0.3333f,-0.8555f,0.f,1.f);
		      }
		      else if(this.menu.currentButton == 2){
		        this.menu.speedbuttonpos = 10.f * this.parent.map(menudrag.x,0.3333f,0.8555f,0.f,1.f);
		      }
		    }
		     else if(r == 0){
		      this.menu.currentButton = 0;
		      this.menu.buttonHit = false;
		    }
		    if(this.menu.showbasket == true || this.menu.shownewevents == true){
		      int col2 = this.menu.subCanvas.get((int)(this.menu.menusize/2.)+(int)(menudrag.x*this.menu.menusize/2.),(int) (200+(int)(this.menu.menusize/2.f)+(int)(menudrag.y*this.menu.menusize/2.f)));
		      int r2 = col2 >> 020 & 0xFF;
		      int g2 = col2 >> 010 & 0xFF;
		    // NEW Events
		    if(this.menu.menu == true && r2!= 0 && g2 == 201){
		      r2 = r2 - 200;
		      if(r2 >= 0 && r2 < this.menu.newids.length){
		    	this.moving = this.menu.newids[r2]-1;
		        this.menu.currentButton = 0;
		        this.menu.buttonHit = false;
		        this.menu.menu = false;
		        this.dragging = true;
		      }
		    }
		    // PENDING EVENTS
		    else if(this.menu.menu == true && r2!= 0 && g2 == 202){
		      r2 = r2 - 200;
		      if(r2 >= 0 && r2 < this.menu.basketids.length){
		    	  this.moving = this.menu.basketids[r2]-1;
		        this.menu.currentButton = 0;
		        this.menu.buttonHit = false;
		        this.menu.menu = false;
		        this.dragging = true;
		      }
		    }
		     }
		   
		  }  
		  
		  if(dragging == true && timeline.mousePos != null){
		        float dragMenuX = parent.map((timeline.dragposx - this.menu.menuposx)+(this.menu.menusize/2.f),0,this.menu.menusize,-1.f,1.f);
		        float dragMenuY = parent.map((timeline.dragposy - this.menu.menuposy)+(this.menu.menusize/2.f),0,this.menu.menusize,-1.f,1.f);  
		        PVector menudrag = new PVector(dragMenuX,dragMenuY);
		        //menudrag.rotate(this.menu.menurotation * -1.f);
		        int col = this.menu.menuCanvas.get((int)(this.menu.menusize/2.)+(int)(menudrag.x*this.menu.menusize/2.),(int)(this.menu.menusize/2.)+(int)(menudrag.y*this.menu.menusize/2.));
		    
		        int r = col >> 020 & 0xFF;
		        int g = col >> 010 & 0xFF;
		        if(r != 0 && g == 203){
		          r = r - 200;
		          //if adding to pending
		          if(r == 0){
		            Event event = eventList.get(moving);
		            event.setStatus(Eventstatus.PENDING);
		            database.timestampMsql.query("UPDATE Event SET timestamp=%s WHERE vertex_id=%s", System.currentTimeMillis(), moving+1);  
		            userGui.updateGUI();
		            oscAction = false;
		            this.menu.eventmenu = false; 
		            moveEvent(false); 
		            OSCReleased();
		            return; 
		            
		          }
		          //if pausing
		          else if(r == 1){
		            Event event = eventList.get(moving);
		            event.setStatus(Eventstatus.DELETED);
		            userGui.updateGUI();
		            oscAction = false;
		            this.menu.eventmenu = false;
		            moveEvent(false);
		            OSCReleased();
		            return;
		          } 
		            
		        }
		        
		        // take from baskets to timeline
		        if(eventList.get(moving).getStatus() != Eventstatus.PLACED){
		          eventList.get(moving).setStatus(Eventstatus.PLACED);
		          userGui.updateGUI();
		        }
		        
		        float mouseYpos = parent.map(timeline.mousePos.y,upperLeftWorld.y* -1.f,upperLeftWorld.y ,-1.f,1.f);
		        if(mouseYpos > 1.) mouseYpos = 1.f;
		        else if (mouseYpos < -1.) mouseYpos = -1.f;
		        eventList.get(moving).setYPos(mouseYpos);
		        eventList.get(moving).setXPos(timeline.mousePos.x);
		        
		        moveEvent(true);
		        
		        
		      }
		}

		void OSCReleased(){
		  timeline.zoommonitor = 0.f;
		  this.menu.clearMenu();
		  wPressed = sPressed = aPressed =  dPressed =  shiftPressed = qPressed = rPressed = false;
		  this.menu.currentButton = 0;
		 if(this.menu.menu == true){
		   this.menu.menu = false; 
		 }
		 if(this.menu.buttonHit == true){
		   this.menu.buttonHit = false;
		 }
		 if(dragging == true){
		   dragging = false;
		 }
		 if(this.menu.eventmenu == true){
		 this.menu.eventmenu = false; 
		 }
		 timeline.mousePos = null;
		}


		void moveEvent(boolean onstage){
		    timeline.mousePos = getUnProjectedPointOnFloor(timeline.dragposx, timeline.dragposy, floorPos, floorDir );
		    float z = timeline.mousePos.z;
		    if(onstage == false){
		      z = maxzoom+100;
		    }
		        int pos = moving*8;
		        PVector v = objectTimeline.getVertex(pos);
		        v.x = timeline.mousePos.x-size;
		        v.y = timeline.mousePos.y-size;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = timeline.mousePos.x+size;
		        v.y = timeline.mousePos.y-size;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = timeline.mousePos.x+size;
		        v.y = timeline.mousePos.y+size;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);      
		         pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = timeline.mousePos.x-size;
		        v.y = timeline.mousePos.y+size;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);  
		        pos += 1;
		        if(timeline.mousePos.y <= 0.){
		          v = objectTimeline.getVertex(pos);
		          v.x = timeline.mousePos.x-(size/4.f);
		          v.y = timeline.mousePos.y+(size);
		          v.z = z;
		          objectTimeline.setVertex(pos,v);  
		          pos += 1;
		          v = objectTimeline.getVertex(pos);
		          v.x = timeline.mousePos.x+(size/4.f);
		          v.y = timeline.mousePos.y+(size);
		          v.z = z;
		          objectTimeline.setVertex(pos,v);
		        }
		        else{
		           v = objectTimeline.getVertex(pos);
		          v.x = timeline.mousePos.x-(size/4.f);
		          v.y = timeline.mousePos.y-(size);
		          v.z = z;
		          objectTimeline.setVertex(pos,v);  
		          pos += 1;
		          v = objectTimeline.getVertex(pos);
		          v.x = timeline.mousePos.x+(size/4.f);
		          v.y = timeline.mousePos.y-(size);
		          v.z = z;
		          objectTimeline.setVertex(pos,v);       
		          
		        }
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = timeline.mousePos.x;
		        v.y = 0.f;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);       
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = timeline.mousePos.x;
		        v.y = 0.f;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);  
		  
		}	  
	
	
	void showall(){
		  this.eventBack.showTimeline();
		  this.cameraPosX  = 0;
		  this.cameraPosZ = (int)this.maxzoom;
		  this.autoCamera = true;
		}
	
	void showTimeline(){
		 float z = this.maxzoom+100;
		 for(Event event : this.eventList){
		  if(event.getStatus() == Eventstatus.DELETED){
		        int pos = (event.getId()-1)*8;
		        PVector v = objectTimeline.getVertex(pos);
		        v.x = 0-this.size;
		        v.y = 0-this.size;
		        v.z = z;
		        this.objectTimeline.setVertex(pos,v);
		        pos += 1;
		        v = this.objectTimeline.getVertex(pos);
		        v.x = 0+this.size;
		        v.y = 0-this.size;
		        v.z = z;
		        this.objectTimeline.setVertex(pos,v);
		        pos += 1;
		        v = this.objectTimeline.getVertex(pos);
		        v.x = 0+this.size;
		        v.y = 0+size;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);      
		         pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = 0-size;
		        v.y = 0+size;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);  
		        pos += 1;
		         v = objectTimeline.getVertex(pos);
		        v.x = 0-(size/4.f);
		        v.y = 0-(size);
		        v.z = z;
		        objectTimeline.setVertex(pos,v);  
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = 0+(size/4.f);
		        v.y = 0-(size);
		        v.z = z;
		        objectTimeline.setVertex(pos,v);       
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = 0.f;
		        v.y = 0.f;
		        v.z = z;
		        objectTimeline.setVertex(pos,v);       
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.x = 0.f;
		        v.y = 0.f;
		        v.z = z;
		        objectTimeline.setVertex(pos,v); 
		      }
		    }

		 this.showtimeline = true;
		 this.cameraPosY = 0; 
		}
	
	void showPaused(){
		  if(this.cameraPosZ <= 1000){
		     this.showtimeline = false;
		     int numDeleted = 0;
		     int z = 0;
		     int itemsColoumn = 3;
		     float yPosPaused = (this.upperLeftWorld.y*-1.f) + (this.size*3);
		     float xPosPaused = 200+this.size+((this.upperLeftWorld.x));
		     for(Event event : eventList){
		      if(event.getStatus() == Eventstatus.DELETED){
		          int pos = (event.getId()-1)*8;
		          float xPos = (xPosPaused + ((this.sizeTextAreaMax+30) * (numDeleted/itemsColoumn))) ;
		          float yPos = (yPosPaused + (Math.abs((this.upperLeftWorld.y*0.9f)/3) * (numDeleted % itemsColoumn)));
		          PVector v = this.objectTimeline.getVertex(pos);
		          v.x = xPos-this.size;
		          v.y = yPos-this.size;
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);
		          pos += 1;
		          v = this.objectTimeline.getVertex(pos);
		          v.x = xPos+this.size;
		          v.y = yPos-this.size;
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);
		          pos += 1;
		          v = this.objectTimeline.getVertex(pos);
		          v.x = xPos+this.size;
		          v.y = yPos+this.size;
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);      
		           pos += 1;
		          v = this.objectTimeline.getVertex(pos);
		          v.x = xPos-this.size;
		          v.y = yPos+this.size;
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);  
		          pos += 1;
		          //colored area
		          v = this.objectTimeline.getVertex(pos);
		          v.x = xPos-(this.size);
		          v.y = yPos-(this.size);
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);  
		          pos += 1;
		          v = this.objectTimeline.getVertex(pos);
		          v.x = xPos+(this.size);
		          v.y = yPos-(this.size);
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);       
		          pos += 1;
		          v = objectTimeline.getVertex(pos);
		          v.x = xPos+(this.size);
		          v.y = yPos-(this.size)-5;
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);    
		          pos += 1;
		          v = this.objectTimeline.getVertex(pos);
		          v.x = xPos-(this.size);
		          v.y = yPos-(this.size)-5;
		          v.z = z;
		          this.objectTimeline.setVertex(pos,v);   
		      
		          numDeleted++;
		    }
		   this.cameraPosY = (int)(-1.f*this.upperLeftWorld.y);
		  }
		 } 
		}

	void showWS(int num){
	    for(Event event : this.eventList){
	          int ws_id = event.getWorkshopID();
	          int eventNum = this.eventList.indexOf(event);
	          int pos = eventNum * 8;
	          float z = 0;
	          
	           //if event should not show up
	          if((ws_id != num) && (num != 0) || ((event.getStatus() == Eventstatus.PENDING) || (event.getStatus() == Eventstatus.NEWEVENT)) || (event.getStatus() == Eventstatus.DELETED)) {
	            z = this.maxzoom + 100;
	          }
	          if((ws_id == num) && (event.getStatus() == Eventstatus.DELETED) && showtimeline == false){
	            z = 0; 
	          }
	          // if everything should show up -> show only placed in timeline
	          if(this.showtimeline == true && num == 0 && ( event.getStatus() == Eventstatus.PENDING || event.getStatus() == Eventstatus.NEWEVENT || event.getStatus() == Eventstatus.DELETED) ){
	            z = this.maxzoom + 100;
	          }
	          else if(this.showtimeline == false && num == 0 && ( event.getStatus() == Eventstatus.PENDING || event.getStatus() == Eventstatus.NEWEVENT) ){
	            z = this.maxzoom + 100;
	          }
	          
	          PVector v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);      
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);  
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);  
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v);       
	          pos += 1;
	          v = objectTimeline.getVertex(pos);
	          v.z = z;
	          objectTimeline.setVertex(pos,v); 
	      }
	    }
	
	void scaleVertical(){
		 for(Event event : this.eventList){
		   if(event.getStatus() != Eventstatus.DELETED){
		        int pos = (int)(eventList.indexOf(event)*8);
		        float center = parent.map(event.getYPos(),-1.1f,1.1f,upperLeftWorld.y,upperLeftWorld.y * -1.f);
		        PVector v = objectTimeline.getVertex(pos);
		        v.y = center-this.size;
		        objectTimeline.setVertex(pos,v);
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.y = center-this.size;
		        objectTimeline.setVertex(pos,v);
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.y = center+this.size;
		        objectTimeline.setVertex(pos,v);      
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.y = center+this.size;
		        objectTimeline.setVertex(pos,v);  
		        pos += 1;
		        if(center <= 0.){
		          v = objectTimeline.getVertex(pos);
		          v.y = center+(this.size);
		          objectTimeline.setVertex(pos,v);  
		          pos += 1;
		          v = objectTimeline.getVertex(pos);
		          v.y = center+(this.size);
		          objectTimeline.setVertex(pos,v);
		        }
		        else{
		          v = objectTimeline.getVertex(pos);
		          v.y = center-(this.size);
		          objectTimeline.setVertex(pos,v);  
		          pos += 1;
		          v = objectTimeline.getVertex(pos);
		          v.y = center-(this.size);
		          objectTimeline.setVertex(pos,v);       
		          
		        }
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.y = 0.f;
		        objectTimeline.setVertex(pos,v);       
		        pos += 1;
		        v = objectTimeline.getVertex(pos);
		        v.y = 0.f;
		        objectTimeline.setVertex(pos,v); 
		   }
		 }
	}
	//camera functions
	void processPosition(){
		  if(this.menu.buttonHit == true){
		    if(this.dPressed ==false &&  this.aPressed == false &&  this.wPressed == false && this.sPressed == false){
		      switch(this.menu.currentButton){
		        case 1: this.aPressed = true;
		                break;
		        case 2: this.dPressed = true;
		                break;
		        case 3: this.wPressed = true;
		                break;
		        case 4: this.sPressed = true;
		                break;
		        // show new events
		        case 5: this.menu.showbasket = false;
		                this.menu.shownewevents = true;
		                this.menu.showmainmenu = false;
		                break;
		        //show basket      
		        case 6: this.menu.shownewevents = false;
		        		this.menu.showbasket = true;
		        		this.menu.showmainmenu = false;
		                break;
		        // back to main menu
		        case 7: this.menu.showmainmenu = true;
		        		this.menu.showbasket = false;
		        		this.menu.shownewevents = false;
		                break;
		      }
		    }
		  }
		  
		  else if(this.menu.buttonHit == false && this.menu.menu == true){
		     this.dPressed = this.aPressed = this.wPressed = this.sPressed = false;
		   }
		  
		  
		  if(this.wPressed == true){
			  this.cameraPosZ -=  this.speed + this.speedscale;
		      this.scaleVertical();
		      this.eventBack.scaleYears();
		      this.speedscale = parent.map(this.newcameraPosZ,this.minzoom,this.maxzoom,0.f,50.f);
		      if(this.cameraPosZ < this.minzoom) this.cameraPosZ = (int)this.minzoom;
		    }
		     if(this.sPressed == true){
		      this.cameraPosZ += speed + speedscale;
		      this.scaleVertical();
		      this.eventBack.scaleYears();
		      this.speedscale = parent.map(this.newcameraPosZ,this.minzoom,this.maxzoom,0.f,50.f);
		      if(this.cameraPosZ > this.maxzoom) this.cameraPosZ = (int)this.maxzoom;
		    }
		     if(this.aPressed == true){
		    	 this.cameraPosX -= this.speed + speedscale + this.menu.speedbuttonpos;
		    }
		     if(this.dPressed == true){
		    	 this.cameraPosX += this.speed + this.speedscale + this.menu.speedbuttonpos;
		    } 
		    if(this.qPressed == true){
		    	this.cameraPosY -= this.speed + this.speedscale;
		    }
		     if(this.rPressed == true){
		    	 this.cameraPosY += this.speed + this.speedscale;
		    } 
		    
		    //if camera is moving without user input e.g. Gui Input
		    if((int)this.newcameraPosZ != (int)this.cameraPosZ && (Math.ceil(this.newcameraPosZ) != (int)this.cameraPosZ)){ 
		      this.scaleVertical();
		      this.eventBack.scaleYears();
		      this.speedscale = this.parent.map(this.newcameraPosZ,this.minzoom,this.maxzoom,0.f,50.f);
		     }  
		    
		    if(this.cameraPosZ <= 250) this.state = 0;
		    else if(this.cameraPosZ > 250 && this.cameraPosZ < 800) this.state = 1;
		    else if(this.cameraPosZ >= 800 && this.cameraPosZ <= this.maxzoom) this.state = 2;
		    else if(this.cameraPosZ > this.maxzoom + 100) this.state = 3;   
		    
		    this.newcameraPosX = parent.lerp(this.newcameraPosX,this.cameraPosX,0.15f); 
		    this.newcameraPosY = parent.lerp(this.newcameraPosY,this.cameraPosY,0.15f); 
		    this.newcameraPosZ = parent.lerp(this.newcameraPosZ,this.cameraPosZ,0.15f); 
		    if(this.newcameraPosZ <= this.minzoom) this.newcameraPosZ = this.minzoom;
		    if(this.newcameraPosZ >= this.maxzoom) this.newcameraPosZ = this.maxzoom;

		}


	void focusEvent(int num){
	  this.eventBack.showTimeline();
	  int pos = num;
	  this.moving = num;
	  PVector v = this.objectTimeline.getVertex(pos*8);
	  PVector v2 = this.objectTimeline.getVertex((pos*8)+2);
	  float x = v.x + (v2.x-v.x);
	  this.cameraPosX  = (int) x;
	  this.cameraPosZ = (int)this.minzoom;
	  this.autoCamera = true;
	}
	
	void focusYear(int year){
	  this.eventBack.showTimeline();
	  float x = (this.eventBack.TLwidth*-1.f) + (this.eventBack.deltaY*(year+0.5f));
	  this.cameraPosX  = (int) x;
	  this.cameraPosZ = 1000;
	  this.autoCamera = true;
	}
	
	
	void startaccess(float x, float y){
			this.access = true;
			this.accesscirc = true;
			this.accessx = x;
			this.accessy = y;
		}

	public void count(){
		  if(this.accesssize<=1.){
			  this.accesssize += 0.01;
			  this.accessinner += 0.04; 
		  }
		  else if(this.accesssize >1.){
			  this.accesscirc = false;
			  this.accesssize = 0.f;
			  this.access = false;
		  }
		}

	void access(){
			this.accessPlane.beginDraw();
			this.accessPlane.clear();
		 if (this.accesscirc == true){
			 this.accessPlane.fill(255,125,125,255-(255*this.accesssize)); 
			 this.accessPlane.noStroke();
			 this.accessPlane.ellipse(this.accessPlane.width/2.f,this.accessPlane.height/2.f,this.accesssize*this.accesradius,this.accesssize*this.accesradius);
			 this.accessPlane.fill(255,255,255,255-(255*this.accesssize)); 
		     //parent.thread("count");
		     Thread t1 = new Thread(new Runnable() {
		         public void run() {
		              count();
		         }
		    });  
		    t1.start();
		  }
		 this.accessPlane.endDraw();
		}	


	PVector getUnProjectedPointOnFloor(float screen_x, float screen_y, PVector floorPosition, PVector floorDirection) {
	 
	  PVector f = floorPosition.get(); // Position of the floor
	  PVector n = floorDirection.get(); // The direction of the floor ( normal vector )
	  
	  PVector w = unProject(screen_x, screen_y, -1.0f); // 3 -dimensional coordinate corresponding to a point on the screen
	  PVector e = getEyePosition(); // Viewpoint position
	  // Computing the intersection of  
	  f.sub(e);
	  w.sub(e);
	  w.mult( n.dot(f)/n.dot(w) );
	  w.add(e);
	 
	  return w;
	}
 
	// Function to get the position of the viewpoint in the current coordinate system
	PVector getEyePosition() {
	  //PMatrix3D mat = (PMatrix3D)getMatrix();
	  PMatrix3D mat = ((PGraphics3D)contentPlane).modelview; //Get the model view matrix
	  mat.invert();
	  return new PVector( mat.m03, mat.m13, mat.m23 );
	}
	
	//Function to perform the conversion to the local coordinate system ( reverse projection ) from the window coordinate system
	PVector unProject(float winX, float winY, float winZ) {
	  
	  PMatrix3D mat = getMatrixLocalToWindow();  
	  mat.invert();
	 
	  float[] in = {winX, winY, winZ, 1.0f};
	  float[] out = new float[4];
	  mat.mult(in, out);  // Do not use PMatrix3D.mult(PVector, PVector)
	
	  if (out[3] == 0 ) {
	    return null;
	  }
	  PVector result = new PVector(out[0]/out[3], out[1]/out[3], out[2]/out[3]); 
	  //println(result);
	  return result;
	}
 
	//Function to compute the transformation matrix to the window coordinate system from the local coordinate system
	PMatrix3D getMatrixLocalToWindow() {
	  PMatrix3D projection = ((PGraphics3D)this.contentPlane).projection; 
	  PMatrix3D modelview = ((PGraphics3D)this.contentPlane).modelview;   

	  // viewport transf matrix
	  PMatrix3D viewport = new PMatrix3D();
	  viewport.m00 = viewport.m03 = windowwidth/2;
	  viewport.m11 = -windowheight/2;
	  viewport.m13 = windowheight/2;

	  // Calculate the transformation matrix to the window coordinate system from the local coordinate system
	  viewport.apply(projection);
	  viewport.apply(modelview);
	  return viewport;
	}
}

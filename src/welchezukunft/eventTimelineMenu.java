package welchezukunft;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

public class eventTimelineMenu {
	
	String [] workshopTitle = {"Pavlina Tcherneva","Harald Schumann","Cho Khong(UK)","Jürg Müller(Switzerland)","Eyvandur Gunnarsson (Iceland)","Evan Liaras (Greece)","José Soeiro(Portugal)","Isabel Feichtner (European law)","Kai von Lewinski (German Law)","Otto Steinmetz (Banks)","Cornelia Dahaim (global workforce)","Joseph Vogl (eternal critic)","Ariella Helfgott","Ulrike Hermann (Moderation)","Volker Heise (Moderation)"};
	boolean menu = false;
	boolean eventmenu = false;
	int menuposx,menuposy = 0;
	float menurotation = 0;
	
	PGraphics menuCanvas,menuText;
	PGraphics subCanvas,subText;
	
	boolean buttonHit = false;
	int currentButton = 0;

	float yearsTextHeight,monthTextHeight,yearsTextWidth; 
	
	PShape [] menubutton; //main manu
	PShape [] menu2button; //event menu
	PShape [] basketbutton = new PShape[0]; // basket menu
	PShape [] newbutton = new PShape[0]; // new menu
	int [] basketids = {};
	int [] newids = {};
	String labels[] = {"<<",">>","in","out","new","cache","back"};
	PVector[] buttonPositions = {new PVector(0.2f,0.5f),new PVector(0.8f,0.5f),new PVector(0.35f,0.8f),new PVector(0.65f,0.8f),new PVector(0.35f,0.2f),new PVector(0.65f,0.2f),new PVector(0.5f,0.5f)};
	String labels2[] = {"to cache","pausing"};
	PVector[] buttonPositions2 = {new PVector(0.3f,0.2f),new PVector(0.7f,0.2f)};
	
	int buttonSizeX = 40;
	int buttonSizeY = 40;
	int basketbuttonSizeY = 20;
	int basketbuttonSizeX = 95;
	int menusize = 300;
	int submenux = 300;
	int submenuy = 200;
	
	
	float speedbuttonpos = 0;

	boolean showbasket = false;
	boolean showmainmenu = true;
	boolean shownewevents = false;
	
	requestSQL database;
	eventTimeline parent;
	

	public eventTimelineMenu(eventTimeline parent) {
		  this.parent = parent;
		  database = timeline.accessSQL;
		//menu
		  menuCanvas = parent.parent.createGraphics(menusize,menusize,PConstants.P3D);
		  menuText = parent.parent.createGraphics(menusize,menusize,PConstants.P3D);
		 //Submenu
		 subCanvas = parent.parent.createGraphics(submenux,submenuy,PConstants.P3D);
		 subText = parent.parent.createGraphics(submenux,submenuy,PConstants.P3D);
		 menuText.textFont(parent.menufont);
		 createButtons();
		 createButtonsEvent();
	}
	
	public void setupMenu() {
		  this.showmainmenu = true;
		  this.showbasket = false;
		  this.shownewevents = false;
		  
		  this.newbutton = null;
		  this.newbutton = new PShape[0];
		  this.basketbutton = null;
		  this.basketbutton = new PShape[0];
		  this.basketids = null;
		  this.basketids = new int[0];
		  this.newids = null;
		  this.newids = new int[0];
		  
		  int newlimit = 0;
		  int basketlimit = 0;
  
		  this.database.eventsTimestampMsql.query("SELECT vertex_id,timestamp FROM event WHERE status='NEWEVENT' ORDER BY timestamp DESC");
		  while(this.database.eventsTimestampMsql.next() && newlimit < 5){
		     createNewButton(this.database.eventsTimestampMsql.getInt(1));
		     newlimit++;
		  }
		  
		  this.database.eventsTimestampMsql.query("SELECT vertex_id FROM event WHERE status='PENDING' ORDER BY timestamp DESC");  
		   while(this.database.eventsTimestampMsql.next() && basketlimit < 5){
		      createBasketButton(this.database.eventsTimestampMsql.getInt(1));
		      basketlimit++;
		   }
		   
		 
		  
		  this.speedbuttonpos = 0;
 
	}
	
	void drawEventMenu(){
	   this.menuCanvas.beginDraw();
	   this.menuCanvas.clear();
	  
	   this.menuCanvas.pushMatrix();
	   this.menuCanvas.translate(this.buttonPositions2[0].x*this.menusize,this.buttonPositions2[0].y*this.menusize,0);
	   this.menuCanvas.shape(this.menu2button[0]);
	   this.menuCanvas.popMatrix();
	
	   this.menuCanvas.pushMatrix();
	   this.menuCanvas.translate(this.buttonPositions2[1].x*this.menusize,this.buttonPositions2[1].y*this.menusize,0);
	   this.menuCanvas.shape(this.menu2button[1]);
	   this.menuCanvas.popMatrix();
	   
	   this.menuCanvas.endDraw();
	}
	
	void drawEventMenuText(){
		this.menuText.beginDraw();
		this.menuText.clear();
		this.menuText.textFont(parent.menufont);
		this.menuText.textAlign(PConstants.CENTER, PConstants.CENTER);
		this.menuText.textSize(18);
		this.menuText.fill(0,0,0,255);
	   // button 1
		this.menuText.pushMatrix();
		this.menuText.translate(this.buttonPositions2[0].x*this.menusize,this.buttonPositions2[0].y*this.menusize,0);
		this.menuText.text(this.labels2[0],0,0);
		this.menuText.popMatrix();
	    // button 2
		this.menuText.pushMatrix();
		this.menuText.translate(this.buttonPositions2[1].x*this.menusize,this.buttonPositions2[1].y*this.menusize,0);
		this.menuText.text(this.labels2[1],0,0);
		this.menuText.popMatrix(); 
		this.menuText.endDraw();
	}

	void drawMenu(){
		this.menuCanvas.beginDraw();
		this.menuCanvas.clear();
	   
	   // button 1: cam left
	   if(this.parent.newcameraPosX >= (this.parent.eventBack.TLwidth * -1.)+100 && this.showmainmenu == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(this.buttonPositions[0].x*this.menusize,this.buttonPositions[0].y*this.menusize,0);
		   this.menuCanvas.shape(this.menubutton[0]);
		   this.menuCanvas.popMatrix();
	   }
	   // button 2:cam right
	   if(this.parent.newcameraPosX <= this.parent.eventBack.TLwidth-100 && this.showmainmenu == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(this.buttonPositions[1].x*this.menusize,this.buttonPositions[1].y*this.menusize,0);
		   this.menuCanvas.shape(this.menubutton[1]);
		   this.menuCanvas.popMatrix();
	   } 
	    // button 3: zoom in
	   if(this.parent.newcameraPosZ >= this.parent.minzoom +10 && this.showmainmenu == true && this.parent.showtimeline == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(buttonPositions[2].x*menusize,buttonPositions[2].y*menusize,0);
		   this.menuCanvas.shape(this.menubutton[2]);
		   this.menuCanvas.popMatrix();
	   }
	   // button 4: zoom out
	   if(this.parent.newcameraPosZ <= this.parent.maxzoom - 10 && this.showmainmenu == true && this.parent.showtimeline == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(buttonPositions[3].x*menusize,buttonPositions[3].y*menusize,0);
		   this.menuCanvas.shape(this.menubutton[3]);
		   this.menuCanvas.popMatrix();
	   } 
	   // button 5: new
	   if(this.newids.length > 0 && this.showbasket == false && this.parent.showtimeline == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(this.buttonPositions[4].x*this.menusize,this.buttonPositions[4].y*this.menusize,0);
		   this.menuCanvas.shape(this.menubutton[4]);
		   this.menuCanvas.popMatrix();
	   }
	 
	   
	   // button 6: basket
	   if(this.basketids.length > 0 && this.shownewevents == false && this.parent.showtimeline == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(this.buttonPositions[5].x*this.menusize,this.buttonPositions[5].y*this.menusize,0);
		   this.menuCanvas.shape(this.menubutton[5]);
		   this.menuCanvas.popMatrix();
	   }
	
	   // button 7: back
	   if(this.showbasket == true || this.shownewevents == true){
		   this.menuCanvas.pushMatrix();
		   this.menuCanvas.translate(this.buttonPositions[6].x*this.menusize,this.buttonPositions[6].y*this.menusize,0);
		   this.menuCanvas.shape(this.menubutton[6]);
		   this.menuCanvas.popMatrix();
	   }
	   this.menuCanvas.endDraw();
	    
	    // submenues
	    
	    //show baskets
	    if(this.showbasket == true){
	    	   this.subCanvas.beginDraw();
	           this.subCanvas.clear();
	           for(int i = 0; i < this.basketbutton.length; i++){
	        	  this.subCanvas.pushMatrix();
	              this.subCanvas.translate(22+(0.2f*i)*this.submenux,0.5f*this.submenuy,0f);
	              this.subCanvas.rotate((float)Math.toRadians(-90));
	              this.subCanvas.shape(this.basketbutton[i]);
	              this.subCanvas.popMatrix();
	           }
	           this.subCanvas.endDraw();
	     }
	     
	     //show new events
	      if(this.shownewevents == true){
	    	  this.subCanvas.beginDraw();
	       this. subCanvas.clear();
	       for(int i = 0; i < newbutton.length; i++){
	    	   this.subCanvas.pushMatrix();
	          this.subCanvas.translate(22+(0.2f*i)*this.submenux,0.5f*submenuy,0f);
	          this.subCanvas.rotate((float)Math.toRadians(-90));
	          this.subCanvas.shape(newbutton[i]);
	          this.subCanvas.popMatrix();
	       }
	       this.subCanvas.endDraw();
	     }  
	     
	     
	  
	}
	
	void clearMenu(){
	   this.menuCanvas.beginDraw();
	   this.menuCanvas.clear(); 
	   this.menuCanvas.endDraw();
	   this.menuText.beginDraw();
	   this.menuText.clear();
	   this.menuText.endDraw();
	   this.subCanvas.beginDraw();
	   this.subCanvas.clear();
	   this.subCanvas.endDraw();
	   this.subText.beginDraw();
	   this.subText.clear();
	   this.subText.endDraw();  
	}
	
	void drawMenuText(){
		this.menuText.beginDraw();
		this.menuText.clear();
		this.menuText.textFont(parent.menufont);
		this.menuText.textAlign(PConstants.CENTER, PConstants.CENTER);
		this.menuText.textSize(18);
		this.menuText.fill(0,0,0,255);
	  
	   // button 1
	   if(this.parent.newcameraPosX >= (this.parent.eventBack.TLwidth * -1.)+100  && this.showmainmenu == true){
		   this.menuText.pushMatrix();
		   this.menuText.translate(buttonPositions[0].x*menusize,buttonPositions[0].y*menusize,0);
		   this.menuText.text(labels[0],0,0);
		   this.menuText.popMatrix();
	   }
	   // button 2
	   if(this.parent.newcameraPosX <= this.parent.eventBack.TLwidth-100  && this.showmainmenu == true){
		   this.menuText.pushMatrix();
		   this.menuText.translate(buttonPositions[1].x*menusize,buttonPositions[1].y*menusize,0);
		   this.menuText.text(labels[1],0,0);
		   this.menuText.popMatrix();
	   } 
	    // button 3
	   if(this.parent.newcameraPosZ >= this.parent.minzoom +10  && this.showmainmenu == true && this.parent.showtimeline == true){
		   this.menuText.pushMatrix();
		   this.menuText.translate(buttonPositions[2].x*menusize,buttonPositions[2].y*menusize,0);
		   this.menuText.text(labels[2],0,0);
		   this.menuText.popMatrix();
	   }
	   // button 4
	   if(this.parent.newcameraPosZ <= this.parent.maxzoom - 10  && this.showmainmenu == true && this.parent.showtimeline == true){
		   this.menuText.pushMatrix();
		   this.menuText.translate(buttonPositions[3].x*menusize,buttonPositions[3].y*menusize,0);
		   this.menuText.text(labels[3],0,0);
		   this.menuText.popMatrix();
	   } 
	   // button 5: new
	   if(this.newids.length > 0 && this.showbasket == false && this.parent.showtimeline == true){
		   this.menuText.pushMatrix();
		   this.menuText.translate(buttonPositions[4].x*menusize,buttonPositions[4].y*menusize,0);
		   this.menuText.text(labels[4],0,0);
		   this.menuText.popMatrix();
	   }  
	    
	   
	   // button 6: basket
	   if(this.basketids.length > 0 && this.shownewevents == false && this.parent.showtimeline == true){
		   this.menuText.pushMatrix();
		   this.menuText.translate(buttonPositions[5].x*menusize,buttonPositions[5].y*menusize,0);
		   this.menuText.text(labels[5],0,0);
		   this.menuText.popMatrix();
	   }
	    
	
	   // button 7: back
	   if(this.showbasket == true || this.shownewevents == true){
		   this.menuText.pushMatrix();
		   this.menuText.textAlign(PConstants.CENTER, PConstants.CENTER);
		   this.menuText.translate(this.buttonPositions[6].x*this.menusize,this.buttonPositions[6].y*this.menusize,0);
		   this.menuText.text(this.labels[6],0,0);
		   this.menuText.popMatrix();
	   } 
	   
	if(this.showbasket == false && this.shownewevents == false && this.parent.showtimeline == true){
		this.menuText.pushMatrix();
	  float scaleTimeY = 0.5f;
	  if(this.basketids.length > 0 || this.newids.length > 0){
	   scaleTimeY = 0.0f; 
	  }
	  this.menuText.translate(this.menusize/2.f,scaleTimeY*this.menusize/2.f,0); 
	  float pos = this.parent.parent.map(this.parent.newcameraPosX,this.parent.eventBack.TLwidth*-1,this.parent.eventBack.TLwidth,2007,2015);
	  int year = (int)Math.floor(pos);
	  int month = (int)((pos - (float)year)*12.f)+1;
	  this.menuText.textAlign(PConstants.CENTER,PConstants.TOP);
	  this.menuText.text(month + "/" + year,0,0);
	  this.menuText.popMatrix();
	}
	
	this.menuText.endDraw();
	  
	  //show new event buttons
	     if(this.shownewevents == true){
	    	 this.subText.beginDraw();
	    	 this.subText.clear();
	    	 this.subText.textFont(parent.menufont);
	    	 this.subText.textAlign(PConstants.CENTER, PConstants.CENTER);
	    	 this.subText.textSize(18);
	    	 this.subText.fill(0);
	         for(int i = 0; i < newids.length; i++){
	        	 this.subText.pushMatrix();
	        	 this.subText.textAlign(PConstants.LEFT, PConstants.CENTER);
	        	 this.subText.translate(22+(0.2f*i)*submenux,0.5f*submenuy,0);
	        	 this.subText.rotate((float)Math.toRadians(-90));
	          Event current = parent.eventList.get(newids[i]-1);
	          this.subText.stroke(0);
	          /*
	          String dateString = "";
		        if(current.getDay() != 0) {
		        	dateString += Integer.toString(current.getDay()) + ".";
		        }
		        if(!current.getMonth().equals("0")) {
		        	dateString += current.getMonth().substring(0, 3) + ".";
		        }
		        if(current.getYear() != 0) {
		        	int yearnum = (current.getYear()-2000);
		        	dateString += String.format("%02d", yearnum) + " / ";
		        }
		        */
	          this.subText.text(current.getHeadline(),(basketbuttonSizeX*-1f)+2f,(basketbuttonSizeY*-1.f)+1f,basketbuttonSizeX*2f,basketbuttonSizeY+5f);
	          this.subText.popMatrix();
	         }  
	         this.subText.endDraw();
	   } 
	  
	   // show basket event buttons
	    if(this.showbasket == true){
	    	this.subText.beginDraw();
	    	this.subText.clear();
	    	this.subText.textFont(this.parent.menufont);
	    	this.subText.textAlign(PConstants.CENTER, PConstants.CENTER);
	    	this.subText.textSize(18);
	    	this.subText.fill(0);
	       for(int i = 0; i < this.basketids.length; i++){
	    	   this.subText.pushMatrix();
	    	   this.subText.textAlign(PConstants.LEFT, PConstants.CENTER);
	    	   this.subText.translate(22+(0.2f*i)*submenux,0.5f*submenuy,0);
	    	   this.subText.rotate((float)Math.toRadians(-90));
	        Event current = this.parent.eventList.get(basketids[i]-1);
	        this.subText.stroke(0);
	        /*String dateString = "";
	        if(current.getDay() != 0) {
	        	dateString += Integer.toString(current.getDay()) + ".";
	        }
	        if(!current.getMonth().equals("0")) {
	        	dateString += current.getMonth().substring(0, 3) + ".";
	        }
	        if(current.getYear() != 0) {
	        	int yearnum = (current.getYear()-2000);
	        	dateString += String.format("%02d", yearnum) + " / ";
	        }
	        */
	        this.subText.text(current.getHeadline(),(this.basketbuttonSizeX*-1)+2,(this.basketbuttonSizeY*-1.f)+1,this.basketbuttonSizeX*2,this.basketbuttonSizeY+5);
	        this.subText.popMatrix();
	       }  
	       this.subText.endDraw();
	 }
	  
	  
	  
	}
	
	//buttons 0 = left, 1 = right, 2 = zoomin, 3 = zoomout, 4 = events
	//5 = movetobasket, 6 = movenormal
	
	void createButtons(){
	  this.menubutton = new PShape[this.labels.length];
	  for(int i = 0; i < this.labels.length; i ++){
		  this.menubutton[i] = this.parent.parent.createShape();
		  this.menubutton[i].beginShape(PConstants.QUADS);
		  this.menubutton[i].fill(200+i,200,200);
		  this.menubutton[i].vertex(-1.f*this.buttonSizeX,-1.f*this.buttonSizeY,0);
		  this.menubutton[i].vertex(this.buttonSizeX,-1.f*this.buttonSizeY,0);
		  this.menubutton[i].vertex(this.buttonSizeX,this.buttonSizeY,0);
		  this.menubutton[i].vertex(-1.f*this.buttonSizeX,this.buttonSizeY,0); 
		  this.menubutton[i].endShape();  
	  }
	}
	
	void createButtonsEvent(){
		this.menu2button = new PShape[this.labels2.length];
	  for(int i = 0; i < this.labels2.length; i ++){
		  this.menu2button[i] = this.parent.parent.createShape();
		  this.menu2button[i].beginShape(PConstants.QUADS);
		  this.menu2button[i].fill(200+i,203,200);
		  this.menu2button[i].vertex(-1.f*this.buttonSizeX,-1.f*this.buttonSizeY,0);
		  this.menu2button[i].vertex(this.buttonSizeX,-1.f*this.buttonSizeY,0);
		  this.menu2button[i].vertex(this.buttonSizeX,this.buttonSizeY,0);
		  this.menu2button[i].vertex(-1.f*this.buttonSizeX,this.buttonSizeY,0); 
		  this.menu2button[i].endShape();  
	  }   
	}
	  
	  
	void createNewButton(int id){
	   int i = this.newbutton.length;
	   this.newbutton = (PShape[]) this.parent.parent.expand(this.newbutton,this.newbutton.length + 1);
	   this.newbutton[i] = this.parent.parent.createShape();
	   this.newbutton[i].beginShape(PConstants.QUADS);
	   this.newbutton[i].fill(200+i,201,200);
	   this.newbutton[i].stroke(0);
	   this.newbutton[i].vertex(-1.f*this.basketbuttonSizeX,-1.f*this.basketbuttonSizeY,0);
	   this.newbutton[i].vertex(this.basketbuttonSizeX,-1.f*this.basketbuttonSizeY,0);
	   this.newbutton[i].vertex(this.basketbuttonSizeX,this.basketbuttonSizeY,0);
	   this.newbutton[i].vertex(-1.f*this.basketbuttonSizeX,this.basketbuttonSizeY,0); 
	   this.newbutton[i].endShape();
	   this.newids = this.parent.parent.expand(this.newids,this.newids.length + 1);
	   this.newids[i] = id;
	}
	
	void createBasketButton(int id){
	   int i = basketbutton.length;
	   this.basketbutton = (PShape[]) parent.parent.expand(this.basketbutton,this.basketbutton.length + 1);
	   this.basketbutton[i] = parent.parent.createShape();
	   this.basketbutton[i].beginShape(PConstants.QUADS);
	   this.basketbutton[i].fill(200+i,202,200);
	   this.basketbutton[i].stroke(0);
	   this.basketbutton[i].vertex(-1.f*this.basketbuttonSizeX,-1.f*this.basketbuttonSizeY,0);
	   this.basketbutton[i].vertex(this.basketbuttonSizeX,-1.f*this.basketbuttonSizeY,0);
	   this.basketbutton[i].vertex(this.basketbuttonSizeX,this.basketbuttonSizeY,0);
	   this.basketbutton[i].vertex(-1.f*this.basketbuttonSizeX,this.basketbuttonSizeY,0); 
	   this.basketbutton[i].endShape();
	   this.basketids = this.parent.parent.expand(this.basketids,this.basketids.length + 1);
	   this.basketids[i] = id;
	}
	
	void drawstatus(){
	  int num = this.parent.currentWSselection - 1;
	  this.parent.statusPlane.beginDraw();
	  this.parent.statusPlane.clear();
	  this.parent.statusPlane.textFont(this.parent.menufont);
	  this.parent.statusPlane.fill(this.parent.cols_rgb[num]);
	  this.parent.statusPlane.noStroke();
	  this.parent.statusPlane.textAlign(PConstants.CENTER,PConstants.CENTER);
	  this.parent.statusPlane.textSize(30);
	  String StatusText = this.workshopTitle[num];
	  Float StatusTextWidth = this.parent.statusPlane.textWidth(StatusText) + 10;
	  this.parent.statusPlane.rect((this.parent.statusPlane.width/2.f)-StatusTextWidth/2.f,0,StatusTextWidth,this.parent.statusPlane.height);
	  this.parent.statusPlane.fill(0);
	  this.parent.statusPlane.text(StatusText,this.parent.statusPlane.width/2.f,parent.statusPlane.height/2.f);
	  this.parent.statusPlane.endDraw();
	}
}
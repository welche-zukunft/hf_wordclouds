package welchezukunft;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;

public class eventBackground {
	
	static int TLwidth = 8000;
	static int years = 6;
	float [] scalesizeYear = {40,8};
	float [] scalesizeMonth = {((((float)this.TLwidth*2.f)/(float)this.years)/(float)12)-20,5};
	float deltaY;
	float deltyYmonth;
	PShape timeline;
	PGraphics timelinePlane;
	eventTimeline parent;
	
	int selectedWS;
	boolean showallWS = true;
	float eventAngle = 0;
	
	
	
	public eventBackground(eventTimeline parent) {
		 this.parent = parent;
	
		 timelinePlane = parent.parent.createGraphics(parent.imgwidth, parent.imgheight, PConstants.P3D);
		 
		 this.deltaY = (float)(this.TLwidth*2)/(float)this.years;
		 this.deltyYmonth = (float)this.deltaY / (float)12;
		 
		 
		 this.timeline = parent.parent.createShape();
		 this.timeline.beginShape(PConstants.QUADS);
		 this.timeline.fill(0);
		 this.timeline.noStroke();
		 for(int i = 0; i < years; i++){
			 this.timeline.vertex(((this.TLwidth*-1.f)+(this.deltaY * i)) - this.scalesizeYear[0] , this.scalesizeYear[1] * -1.f,0f);
			 this.timeline.vertex(((this.TLwidth*-1.f)+(this.deltaY * i)) + this.scalesizeYear[0] , this.scalesizeYear[1] * -1.f,0f); 
			 this.timeline.vertex(((this.TLwidth*-1.f)+(this.deltaY * i)) + this.scalesizeYear[0] , this.scalesizeYear[1]  ,0f); 
			 this.timeline.vertex(((this.TLwidth*-1.f)+(this.deltaY * i)) - this.scalesizeYear[0] , this.scalesizeYear[1]  ,0f); 
			 for(int j = 0 ; j < 12; j++){
				  this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * i)+(this.deltyYmonth * j)) - this.scalesizeMonth[0] , this.scalesizeMonth[1] * -1.f,0f);
				  this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * i)+(this.deltyYmonth * j)) + this.scalesizeMonth[0] , this.scalesizeMonth[1] * -1.f,0f); 
				  this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * i)+(this.deltyYmonth * j)) + this.scalesizeMonth[0] , this.scalesizeMonth[1]  ,0f); 
				  this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * i)+(this.deltyYmonth * j)) - this.scalesizeMonth[0] , this.scalesizeMonth[1]  ,0f);
			 }
		 }
		 //Last Year Badge
		 this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * 12)) - this.scalesizeYear[0] , this.scalesizeYear[1] * -1.f,0f);
		 this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * 12)) + this.scalesizeYear[0] , this.scalesizeYear[1] * -1.f,0f); 
		 this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * 12)) + this.scalesizeYear[0] , this.scalesizeYear[1]  ,0f); 
		 this.timeline.vertex( ((this.TLwidth*-1.f)+(this.deltaY * 12)) - this.scalesizeYear[0] , this.scalesizeYear[1]  ,0f); 
		 this.timeline.endShape();
	}

	void drawTimeline(){
		this.timelinePlane.beginDraw();
		this.timelinePlane.clear();
		this.timelinePlane.background(0);
		this.timelinePlane.textFont(this.parent.menufont);
		this.timelinePlane.textSize(12.2f + ((this.parent.cameraPosZ-200)/2800)*80);
		this.timelinePlane.camera(this.parent.newcameraPosX, this.parent.newcameraPosY, this.parent.newcameraPosZ,this.parent.newcameraPosX, this.parent.newcameraPosY, 0.f,0.f,1.f,0.f); 
		this.timelinePlane.pushMatrix();
		this.timelinePlane.shape(this.timeline);
		this.timelinePlane.popMatrix();
		this.timelinePlane.textAlign(PConstants.CENTER,PConstants.CENTER);
		this.timelinePlane.endDraw();
	}

	void scaleYears(){
		  for(int i = 0; i <= this.years; i++){
			  float tw = parent.yearsTextWidth;
			  float th = parent.yearsTextHeight;
			  int pos = (i*4)+(i*12*4);
			  PVector v = this.timeline.getVertex(pos);
			  v.x = ((this.TLwidth*-1.f)+(this.deltaY * i)) - tw/2.f;
			  v.y = (th/2.f) * -1;
			  this.timeline.setVertex(pos,v);
			  pos++;
			  v = this.timeline.getVertex(pos);
			  v.x = ((this.TLwidth*-1.f)+(this.deltaY * i)) + tw/2.f;
			  v.y = (th/2.f) * -1;
			  this.timeline.setVertex(pos,v);
			  pos++;
			  v = this.timeline.getVertex(pos);
			  v.x = ((this.TLwidth*-1.f)+(this.deltaY * i)) + tw/2.f;
			  v.y = th/2.f;
			  this.timeline.setVertex(pos,v);
			  pos++;
			  v = this.timeline.getVertex(pos);
			  v.x = ((this.TLwidth*-1.f)+(this.deltaY * i)) - tw/2.f;
			  v.y = th/2.f;
			  this.timeline.setVertex(pos,v);
		  }
	}


	boolean in_frustum_tl(PVector pos) {
	        PMatrix3D MVP = ((PGraphics3D)timelinePlane).projmodelview;
	
	        float[] where = {pos.x,pos.y,pos.z,1.f};
	        float[] Pclip = new float[4];
	        MVP.mult(where, Pclip);
	        return Math.abs(Pclip[0]) < Pclip[3] && 
	               Math.abs(Pclip[1]) < Pclip[3] && 
	               0 < Pclip[2] && 
	               Pclip[2] < Pclip[3];
	    }


	void showWS(int num){
	    for(Event event : parent.eventList){
	          int ws_id = event.getWorkshopID();
	          int eventNum = parent.eventList.indexOf(event);
	          int pos = eventNum * 8;
	          float z = 0;
	          
	           //if event should not show up
	          if((ws_id != num) && (num != 0) || ((event.getStatus() == Eventstatus.PENDING) || (event.getStatus() == Eventstatus.NEWEVENT)) || (event.getStatus() == Eventstatus.DELETED)) {
	            z = parent.maxzoom + 100;
	          }
	          if((ws_id == num) && (event.getStatus() == Eventstatus.DELETED) && parent.showtimeline == false){
	            z = 0; 
	          }
	          // if everything should show up -> show only placed in timeline
	          if(parent.showtimeline == true && num == 0 && ( event.getStatus() == Eventstatus.PENDING || event.getStatus() == Eventstatus.NEWEVENT || event.getStatus() == Eventstatus.DELETED) ){
	            z = parent.maxzoom + 100;
	          }
	          else if(parent.showtimeline == false && num == 0 && ( event.getStatus() == Eventstatus.PENDING || event.getStatus() == Eventstatus.NEWEVENT) ){
	            z = parent.maxzoom + 100;
	          }
	          
	          PVector v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v =  parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);      
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);  
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);  
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);       
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v); 
	      }
    }
  

	PVector rotateX(PVector vector,float angle) { // angle in radians
		  //normalize(vector); // No  need to normalize, vector is already ok...
		  float x1 = vector.x;
		  float z1 = (float)(vector.z * Math.cos(angle) - vector.y * Math.sin(angle));
		  float y1 = (float)(vector.z * Math.sin(angle) + vector.y * Math.cos(angle)) ;
		  return new PVector(x1, y1,z1);
	}

	void showPaused(){
	  if(parent.cameraPosZ <= 1000){
	     parent.showtimeline = false;
	     int numDeleted = 0;
	     int z = 0;
	     int itemsColoumn = 3;
	     float yPosPaused = (parent.upperLeftWorld.y*-1.f) + (parent.size*3);
	     float xPosPaused = 200+parent.size+((parent.upperLeftWorld.x));
	     // deltax = parent.sizeTextAreaMax; deltay = HLcurrentTextparent.size
	     for(Event event : parent.eventList){
	      if(event.getStatus() == Eventstatus.DELETED){
	          int pos = (event.getId()-1)*8;
	          float xPos = (xPosPaused + ((parent.sizeTextAreaMax+30) * (numDeleted/itemsColoumn))) ;
	          float yPos = (yPosPaused + (Math.abs((parent.upperLeftWorld.y*0.9f)/3) * (numDeleted % itemsColoumn)));
	          PVector v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos-parent.size;
	          v.y = yPos-parent.size;
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos+parent.size;
	          v.y = yPos-parent.size;
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos+parent.size;
	          v.y = yPos+parent.size;
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);      
	           pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos-parent.size;
	          v.y = yPos+parent.size;
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);  
	          pos += 1;
	          //colored area
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos-(parent.size);
	          v.y = yPos-(parent.size);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);  
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos+(parent.size);
	          v.y = yPos-(parent.size);
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);       
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos+(parent.size);
	          v.y = yPos-(parent.size)-5;
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);    
	          pos += 1;
	          v = parent.objectTimeline.getVertex(pos);
	          v.x = xPos-(parent.size);
	          v.y = yPos-(parent.size)-5;
	          v.z = z;
	          parent.objectTimeline.setVertex(pos,v);   
	      
	          numDeleted++;
	    }
	   parent.cameraPosY = (int)(-1.*parent.upperLeftWorld.y);
	  }
	 } 
	}

	void showTimeline(){
	 float z = parent.maxzoom+100;
	 for(Event event : parent.eventList){
	  if(event.getStatus() == Eventstatus.DELETED){
	        int pos = (event.getId()-1)*8;
	        PVector v = parent.objectTimeline.getVertex(pos);
	        v.x = 0-parent.size;
	        v.y = 0-parent.size;
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);
	        pos += 1;
	        v = parent.objectTimeline.getVertex(pos);
	        v.x = 0+parent.size;
	        v.y = 0-parent.size;
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);
	        pos += 1;
	        v = parent.objectTimeline.getVertex(pos);
	        v.x = 0+parent.size;
	        v.y = 0+parent.size;
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);      
	         pos += 1;
	        v = parent.objectTimeline.getVertex(pos);
	        v.x = 0-parent.size;
	        v.y = 0+parent.size;
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);  
	        pos += 1;
	         v = parent.objectTimeline.getVertex(pos);
	        v.x = 0-(parent.size/4.f);
	        v.y = 0-(parent.size);
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);  
	        pos += 1;
	        v = parent.objectTimeline.getVertex(pos);
	        v.x = 0+(parent.size/4.f);
	        v.y = 0-(parent.size);
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);       
	        pos += 1;
	        v = parent.objectTimeline.getVertex(pos);
	        v.x = 0.f;
	        v.y = 0.f;
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v);       
	        pos += 1;
	        v = parent.objectTimeline.getVertex(pos);
	        v.x = 0.f;
	        v.y = 0.f;
	        v.z = z;
	        parent.objectTimeline.setVertex(pos,v); 
	      }
	    }
	
	 parent.showtimeline = true;
	 parent.cameraPosY = 0; 
	}
}
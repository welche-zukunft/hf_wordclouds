package welchezukunft;

import org.gicentre.utils.move.Ease;

import processing.core.PConstants;
import processing.core.PGraphics;

public class logo {

	float logoPos = 0;
	float logoPosFinal = 0;
	float direction = 1;
	boolean animatelogo = true;
	float fontspeed = 0.0125f;
	boolean showlogo = false;
	
	PGraphics logoPlane;
	timeline parent;
	
	public logo(timeline parent) {
		this.parent = parent;
		this.logoPlane = parent.createGraphics(parent.sizeTableX,parent.sizeTableY,PConstants.P3D);
	}
	
	
	void drawlogo(){
		 this.logoPlane.beginDraw();
		 this.logoPlane.clear();
		 //this.logoPlane.background(0);
		 this.logoPlane.fill(0);
		 this.logoPlane.textFont(timeline.eventLine.effectfont);
		 this.logoPlane.textSize(288);
		 this.logoPlane.rotateZ((float)Math.toRadians(-11));
		 this.logoPlane.translate(-175,350,0);
		 this.logoPlane.pushMatrix();
		 this.logoPlane.noStroke();
		 this.logoPlane.fill(255,255,255,230);
		 String word1 = "WELCHE";
		 float textwidth = this.logoPlane.textWidth(word1);
		 this.logoPlane.rect((this.logoPlane.width * parent.map(this.logoPosFinal,0f,1f,1.5f,0.45f))-textwidth/2.f,this.logoPlane.height/2.f,this.logoPlane.width,-1.f*this.logoPlane.height);
		 this.logoPlane.fill(0);
		 this.logoPlane.translate(this.logoPlane.width * parent.map(this.logoPosFinal,0f,1f,1.5f,0.45f),(this.logoPlane.height/2.f)+60f,0f);
		 this.logoPlane.textAlign(PConstants.CENTER,PConstants.BOTTOM);
		 this.logoPlane.text(word1,0,0);
		 this.logoPlane.popMatrix();
		 
		 this.logoPlane.pushMatrix();
		 this.logoPlane.fill(255,255,255,230);
		 String word2 = "ZUKUNFT ?!";
		 float textwidth2 = this.logoPlane.textWidth(word2);
		 this.logoPlane.rect((this.logoPlane.width * parent.map(logoPosFinal,0f,1f,-0.5f,0.55f)) +textwidth2/2.f,this.logoPlane.height/2.f,this.logoPlane.width * -1.f,this.logoPlane.height);
		 this.logoPlane.fill(0);
		 this.logoPlane.translate(this.logoPlane.width * parent.map(logoPosFinal,0f,1f,-0.5f,0.55f),(this.logoPlane.height/2.f)-60,0f);
		 this.logoPlane.textAlign(PConstants.CENTER,PConstants.TOP);
		 this.logoPlane.text(word2,0,0);
		 this.logoPlane.popMatrix(); 

		 this.logoPlane.endDraw();
		 this.movelogos();
		}
		

		void movelogos(){
		  if(this.animatelogo == true){
			 this.logoPos = this.logoPos + (this.fontspeed * this.direction);
			 this.logoPosFinal = Ease.sinOut(this.logoPos);
		  }
		  if(this.logoPos >= 0.99) {
			 this.direction = -1;
			 this.animatelogo = false;  
		  }
		  else if (this.logoPos <= 0.01) {
			this.direction = 1;
			this.animatelogo = false;  
		    this.showlogo = false;
		  }
		}
}
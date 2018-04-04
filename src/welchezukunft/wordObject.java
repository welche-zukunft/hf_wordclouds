package welchezukunft;

import processing.core.*;

public class wordObject {

	String word;
	PVector pos;
	int id;
	PApplet parent;
	float init = 1;
	
	wordObject(String word,PVector position,int id, PApplet p){
		this.word = word;
		this.pos = position;
		this.id = id;
		this.parent = p;
		createBadge();
	}
	
	private void createBadge(){
		float halfwidth = 50;
		float halfheigth = 35;
		timeline.wordCloud.beginShape(PConstants.QUADS);
		timeline.wordCloud.noStroke();
		timeline.wordCloud.fill(timeline.colors.get(this.id));
		timeline.wordCloud.vertex(this.pos.x - halfwidth,this.pos.y - halfheigth,(float)0.);
		timeline.wordCloud.vertex(this.pos.x + halfwidth,this.pos.y - halfheigth,(float)0.);
		timeline.wordCloud.vertex(this.pos.x + halfwidth,this.pos.y + halfheigth,(float)0.);
		timeline.wordCloud.vertex(this.pos.x - halfwidth,this.pos.y + halfheigth,(float)0.);
		timeline.wordCloud.endShape();	
	}
	

}

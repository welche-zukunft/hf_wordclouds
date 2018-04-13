package welchezukunft;

import processing.core.*;

public class wordObject {

	String word;
	PVector pos;
	int id;
	wordcloud parent;
	float init;
	int time;
	
	wordObject(String word,PVector position,int id, float fxVal, int time, wordcloud p){
		this.word = word;
		this.pos = position;
		this.id = id;
		this.parent = p;
		this.init = fxVal;
		this.time = time;
		createBadge();
	}
	
	private void createBadge(){
		float halfwidth = 50;
		float halfheigth = 35;
		PShape wcloud = parent.wordCloud;
		wcloud.beginShape(PConstants.QUADS);
		wcloud.noStroke();
		wcloud.fill(parent.colors.get(this.id));
		wcloud.vertex(this.pos.x - halfwidth,this.pos.y - halfheigth,(float)0.);
		wcloud.vertex(this.pos.x + halfwidth,this.pos.y - halfheigth,(float)0.);
		wcloud.vertex(this.pos.x + halfwidth,this.pos.y + halfheigth,(float)0.);
		wcloud.vertex(this.pos.x - halfwidth,this.pos.y + halfheigth,(float)0.);
		wcloud.endShape();	
	}
	

}

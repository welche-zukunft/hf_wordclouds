package welchezukunft;

import processing.core.*;

public class wordObject {

	String word;
	PVector pos;
	int id;
	wordcloud parent;
	float init;
	int time;
	boolean overtime;
	
	wordObject(String word,PVector position,int id, float fxVal, int time, boolean overtime, wordcloud p){
		this.word = word;
		this.pos = position;
		this.id = id;
		this.parent = p;
		this.init = fxVal;
		this.time = time;
		this.overtime = overtime;
		createBadge();
	}
	
	private void createBadge(){
		float halfwidth = timeline.badgeSizeX / 2f;
		float halfheigth = timeline.badgeSizeY / 2f;
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

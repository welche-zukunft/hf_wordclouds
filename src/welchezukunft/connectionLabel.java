package welchezukunft;

import processing.core.PConstants;
import processing.core.PGraphics;

public class connectionLabel {
	
	timeline parent;
	PGraphics connector;
	
	public connectionLabel(timeline timeline) {
		this.parent = timeline;
		this.connector = parent.createGraphics(350,60,PConstants.P3D);
	}
	
	public void draw() {
		this.connector.beginDraw();
		this.connector.clear();
		this.connector.textFont(parent.menufont);
		this.connector.textSize(30);
		this.connector.fill(0);
		this.connector.text("Ideen: 192.168.179.24",10,35);
		this.connector.endDraw();
		timeline.connectionlabel = true;
	}

}
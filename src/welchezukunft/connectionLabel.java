package welchezukunft;

import processing.core.PConstants;
import processing.core.PGraphics;

public class connectionLabel {
	
	eventTimeline parent;
	PGraphics connector;
	
	public connectionLabel(eventTimeline parent) {
		this.parent = parent;
		this.connector = parent.parent.createGraphics(500,100,PConstants.P3D);
		this.connector.beginDraw();
		this.connector.textFont(parent.menufont);
		this.connector.textSize(30);
		this.connector.fill(0,255);
		this.connector.text("Ideen: 192.168.205.66",60,30);
		this.connector.endDraw();
	
	}

}
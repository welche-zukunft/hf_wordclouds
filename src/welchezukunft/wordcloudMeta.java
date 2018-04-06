package welchezukunft;

import processing.core.PApplet;
import processing.data.JSONObject;


public class wordcloudMeta {
	
	int wordcount;
	float minY;
	float maxY;
	int starttime;
	int endtime;
	int r1, r2, g1, g2, b1,b2;
	PApplet parent;
	
	public wordcloudMeta(PApplet parent) {
		this.parent = parent;
		setColorGrad();

	}
	
	private void setColorGrad() {
		JSONObject json = parent.loadJSONObject("./resources/data/colors.json");
		this.r1 = json.getInt("redS");
		this.r2 = json.getInt("greenS");
		this.g1 = json.getInt("blueS");
		this.g2 = json.getInt("redE");
		this.b1 = json.getInt("greenE");
		this.b2 = json.getInt("blueE");
	}
	
	public int getColor(float time) {
		int col;
		int R = (int)(r1 * time + r2 * (1 - time));
		int G = (int)(g1 * time + g2 * (1 - time));
		int B = (int)(b1 * time + b2 * (1 - time));
		col = parent.color(R,G,B);
		return col;
	}

}

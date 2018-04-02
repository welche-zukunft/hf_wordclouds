package welchezukunft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gicentre.utils.move.Ease;

import processing.core.*;

public class knotObject {
	PVector position;
	int id;
	PApplet parent;
	List<Integer> childs;
	boolean init = false;
	
	knotObject(float x, float y, int id, PApplet parent){
		this.position = new PVector(x,y,0);
		this.id = id;
		this.parent = parent;
		childs = new ArrayList<Integer>();
		connect();
	}
	
	public void changeposition(float x) {
		this.position.x=x;
		int sign = (0>this.position.y)?-1:1;
		this.position.y+=this.position.x * 0.01 * sign;
		
		Thread thread = new Thread(){
		    public void run(){
		    	connect();
		    }
		  };
		  thread.setName("connector");
		  thread.start();
		
	}
	
	private void connect() {
		int wordID = this.id;
		int coly = timeline.colors[wordID];
		//get count of appearances 
		int count = (int) timeline.words.stream().filter(wordObject ->  wordObject.id == this.id).count();
		
		// get objects with same id
		Stream<wordObject> testStream = timeline.words.stream().filter(wordObject -> wordObject.id == this.id);
		List<wordObject> testList = testStream.collect(Collectors.toList());
		
		int maxCount = Collections.max(timeline.wordCount);
		//calculate alpha
		float div= (float)this.childs.size()/ (float)maxCount;
		float alp = 60f + parent.map(parent.lerp(0,(float)maxCount,Ease.quarticIn(div,0.008f)), 0, (float)maxCount, 60, 255);

		for(wordObject w : testList) {
				int idFilter = testList.indexOf(w);
				if(idFilter == testList.size()-1) {
					float deltax = this.position.x - w.pos.x;
					float deltay = this.position.y - w.pos.y;
					PShape connection = this.parent.createShape();
					connection.beginShape();
					connection.bezierDetail(30);
					connection.strokeWeight(2);
					connection.stroke(coly,alp);
					connection.noFill();
					if(testList.size() < 2) {
						connection.vertex(w.pos.x,w.pos.y,0);
						connection.bezierVertex(w.pos.x, w.pos.y, w.pos.x, w.pos.y, w.pos.x,w.pos.y);
					}
					else {
						connection.vertex(this.position.x,this.position.y,0);
						connection.bezierVertex(this.position.x, this.position.y - (deltay/1f), w.pos.x, w.pos.y+(deltay/1f), w.pos.x,w.pos.y);
					}
					connection.endShape();
					
					int childID = timeline.connections.getChildCount();
					childs.add(childID);
					timeline.connections.addChild(connection);
				}
				else {
					PShape curve = timeline.connections.getChild(childs.get(idFilter));
					//TODO change alpha value
					int col = parent.color(parent.red(coly),parent.green(coly),parent.blue(coly),alp);
					curve.setStroke(col);
					
					PVector v1 = curve.getVertex(0);
					PVector v2 = curve.getVertex(1);
					PVector v3 = curve.getVertex(2);
					PVector v4 = curve.getVertex(3);
					float deltax = this.position.x - w.pos.x;
					float deltay = this.position.y - w.pos.y;
					v1 = new PVector(this.position.x,this.position.y,0);
					v2 = new PVector(this.position.x, this.position.y - (deltay/1f));
					v3 = new PVector(w.pos.x, w.pos.y+(deltay/1f),0);
					v4 = new PVector(w.pos.x,w.pos.y,0.f);
					curve.setVertex(0, v1);
					curve.setVertex(1, v2);
					curve.setVertex(2, v3);
					curve.setVertex(3, v4);	
				}
			}
		
	}
	
	
	
}

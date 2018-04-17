package welchezukunft;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;

import org.gicentre.utils.move.Ease;

import processing.core.*;

public class knotObject {
	PVector position;
	int id;
	wordcloud parent;
	List<Integer> childs;
	boolean init = false;
	String word;
	//radius
	float rad = 0;
	//1 if over :: -1 if under
	int sign = 1;
	int knotCol;
	
	knotObject(float x, float y, int id, String word,wordcloud parent){
		this.position = new PVector(x,y,0);
		this.id = id;
		this.word = word;
		this.parent = parent;
		childs = new ArrayList<Integer>();
		//connect adds also child objects -> connect first than (re)place circle
		connect();
		placeCircle();
	}
	
	public int getCount() {
		return childs.size();
	}
	
	//TODO check position
	public boolean checkPosition(PVector pos, float size) {
		boolean canbeplaced = true;
		for(knotObject k : parent.knots) {
			float dist = (float) Math.sqrt(Math.pow((k.position.x - pos.x), 2) + Math.pow((k.position.y - pos.y), 2));
			if(dist > k.rad + size) {
				continue;
			}
			if(pos.y - size < k.position.y + k.rad ) {
			
				
			}
			if(canbeplaced == false) {
				break;
			}
		}
		return canbeplaced;
	}
	
	public void changeposition(float x) {
		this.position.x=x;
		this.sign = (0>this.position.y)?-1:1;
		this.position.y+=this.position.x * 0.01 * this.sign;
		connect();
		placeCircle();
	}
	
	private void placeCircle() {
		//if only one child = circle not visible
		if(this.childs.size() < 2) {
			PShape circle = this.parent.parent.createShape();
			circle.beginShape(PConstants.TRIANGLE_FAN);
			circle.fill(knotCol);
			circle.noStroke();
			circle.vertex(this.position.x, this.position.y);
			for (int i = 0; i <= 10; i++)   {
				circle.vertex((float)(this.position.x + (this.rad * Math.cos(i * PConstants.TWO_PI / 10f))),(float)(this.position.y + (this.rad * Math.sin(i * PConstants.TWO_PI / 10f))));	
			}	
			circle.endShape();
			parent.allCircles.addChild(circle);
		}
		//if more than one child = visible circle and draw at actual position
		else if(this.childs.size() >= 2) {	
			PShape circle = parent.allCircles.getChild(this.id);
			this.rad = 100.f+this.childs.size()*10;
			circle.setVertex(0,position);
			for (int j = 1; j <= 11; j++)   {
				PVector v1 = new PVector((float)(this.position.x + (this.rad * Math.cos(j * PConstants.TWO_PI / 10f))), (float)(this.position.y + (this.rad * Math.sin(j * PConstants.TWO_PI / 10f))));
				circle.setVertex(j, v1);
			}
		}
		
	}
	
	private void connect() {
		this.knotCol = parent.colors.get(this.id);
		//get count of appearances 
		int count = (int) parent.words.stream().filter(wordObject ->  wordObject.id == this.id).count();
		
		// get objects with same id
		Stream<wordObject> testStream = parent.words.stream().filter(wordObject -> wordObject.id == this.id);
		List<wordObject> testList = testStream.collect(Collectors.toList());
		
		// calculate maxCount
		int maxCount = 0;
		if(parent.knots.size() > 0) {
			maxCount = parent.knots.stream()
					.sorted(Comparator.comparing(knotObject::getCount)
					.reversed())
					.collect(Collectors.toList()).get(0).childs.size();
		}

		//calculate alpha
		float div = (float)this.childs.size() / (float)maxCount;
		float alp = 60f + parent.parent.map(parent.parent.lerp(0,(float)maxCount,Ease.quarticIn(div,0.008f)), 0, (float)maxCount, 60, 255);
		int col = parent.parent.color(parent.parent.red(this.knotCol),parent.parent.green(this.knotCol),parent.parent.blue(this.knotCol),alp);

		//create or shift connections
		for(wordObject w : testList) {
				int idFilter = testList.indexOf(w);
				//do for last object in List
				if(idFilter == testList.size()-1) {
					float deltay = this.position.y - w.pos.y;
					PShape connection = parent.parent.createShape();
					connection.beginShape();
					connection.bezierDetail(30);
					connection.strokeWeight(5);
					connection.stroke(col);
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
					int childID = parent.connections.getChildCount();
					this.childs.add(childID);
					parent.connections.addChild(connection);
				}
				//do for all Objects except last object
				else {
					PShape curve = parent.connections.getChild(this.childs.get(idFilter));
					curve.setStroke(col);
					PVector v1 = curve.getVertex(0);
					PVector v2 = curve.getVertex(1);
					PVector v3 = curve.getVertex(2);
					PVector v4 = curve.getVertex(3);
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

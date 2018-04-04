package welchezukunft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;

import org.gicentre.utils.move.Ease;

import jogamp.opengl.glu.nurbs.Knotspec;
import processing.core.*;

public class knotObject {
	PVector position;
	int id;
	PApplet parent;
	List<Integer> childs;
	boolean init = false;
	String word;
	
	knotObject(float x, float y, int id, String word,PApplet parent){
		this.position = new PVector(x,y,0);
		this.id = id;
		this.word = word;
		this.parent = parent;
		childs = new ArrayList<Integer>();

		//connect adds also child objects
		connect();
		placeCircle();
	}
	
	public int getCount() {
		return childs.size();
	}
	
	public void changeposition(float x) {
		this.position.x=x;
		int sign = (0>this.position.y)?-1:1;
		this.position.y+=this.position.x * 0.01 * sign;
		connect();
		placeCircle();
	}
	
	private void placeCircle() {
		//if only one child circle not visible
		if(this.childs.size() < 2) {
			System.out.println("init circle " + this.id);
			PShape circle = this.parent.createShape();
			float radius = 0;
			circle.beginShape(PConstants.TRIANGLE_FAN);
			circle.fill(timeline.colors.get(this.id));
			circle.noStroke();
			circle.vertex(this.position.x, this.position.y);
			for (int i = 0; i <= 10; i++)   {
				circle.vertex(this.position.x + (radius * parent.cos(i * parent.TWO_PI / 20f)), (this.position.y + (radius * parent.sin(i * parent.TWO_PI / 20f))));	
			}	
			circle.endShape();
			timeline.allCircles.addChild(circle);
		}
		//if more than one childs visible circle and draw at actual position
		else if(this.childs.size() >= 2) {
			System.out.println("move circle " + this.id);
			PShape circle = timeline.allCircles.getChild(this.id);
			float radius = 100.f+this.childs.size()*10;
			circle.setVertex(0,position);
			for (int j = 1; j <= 11; j++)   {
				PVector v1 = new PVector(this.position.x + (radius * parent.cos(j * parent.TWO_PI / 20f)), (this.position.y + (radius * parent.sin(j * parent.TWO_PI / 20f))));
				circle.setVertex(j, v1);
			}
		}
		
	}
	
	private void connect() {
		int coly = timeline.colors.get(this.id);
		//get count of appearances 
		int count = (int) timeline.words.stream().filter(wordObject ->  wordObject.id == this.id).count();
		System.out.println("count: " + count + " (" + this.word +")");
		// get objects with same id
		Stream<wordObject> testStream = timeline.words.stream().filter(wordObject -> wordObject.id == this.id);
		List<wordObject> testList = testStream.collect(Collectors.toList());
		
		/*
		System.out.println("---Sorting using Comparator by Age with reverse order---");
		slist = list.stream().sorted(Comparator.comparing(Student::getAge).reversed()).collect(Collectors.toList());
		slist.forEach(e -> System.out.println("Id:"+ e.getId()+", Name: "+e.getName()+", Age:"+e.getAge()));
		*/
		int maxCount = 0;
		if(timeline.knots.size() > 0) {
			maxCount = timeline.knots.stream()
					.sorted(Comparator.comparing(knotObject::getCount)
					.reversed())
					.collect(Collectors.toList()).get(0).childs.size();
		}
		
		//int maxCount = Collections.max(timeline.wordCount);
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
					this.childs.add(childID);
					timeline.connections.addChild(connection);
				}
				
				else {
					System.out.println(this.childs.size() + " / " + idFilter);
					PShape curve = timeline.connections.getChild(this.childs.get(idFilter));
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

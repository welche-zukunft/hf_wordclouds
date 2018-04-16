package welchezukunft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import shiffman.box2d.Box2DProcessing;

public class physicsSum {

	Box2DProcessing box2d;
	PApplet parent;
	PGraphics targetplane;
	private List<Mover> movers;
	private Attractor a;
	
	public physicsSum(PApplet parent) {
		this.parent = parent;
		targetplane = parent.createGraphics(3840,1080,PConstants.P2D);
		movers  = new ArrayList<Mover>();//= new Mover[65];
		
		box2d = new Box2DProcessing(parent);
		box2d.createWorld();
		// No global gravity force
		box2d.setGravity(0,0);	
		this.a = new Attractor(32,targetplane.width/2,targetplane.height/2,this);
	}
	
	public void removeBodies() {
		if(box2d.world.getBodyCount() > 0){
		    for(int i = movers.size() - 1; i > 0; i--){
		        Body b = movers.get(i).body;
		        if(b == null){
		            movers.remove(i);
		            continue;
		        }
		        box2d.world.destroyBody(b);
		        movers.remove(i);
		    }
		}else{
		    // There is no bodies in the world, so the bodies in our list are just
		    // random memory hogs, leave them for the GC by clearing the list
		    movers.clear();
		}
	}
	
	
	public void addMover(int top) {
		removeBodies();
		float minX = 8.0f;
		float maxX = 16.0f;
		Random rand = new Random();
		for (int i = 0; i < top; i++) {
			  movers.add(new Mover(rand.nextFloat() * (maxX - minX) + minX,rand.nextFloat()*targetplane.width,rand.nextFloat()*targetplane.height,this));
			}
	}
	
	public void drawPhysics(){
		if(timeline.showlogo == true){
	    	timeline.logoWZ.drawlogo();
		}
		targetplane.beginDraw();
		targetplane.clear();
		targetplane.background(100);
		box2d.step();
		
		this.a.display();
		for (int i = 0; i < movers.size(); i++) {
		    // Look, this is just like what we had before!
		    Vec2 force = a.attract(movers.get(i));
		    movers.get(i).applyForce(force);
		    movers.get(i).display();
		  }
		if(timeline.showlogo == true){
			targetplane.image(timeline.logoWZ.logoPlane,0,0,timeline.logoWZ.logoPlane.width,timeline.logoWZ.logoPlane.height);	
		}
		targetplane.endDraw();
	}

	
	
	
}

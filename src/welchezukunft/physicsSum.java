package welchezukunft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import com.sun.prism.Image;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PShader;
import shiffman.box2d.Box2DProcessing;

public class physicsSum {

	Box2DProcessing box2d;
	PApplet parent;
	PGraphics targetplane, allplane, textplane, legend;
	boolean legendInit = false;
	PShader circleShader;
	PShader blur;
	List<Mover> movers;
	Attractor a;
	Random rand;
	
	public physicsSum(PApplet parent) {
		rand = new Random();
		this.parent = parent;
		targetplane = parent.createGraphics(3840,1080,PConstants.P2D);
		allplane = parent.createGraphics(3840,1080,PConstants.P2D);
		textplane = parent.createGraphics(3840,1080,PConstants.P2D);
		
		legend = parent.createGraphics(1920,360,PConstants.P2D);
		
		circleShader = parent.loadShader("./resources/shader/circle.glsl");
		blur = parent.loadShader("./resources/shader/blur.glsl");
		
		movers  = new ArrayList<Mover>();//= new Mover[65];
		
		box2d = new Box2DProcessing(parent);
		box2d.createWorld();
		// No global gravity force
		box2d.setGravity(0,0);	
		this.a = new Attractor(80,targetplane.width/2,targetplane.height/2,this);
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
	
	
	public void addMover() {
		legendInit  = false;
		removeBodies();
		List<knotObject> collectKnots = timeline.clouds.stream()
				.filter(t -> t.knots.size() > 0)
                .flatMap(wordcloud::getKnots)
                .collect(Collectors.toList());

		for(knotObject k : collectKnots) {
			if(k.childs.size() > 1) {
				float radius = k.childs.size() * 30.f;		
				int color = timeline.workshopColors.get(k.parent.id - 1);
				movers.add(new Mover(radius,k.word,color,this));
			}
		}

	}
	
	public void drawPhysics(){
		if(legendInit == false) {
			drawLegend();
		}
		
		if(timeline.showlogo == true){
	    	timeline.logoWZ.drawlogo();
		}
		
		// mover output
		targetplane.beginDraw();
		targetplane.clear();
		box2d.step();
		this.a.changeG();
		this.a.display();
		for (int i = 0; i < movers.size(); i++) {
		    // Look, this is just like what we had before!
		    Vec2 force = a.attract(movers.get(i));
		    movers.get(i).applyForce(force);
		    movers.get(i).display();
		  }
		targetplane.endDraw();
		
		
		// text plane
		textplane.beginDraw();
		textplane.clear();
		for (int i = 0; i < movers.size(); i++) {
		    movers.get(i).displayText();
		  }
		textplane.endDraw();

		//final output
		allplane.beginDraw();
		allplane.clear();
		circleShader.set("resolution", 3840f, 1080f);
		circleShader.set("time", parent.frameCount * 0.031f);
		allplane.filter(circleShader);
		allplane.rect(0,0,3840,0);
		allplane.resetShader();
		
		allplane.image(targetplane,0,0);
		
		//allplane.shader(blur);
		allplane.image(textplane,0,0);
		//allplane.resetShader();
		
		if(timeline.showlogo == true){
			targetplane.image(timeline.logoWZ.logoPlane,0,0,timeline.logoWZ.logoPlane.width,timeline.logoWZ.logoPlane.height);	
		}
		
		allplane.endDraw();
		
	}

	
	void drawLegend() {
		List<wordcloud> collectClouds = timeline.clouds.stream()
				.filter(t -> t.knots.size() > 0)
				.filter(t -> t.getKnots().anyMatch(w -> w.childs.size() >= 1))
				.collect(Collectors.toList());
	
		int t = timeline.clouds.get(0).knots.get(0).getCount();
		legend.beginDraw();
		float deltax = legend.width/(collectClouds.size()+1);
		for(wordcloud w : collectClouds) {
			legend.pushMatrix();
			legend.translate((float) ((deltax/2.) + deltax * collectClouds.indexOf(w)), 30);
			legend.fill(timeline.workshopColors.get(w.getId()-1));
			legend.noStroke();
			legend.rect(0, 0, deltax * 0.9f, 80f);
			legend.popMatrix();
			
		}
		legend.endDraw();
		legendInit = true;
	}
	
	
}

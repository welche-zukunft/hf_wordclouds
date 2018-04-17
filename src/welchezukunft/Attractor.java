package welchezukunft;

import java.util.Random;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

import processing.core.PImage;

public class Attractor {

private Body body;
private float r;
private physicsSum parent;
public float G = 100;
private int time = 0;
private int currentImpulse = 0;
private Random rand;
public boolean impulseC = false; 


	Attractor(float r_, float x, float y,physicsSum parent) {
		 rand = new Random();
		 this.parent = parent;
		 r = r_;
		 // Define a body
		 BodyDef bd = new BodyDef();
		 bd.type = BodyType.STATIC;
		 // Set its position
		 bd.position = parent.box2d.coordPixelsToWorld(x,y);
		 body = parent.box2d.world.createBody(bd);
		
		 // Make the body's shape a circle
		 CircleShape cs = new CircleShape();
		 cs.m_radius = parent.box2d.scalarPixelsToWorld(r);
		 
		 body.createFixture(cs,1);
	
	}


// Formula for gravitational attraction
// We are computing this in "world" coordinates
// No need to convert to pixels and back
	
	Vec2 attract(Mover m) {
		 // clone() makes us a copy
		 Vec2 pos = body.getWorldCenter();    
		 Vec2 moverPos = m.body.getWorldCenter();
		 // Vector pointing from mover to attractor
		 Vec2 force = pos.sub(moverPos);
		 float distance = force.length();
		 // Keep force within bounds
		 if(distance > 5) distance = 5;
		 else if (distance < 1) distance = 1;
		 //distance = constrain(distance,1,5);
		 force.normalize();
		 // Note the attractor's mass is 0 because it's fixed so can't use that
		 float strength = (G * 1 * m.body.m_mass) / (distance * distance); // Calculate gravitional force magnitude
		 force.mulLocal(strength);         // Get force vector --> magnitude * direction
		 return force;
	}

	void changeG() {
		if(impulseC == true) {
			int change = rand.nextInt(currentImpulse/10);
			this.G += change;
			//System.out.println(this.G + "/" + change + "/" + currentImpulse);
			if(this.G > 100) {
				impulseC = false;	
			}
		}
		
		else if(impulseC == false) {
			if(rand.nextFloat() > 0.99) {
				time = 0;
				currentImpulse = rand.nextInt(1200)+40;
				this.G = (float) (-1. * currentImpulse);
				impulseC = true;
			}	
		}
		
	}
	
	void impulse(boolean direction) {
		if(direction == true) {
			this.G = 5000;
			impulseC = false;
			time = 0;
		}
		if(direction == false) {
			this.G = -1500;
			currentImpulse = 1500;
			impulseC = true;
			time = 0;
		}		
	}

	void display() {
		 time ++;
		 Vec2 pos = parent.box2d.getBodyPixelCoord(body);
		 float a = body.getAngle();
		 
		 parent.targetplane.pushMatrix();
		 parent.targetplane.translate(pos.x,pos.y);
		 parent.targetplane.rotate(a);
		 parent.targetplane.fill(0);
		 parent.targetplane.stroke(0);
		 parent.targetplane.strokeWeight(1);
		 parent.targetplane.ellipse(0,0,r*2,r*2);
		 parent.targetplane.image(timeline.imagelogo,-r,-r,2*r,(2*r) * (timeline.imagelogo.width/timeline.imagelogo.height));
		 parent.targetplane.popMatrix();
	}
}
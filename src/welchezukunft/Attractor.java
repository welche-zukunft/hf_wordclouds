package welchezukunft;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

public class Attractor {

private Body body;
private float r;
private physicsSum parent;

	Attractor(float r_, float x, float y,physicsSum parent) {
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
		 float G = 100;
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

	void display() {
		 // We look at each body and get its screen position
		 Vec2 pos = parent.box2d.getBodyPixelCoord(body);
		 // Get its angle of rotation
		 float a = body.getAngle();
		 parent.targetplane.pushMatrix();
		 parent.targetplane.translate(pos.x,pos.y);
		 parent.targetplane.rotate(a);
		 parent.targetplane.fill(0);
		 parent.targetplane.stroke(0);
		 parent.targetplane.strokeWeight(1);
		 parent.targetplane.ellipse(0,0,r*2,r*2);
		 parent.targetplane.popMatrix();
	}
}
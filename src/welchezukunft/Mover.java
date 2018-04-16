package welchezukunft;

import java.util.Random;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

public class Mover {

	public Body body;
	private float r;
	private int col;
	private physicsSum parent;

	Mover(float r_, float x, float y, physicsSum parent) {
	 this.parent = parent;
	 this.col = (int)(Math.random() * 255);
	 r = r_;
	 // Define a body
	 BodyDef bd = new BodyDef();
	 bd.type = BodyType.DYNAMIC;
	
	 // Set its position
	 bd.position = parent.box2d.coordPixelsToWorld(x,y);
	 body = parent.box2d.world.createBody(bd);
	
	 // Make the body's shape a circle
	 CircleShape cs = new CircleShape();
	 cs.m_radius = parent.box2d.scalarPixelsToWorld(r);
	 
	 // Define a fixture
	 FixtureDef fd = new FixtureDef();
	 fd.shape = cs;
	 // Parameters that affect physics
	 fd.density = 1;
	 fd.friction = 0.3f;
	 fd.restitution = 0.5f;
	
	 body.createFixture(fd);
	
	 float minX = -5.0f;
	 float maxX = 5.0f;
	 Random rand = new Random();
 
	 body.setLinearVelocity(new Vec2(rand.nextFloat() * (maxX - minX) + minX,rand.nextFloat() * (maxX - minX) + minX));
	 
	 minX = -1.0f;
	 maxX = 1.0f;
	 
	 body.setAngularVelocity(rand.nextFloat() * (maxX - minX) + minX);
	}

	void applyForce(Vec2 v) {
	 body.applyForce(v, body.getWorldCenter());
	}


	void display() {
	 // We look at each body and get its screen position
	 Vec2 pos = parent.box2d.getBodyPixelCoord(body);
	 // Get its angle of rotation
	 float a = body.getAngle();
	 parent.targetplane.pushMatrix();
	 parent.targetplane.translate(pos.x,pos.y);
	 parent.targetplane.rotate(a);
	 parent.targetplane.fill(col);
	 parent.targetplane.stroke(0);
	 parent.targetplane.strokeWeight(1);
	 parent.targetplane.ellipse(0,0,r*2,r*2);
	 // Let's add a line so we can see the rotation
	 parent.targetplane.line(0,0,r,0);
	 parent.targetplane.popMatrix();
	}
	
}
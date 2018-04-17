package welchezukunft;

import java.util.Random;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.PConstants;
import processing.core.PVector;

public class Mover {
	
	
	public Body body;
	private float r;
	private int col;
	private physicsSum parent;
	private String Word;
	private PVector textposition;

	Mover(float r_, String word,int col, physicsSum parent) {
	 this.parent = parent;
	 this.col = col;
	 this.Word = word;
	 
	 float minX = 8.0f;
	 float maxX = 16.0f;
	 Random rand = new Random();
	 
	 float x = rand.nextFloat()*parent.targetplane.width;
	 float y = rand.nextFloat()*parent.targetplane.height;
	 
	 textposition = new PVector();
	 textposition.x = x;
	 textposition.y = y;
	 
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
	 fd.friction = 0.1f + (rand.nextFloat() * 0.5f);
	 fd.restitution = 0.1f + (rand.nextFloat() * 0.5f);
	
	 body.createFixture(fd);
	
	 minX = -5.0f;
	 maxX = 5.0f;
 
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
	 parent.targetplane.noStroke();
	 parent.targetplane.ellipse(0,0,r*2,r*2);
	 parent.targetplane.popMatrix();

	}
	
	void displayText() {
		 Vec2 pos = parent.box2d.getBodyPixelCoord(body);
		 Vec2 circle = new Vec2();
		 circle.x = pos.x;
		 circle.y = pos.y;
		 
		 if(pos.x > parent.targetplane.width / 2.f) pos.x += r * 1.8;
		 else if (pos.x < parent.targetplane.width / 2.f) pos.x -= r * 1.8;
		 
		 if(pos.y > parent.targetplane.height / 2.f) pos.y += r * 1.9;
		 else if (pos.y < parent.targetplane.height / 2.f) pos.y -= r * 1.9;
		 
		 textposition.x = timeline.lerp(textposition.x,pos.x,0.03f);
		 textposition.y = timeline.lerp(textposition.y,pos.y,0.03f);
		 
	
		 if(circle.x > -10 && circle.x < 3850 && circle.y > -10 && circle.y < 1090) {
			 parent.textplane.pushMatrix();
			 parent.textplane.translate(textposition.x,textposition.y);
			 parent.textplane.fill(0);
			 parent.textplane.textAlign(PConstants.CENTER,PConstants.CENTER);
			 parent.textplane.textSize(28);
			 parent.textplane.text(this.Word, 0, 0);
			 parent.textplane.popMatrix();
			 
			 parent.textplane.pushMatrix();
			 float startlinex = (textposition.x > parent.targetplane.width / 2.f) ? textposition.x - 40 : textposition.x + 40;
			 float startliney = (textposition.y > parent.targetplane.height / 2.f) ? textposition.y - 20 : textposition.y + 20;
			 parent.textplane.strokeWeight(3);
			 parent.textplane.stroke(70);
			 parent.textplane.line(startlinex, startliney, circle.x, circle.y);
			 parent.textplane.ellipse(circle.x,circle.y,5,5);
			 parent.textplane.popMatrix();
		 }
	}
	
	
}
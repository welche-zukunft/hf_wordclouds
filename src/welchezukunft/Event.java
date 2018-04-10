package welchezukunft;

import java.util.List;

import de.bezier.data.sql.MySQL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class Event {

  private int id;
  private String content;
  private String headline;
  private String imagePath;
  private float xPos;
  private float yPos;
  private int workshop_id;
  private Eventstatus status;
  private eventTimeline parent;
  private requestSQL database;
  
  public Event(int id, String content, String imagePath, String headline, float xPos, float yPos,int workshop_id, String status){
    this.id = id;
    this.headline = headline;
    this.content = content;
    this.imagePath = imagePath;
    this.xPos = xPos;
    this.yPos = yPos;
    this.setWorkshop_id(workshop_id);
    this.parent = timeline.eventLine;
    
    database = timeline.accessSQL;
   
    if(status.equals("NEWEVENT"))  this.status = Eventstatus.NEWEVENT;  
    else if (status.equals("PENDING"))   this.status = Eventstatus.PENDING;
    else if (status.equals("PLACED"))   this.status = Eventstatus.PLACED;
    else if (status.equals("DELETED"))   this.status = Eventstatus.DELETED;
    this.createNewBadge();
  }
  
  
  public void setId(int id){
    this.id = id;
  }
  
  public int getId(){
    return this.id;
  }
  
  public void setHeadline(String headline){
    this.headline = headline;  
  }
  
  public String getHeadline(){
     return this.headline; 
  }
  
  public void setContent(String content){
    this.content = content;
  }
  
  public String getContent(){
    return this.content;
  }
  
    public void setImagePath(String imagePath){
    this.imagePath = imagePath;
  }
  
  public String getImagePath(){
    return this.imagePath;
  }
  
  public void setXPos(float xPos){
    this.xPos = xPos;
    database.eventsMsql.query("UPDATE event SET x_value = '" +  xPos + "' WHERE ID = " + this.id);
    
    
  }
  
  public float getXPos(){
    return this.xPos;
  }
  
  public void setYPos(float yPos){
    this.yPos = yPos;
    database.eventsMsql.query("UPDATE event SET y_value = '" +  yPos + "' WHERE ID = " + this.id);
  }
  
  public float getYPos(){
    return this.yPos;
  }
  
  public void setWorkshopID(int workshop_id){
   this.setWorkshop_id(workshop_id); 
  }
  
  public int getWorkshopID(){
    return this.getWorkshop_id(); 
  }
  
  public void setStatus(Eventstatus status){
    this.status = status;
    database.eventsMsql.query("UPDATE event SET status = '" +  status + "' WHERE ID = " + this.id);
  }
  
  public Eventstatus getStatus(){
    return this.status;
  }
  
  private void createNewBadge(){
	int size = parent.size;
	
    float x = this.xPos;
    float y = this.yPos;
    
    int type = this.getWorkshop_id() - 1;
    //minus one because id from sql starts with 1
    int num3 = (this.id-1) % 10;
    int num2 = (this.id-1) / 10 % 10;
    int num1 = (this.id-1) /100 % 100;  
    
    float r = 0 + num1;
    float g = 0 + num2;
    float b = 1 + num3;
    float z =  parent.maxzoom + 100;
    if(this.status == Eventstatus.PLACED) z = 0;
    parent.objectTimeline.beginShape(PConstants.QUADS);
    parent.objectTimeline.fill(r,g,b);
    parent.objectTimeline.vertex(x-size,y-size,z);
    parent.objectTimeline.vertex(x+size,y-size,z);
    parent.objectTimeline.vertex(x+size,y+size,z);
    parent.objectTimeline.vertex(x-size,y+size,z);
    parent.objectTimeline.fill(parent.cols_rgb[type]);
    if(y < 0.){
    	parent.objectTimeline.vertex(x-(size/3.f),y+(size),z);
    	parent.objectTimeline.vertex(x+(size/3.f),y+(size),z);
    }
    else{
    	parent.objectTimeline.vertex(x-(size/3.f),y-(size),z);
    	parent.objectTimeline.vertex(x+(size/3.f),y-(size),z);
    }
    parent.objectTimeline.vertex(x,0.f,z);
    parent.objectTimeline.vertex(x,0.f,z);
    parent.objectTimeline.endShape();  
  }


	public int getWorkshop_id() {
		return workshop_id;
	}
	
	
	public void setWorkshop_id(int workshop_id) {
		this.workshop_id = workshop_id;
	}
  
}

package welchezukunft;

import de.bezier.data.sql.*;
import javafx.util.Pair;
import processing.core.PApplet;
import processing.core.PVector;

import static java.lang.Math.toIntExact;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class requestSQL {

	MySQL msql;
	MySQL timestampMsql;
	
	boolean sql=true;
	
	int currentMaxId = 0;
	PApplet parent;

	public requestSQL(PApplet parent){
		this.parent = parent;
	    String user     = "root";
	    String pass     = "autoIndex2026";
	    String database = "zukunft";
	    msql = new MySQL(parent, "localhost", database, user, pass );
	    msql.connect();
	
	    //timestampMsql = new MySQL(parent, "localhost", database, user, pass );
	    //timestampMsql.connect();
	}

	public void getWordsSetup(){
		msql.query("SELECT * FROM keyword");
		while(msql.next()){
			System.out.println(msql.getInt(1) + msql.getInt(2) + msql.getString(3) + msql.getInt(4));
		}
		msql.query("SELECT * FROM sentence");
		while(msql.next()){
			System.out.println(msql.getInt(1) + msql.getString(2) + msql.getTime(3));
		}		
	}

	
	/*
	//Load Database on startup
	public List<Event> getNewEventsSetup(){
	   List<Event> res = new ArrayList<Event>();
	   //println(currentMaxId);
	   msql.query("SELECT * FROM event");
	   int i = 0;
	   while(msql.next()){
	     Event event = new Event(msql.getInt(9), msql.getString(2), msql.getString(3), msql.getString(8), msql.getFloat(11),msql.getFloat(12),msql.getInt(10),msql.getString(6));
	     res.add(event);
	     //println("# ",  i++);
	     currentMaxId = msql.getInt(9);
	     msql.query("UPDATE event SET load_flag = 'NOLOAD'");
	     msql.query("SELECT * FROM event WHERE vertex_id > %s", currentMaxId);
	   }
	   
	   return res;
	}

	// Load Database in runtime
	void updateEvents(){
	   List<Event> res = new ArrayList<Event>();
	   //println(currentMaxId);
	   msql.query("SELECT * FROM event WHERE load_flag = 'LOAD'");
	   int i = 0;
	   while(msql.next()){
	     //check if id is new -> then add to eventlist 
	     if(msql.getInt(9) > currentMaxId){
	       Event event = new Event(msql.getInt(9), msql.getString(2), msql.getString(3), msql.getString(8), msql.getFloat(11),msql.getFloat(12),msql.getInt(10),msql.getString(6));
	       res.add(event);
	       currentMaxId = msql.getInt(9);
	       
	     }
	     // if event is already in list just update content
	     else if(msql.getInt(9) <= currentMaxId){
	       int pos = msql.getInt(9) -1;
	       eventList.get(pos).setHeadline(msql.getString(8));
	       eventList.get(pos).setContent(msql.getString(2));
	       eventList.get(pos).setImagePath(msql.getString(3));
	       eventList.get(pos).setWorkshopID(msql.getInt(10));
	       updateGUI();
	     }
	     
	     //println("# ",  i++);
	     msql.execute("UPDATE event SET load_flag = 'NOLOAD'");
	     msql.query("SELECT * FROM event WHERE load_flag = 'LOAD'");
	     
	     eventList.addAll(res);
	     updateGUI();
	   }
    
	}
	*/
}
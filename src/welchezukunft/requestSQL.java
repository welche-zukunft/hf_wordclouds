package welchezukunft;

import de.bezier.data.sql.*;
import javafx.util.Pair;
import processing.core.PApplet;
import processing.core.PVector;

import static java.lang.Math.toIntExact;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class requestSQL {
	MySQL msql;
	MySQL timestampMsql;
	
	MySQL eventsMsql;
	MySQL eventsTimestampMsql;
	
	boolean sql=true;
	
	int currentMaxIdKeywords = 0;
	int currentMaxIdEvents = 0;
	PApplet parent;
	
	private BlockingQueue<List<newKeyword>> sentenceQueue;

	public requestSQL(PApplet parent){
		this.parent = parent;
	    String user     = "root";
	    String pass     = "autoIndex2026";
	    String database = "zukunft";
	    msql = new MySQL(parent, "localhost", database, user, pass );
	    msql.connect();
	
	    timestampMsql = new MySQL(parent, "localhost", database, user, pass );
	    timestampMsql.connect();
	    
	    eventsMsql = new MySQL(parent, "localhost", database, user, pass );
	    eventsMsql.connect();
	    
	    eventsTimestampMsql = new MySQL(parent, "localhost", database, user, pass );
	    eventsTimestampMsql.connect();
	    
	    sentenceQueue = new ArrayBlockingQueue<List<newKeyword>>(512);
	    batchCreator bCreator = new batchCreator(sentenceQueue);
		new Thread(bCreator).start();
	}

	public void getWordsSetup(){
		msql.query("SELECT * FROM keyword");
		while(msql.next()){
			// id , keyindex , keyword, sentence_id
			int id = msql.getInt(1);
			int keyindex = msql.getInt(2);
			String word = msql.getString(3);
			int sentence_id = msql.getInt(4);
			int seconds = 0;
			int cloudId = 1;
			
			
			timestampMsql.query("SELECT * FROM sentence WHERE id ='"+ sentence_id + "'");
				while(timestampMsql.next()){
					Time time = timestampMsql.getTime(4);
					seconds = time.getSeconds() + time.getMinutes()*60 + time.getHours() * 3600;
					cloudId = timestampMsql.getInt(5);
				}
				
			this.currentMaxIdKeywords = id;	
			int cloudID2 = cloudId;
	
			boolean wcExists = timeline.clouds.stream().anyMatch(wordcloud -> wordcloud.id == cloudID2);
			
			//create new wordcloud
			if(wcExists == false) {
				wordcloud newcloud = new wordcloud(parent,cloudID2);
				newcloud.id = cloudID2;
				System.out.println(newcloud.id);
				newcloud.createBadge(word, seconds,false);
				timeline.clouds.add(newcloud);
				timeline.currentCloudid = timeline.clouds.size()-1;
			}
			
			//add to old wordcloud
			else if(wcExists == true) {
				wordcloud targetCloud = timeline.clouds.stream().filter(wordcloud -> wordcloud.id == cloudID2).findFirst().get();
				targetCloud.createBadge(word, seconds,false);
			}
			
		}
		timeline.eventLine.userGui.updateGUIKeywords();
		System.out.println(timeline.clouds.size());
	}
	
		
	public void updateWords() {
		
		//check how many keywords are in actual sentence
		int keywordCount = 0;
		int currentMaxId_old = this.currentMaxIdKeywords;
		//create List to add later in thread
		List<newKeyword> newKeywordList = new ArrayList<newKeyword>();
		//query Database
		msql.query("SELECT * FROM keyword WHERE id > '" + this.currentMaxIdKeywords + "'");
		while(msql.next()){
			// id , keyindex , keyword, sentence_id
			//System.out.println(msql.getInt(1) + msql.getInt(2) + msql.getString(3) + msql.getInt(4));
			int id = msql.getInt(1);
			int keyindex = msql.getInt(2);
			String word = msql.getString(3);
			int sentence_id = msql.getInt(4);
			int seconds = 0;
			int cloudId = 0;
			
			this.currentMaxIdKeywords = id;	
			
			timestampMsql.query("SELECT * FROM sentence WHERE id ='"+ sentence_id + "'");
				while(timestampMsql.next()){
					Time time = timestampMsql.getTime(4);
					seconds = time.getSeconds() + time.getMinutes()*60 + time.getHours() * 3600;
					keywordCount = timestampMsql.getInt(3);
					cloudId = timestampMsql.getInt(5);
				}
			newKeywordList.add(new newKeyword(word,seconds, cloudId, id));	
		}
		
		
		//check if all Keywords are loaded to List
		//if yes = start BatchCreation
		if(newKeywordList.size() == keywordCount && keywordCount != 0) {
			try {
				sentenceQueue.put(newKeywordList);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//else reverse to previous state to query again in next turn
		else{
			this.currentMaxIdKeywords = currentMaxId_old;
		}
		
	}
	
	// Load Events from Database at start
	public List<Event> getNewEventsSetup(){
		   List<Event> res = new ArrayList<Event>();
		   //println(currentMaxId);
		   eventsMsql.query("SELECT * FROM event");
		   int i = 0;
		   while(eventsMsql.next()){
			  
		     Event event = new Event(eventsMsql.getInt(11), eventsMsql.getString(2), eventsMsql.getString(4), eventsMsql.getString(10), eventsMsql.getFloat(13),eventsMsql.getFloat(14),eventsMsql.getInt(12),eventsMsql.getString(8),eventsMsql.getInt(3),eventsMsql.getString(6),eventsMsql.getInt(15));
		     res.add(event);
		     //println("# ",  i++);
		     this.currentMaxIdEvents = eventsMsql.getInt(11);
		     eventsMsql.query("UPDATE event SET load_flag = 'NOLOAD'");
		     eventsMsql.query("SELECT * FROM event WHERE vertex_id > %s", this.currentMaxIdEvents);
		   }
		   
		   return res;
		}

	// Load Events from Database in runtime
	void updateEvents(){
		   List<Event> res = new ArrayList<Event>();
		   //println(currentMaxId);
		   this.eventsMsql.query("SELECT * FROM event WHERE load_flag = 'LOAD'");
		   int i = 0;
		   while(this.eventsMsql.next()){
		     //check if id is new -> then add to eventlist 
		     if(this.eventsMsql.getInt(11) > this.currentMaxIdEvents){
		       Event event = new Event(this.eventsMsql.getInt(11), this.eventsMsql.getString(2), this.eventsMsql.getString(4), this.eventsMsql.getString(10), this.eventsMsql.getFloat(13),this.eventsMsql.getFloat(14),this.eventsMsql.getInt(12),this.eventsMsql.getString(8),eventsMsql.getInt(3),eventsMsql.getString(6),eventsMsql.getInt(15));
		       res.add(event);
		       this.currentMaxIdEvents = this.eventsMsql.getInt(11);
		       
		     }
		     
		     // if event is already in list just update content
		     else if(this.eventsMsql.getInt(11) <= this.currentMaxIdEvents){
		    	 System.out.println(this.eventsMsql.getInt(11) + "/ " + this.currentMaxIdEvents);
		       int pos = this.eventsMsql.getInt(11) - 1;
		       timeline.eventLine.eventList.get(pos).setHeadline(eventsMsql.getString(10));
		       timeline.eventLine.eventList.get(pos).setContent(eventsMsql.getString(2));
		       timeline.eventLine.eventList.get(pos).setImagePath(eventsMsql.getString(4));
		       timeline.eventLine.eventList.get(pos).setWorkshopID(eventsMsql.getInt(12));
		       timeline.eventLine.eventList.get(pos).setDay(eventsMsql.getInt(3));
		       timeline.eventLine.eventList.get(pos).setMonth(eventsMsql.getString(6));
		       timeline.eventLine.eventList.get(pos).setYear(eventsMsql.getInt(15));
		       timeline.eventLine.userGui.updateGUI();
		     }
		     
		     //println("# ",  i++);
		     this.eventsMsql.execute("UPDATE event SET load_flag = 'NOLOAD'");
		     this.eventsMsql.query("SELECT * FROM event WHERE load_flag = 'LOAD'");
		     
		     timeline.eventLine.eventList.addAll(res);
		     timeline.eventLine.userGui.updateGUI();
		   }
		}
		  		
}
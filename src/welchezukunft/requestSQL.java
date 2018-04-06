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
	
	boolean sql=true;
	
	int currentMaxId = 0;
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
	    
	    sentenceQueue = new ArrayBlockingQueue<List<newKeyword>>(512);
	    batchCreator bCreator = new batchCreator(sentenceQueue);
		new Thread(bCreator).start();
	}

	public void getWordsSetup(){
		msql.query("SELECT * FROM keyword");
		while(msql.next()){
			// id , keyindex , keyword, sentence_id
			//System.out.println(msql.getInt(1) + msql.getInt(2) + msql.getString(3) + msql.getInt(4));
			int id = msql.getInt(1);
			int keyindex = msql.getInt(2);
			String word = msql.getString(3);
			int sentence_id = msql.getInt(4);
			int seconds = 0;
			
			int cloudId = 0;
			
			timestampMsql.query("SELECT * FROM sentence WHERE id ="+ sentence_id);
				while(timestampMsql.next()){
					Time time = timestampMsql.getTime(4);
					seconds = time.getSeconds();
					cloudId = timestampMsql.getInt(5);
				}
				
			currentMaxId = id;	
			int test = cloudId;
			
			//create new wordcloud
			boolean wcExists = timeline.clouds.stream().anyMatch(wordcloud -> wordcloud.id == test);
			
			if(wcExists == false) {
				wordcloud newcloud = new wordcloud(parent,test);
				timeline.clouds.add(newcloud);
				newcloud.id = cloudId;
				newcloud.createBadge(word, seconds);
				timeline.currentCloudid = timeline.clouds.size()-1;

			}
			
			//add to old wordcloud
			else if(wcExists == true) {
				wordcloud targetCloud = timeline.clouds.stream().filter(wordcloud -> wordcloud.id == test).findFirst().get();
				targetCloud.createBadge(word, seconds);
			}
			
		}

	}
	
		
	public void updateWords() {
		//check how many keywords are in actual sentence
		int keywordCount = 0;
		int currentMaxId_old = currentMaxId;
		//create List to add later in thread
		List<newKeyword> newKeywordList = new ArrayList<newKeyword>();
		//query Database
		msql.query("SELECT * FROM keyword WHERE id >" + currentMaxId);
		while(msql.next()){
			// id , keyindex , keyword, sentence_id
			//System.out.println(msql.getInt(1) + msql.getInt(2) + msql.getString(3) + msql.getInt(4));
			int id = msql.getInt(1);
			int keyindex = msql.getInt(2);
			String word = msql.getString(3);
			int sentence_id = msql.getInt(4);
			int seconds = 0;
			int cloudId = 0;
			
			currentMaxId = id;	
			
			timestampMsql.query("SELECT * FROM sentence WHERE id ="+ sentence_id);
				while(timestampMsql.next()){
					Time time = timestampMsql.getTime(4);
					seconds = time.getSeconds();
					keywordCount = timestampMsql.getInt(3);
					cloudId = timestampMsql.getInt(5);
				}
			newKeywordList.add(new newKeyword(word,seconds, cloudId, id));	
		}
		
		
		//check if all Keywords are loaded to List
		//if yes = start BatchCreation
		if(newKeywordList.size() == keywordCount) {
			try {
				sentenceQueue.put(newKeywordList);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//else reverse to previous state to query again in next turn
		else{
			currentMaxId = currentMaxId_old;
		}
		
	}

	private void startBadgeCreationBatch(List<newKeyword> newKeywords) {

	}


	
	/*
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
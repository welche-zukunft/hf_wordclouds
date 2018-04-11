package welchezukunft;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import processing.core.PApplet;
import processing.core.PVector;

public class mainGui {
	
	eventTimeline parent;
	
	JFrame guiframe;
	JPanel topPanel,topPanel2;
	JPanel mainPanelGui,mainPanelGui2,switchModePanel;
	JPanel changeStatus;
	JButton GUIbutton,newButton,pendingButton,pausedButton;
	
	DefaultListModel yearmodel, wsmodel,modemodel, eventmodelPlaced,eventmodelNew,eventmodelPending,eventmodelDeleted;
	JList yearList, wsList,modeList, eventsListPlaced,eventsListNew,eventsListPending,eventsListDeleted;
	
	DefaultListModel keywordModel;
	JList keywordList;
	
	String[] GUIyears = {"alles","2007", "2008", "2009", "2010", "2011","2012"};
	String[] GUIworkshops  = {"alle","Pavlina Tcherneva","Harald Schumann","Cho Khong(UK)","Jürg Müller(Switzerland)","Eyvandur Gunnarsson (Iceland)","Evan Liaras (Greece)","José Soeiro(Portugal)","Isabel Feichtner (European law)","Kai von Lewinski (German Law)","Otto Steinmetz (Banks)","Cornelia Dahaim (global workforce)","Joseph Vogl (eternal critic)","Ariella Helfgott","Ulrike Hermann (Moderation)","Volker Heise (Moderation)"};
	String[] GUImode = {"timeline","paused"};
	String selectedItem;
	JLabel changeEvent;
	
	requestSQL database;
	
	public mainGui(eventTimeline parent) {
		this.parent = parent;
		this.database = timeline.accessSQL;
		this.createMainGui();
	}

	public void updateGUI(){
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			  eventmodelPlaced.clear();
			  eventmodelNew.clear();
			  eventmodelPending.clear();
			  eventmodelDeleted.clear();
			  
			   for(Event event : parent.eventList){
			    String elem = Integer.toString(event.getId()) + "/ WS:" + Integer.toString(event.getWorkshop_id()) + " / " + event.getHeadline(); 
			      if(event.getStatus() == Eventstatus.PLACED){
			      eventmodelPlaced.addElement(elem);   
			    } 
			      if(event.getStatus() == Eventstatus.NEWEVENT){
			      eventmodelNew.addElement(elem);
			   }
			      if(event.getStatus() == Eventstatus.PENDING){
			      eventmodelPending.addElement(elem);
			   }
			      if(event.getStatus() == Eventstatus.DELETED){
			      eventmodelDeleted.addElement(elem);
			   }   
		  }
		
		 eventsListPlaced.setModel(eventmodelPlaced);
		 eventsListNew.setModel(eventmodelNew);
		 eventsListPending.setModel(eventmodelPending);
		 eventsListDeleted.setModel(eventmodelDeleted);
		    }
		  });
	}
	
	public void updateGUIKeywords() {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	keywordModel.clear();
		    	int position = 0;
		    	for(wordcloud wc : timeline.clouds){
				    String elem = Integer.toString(position) + "/ WS:" + Integer.toString(wc.getId()) + " / " + wc.getWsName();  
				    keywordModel.addElement(elem);   
				    position++;
		    	}
		    	 keywordList.setModel(keywordModel); 
		    }
		});
	}

	public void setMode(int moder){
	  if(moder == 0){
	  SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      modeList.setSelectedIndex(0);
	      timeline.eventLine.eventBack.showTimeline();
	    }
	  });}
	  else if (moder == 1 && parent.showtimeline == true){
	  SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      modeList.setSelectedIndex(1);
	      timeline.eventLine.eventBack.showPaused();
	    }
	  });}
	}



void createMainGui(){
	this.guiframe = new JFrame("Welche Zukunft?! - GUI");
	this.guiframe.setBounds(10, 10, 1610, 810);
	this.guiframe.setAlwaysOnTop( true );
	
    Dimension d = new Dimension(1600,800);
    Container con = this.guiframe.getContentPane();
    con.setPreferredSize(d);
    this.mainPanelGui = new JPanel();
    this.mainPanelGui.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    this.mainPanelGui.setLayout(new GridLayout(1,7));
    this.mainPanelGui.setPreferredSize(new Dimension(1600,700));
 
    this.mainPanelGui2 = new JPanel();
    this.mainPanelGui2.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    this.mainPanelGui2.setLayout(new GridLayout(1,0));
    this.mainPanelGui2.setPreferredSize(new Dimension(1600,700));
   
    this.switchModePanel = new JPanel();
    this.switchModePanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    this.switchModePanel.setPreferredSize(new Dimension(1600,100));
     
    this.topPanel = new JPanel();
    this.topPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    //this.topPanel.setLayout(new GridLayout(3,0));
    this.topPanel.setPreferredSize(new Dimension(1600,800)); 
    
    this.topPanel2 = new JPanel();
    this.topPanel2.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    //this.topPanel2.setLayout(new GridLayout(3,0));
    this.topPanel2.setPreferredSize(new Dimension(1600,800));     
    
    //top Panel for Events
    // select a mode
   JPanel modepanel = new JPanel();
   modepanel.setLayout(new GridLayout(2,1));
   
   JLabel modeHL = new JLabel("Modus auswählen (paused only in state 0 & 1 accessable",SwingConstants.CENTER);
   modepanel.add(modeHL);
   modemodel = new DefaultListModel();
   for(String mode : GUImode){
     modemodel.addElement(mode);
   }
   modeList = new JList(modemodel);
   modeList.setSelectedIndex(0);
   modeList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            String selectedItem = (String) modeList.getSelectedValue();
            if(selectedItem == "paused" && parent.showtimeline == true){
              parent.showPaused();
            }
            else if (selectedItem == "timeline"){
              parent.eventBack.showTimeline(); 
            }
            
        }
    });
   JScrollPane modescrollPane = new JScrollPane();
   modescrollPane.setViewportView(modeList);
   modeList.setLayoutOrientation(JList.VERTICAL);
   modepanel.add(modescrollPane);
   modepanel.setBackground(new Color(0.8f,0.8f,0.8f));
   mainPanelGui.add(modepanel);
 
 
   
   
 
 
   // select a year to focus
   JPanel yearpanel = new JPanel();
   yearpanel.setLayout(new GridLayout(2,1));
   
   JLabel yearHL = new JLabel("JAHR auswählen",SwingConstants.CENTER);
   yearpanel.add(yearHL);
   this.yearmodel = new DefaultListModel();
   for(String year : GUIyears){
	   this.yearmodel.addElement(year);
   }
   this.yearList = new JList(this.yearmodel);
   this.yearList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            String selectedItem = (String) yearList.getSelectedValue();
            if(selectedItem == "alles"){
              parent.showall();
            }
            else{parent.focusYear(Integer.parseInt(selectedItem)-Integer.parseInt(GUIyears[1]));}
            setMode(0);
        }
    });
   JScrollPane yearscrollPane = new JScrollPane();
   yearscrollPane.setViewportView(yearList);
   this.yearList.setLayoutOrientation(JList.VERTICAL);
   yearpanel.add(yearscrollPane);
   yearpanel.setBackground(new Color(0.8f,0.8f,0.8f));
   this.mainPanelGui.add(yearpanel);
   this.yearList.setSelectedIndex(0);
   
   
    // select WS to show
   JPanel wspanel = new JPanel();
   wspanel.setLayout(new GridLayout(2,1));
   
   JLabel wsHL = new JLabel("Workshopauswählen",SwingConstants.CENTER);
   wspanel.add(wsHL);
   wsmodel = new DefaultListModel();
   for(String ws : GUIworkshops){
     wsmodel.addElement(ws);
   }
   wsList = new JList(wsmodel);
   wsList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            int index = wsList.locationToIndex(e.getPoint());
            parent.showWSselection = false;
            if(index != 0) parent.showWSselection = true; 
            parent.showWS(index);
            parent.currentWSselection = index;
        }
    });
   JScrollPane wsscrollPane = new JScrollPane();
   wsscrollPane.setViewportView(wsList);
   wsList.setLayoutOrientation(JList.VERTICAL);
   wspanel.add(wsscrollPane);
   wspanel.setBackground(new Color(0.8f,0.8f,0.8f));
   mainPanelGui.add(wspanel);
   wsList.setSelectedIndex(0);
   
   
   
   
   // FOCUS ON PLACED OBJECT
   JPanel placedpanel = new JPanel();
   placedpanel.setLayout(new GridLayout(2,1));
   
   JLabel placedHL = new JLabel("Fokus Platziertes Objekt",SwingConstants.CENTER);
   placedpanel.add(placedHL);
   eventmodelPlaced = new DefaultListModel();
   eventsListPlaced = new JList(eventmodelPlaced);
   eventsListPlaced.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            int index = eventsListPlaced.locationToIndex(e.getPoint());
            String selectedItem = (String) eventmodelPlaced.getElementAt(index);
            String [] number = selectedItem.split("/",0);
            parent.focusEvent(Integer.parseInt(number[0])-1);
            setMode(0);
        }
    });
  
   JScrollPane eventPlacedscrollPane = new JScrollPane();
   eventPlacedscrollPane.setViewportView(eventsListPlaced);
   eventsListPlaced.setLayoutOrientation(JList.VERTICAL);
   placedpanel.add(eventPlacedscrollPane);
   placedpanel.setBackground(new Color(0.8f,0.8f,0.8f));
   mainPanelGui.add(placedpanel);
  
   
   
   // Show new Events
   JPanel newpanel = new JPanel();
   newpanel.setLayout(new GridLayout(2,1));
   JLabel newHL = new JLabel("Neue Events",SwingConstants.CENTER);
   newpanel.add(newHL);
   
   eventmodelNew = new DefaultListModel();
   eventsListNew = new JList(eventmodelNew);
   eventsListNew.addMouseListener(new MouseAdapter() {
       public void mouseClicked(java.awt.event.MouseEvent e) {
            int index = eventsListNew.locationToIndex(e.getPoint());
            selectedItem = (String) eventmodelNew.getElementAt(index);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                changeEvent.setText(selectedItem); 
                changeEvent.setForeground(Color.BLACK);
              }
            });
        }   
   });
  
   JScrollPane eventNewscrollPane = new JScrollPane();
   eventNewscrollPane.setViewportView(eventsListNew);
   eventsListNew.setLayoutOrientation(JList.VERTICAL);
   newpanel.add(eventNewscrollPane);
   mainPanelGui.add(newpanel); 



   // Show pending Events
   JPanel pendingpanel = new JPanel();
   pendingpanel.setLayout(new GridLayout(2,1));
   JLabel pendingHL = new JLabel("Events in Basket",SwingConstants.CENTER);
   pendingpanel.add(pendingHL);
   
   eventmodelPending = new DefaultListModel();
   eventsListPending = new JList(eventmodelPending);
   eventsListPending.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            int index = eventsListPending.locationToIndex(e.getPoint());
            selectedItem = (String) eventmodelPending.getElementAt(index);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                changeEvent.setText(selectedItem); 
                changeEvent.setForeground(Color.BLACK);
              }
            });
        }
    });
  
   JScrollPane eventPendingscrollPane = new JScrollPane();
   eventPendingscrollPane.setViewportView(eventsListPending);
   eventsListPending.setLayoutOrientation(JList.VERTICAL);
   pendingpanel.add(eventPendingscrollPane);    
   mainPanelGui.add(pendingpanel);



   // Show deleted Events
   JPanel deletedpanel = new JPanel();
   deletedpanel.setLayout(new GridLayout(2,1));
   JLabel deletedHL = new JLabel("pausierte Events",SwingConstants.CENTER);
   deletedpanel.add(deletedHL);
   
   eventmodelDeleted = new DefaultListModel();
   eventsListDeleted = new JList(eventmodelDeleted);
   eventsListDeleted.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            int index = eventsListDeleted.locationToIndex(e.getPoint());
            selectedItem = (String) eventmodelDeleted.getElementAt(index);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                changeEvent.setText(selectedItem); 
                changeEvent.setForeground(Color.BLACK);
              }
            });
        }
    });
  
   JScrollPane eventDeletedscrollPane = new JScrollPane();
   eventDeletedscrollPane.setViewportView(eventsListDeleted);
   eventsListDeleted.setLayoutOrientation(JList.VERTICAL);
   deletedpanel.add(eventDeletedscrollPane); 
   mainPanelGui.add(deletedpanel);    
   
   JPanel extrapanel = new JPanel();
   extrapanel.setLayout(new GridLayout(2,1));

  //Button ScreenSaver
  JPanel buttonpanel = new JPanel();
  buttonpanel.setLayout(new GridLayout(4,1));
  
  
  // Checkbox Zoom
  
  JCheckBox check = new JCheckBox("Autozoom");
  check.setSelected(true);
  check.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        AbstractButton abstractButton = (AbstractButton) e.getSource();
        boolean selected = abstractButton.getModel().isSelected();
        parent.autozoom = selected;
       }
    });
    
  buttonpanel.add(check);
  
  JCheckBox check2 = new JCheckBox("PLENUM");
  check2.setSelected(true);
  check2.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        AbstractButton abstractButton = (AbstractButton) e.getSource();
        boolean selected = abstractButton.getModel().isSelected();
        //parent.plenum = selected;
       }
    });
    
   buttonpanel.add(check2);      
   extrapanel.add(buttonpanel);
   
   // Status Changer
   changeStatus = new JPanel();
   changeStatus.setLayout(new GridLayout(6,1));
 
   changeEvent = new JLabel();
   changeEvent.setText("select event to change"); 
   changeEvent.setForeground(Color.RED);
   newButton = new JButton("set New (newevents)");
   //Set object NEWEVENT
   newButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String content = changeEvent.getText();
        String [] number = content.split("/",0);
        if(number[0] != "select event to change"){
          parent.eventList.get(Integer.parseInt(number[0])-1).setStatus(Eventstatus.NEWEVENT);
          changeEvent.setText("select event to change"); 
          changeEvent.setForeground(Color.RED);
          updateGUI();
        }
      }
    });
    //SET object Pending
   pendingButton = new JButton("set Pending (basket)");
   pendingButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String content = changeEvent.getText();
        String [] number = content.split("/",0);
        if(number[0] != "select event to change"){
        parent.eventList.get(Integer.parseInt(number[0])-1).setStatus(Eventstatus.PENDING);   
        database.eventsTimestampMsql.query("UPDATE Event SET timestamp=%s WHERE vertex_id=%s", System.currentTimeMillis(), number[0]); 
        changeEvent.setText("select event to change"); 
        changeEvent.setForeground(Color.RED);
        updateGUI();
        }
      }
    });  
    //set object Paused
   pausedButton = new JButton("set Paused (deleted)");  
   pausedButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String content = changeEvent.getText();
        String [] number = content.split("/",0);
         if(number[0] != "select event to change"){
        parent.eventList.get(Integer.parseInt(number[0])-1).setStatus(Eventstatus.DELETED);
        changeEvent.setText("select event to change"); 
        changeEvent.setForeground(Color.RED);
        updateGUI();
         }
      }
    });  
   
   changeStatus.add(changeEvent);
   changeStatus.add(newButton);
   changeStatus.add(pendingButton);
   changeStatus.add(pausedButton);
   
   extrapanel.add(changeStatus);
   mainPanelGui.add(extrapanel);
   
   //change Mode Panel
   FlowLayout experimentLayout = new FlowLayout();
   switchModePanel.setLayout(experimentLayout);
   switchModePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
   
   JButton switchMode = new JButton("Switch to Events");
   switchMode.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
    	   timeline.modus = mode.CRISIS;
    	   topPanel2.remove(switchModePanel);
    	   topPanel.add(switchModePanel);
    	   guiframe.setContentPane(topPanel);
    	   guiframe.validate();
       }
     });
   
   switchModePanel.add(switchMode);

   JButton switchMode2 = new JButton("Switch to Wordcloud");
   switchMode2.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
    	   timeline.modus = mode.KEYWORDS;
    	   topPanel.remove(switchModePanel);
    	   topPanel2.add(switchModePanel);
    	   guiframe.setContentPane(topPanel2);
    	   guiframe.validate();
    	   
       }
     });
   switchModePanel.add(switchMode2);

   //Screensaverbutton
   JButton ScreenSaverButton = new JButton("Screensaver");
   ScreenSaverButton.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
         if(timeline.showlogo == false) timeline.showlogo = true;
         if(timeline.logoWZ.animatelogo == false) timeline.logoWZ.animatelogo = true;
       }
     });
   switchModePanel.add(ScreenSaverButton);

   //Button CLOSE
   GUIbutton = new JButton("CLOSE");
   switchModePanel.add(GUIbutton);  
  
   GUIbutton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        guiframe.setVisible(false);
        guiframe.dispose();
      }
    });  
   
   
   

   //bottom Panel for Keyowrds
   // select a wordcloud
   
   JPanel wordcloudpanel = new JPanel();
   wordcloudpanel.setLayout(experimentLayout);
   wordcloudpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
   JLabel wordcloudHL = new JLabel("select wordcloud to show",SwingConstants.CENTER);
   wordcloudpanel.add(wordcloudHL);
  
   keywordModel = new DefaultListModel();
   for(String mode : GUImode){
 	  keywordModel.addElement(mode);
   }
   keywordList = new JList(keywordModel);
   //keywordList.setSelectedIndex(0);
   keywordList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
           Integer selectedItem = keywordList.getSelectedIndex();
           timeline.currentCloudid = selectedItem;
       }
   });
   JScrollPane keywordPane = new JScrollPane();
   keywordPane.setViewportView(keywordList);
   keywordList.setLayoutOrientation(JList.VERTICAL);
   wordcloudpanel.add(keywordPane);
   wordcloudpanel.setBackground(new Color(0.8f,0.8f,0.8f));
   mainPanelGui2.add(wordcloudpanel);
   
   
   topPanel.add(mainPanelGui);
   topPanel.add(new JSeparator());
   topPanel.add(switchModePanel);
   
   topPanel2.add(mainPanelGui2);
   topPanel2.add(new JSeparator());
   //topPanel2.add(switchModePanel);
  
   
   guiframe.setContentPane(topPanel);
   
   guiframe.pack(); 
   guiframe.setVisible(false);   

	}

}
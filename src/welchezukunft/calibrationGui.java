package welchezukunft;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PApplet;
import processing.data.JSONObject;

public class calibrationGui {


	JFrame calframe;
	JPanel mainPanel;
	JPanel panel0,panel0b,panel1,panel1b,panel2,panel3;
	JButton button,button2,button3;
	
	JLabel anzeigen [];
	JLabel anzeigen2 [];
	JLabel anzeigen3 [];
	
	int ranger = 400;
	int windowwidth = 3840;
	int windowheight = 1080;
	
	int vals[], vals2[],vals3[] ;
	
	String label[] = {"LOX","LOY","ROX","ROY","RUX","RUY","LUX","LUY","LOX2","LOY2","ROX2","ROY2","RUX2","RUY2","LUX2","LUY2"};
	String label2[] = {"rotate","u1start","u1stop","v1start","v1stop","nonlinear1", "rotate2","u2start","u2stop","v2start","v2stop","nonlinear2"};
	String label3[] = {"shifty","shiftx"};
	
	int ranges[][];
	int ranges2[][];
	int ranges3[][];
	
	JSONObject json;
	
	PApplet parent;

	public calibrationGui(PApplet parent) {
		this.parent = parent;
		createarrays();
		loadVals();
		setslider(); 
		createCalibGui();
	}

	void loadVals(){  
		 json = parent.loadJSONObject("./resources/data/new.json");
		 vals[0] = json.getInt("LOX");
		 vals[1] = json.getInt("LOY");
		 vals[2] = json.getInt("ROX");
		 vals[3] = json.getInt("ROY");  
		 vals[4] = json.getInt("RUX");
		 vals[5] = json.getInt("RUY");
		 vals[6] = json.getInt("LUX");
		 vals[7] = json.getInt("LUY"); 
		 vals2[0] = json.getInt("rotate"); 
		 vals2[1] = json.getInt("u1start"); 
		 vals2[2] = json.getInt("u1stop");
		 vals2[3] = json.getInt("v1start"); 
		 vals2[4] = json.getInt("v1stop");
		 vals2[5] = json.getInt("nonlinear1");
		 
		 vals[8] = json.getInt("LOX2");
		 vals[9] = json.getInt("LOY2");
		 vals[10] = json.getInt("ROX2");
		 vals[11] = json.getInt("ROY2");  
		 vals[12] = json.getInt("RUX2");
		 vals[13] = json.getInt("RUY2");
		 vals[14] = json.getInt("LUX2");
		 vals[15] = json.getInt("LUY2"); 
		 vals2[6] = json.getInt("rotate2");  
		 vals2[7] = json.getInt("u2start"); 
		 vals2[8] = json.getInt("u2stop"); 
		 vals2[9] = json.getInt("v2start"); 
		 vals2[10] = json.getInt("v2stop");   
		 vals2[11] = json.getInt("nonlinear2");
		 
		 vals3[0] = json.getInt("shifty");
		 vals3[1] = json.getInt("shiftx"); 

		}

		void saver(){

		  json = new JSONObject();
		  for(int i = 0; i < label.length; i ++){
		    json.setInt(label[i],vals[i]);
		  }
		  for(int i = 0; i < label2.length; i ++){
		    json.setInt(label2[i],vals2[i]);
		  }
		  for(int i = 0; i < label3.length; i ++){
		    json.setInt(label3[i],vals3[i]);
		  }  

		  parent.saveJSONObject(json, "data/new.json");
		  System.out.println("SAVED !!");
		}
	
	
	
	void createarrays(){
	  anzeigen = new JLabel[label.length];
	  vals = new int[label.length];
	  anzeigen2 = new JLabel[label2.length];
	  vals2 = new int[label2.length]; 
	  anzeigen3 = new JLabel[label3.length];
	  vals3 = new int[label3.length];
	}

	void setslider(){
	
	  int midpointx = windowwidth/2;
	  
	  ranges = new int[label.length][2];
	  //LOX
	  ranges[0][0] = 0-ranger+1920;
	  ranges[0][1] = 0+ranger+1920;
	  //LOY
	  ranges[1][0] = 0-ranger;
	  ranges[1][1] = 0+ranger;
	  //ROX
	  ranges[2][0] = midpointx-ranger+1920;
	  ranges[2][1] = midpointx+ranger+1920;  
	  //ROY
	  ranges[3][0] = 0-ranger;
	  ranges[3][1] = 0+ranger;
	  //RUX
	  ranges[4][0] = midpointx-ranger+1920;
	  ranges[4][1] = midpointx+ranger+1920;
	  //RUY
	  ranges[5][0] = windowheight-ranger;
	  ranges[5][1] = windowheight+ranger;
	  //LUX
	  ranges[6][0] = 0-ranger+1920;
	  ranges[6][1] = 0+ranger+1920;  
	  //LUY
	  ranges[7][0] = windowheight-ranger;
	  ranges[7][1] = windowheight+ranger; 
	   //LOX2
	  ranges[8][0] = midpointx-ranger+1920;
	  ranges[8][1] = midpointx+ranger+1920;
	  //LOY2
	  ranges[9][0] = 0-ranger;
	  ranges[9][1] = 0+ranger;
	  //ROX2
	  ranges[10][0] = windowwidth-ranger+1920;
	  ranges[10][1] = windowwidth+ranger+1920;  
	  //ROY2
	  ranges[11][0] = 0-ranger;
	  ranges[11][1] = 0+ranger;
	  //RUX2
	  ranges[12][0] = windowwidth-ranger+1920;
	  ranges[12][1] = windowwidth+ranger+1920;
	  //RUY2
	  ranges[13][0] = windowheight-ranger;
	  ranges[13][1] = windowheight+ranger;
	  //LUX2
	  ranges[14][0] = midpointx-ranger+1920;
	  ranges[14][1] = midpointx+ranger+1920;  
	  //LUY2
	  ranges[15][0] = windowheight-ranger;
	  ranges[15][1] = windowheight+ranger;  
	  
	  // third value is divider for floats
	  ranges2 = new int[label.length][3];
	  
	  //rotate1
	  ranges2[0][0] = 160;
	  ranges2[0][1] = 200;
	  ranges2[0][2] = 1;
	  //startu1 + stopu1 + startv1 + stopv1
	  ranges2[1][0] = ranges2[2][0] = ranges2[3][0] = ranges2[4][0] = 0;
	  ranges2[1][1] = ranges2[2][1] = ranges2[3][1] = ranges2[4][1] = 1000;
	  ranges2[1][2] = ranges2[2][2] = ranges2[3][2] = ranges2[4][2] = 1000;
	  //nonlinear1
	  ranges2[5][0] = -1000;
	  ranges2[5][1] = 1000;
	  ranges2[5][2] = 1000;
	   //rotate1
	  ranges2[6][0] = -30;
	  ranges2[6][1] = 30;
	  ranges2[6][2] = 1;
	  //startu1 + stopu1 + startv1 + stopv1
	  ranges2[7][0] = ranges2[8][0] = ranges2[9][0] = ranges2[10][0] = 0;
	  ranges2[7][1] = ranges2[8][1] = ranges2[9][1] = ranges2[10][1] = 1000;
	  ranges2[7][2] = ranges2[8][2] = ranges2[9][2] = ranges2[10][2] = 1000;
	  //nonlinear1
	  ranges2[11][0] = -1000;
	  ranges2[11][1] = 1000;
	  ranges2[11][2] = 1000; 
	  
	  ranges3 = new int[label.length][2];
	  
	  //y shift
	  ranges3[0][0] = -50;
	  ranges3[0][1] = 50; 
	  //x shift
	  ranges3[1][0] = 1900;
	  ranges3[1][1] = 2000;
	
	}

	void createCalibGui(){
	  calframe = new JFrame("Calibration Zukunft");
	  calframe.setBounds(150, 50, 450, 500);
	  
	   Dimension d = new Dimension(800,800);
	   Container con = calframe.getContentPane();
	   con.setPreferredSize(d);
	 
	   mainPanel = new JPanel();
	   mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	 
	   panel0 = new JPanel();
	   panel0.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   panel0.setLayout(new GridLayout(2,label.length));
	   panel0.setPreferredSize(new Dimension(800,50));
	   panel0.setBackground(Color.LIGHT_GRAY);
	   
	   panel1 = new JPanel();
	   panel1.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   panel1.setLayout(new GridLayout(1,label.length));
	   panel1.setPreferredSize(new Dimension(800,290));
	 
	   panel0b = new JPanel();
	   panel0b.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   panel0b.setLayout(new GridLayout(2,label2.length));
	   panel0b.setPreferredSize(new Dimension(800,50));
	   panel0b.setBackground(Color.LIGHT_GRAY);
	   
	   panel1b = new JPanel();
	   panel1b.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   panel1b.setLayout(new GridLayout(1,label2.length));
	   panel1b.setPreferredSize(new Dimension(800,290));
	   
	   panel2 = new JPanel(); 
	   panel2.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   panel2.setLayout(new GridLayout(label3.length,8));
	   panel2.setPreferredSize(new Dimension(800,80));
	   
	   panel3 = new JPanel(); 
	   panel3.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   panel3.setLayout(new GridLayout(1,1));    
	   panel3.setPreferredSize(new Dimension(800,30));
	   
	   //row1: LABEL (POINT & DIRECTION)
	    for(int i = label.length-1; i>=0; i--) {
	        panel0.add(new JLabel(label[i]),SwingConstants.CENTER);
	    } 
	    //row1b: VALUE
	    for(int i =0; i < label.length; i++) {
	        anzeigen[i] = new JLabel(Integer.toString(vals[i]),SwingConstants.CENTER);
	        panel0.add(anzeigen[i]);
	    }  
	    //row2: SLIDER
	     for(int i =0; i < label.length; i++) {   
	        JSlider thisvalue = new JSlider(JSlider.VERTICAL, ranges[i][0], ranges[i][1], vals[i]);
	        thisvalue.setName(Integer.toString(i));
	        thisvalue.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                String name = ((JSlider)e.getSource()).getName();
	                vals[Integer.parseInt(name)] = ((JSlider)e.getSource()).getValue();
	                anzeigen[Integer.parseInt(name)].setText(Integer.toString(vals[Integer.parseInt(name)]));
	            }
	       
	        });
	     panel1.add(thisvalue);
	    }
	    
	    //row3: LABEL (EXTRA VALUE)
	    for(int i = label2.length-1; i>=0; i--) {
	        panel0b.add(new JLabel(label2[i]),SwingConstants.CENTER);
	    }  
	    //row3b: VALUE
	    for(int i =0; i < label2.length; i++) {
	        anzeigen2[i] = new JLabel(Integer.toString((int)((float)(vals2[i])/(float)(ranges2[i][2]))),SwingConstants.CENTER);
	        panel0b.add(anzeigen2[i]);
	    }   
	     for(int i =0; i < label2.length; i++) {   
	        JSlider thisvalue = new JSlider(JSlider.VERTICAL, ranges2[i][0], ranges2[i][1], vals2[i]);
	        thisvalue.setName(Integer.toString(i));
	        thisvalue.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                String name = ((JSlider)e.getSource()).getName();
	                vals2[Integer.parseInt(name)] = ((JSlider)e.getSource()).getValue();
	                anzeigen2[Integer.parseInt(name)].setText(Integer.toString((int)((float)(vals2[Integer.parseInt(name)])/(float)(ranges2[Integer.parseInt(name)][2]))));
	            }
	       
	        });
	     panel1b.add(thisvalue);
	    }
	   
	   for(int i = label3.length-1; i>=0; i--){
	     panel2.add(new JLabel(label3[i]));
	     anzeigen3[i] = new JLabel(Integer.toString(vals3[i]));
	     panel2.add(anzeigen3[i]);
	     JSlider slider = new JSlider(JSlider.HORIZONTAL, ranges3[i][0], ranges3[i][1], vals3[i]);
	     slider.setName(Integer.toString(i));
	     slider.addChangeListener(new ChangeListener() {
	              public void stateChanged(ChangeEvent e) {
	                  String name = ((JSlider)e.getSource()).getName();              
	                  vals3[Integer.parseInt(name)] = ((JSlider)e.getSource()).getValue();
	                  anzeigen3[Integer.parseInt(name)].setText(Integer.toString(vals3[Integer.parseInt(name)]));
	              }
	          });
	     panel2.add(slider);
	   }
	 
	  //Button CLOSE
	  button = new JButton("CLOSE");
	  panel3.add(button);  
	  
	  button.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
	    calframe.setVisible(false);
	    calframe.dispose();
	  }
	});
	  button2 = new JButton("SAVE");
	  panel3.add(button2);  
	  
	  button2.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
	    if(timeline.init == true){
	      //fixit(); 
	      saver();
	    }
	  }
	});
	  button3 = new JButton("CALIBRATE/WORK");
	  panel3.add(button3);  
	  
	  button3.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
	    if(timeline.init == true){
	      timeline.calibration = !timeline.calibration;
	      if(timeline.calibration == false){
	        //fixit(); 
	        }
	      System.out.println("show work / show calibrate");
	    }
	  }
	});
	  
	  //  panel.setLayout(null);
	  mainPanel.add(panel0);
	  mainPanel.add(panel1);
	  mainPanel.add(panel0b);
	  mainPanel.add(panel1b);  
	  mainPanel.add(panel2);
	  mainPanel.add(panel3);
	  calframe.add(mainPanel);
	  calframe.pack(); 
	  calframe.setVisible(true);
	  
	
	}

  /*
	void closing(){
	  frame.dispose();
	}
*/
}
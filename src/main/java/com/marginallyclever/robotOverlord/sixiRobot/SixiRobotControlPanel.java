package com.marginallyclever.robotOverlord.sixiRobot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.RobotOverlord;

public class SixiRobotControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private final double [] speedOptions = {0.01, 0.05,
											0.1, 0.5, 
			                                1, 5, 
			                                10, 50};

	
	private JButton fk5pos;
	private JButton fk5neg;
	private JButton fk4pos;
	private JButton fk4neg;
	private JButton fk3pos;
	private JButton fk3neg;
	private JButton fk2pos;
	private JButton fk2neg;
	private JButton fk1pos;
	private JButton fk1neg;
	private JButton fk0pos;
	private JButton fk0neg;
	
	private JButton ikXpos;
	private JButton ikXneg;
	private JButton ikYpos;
	private JButton ikYneg;
	private JButton ikZpos;
	private JButton ikZneg;
	private JButton ikUpos;
	private JButton ikUneg;
	private JButton ikVpos;
	private JButton ikVneg;
	private JButton ikWpos;
	private JButton ikWneg;
	
	public JLabel xPos,yPos,zPos,uPos,vPos,wPos;
	public JLabel angle5,angle4,angle3,angle2,angle1,angle0;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;

	private JButton showDebug;
	private JButton findHome;
	private JButton where;
	private JButton about;
	
	private SixiRobot robot=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public SixiRobotControlPanel(RobotOverlord gui,SixiRobot robot) {
		super();

		JPanel p;
		
		this.robot = robot;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);
		con1.gridy++;

		// used for fk 
		CollapsiblePanel fkPanel = new CollapsiblePanel("Forward Kinematics");
		this.add(fkPanel,con1);
		con1.gridy++;
		p = new JPanel(new GridLayout(6,3));
		fkPanel.getContentPane().add(p);
		
		p.add(fk5pos = createButton("A5+"));		p.add(angle5 = new JLabel("0.00"));		p.add(fk5neg = createButton("A5-"));
		p.add(fk4pos = createButton("A4+"));		p.add(angle4 = new JLabel("0.00"));		p.add(fk4neg = createButton("A4-"));
		p.add(fk3pos = createButton("A3+"));		p.add(angle3 = new JLabel("0.00"));		p.add(fk3neg = createButton("A3-"));
		p.add(fk2pos = createButton("A2+"));		p.add(angle2 = new JLabel("0.00"));		p.add(fk2neg = createButton("A2-"));
		p.add(fk1pos = createButton("A1+"));		p.add(angle1 = new JLabel("0.00"));		p.add(fk1neg = createButton("A1-"));
		p.add(fk0pos = createButton("A0+"));		p.add(angle0 = new JLabel("0.00"));		p.add(fk0neg = createButton("A0-"));
		
		// used for ik 
		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;
		p = new JPanel(new GridLayout(6,3));
		ikPanel.getContentPane().add(p);
		
		p.add(ikXpos = createButton("X+"));		p.add(xPos = new JLabel("0.00"));		p.add(ikXneg = createButton("X-"));
		p.add(ikYpos = createButton("Y+"));		p.add(yPos = new JLabel("0.00"));		p.add(ikYneg = createButton("Y-"));
		p.add(ikZpos = createButton("Z+"));		p.add(zPos = new JLabel("0.00"));		p.add(ikZneg = createButton("Z-"));
		p.add(ikUpos = createButton("U+"));		p.add(uPos = new JLabel("0.00"));		p.add(ikUneg = createButton("U-"));
		p.add(ikVpos = createButton("V+"));		p.add(vPos = new JLabel("0.00"));		p.add(ikVneg = createButton("V-"));
		p.add(ikWpos = createButton("W+"));		p.add(wPos = new JLabel("0.00"));		p.add(ikWneg = createButton("W-"));

		CollapsiblePanel miscPanel = new CollapsiblePanel("Misc");
		this.add(miscPanel, con1);
		con1.gridy++;
		p = new JPanel(new GridLayout(4,1));
		miscPanel.getContentPane().add(p);
		
		p.add(showDebug = createButton("Toggle debug view"));
		p.add(findHome = createButton("Find Home"));
		p.add(where = createButton("Where"));
		p.add(about = createButton("About this robot"));
	}
	
	protected CollapsiblePanel createSpeedPanel() {
		double speed=robot.getSpeed();
		int speedIndex;
		for(speedIndex=0;speedIndex<speedOptions.length;++speedIndex) {
			if( speedOptions[speedIndex] >= speed )
				break;
		}
		speedNow = new JLabel(Double.toString(speedOptions[speedIndex]),JLabel.CENTER);
		java.awt.Dimension dim = speedNow.getPreferredSize();
		dim.width = 50;
		speedNow.setPreferredSize(dim);

		CollapsiblePanel speedPanel = new CollapsiblePanel("Speed");
		
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;
		speedPanel.getContentPane().add(speedNow,con2);

		speedControl = new JSlider(0,speedOptions.length-1,speedIndex);
		speedControl.addChangeListener(this);
		speedControl.setMajorTickSpacing(speedOptions.length-1);
		speedControl.setMinorTickSpacing(1);
		speedControl.setPaintTicks(true);
		con2.anchor=GridBagConstraints.NORTHEAST;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.weightx=0.75;
		con2.gridx=1;
		speedPanel.getContentPane().add(speedControl,con2);
		
		return speedPanel;
	}

	protected void setSpeed(double speed) {
		robot.setSpeed(speed);
		speedNow.setText(Double.toString(robot.getSpeed()));
	}
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		if( subject == speedControl ) {
			int i=speedControl.getValue();
			setSpeed(speedOptions[i]);
		}
	}
	
	
	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == fk5pos ) robot.moveA(1);
		if( subject == fk5neg ) robot.moveA(-1);
		if( subject == fk4pos ) robot.moveB(1);
		if( subject == fk4neg ) robot.moveB(-1);
		if( subject == fk3pos ) robot.moveC(1);
		if( subject == fk3neg ) robot.moveC(-1);
		if( subject == fk2pos ) robot.moveD(1);
		if( subject == fk2neg ) robot.moveD(-1);
		if( subject == fk1pos ) robot.moveE(1);
		if( subject == fk1neg ) robot.moveE(-1);
		if( subject == fk0pos ) robot.moveF(1);
		if( subject == fk0neg ) robot.moveF(-1);
		
		if( subject == ikXpos ) robot.moveX(1);
		if( subject == ikXneg ) robot.moveX(-1);
		if( subject == ikYpos ) robot.moveY(1);
		if( subject == ikYneg ) robot.moveY(-1);
		if( subject == ikZpos ) robot.moveZ(1);
		if( subject == ikZneg ) robot.moveZ(-1);
		
		if( subject == ikUpos ) robot.moveU(1);
		if( subject == ikUneg ) robot.moveU(-1);
		if( subject == ikVpos ) robot.moveV(1);
		if( subject == ikVneg ) robot.moveV(-1);
		if( subject == ikWpos ) robot.moveW(1);
		if( subject == ikWneg ) robot.moveW(-1);
		
		if( subject == findHome ) robot.findHome();
		if( subject == showDebug ) robot.toggleDebug();
		if( subject == where ) doWhere();
		if( subject == about ) doAbout();
	}
	
	protected void doWhere() {
		robot.sendLineToRobot("M114");
	}
	
	protected void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(this.getRootPane(), "<html><body>"
				+"<h1>SIXI</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A six axis manipulator.</p><br>"
				+"</body></html>", "About "+this.robot.getDisplayName());
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
}

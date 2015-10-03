package com.marginallyclever.evilOverlord;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Vector3f;


/**
 * an object in the world that can have a gui interface
 * @author danroyer
 *
 */
public class ObjectInWorld implements ActionListener {
	protected List<ObjectInWorld> children;
	protected Vector3f position;
	protected String displayName;
	protected int pickName;
	
	// unique ids for all objects in the world.  zero is reserved to indicate no object.
	static protected int pickNameCounter=1;
	
	private CollapsiblePanel oiwPanel;
	protected JTextField fieldX,fieldY,fieldZ;
	
	public ObjectInWorld() {
		pickName = pickNameCounter++;
		children = new ArrayList<ObjectInWorld>();
		position = new Vector3f();
	}
	
	
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		
		oiwPanel = new CollapsiblePanel("Position");
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		JLabel x=new JLabel("X",JLabel.CENTER);
		fieldX = new JTextField(Float.toString(position.x));
		x.setLabelFor(fieldX);
		fieldX.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(x,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldX,con1);
		con1.gridy++;
		
		JLabel y=new JLabel("Y",JLabel.CENTER);
		fieldY = new JTextField(Float.toString(position.y));
		y.setLabelFor(fieldY);
		fieldY.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(y,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldY,con1);
		con1.gridy++;
		
		JLabel z=new JLabel("Z",JLabel.CENTER);
		fieldZ = new JTextField(Float.toString(position.z));
		z.setLabelFor(fieldZ);
		fieldZ.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(z,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldZ,con1);
		con1.gridy++;
		
		list.add(oiwPanel);

		return list;
	}
	
	public JPanel buildPanel() {
		JPanel sum = new JPanel();
		sum.setLayout(new BoxLayout(sum, BoxLayout.PAGE_AXIS));
		
		ArrayList<JPanel> list = getControlPanels();
		Iterator<JPanel> pi = list.iterator();
		while(pi.hasNext()) {
			JPanel p = pi.next();
			sum.add(p);
		}
		return sum;
	}
	
	
	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public int getPickName() {
		return pickName;
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if(source == fieldX) {
			try {
				float f = Float.parseFloat(fieldX.getText());
				this.position.x = f;
			} catch(NumberFormatException e) {}
		}
		
		if(source == fieldY) {
			try {
				float f = Float.parseFloat(fieldY.getText());
				this.position.y = f;
			} catch(NumberFormatException e) {}
		}
		
		if(source == fieldZ) {
			try {
				float f = Float.parseFloat(fieldZ.getText());
				this.position.z = f;
			} catch(NumberFormatException e) {}
		}
	}
}
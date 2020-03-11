package com.marginallyclever.robotOverlord.entity.gridEntity;

import java.util.ArrayList;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.EntityPanel;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

public class GridEntity extends PhysicalObject {
	public double width=100;
	public double height=100;
	
	protected transient GridEntityPanel gridEntityControlPanel;

	public GridEntity() {
		super();
		setName("Grid");
	}
	
	
	/**
	 * Get the {@link EntityPanel} for this class' superclass, then the EntityPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of EntityPanels 
	 */
	@Override
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		gridEntityControlPanel = new GridEntityPanel(gui,this);
		list.add(gridEntityControlPanel);

		return list;
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, this.pose);
		PrimitiveSolids.drawGrid(gl2,(int)width,(int)height,1);
		gl2.glPopMatrix();
	}
}

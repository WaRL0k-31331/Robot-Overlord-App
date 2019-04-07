package com.marginallyclever.robotOverlord.dhRobot;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A robot designed using D-H parameters.
 * @author Dan Royer
 *
 */
public abstract class DHRobot extends Robot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@value links} a list of DHLinks describing the kinematic chain.
	 */
	LinkedList<DHLink> links;
	
	/**
	 * {@value poseNow} keyframe describing the current pose of the kinematic chain.
	 */
	DHKeyframe poseNow;
	
	/**
	 * {@value panel} the GUI panel for controlling this robot.
	 */
	DHPanel panel;
	
	/**
	 * {@value endMatrix} the world frame pose of the last link in the kinematic chain.
	 */
	Matrix4d endMatrix;
	
	/**
	 * {@value drawSkeleton} true if the skeleton should be visualized on screen.  Default is false.
	 */
	boolean drawSkeleton;
	
	
	public DHRobot() {
		super();
		setDisplayName("DHRobot");
		links = new LinkedList<DHLink>();
		endMatrix = new Matrix4d();
		drawSkeleton=true;
		
		setupLinks();
		
		refreshPose();
	}
	
	/**
	 * Override this method with your robot's setup.
	 */
	public abstract void setupLinks();
	/**
	 * Override this method to return the correct solver for your type of robot.
	 * @return the IK solver for a specific type of robot.
	 */
	public abstract DHIKSolver getSolverIK();

	@Override
	public RobotKeyframe createKeyframe() {
		return new DHKeyframe();
	}

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		panel = new DHPanel(gui,this);
		list.add(panel);
		
		return list;
	}
	
	@Override
	public void render(GL2 gl2) {
		if(!drawSkeleton) return;

		boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);

		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					link.renderPose(gl2);
				}
			gl2.glPopMatrix();
			
			MatrixHelper.drawMatrix(gl2, 
					new Vector3d((float)endMatrix.m03,(float)endMatrix.m13,(float)endMatrix.m23),
					new Vector3d((float)endMatrix.m00,(float)endMatrix.m10,(float)endMatrix.m20),
					new Vector3d((float)endMatrix.m01,(float)endMatrix.m11,(float)endMatrix.m21),
					new Vector3d((float)endMatrix.m02,(float)endMatrix.m12,(float)endMatrix.m22)
					);

		gl2.glPopMatrix();
		if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
	}
	
	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find the end position.
	 */
	public void refreshPose() {
		endMatrix.setIdentity();
		
		Iterator<DHLink> i = links.iterator();
		while(i.hasNext()) {
			DHLink link = i.next();
			// update matrix
			link.refreshPoseMatrix();
			// find cumulative matrix
			endMatrix.mul(link.pose);
			link.poseCumulative.set(endMatrix);
		}
	}
	
	/**
	 * Adjust the number of links in this robot
	 * @param newSize must be greater than 0
	 */
	public void setNumLinks(int newSize) {
		if(newSize<1) newSize=1;
		
		int oldSize = links.size();
		while(oldSize>newSize) {
			oldSize--;
			links.pop();
		}
		while(oldSize<newSize) {
			oldSize++;
			links.push(new DHLink());
		}
	}

	/**
	 * Adjust the world transform of the robot
	 * @param pos the new world position for the local origin of the robot.
	 */
	@Override
	public void setPosition(Vector3d pos) {
		super.setPosition(pos);
		refreshPose();
		if(panel!=null) panel.updateEnd();
	}
}
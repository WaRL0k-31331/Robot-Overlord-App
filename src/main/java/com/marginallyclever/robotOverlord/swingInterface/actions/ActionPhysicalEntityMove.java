package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.scene.SceneEntity;

/**
 * An undoable command to make a physical entity move.
 *  
 * @author Dan Royer
 *
 */
public class ActionPhysicalEntityMove extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private SceneEntity entity;
	private Matrix4d next;
	private Matrix4d prev;
	
	/**
	 * 
	 * @param robot which machine
	 * @param axis index of axis
	 * @param direction 1 or -1
	 */
	public ActionPhysicalEntityMove(SceneEntity entity,Matrix4d newPose) {
		super();
		
		this.entity = entity;
		this.prev = entity.getPose();
		this.next = newPose;

		entity.setPose(next);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		entity.setPose(next);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.setPose(prev);
	}
	
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if(anEdit instanceof ActionPhysicalEntityMove) {
			ActionPhysicalEntityMove APEM = (ActionPhysicalEntityMove)anEdit;
			if(APEM.entity==this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}

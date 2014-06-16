/**
* Record the effect of adding an object instance for the undo
*
* 
*/

package org.lateralgm.util;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;

public class AddObjectInstance extends AbstractUndoableEdit
{

  private Instance instance;
  private int index;
  private Room room;

  public AddObjectInstance(Room room, Instance instance, int index)
  {
  	this.instance = instance;
  	this.index = index;
  	this.room = room;
  }

  public void undo() throws CannotUndoException
  {
  	room.instances.remove(index);
  }

  public void redo() throws CannotRedoException
  {
		room.instances.add(instance);
  }

  public boolean canUndo()
  {
     return true;
  }

  public boolean canRedo()
  {
     return true;
  }

}
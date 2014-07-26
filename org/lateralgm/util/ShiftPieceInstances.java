/**
* Record the effect of shifting piece (objects/tiles) instances for the undo
*
* Copyright (C) 2014, egofree
*
* This file is part of LateralGM.
* LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
* See LICENSE for details.
*/

package org.lateralgm.util;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.lateralgm.resources.Room;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;

public class ShiftPieceInstances extends AbstractUndoableEdit
	{
	private static final long serialVersionUID = 1L;

	private int horizontalShift;
	private int verticalShift;
	private boolean shiftTiles;
	private Room room;

	public ShiftPieceInstances(Room room, boolean shiftTiles, int horizontalShift, int verticalShift)
		{
		this.horizontalShift = horizontalShift;
		this.verticalShift = verticalShift;
		this.room = room;
		this.shiftTiles = shiftTiles;
		}

	@Override
	public void undo() throws CannotUndoException
		{
		// If we are shifting tiles
		if (shiftTiles)
			{

			for (Tile tile : room.tiles)
				{
				Point newPosition = new Point(tile.getPosition().x - horizontalShift,tile.getPosition().y
						- verticalShift);
				tile.setPosition(newPosition);
				}

			}
		else
			// Shift the objects
			{

			for (Instance instance : room.instances)
				{
				Point newPosition = new Point(instance.getPosition().x - horizontalShift,
						instance.getPosition().y - verticalShift);
				instance.setPosition(newPosition);
				}
			}
		}

	@Override
	public void redo() throws CannotRedoException
		{
		// If we are shifting tiles
		if (shiftTiles)
			{

			for (Tile tile : room.tiles)
				{
				Point newPosition = new Point(tile.getPosition().x + horizontalShift,tile.getPosition().y
						+ verticalShift);
				tile.setPosition(newPosition);
				}

			}
		else
			// Shift the objects
			{

			for (Instance instance : room.instances)
				{
				Point newPosition = new Point(instance.getPosition().x + horizontalShift,
						instance.getPosition().y + verticalShift);
				instance.setPosition(newPosition);
				}
			}
		}

	@Override
	public boolean canUndo()
		{
		return true;
		}

	@Override
	public boolean canRedo()
		{
		return true;
		}

	}
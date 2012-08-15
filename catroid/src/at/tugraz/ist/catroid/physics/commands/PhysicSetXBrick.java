/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010  Catroid development team 
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.physics.commands;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.content.bricks.SetXBrick;
import at.tugraz.ist.catroid.physics.PhysicWorld;

/**
 * @author robert
 * 
 */
public class PhysicSetXBrick implements Brick, OnClickListener {
	private static final long serialVersionUID = 1L;
	private transient final PhysicWorld world = ProjectManager.getInstance().getCurrentProject().getPhysicWorld();
	private final SetXBrick setXBrick;

	public PhysicSetXBrick(SetXBrick setXBrick) {
		this.setXBrick = setXBrick;
	}

	public void onClick(View view) {
		setXBrick.onClick(view);
	}

	@Override
	public Brick clone() {
		return setXBrick.clone();
	}

	public void execute() {
		world.getPhysicObject(this.getSprite()).setXPosition(setXBrick.getXPosition());
	}

	public Sprite getSprite() {
		return setXBrick.getSprite();
	}

	public View getView(Context context, int brickId, BaseAdapter adapter) {
		return setXBrick.getView(context, brickId, adapter);
	}

	public View getPrototypeView(Context context) {
		return setXBrick.getPrototypeView(context);
	}

	public int getRequiredResources() {
		return setXBrick.getRequiredResources();
	}

}
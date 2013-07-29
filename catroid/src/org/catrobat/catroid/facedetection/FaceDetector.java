/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.facedetection;

import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.common.ScreenValues;

import android.graphics.Point;

public abstract class FaceDetector {

	private List<OnFaceDetectedListener> faceDetectedListeners = new LinkedList<OnFaceDetectedListener>();
	private List<OnFaceDetectionStatusChangedListener> faceDetectionStatusListeners = new LinkedList<OnFaceDetectionStatusChangedListener>();

	private boolean faceDetected = false;

	public abstract void startFaceDetection();

	public abstract void stopFaceDetection();

	public void removeAllListeners() {
		faceDetectedListeners.clear();
		faceDetectionStatusListeners.clear();
		stopFaceDetection();
	}

	public void addOnFaceDetectedListener(OnFaceDetectedListener listener) {
		if (listener == null) {
			return;
		}
		faceDetectedListeners.add(listener);
		startFaceDetection();
	}

	public void removeOnFaceDetectedListener(OnFaceDetectedListener listener) {
		faceDetectedListeners.remove(listener);
		contitionalStop();
	}

	public void addOnFaceDetectionStatusListener(OnFaceDetectionStatusChangedListener listener) {
		if (listener == null) {
			return;
		}
		faceDetectionStatusListeners.add(listener);
		startFaceDetection();
	}

	public void removeOnFaceDetectionStatusListener(OnFaceDetectionStatusChangedListener listener) {
		faceDetectionStatusListeners.remove(listener);
		contitionalStop();
	}

	protected void onFaceDetected(Point position, int size) {
		for (OnFaceDetectedListener faceDetectedListener : faceDetectedListeners) {
			faceDetectedListener.onFaceDetected(position, size);
		}
	}

	protected void onFaceDetected(boolean faceDetected) {
		if (this.faceDetected != faceDetected) {
			this.faceDetected = faceDetected;
			for (OnFaceDetectionStatusChangedListener listener : faceDetectionStatusListeners) {
				listener.onFaceDetectionStatusChanged(faceDetected);
			}
		}
	}

	public boolean isRunning() {
		if (faceDetectedListeners.size() > 0) {
			return true;
		}
		if (faceDetectionStatusListeners.size() > 0) {
			return true;
		}
		return false;
	}

	private void contitionalStop() {
		if (isRunning()) {
			return;
		}
		stopFaceDetection();
	}

	protected Point getRelationForFacePosition() {
		return new Point(-ScreenValues.SCREEN_WIDTH, -ScreenValues.SCREEN_HEIGHT);
	}
}

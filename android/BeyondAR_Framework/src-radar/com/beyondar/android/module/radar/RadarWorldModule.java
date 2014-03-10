/*
 * Copyright (C) 2013 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* This code is based on Yasir.Ali <ali.yasir0@gmail.com> work. More on
 *  https://github.com/yasiralijaved/GenRadar
 */
package com.beyondar.android.module.radar;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.beyondar.android.module.WorldModule;
import com.beyondar.android.sensor.BeyondarSensorListener;
import com.beyondar.android.sensor.BeyondarSensorManager;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

public class RadarWorldModule implements WorldModule, BeyondarSensorListener {

	private World mWorld;
	private RadarView mRadarView;

	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];
	private float currentDegree = 0;

	private double mMaxDistance = -1;

	public RadarWorldModule() {
	}

	World getWorld() {
		return mWorld;
	}

	@Override
	public void onDetached() {
		BeyondarSensorManager.unregisterSensorListener(this);
	}

	@Override
	public boolean isAttached() {
		return false;
	}

	public void setup(World world) {
		mWorld = world;
		if (mMaxDistance == -1) {
			mMaxDistance = mWorld.getArViewDistance();
		}

		addModuleToAllObjects();

		BeyondarSensorManager.registerSensorListener(this);

	}

	private void addModuleToAllObjects() {
		ArrayList<BeyondarObjectList> beyondARLists = mWorld.getBeyondarObjectLists();
		for (BeyondarObjectList list : beyondARLists) {
			for (BeyondarObject beyondarObject : list) {
				addRadarPointModule(beyondarObject);
			}
		}
	}

	/**
	 * This method adds the {@link RadarPointModule} to the {@link GeoObject}
	 * 
	 * @param beyondarObject
	 */
	protected void addRadarPointModule(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObject) {
			if (!beyondarObject.containsAnyModule(RadarPointModule.class)) {
				RadarPointModule module = new RadarPointModule(this, beyondarObject);
				beyondarObject.addModule(module);
			}
		}
	}

	public void setRadarView(RadarView radarView) {
		mRadarView = radarView;
		mRadarView.setRadarModule(this);
	}

	@Override
	public void onBeyondarObjectAdded(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
		addRadarPointModule(beyondarObject);
	}

	@Override
	public void onBeyondarObjectRemoved(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {

	}

	@Override
	public void onBeyondarObjectListCreated(BeyondarObjectList beyondarObjectList) {

	}

	@Override
	public void onWorldCleaned() {
		// mRadarPoints.clear();
	}

	@Override
	public void onGeoPositionChanged(double latitude, double longitude, double altitude) {
	}

	@Override
	public void onDefaultImageChanged(String uri) {
	}

	@Override
	public void onSensorChanged(float[] filteredValues, SensorEvent event) {
		if (mRadarView == null)
			return;
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			mLastAccelerometer = filteredValues;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mLastMagnetometer = filteredValues;
			break;
		}
		if (mLastAccelerometer == null || mLastMagnetometer == null)
			return;
		
		boolean success = SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
		SensorManager.getOrientation(mR, mOrientation);
		if (success)
			rotateView((float) Math.toDegrees(mOrientation[0]));

	}

	/**
	 * Set the max distance rendered by the view in meters
	 * 
	 * @param maxDistance
	 */
	public void setMaxDistance(double maxDistance) {
		mMaxDistance = maxDistance;
	}

	/**
	 * Get the max distance rendered by the view in meters
	 * 
	 * @return max distance to render the {@link GeoObject}s
	 */
	public double getMaxDistance() {
		return mMaxDistance;
	}

	private void rotateView(float degree) {
		// create a rotation animation (reverse turn degree degrees)
		RotateAnimation animation = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

		// how long the animation will take place
		animation.setDuration(210);

		// set the animation after the end of the reservation status
		animation.setFillAfter(true);

		// Start the animation
		mRadarView.startAnimation(animation);
		currentDegree = -degree;
	}

}

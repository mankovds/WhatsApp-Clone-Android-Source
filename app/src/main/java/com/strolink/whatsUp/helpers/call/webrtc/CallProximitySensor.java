package com.strolink.whatsUp.helpers.call.webrtc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.util.Log;


public class CallProximitySensor implements SensorEventListener {
    private boolean lastStateReportIsNear = false;
    private final NonThreadSafe nonThreadSafe = new NonThreadSafe();
    private final Runnable onSensorStateListener;
    private Sensor proximitySensor = null;
    private final SensorManager sensorManager;

    static CallProximitySensor create(Context context, Runnable sensorStateListener) {
        return new CallProximitySensor(context, sensorStateListener);
    }

    private CallProximitySensor(Context context, Runnable sensorStateListener) {
        this.onSensorStateListener = sensorStateListener;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }


    public boolean start() {
        checkIfCalledOnValidThread();
        if (!initDefaultSensor()) {
            return false;
        }
        this.sensorManager.registerListener(this, this.proximitySensor, 3);
        return true;
    }

    public void stop() {
        checkIfCalledOnValidThread();
        if (this.proximitySensor != null) {
            this.sensorManager.unregisterListener(this, this.proximitySensor);
        }
    }

    public boolean sensorReportsNearState() {
        checkIfCalledOnValidThread();
        return this.lastStateReportIsNear;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        checkIfCalledOnValidThread();
        assertIsTrue(sensor.getType() == 8);
        if (accuracy == 0) {
            Log.e("AppRTCProximitySensor", "The values returned by this sensor cannot be trusted");
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        boolean z;
        checkIfCalledOnValidThread();
        if (event.sensor.getType() == 8) {
            z = true;
        } else {
            z = false;
        }
        assertIsTrue(z);
        if (event.values[0] < this.proximitySensor.getMaximumRange()) {
            this.lastStateReportIsNear = true;
        } else {
            this.lastStateReportIsNear = false;
        }
        if (this.onSensorStateListener != null) {
            this.onSensorStateListener.run();
        }
    }

    private boolean initDefaultSensor() {
        if (this.proximitySensor != null) {
            return true;
        }
        this.proximitySensor = this.sensorManager.getDefaultSensor(8);
        if (this.proximitySensor == null) {
            return false;
        }
        logProximitySensorInfo();
        return true;
    }

    private void logProximitySensorInfo() {
        if (this.proximitySensor != null) {
            StringBuilder info = new StringBuilder("Proximity sensor: ");
            info.append("name=" + this.proximitySensor.getName());
            info.append(", vendor: " + this.proximitySensor.getVendor());
            info.append(", power: " + this.proximitySensor.getPower());
            info.append(", resolution: " + this.proximitySensor.getResolution());
            info.append(", max range: " + this.proximitySensor.getMaximumRange());
            if (VERSION.SDK_INT >= 9) {
                info.append(", min delay: " + this.proximitySensor.getMinDelay());
            }
            if (VERSION.SDK_INT >= 20) {
                info.append(", type: " + this.proximitySensor.getStringType());
            }
            if (VERSION.SDK_INT >= 21) {
                info.append(", max delay: " + this.proximitySensor.getMaxDelay());
                info.append(", reporting mode: " + this.proximitySensor.getReportingMode());
                info.append(", isWakeUpSensor: " + this.proximitySensor.isWakeUpSensor());
            }
        }
    }

    private void checkIfCalledOnValidThread() {
        if (!this.nonThreadSafe.calledOnValidThread()) {
            throw new IllegalStateException("Method is not called on valid thread");
        }
    }


    public class NonThreadSafe {
        private final Long threadId = Thread.currentThread().getId();

        public boolean calledOnValidThread() {
            return this.threadId.equals(Thread.currentThread().getId());
        }
    }


    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }
}

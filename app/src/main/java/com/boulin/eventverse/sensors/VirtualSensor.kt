package com.boulin.eventverse.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

typealias ValueChangedCallback<T> = (value: T) -> Unit

abstract class VirtualSensor<T> : SensorEventListener{

    private var _callback: ValueChangedCallback<T>? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    abstract fun register(context: Context);

    abstract fun unregister(context: Context);

    fun onValueChanged(callback: ValueChangedCallback<T>){
        _callback = callback
    }

    protected fun fireEvent(value: T){
        _callback?.also { callback ->
            callback.invoke(value)
        }
    }
}

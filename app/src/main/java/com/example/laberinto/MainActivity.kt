package com.example.laberinto

import android.content.Context
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_GYROSCOPE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var canvas: Canvas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.canvas = findViewById(R.id.canvasview)

        val adminSensor: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        adminSensor.getSensorList(Sensor.TYPE_GYROSCOPE).first()?.let { sensor->
            adminSensor.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        adminSensor.getSensorList(TYPE_ACCELEROMETER)?.first()?.let { sensor->
            adminSensor.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.run {
            when(sensor.type){
                TYPE_ACCELEROMETER -> {
                    canvas.moveBall(PointF(sensorEvent.values[0], sensorEvent.values[1]))
                    acelerometro_x.text = "X: ${sensorEvent.values[0]}"
                    acelerometro_y.text = "Y: ${sensorEvent.values[1]}"
                    acelerometro_z.text = "Z: ${sensorEvent.values[2]}"
                }
                TYPE_GYROSCOPE -> {
                    //canvas.moveBall(PointF(sensorEvent.values[1]*1, sensorEvent.values[0]*1))
                    giroscopio_x.text = "X: ${sensorEvent.values[0]}"
                    giroscopio_y.text = "Y: ${sensorEvent.values[1]}"
                    giroscopio_z.text = "Z: ${sensorEvent.values[2]}"
                }
                else -> {}
            }
        }
    }
}
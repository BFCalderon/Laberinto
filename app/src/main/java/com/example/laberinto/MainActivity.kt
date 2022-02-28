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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.acelerometroBT
import kotlinx.android.synthetic.main.activity_main.acelerometro_x
import kotlinx.android.synthetic.main.activity_main.acelerometro_y
import kotlinx.android.synthetic.main.activity_main.acelerometro_z
import kotlinx.android.synthetic.main.activity_main.bottomId
import kotlinx.android.synthetic.main.activity_main.controlsButton
import kotlinx.android.synthetic.main.activity_main.giroscopio_x
import kotlinx.android.synthetic.main.activity_main.giroscopio_y
import kotlinx.android.synthetic.main.activity_main.giroscopio_z
import kotlinx.android.synthetic.main.activity_main.leftId
import kotlinx.android.synthetic.main.activity_main.mainControlContainer
import kotlinx.android.synthetic.main.activity_main.rightId
import kotlinx.android.synthetic.main.activity_main.topId

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var canvas: Canvas
    private var lockSensors = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.canvas = findViewById(R.id.canvasview)

        val adminSensor: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        adminSensor.getSensorList(TYPE_GYROSCOPE).first()?.let { sensor->
            adminSensor.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        adminSensor.getSensorList(TYPE_ACCELEROMETER)?.first()?.let { sensor->
            adminSensor.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        rightId.setOnClickListener {
            canvas.moveBall(PointF(-40f,0f))
        }
        bottomId.setOnClickListener {
            canvas.moveBall(PointF(0f,40f))
        }
        leftId.setOnClickListener {
            canvas.moveBall(PointF(40f,0f))
        }
        topId.setOnClickListener {
            canvas.moveBall(PointF(0f,-40f))
        }
        acelerometroBT.setOnClickListener {
            mainControlContainer.visibility = View.GONE
            lockSensors = false
        }
        controlsButton.setOnClickListener {
            mainControlContainer.visibility = View.VISIBLE
            lockSensors = true
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.run {
            if(!lockSensors) {
                when(sensor.type) {
                    TYPE_ACCELEROMETER -> {
                        canvas.moveBall(PointF(sensorEvent.values[0]*5, sensorEvent.values[1]*5))
                        acelerometro_x.text = "X: ${sensorEvent.values[0]}"
                        acelerometro_y.text = "Y: ${sensorEvent.values[1]}"
                        acelerometro_z.text = "Z: ${sensorEvent.values[2]}"
                    }
                    TYPE_GYROSCOPE -> {
                        canvas.moveBall(PointF(-sensorEvent.values[1]*10, sensorEvent.values[0]*10))
                        giroscopio_x.text = "X: ${sensorEvent.values[0]}"
                        giroscopio_y.text = "Y: ${sensorEvent.values[1]}"
                        giroscopio_z.text = "Z: ${sensorEvent.values[2]}"
                    }
                    else -> {}
                }
            }
        }
    }
}
package com.example.laberinto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import kotlin.math.pow
import kotlin.math.sqrt

class Canvas(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var ballPosition: PointF? = null
    private lateinit var colorPoints: ArrayList<PointF>
    private val colors = listOf(Color.WHITE, Color.BLUE, Color.GREEN, Color.CYAN, Color.YELLOW, Color.DKGRAY, Color.BLACK)
    private lateinit var rana: Bitmap
    private lateinit var argolla: Bitmap
    var score = 0
    private var lastIndex = -1

    init {
        setBackgroundColor(Color.WHITE)
        this.waitForLayout {
            ballPosition = PointF(width/2f, height*0.9f)
            colorPoints = arrayListOf(
                PointF(75f, 500f),
                PointF(width/2f, height*0.4f),
                PointF(width/2f, height*0.3f),
                PointF(width/2f, height*0.2f)
            )
            rana = getScaleBitmapFromResourcesPNGAndJPG(R.drawable.rana, 100f)
            argolla = getScaleBitmapFromResourcesPNGAndJPG(R.drawable.argolla, 100f)
        }
    }

    /**
     * Funcion que crea un bitmap a partir de una imagen
     */
    private fun getScaleBitmapFromResourcesPNGAndJPG(imageResource: Int, widthInMeters: Float): Bitmap {
        val bitmap = BitmapFactory.decodeResource(resources, imageResource)
        val factorScale = bitmap!!.width.toFloat()/bitmap.height.toFloat()
        val scaledWidth = widthInMeters*factorScale
        return Bitmap.createScaledBitmap(bitmap, scaledWidth.toInt(), widthInMeters.toInt(), false)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            colorPoints.forEachIndexed { index, pointF ->
                if(index == 0){
                    drawCircle(pointF.x, pointF.y, 50f, Paint().apply { color = colors[index] })
                } else {
                    drawBitmap(rana, pointF.x - 50, pointF.y - 50, null)
                }
                if(index == 0) {
                    drawText("RESET", pointF.x + 70, 515f, Paint().apply {
                        textSize = 50f
                        color = Color.WHITE
                    })
                }
            }
            ballPosition?.let {
                drawBitmap(argolla, it.x-50, it.y-50,null)
            }

            drawRoundRect(RectF(50f,250f,500f,350f),20f,20f, Paint().apply {
                color = Color.BLACK
                alpha = 200
            })
            drawText("PUNTAJE: $score", 70f, 315f, Paint().apply {
                textSize = 50f
                color = Color.WHITE
            })
        }
    }

    fun moveBall(factorPosition: PointF) {
        ballPosition?.let {ballPosition->
            width.let { ancho ->
                ballPosition.x -= factorPosition.x
                when {
                    (ballPosition.x > ancho) -> ballPosition.x = ancho.toFloat()
                    (ballPosition.x < 0) -> ballPosition.x = 0f
                }
            }
            height.let { alto ->
                ballPosition.y += factorPosition.y
                when {
                    (ballPosition.y > alto) -> ballPosition.y = alto.toFloat()
                    (ballPosition.y < height*0.1f) -> ballPosition.y = height*0.1f
                }
            }
            for (color in colorPoints) {
                if (isSelected(color, ballPosition)) {
                    setBackgroundColor(colors[colorPoints.indexOf(color)])
                    val currentIndex = colorPoints.indexOf(color)
                    if(lastIndex != currentIndex){
                        score += currentIndex
                        lastIndex = currentIndex
                    }
                    if(lastIndex == 0) {
                        score = 0
                    }
                }
            }
            invalidate()
        }
    }

    private inline fun View.waitForLayout(crossinline notifyLayoutCreated: () -> Unit) = with(viewTreeObserver) {
        addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                notifyLayoutCreated()
            }
        })
    }

    private fun isSelected(touchCoor: PointF, referencePoint: PointF, radio: Float = 100f) =
        sqrt((referencePoint.x - touchCoor.x).pow(2)+(referencePoint.y - touchCoor.y).pow(2)) <= radio

}
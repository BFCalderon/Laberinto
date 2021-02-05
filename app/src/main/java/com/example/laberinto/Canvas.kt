package com.example.laberinto

import android.content.Context
import android.graphics.*
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewTreeObserver
import kotlin.math.*

class Canvas(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var ballPosition: PointF? = null
    private val paint = Paint().apply { color = Color.RED }
    private val paintPath = Paint().apply { strokeWidth = 10f; style = Paint.Style.STROKE; color = Color.BLACK }

    private var lastCoor: PointF = PointF()
    private val labyrinthPath = Path()
    private var sidePaths = listOf<Path>()
    private var colorPoints = arrayListOf<PointF>()
    private val colors = listOf(Color.MAGENTA, Color.BLUE, Color.YELLOW, Color.CYAN, Color.GREEN, Color.DKGRAY, Color.BLACK)
    private val colorsPaint = colors.map { Paint().apply { color = it } }
    private var samplePoints = arrayListOf<PointF>()
    private val transparentPaint = Paint().apply { color = Color.HSVToColor(50, floatArrayOf(150f, 150f, 150f)) }

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            colorPoints.forEachIndexed { index, pointF ->
                drawCircle(pointF.x, pointF.y, 50f, colorsPaint[index])
            }
            //samplePoints.map { drawCircle(it.x, it.y, 25f, transparentPaint) }

            ballPosition?.let {
                drawCircle(it.x, it.y, 25f, paint)
            }

            if(sidePaths.isEmpty()){
                drawPath(labyrinthPath, paintPath)
            } else {
                sidePaths.map {
                    drawPath(it, paintPath)
                }
            }
        }
    }

    private var lastBallPosition = PointF()

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
                    (ballPosition.y < 0) -> ballPosition.y = 0f
                }
            }
            for (color in colorPoints) {
                if (isSelected(color, ballPosition)) {
                    setBackgroundColor(colors[colorPoints.indexOf(color)])
                }
            }
            var isInside = false
            for (samplePoint in samplePoints) {
                if (isSelected(samplePoint, ballPosition, 25f)) {
                    isInside = true
                    break
                }
            }
            if (!isInside) {
                this.ballPosition = PointF(lastBallPosition.x, lastBallPosition.y)
            } else {
                lastBallPosition = PointF(ballPosition.x, ballPosition.y)
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

    /**
     * Función que agrega un traso al path
     */
    private fun setNewPathPoint(coordinate: PointF){
        val dx = kotlin.math.abs(coordinate.x - lastCoor.x)
        val dy = kotlin.math.abs(coordinate.y - lastCoor.y)
        val tolerance = 5
        if(dx >= tolerance || dy >= tolerance){
            labyrinthPath.quadTo(this.lastCoor.x, this.lastCoor.y, (coordinate.x + lastCoor.x)/2,
                (coordinate.y + lastCoor.y)/2)
            this.lastCoor = coordinate
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(samplePoints.size == 0) {
            event?.run {
                when (action) {
                    ACTION_DOWN -> {
                        labyrinthPath.reset()
                        sidePaths = listOf()
                        colorPoints = arrayListOf()
                        labyrinthPath.moveTo(x, y)
                        lastCoor = PointF(x, y)
                        samplePoints = arrayListOf()
                        setBackgroundColor(Color.WHITE)
                    }
                    ACTION_MOVE -> {
                        setNewPathPoint(PointF(x, y))
                        lastCoor = PointF(x, y)
                    }
                    ACTION_UP -> {
                        sidePaths = getSidesRoadLines(labyrinthPath)
                        getColorsPosition(labyrinthPath, 5)
                        getSamplePoints(labyrinthPath, 25)
                        samplePoints.first().let {
                            ballPosition = PointF(it.x, it.y)
                            lastBallPosition = PointF(it.x, it.y)
                        }
                    }
                    else -> {
                    }
                }
            }
        }
        return true
    }

    /**
     * Funcion que obtiene las lineas continuas de los lados de las vias
     * @param elementalRoadPath path elemental de la vía
     */
    private fun getSidesRoadLines(elementalRoadPath: Path): List<Path>{
        val elementalRoadPathMeasure = PathMeasure(elementalRoadPath, false)
        var distance = 0f
        val sumFactor = 1f
        val slope = FloatArray(2)
        val position = FloatArray(2)
        val halfRoadMeasure = 50f
        val rightEdgePath = Path()
        val leftEdgePath = Path()
        var currentDisplacement = 0f
        var lastRight = PointF()
        var lastLeft = PointF()

        var isFirstTime = true
        while (distance < elementalRoadPathMeasure.length) {//Agrega los puntos de seleccion
            elementalRoadPathMeasure.getPosTan(distance, position, slope)
            val offset = getOffsetSignal(
                true,
                halfRoadMeasure,
                atan2(slope[0], slope[1])
            )
            if (isFirstTime) {//Para mover el path al punto inicial
                lastRight = PointF(position[0] + offset.x, position[1] - offset.y)
                lastLeft = PointF(position[0] - offset.x, position[1] + offset.y)
                rightEdgePath.moveTo(position[0] + offset.x, position[1] - offset.y)
                leftEdgePath.moveTo(position[0] - offset.x, position[1] + offset.y)
                isFirstTime = false
                currentDisplacement += sumFactor
                distance += sumFactor
                continue
            }
            rightEdgePath.lineTo(position[0] + offset.x, position[1] - offset.y)
            //setNewPathPoint(lastRight, PointF(position[0] + offset.x, position[1] - offset.y), rightEdgePath)
            //setNewPathPoint(lastLeft, PointF(position[0] - offset.x, position[1] + offset.y), leftEdgePath)
            leftEdgePath.lineTo(position[0] - offset.x, position[1] + offset.y)
            lastRight = PointF(position[0] + offset.x, position[1] - offset.y)
            lastLeft = PointF(position[0] - offset.x, position[1] + offset.y)
            distance += sumFactor
        }
        return listOf(leftEdgePath, rightEdgePath)
    }

    /**
     * Funcion que obtiene el offset mediante el cual se le hará el corrimiento al lado de la vida de la señal
     */
    private fun getOffsetSignal(
        isRight: Boolean,
        separation: Float,
        currentAngle: Float
    ): PointF {
        val invert = when (isRight) {
            true -> -1
            false -> 1
        }
        val factorX = invert * separation * cos(currentAngle)
        val factorY = invert * separation * sin(currentAngle)
        return PointF(factorX, factorY)
    }

    /**
     * Funcion que valida si un elemento fue seleccionado
     */
    private fun isSelected(touchCoor: PointF, referencePoint: PointF, radio: Float = 75f) =
        magnitude(moveToZero(touchCoor, referencePoint)) <= radio

    /**
     * Halla la magnitud de cada una de las coordenadas con respecto al origen
     */
    private fun magnitude(coordinate: PointF) =
        sqrt(coordinate.x.pow(2) + coordinate.y.pow(2))

    /**
     * Mueve dos puntos a una posicion (0, 0)
     */
    private fun moveToZero(initialPoint : PointF, finalPoint : PointF) : PointF {
        return PointF(finalPoint.x - initialPoint.x,finalPoint.y - initialPoint.y)
    }

    /**
     * Función que obtiene los puntos donde se posicionaran los puntos de colores
     */
    private fun getColorsPosition(path: Path, pointsNumber: Int){
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length
        val sumFactor = pathLength / pointsNumber
        var position = sumFactor
        val positionPoint = FloatArray(2)
        this.colorPoints.clear()
        while (position <= pathLength){
            pathMeasure.getPosTan(position, positionPoint, null)
            this.colorPoints.add(PointF(positionPoint[0], positionPoint[1]))
            position += sumFactor
        }
    }

    /**
     * Obtiene una muestra de puntos a lo largo del path principal para guiar la bola dentro del laberinto
     */
    private fun getSamplePoints(path: Path, ballRadius: Int){
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length
        var position = 0f
        val positionPoint = FloatArray(2)
        this.samplePoints.clear()
        while (position <= pathLength){
            pathMeasure.getPosTan(position, positionPoint, null)
            this.samplePoints.add(PointF(positionPoint[0], positionPoint[1]))
            position += ballRadius
        }
    }

}
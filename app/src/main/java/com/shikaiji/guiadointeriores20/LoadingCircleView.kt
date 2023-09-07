package com.shikaiji.guiadointeriores20
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LoadingCircleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint: Paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
    }

    private var currentAngle = 0f
    private val sweepAngle = 300f
    private val rotationSpeed = 8

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 3f

        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            currentAngle,
            sweepAngle,
            false,
            paint
        )
    }

    fun startLoadingAnimation() {
        // Start the animation by invalidating the view at a fixed interval
        // and incrementing the current angle to create the loading effect.
        post(object : Runnable {
            override fun run() {
                currentAngle += rotationSpeed
                if (currentAngle > 360f) {
                    currentAngle -= 360f
                }
                invalidate()
                postDelayed(this, 20)
            }
        })
    }
    fun holla() {
        val a=10
    }

    companion object
}
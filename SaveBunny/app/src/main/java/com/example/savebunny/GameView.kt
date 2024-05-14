package com.example.savebunny

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.NonNull
import java.util.*
import kotlin.collections.ArrayList

class GameView : View {
    private var background: Bitmap? = null
    private var ground: Bitmap? = null
    private var rabbit: Bitmap? = null
    private lateinit var rectBackground: Rect
    private lateinit var rectGround: Rect
    private var context: Context? = null
    private var handler: Handler? = null
    private val UPDATE_MILLIS: Long = 30
    private lateinit var runnable: Runnable
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val TEXT_SIZE = 120f
    private var points = 0
    private var life = 3
    companion object {
        var dWidth = 0
    }
    private var dHeight = 0
    private val random = Random()
    private var rabbitX = 0f
    private var rabbitY = 0f
    private var oldX = 0f
    private var oldRabbitX = 0f
    private val spikes = ArrayList<Spike>()
    private val explosions = ArrayList<Explosion>()

    constructor(context: Context) : super(context) {
        this.context = context
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.context = context
        initialize()
    }

    private fun initialize() {
        background = BitmapFactory.decodeResource(resources, R.drawable.backgroundimg)
        ground = BitmapFactory.decodeResource(resources, R.drawable.sandmid)
        rabbit = BitmapFactory.decodeResource(resources, R.drawable.rabbit)
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        rectBackground = Rect(0, 0, dWidth, dHeight)
        rectGround = Rect(0, dHeight - ground!!.height, dWidth, dHeight)
        handler = Handler()
        runnable = Runnable { invalidate() }
        textPaint.color = Color.rgb(255, 165, 0)
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        healthPaint.color = Color.GREEN
        rabbitX = (dWidth / 2 - rabbit!!.width / 2).toFloat()
        rabbitY = (dHeight - ground!!.height - rabbit!!.height).toFloat()
        for (i in 0..2) {
            val spike = Spike(context!!)
            spikes.add(spike)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(background!!, null, rectBackground, null)
        canvas.drawBitmap(ground!!, null, rectGround, null)
        canvas.drawBitmap(rabbit!!, rabbitX, rabbitY, null)

        for (i in spikes.indices) {
            val bitmap = spikes[i].getSpike(spikes[i].spikeFrame) ?: continue
            canvas.drawBitmap(bitmap, spikes[i].spikeX.toFloat(), spikes[i].spikeY.toFloat(), null)

            spikes[i].spikeFrame++
            if (spikes[i].spikeFrame > 2) {
                spikes[i].spikeFrame = 0
            }
            spikes[i].spikeY += spikes[i].spikeVelocity
            if (spikes[i].spikeY + spikes[i].getSpikeHeight() >= dHeight - ground!!.height) {
                points += 10
                val explosion = Explosion(context!!)
                explosion.explosionX = spikes[i].spikeX
                explosion.explosionY = spikes[i].spikeY
                explosions.add(explosion)
                spikes[i].resetPosition()
            }
        }

        var i = 0
        while (i < spikes.size) {
            if (spikes[i].spikeX + spikes[i].getSpikeWidth() >= rabbitX
                && spikes[i].spikeX <= rabbitX + rabbit!!.width
                && spikes[i].spikeY + spikes[i].getSpikeWidth() >= rabbitY
                && spikes[i].spikeY + spikes[i].getSpikeWidth() <= rabbitY + rabbit!!.height
            ) {
                life--
                spikes[i].resetPosition()
                if (life == 0) {
                    val intent = Intent(context, GameOver::class.java)
                    intent.putExtra("points", points)
                    context?.startActivity(intent)
                    (context as Activity).finish()
                }
            }
            i++
        }

        i = explosions.size - 1
        while (i >= 0) {
            val bitmap = explosions[i].getExplosion(explosions[i].explosionFrame) ?: continue
            canvas.drawBitmap(bitmap, explosions[i].explosionX.toFloat(), explosions[i].explosionY.toFloat(), null)

            explosions[i].explosionFrame++
            if (explosions[i].explosionFrame > 3) {
                explosions.removeAt(i)
            }
            i--
        }

        if (life == 2) {
            healthPaint.color = Color.YELLOW
        } else if (life == 1) {
            healthPaint.color = Color.RED
        }
        canvas.drawRect((dWidth - 200).toFloat(), 30f,
            (dWidth - 200 + 60 * life).toFloat(), 80f, healthPaint)
        canvas.drawText("$points", 20f, TEXT_SIZE, textPaint)
        handler?.postDelayed(runnable, UPDATE_MILLIS)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= rabbitY) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.x
                oldRabbitX = rabbitX
            }
            if (action == MotionEvent.ACTION_MOVE) {
                val shift = oldX - touchX
                val newRabbitX = oldRabbitX - shift
                if (newRabbitX <= 0) rabbitX = 0f
                else if (newRabbitX >= dWidth - rabbit!!.width) rabbitX = (dWidth - rabbit!!.width).toFloat()
                else rabbitX = newRabbitX
            }
        }
        return true
    }
}
package com.example.savebunny

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class Explosion(context: Context) {
    private val explosion = arrayOfNulls<Bitmap>(4)
    var explosionFrame = 0
    var explosionX = 0
    var explosionY = 0

    init {
        explosion[0] = BitmapFactory.decodeResource(context.resources, R.drawable.explodea)
        explosion[1] = BitmapFactory.decodeResource(context.resources, R.drawable.explodeb)
        explosion[2] = BitmapFactory.decodeResource(context.resources, R.drawable.explodec)
        explosion[3] = BitmapFactory.decodeResource(context.resources, R.drawable.exploded)
    }

    fun getExplosion(explosionFrame: Int): Bitmap? {
        return explosion[explosionFrame]
    }
}
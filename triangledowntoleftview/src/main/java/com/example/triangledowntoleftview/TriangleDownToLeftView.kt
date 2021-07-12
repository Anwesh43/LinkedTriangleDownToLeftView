package com.example.triangledowntoleftview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<Int> = arrayOf(
    "#1A237E",
    "#EF5350",
    "#AA00FF",
    "#C51162",
    "#00C853"
).map {
    Color.parseColor(it)
}.toTypedArray()
val strokeFactor : Float = 90f
val sizeFactor : Float = 7.2f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val parts : Int  = 5
val scGap : Float = 0.02f / parts
val deg : Float = 90f
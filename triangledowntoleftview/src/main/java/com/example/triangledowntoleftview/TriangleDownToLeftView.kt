package com.example.triangledowntoleftview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log

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
val scGap : Float = 0.05f / parts
val deg : Float = 90f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawTriangleDownToLeft(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sc1 : Float = scale.divideScale(0, parts)
    val sc2 : Float = scale.divideScale(1, parts)
    val sc3 : Float = scale.divideScale(2, parts)
    val sc4 : Float = scale.divideScale(3, parts)
    val sc5 : Float = scale.divideScale(4, parts)
    Log.d("TRIANGLE_DOWN_TO_LEFT", "$sc1 $sc2 $sc3 $sc4 $sc5")
    save()
    translate(
        w / 2 + (w / 2) * sc5,
        h / 2 + (h / 2 - size / 2) * (1 - sc3)
    )
    rotate(deg * sc4)
    if (sc1 > 0f) {
        drawLine(
            -size / 2,
            size / 2,
            -size / 2 + (size / 2) * sc1,
            (size / 2) - size * sc1,
            paint
        )
    }
    if (sc2 > 0f) {
        drawLine(
            0f,
            -size / 2,
            (size / 2) * sc2,
            -size / 2 + size * sc2,
            paint
        )
    }
    restore()
}

fun Canvas.drawTDTLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawTriangleDownToLeft(scale, w, h, paint)
}

class TriangleDownToLeftView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TDTLNode(var i : Int, val state : State = State()) {

        private var next : TDTLNode? = null
        private var prev : TDTLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = TDTLNode(i + 1)
                prev?.next = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTDTLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TDTLNode {
            var curr : TDTLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TriangleDownToLeft(var i : Int) {

        private var curr : TDTLNode = TDTLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriangleDownToLeftView) {

        private val animator : Animator = Animator(view)
        private val tdl : TriangleDownToLeft = TriangleDownToLeft(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            tdl.draw(canvas, paint)
            animator.animate {
                tdl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tdl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : TriangleDownToLeftView {
            val view : TriangleDownToLeftView = TriangleDownToLeftView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
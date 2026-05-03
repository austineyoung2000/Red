package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.TextView

class TriggerTouchOverlayService : Service() {

    private lateinit var wm: WindowManager
    private var leftView: View? = null
    private var rightView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlay()
    }

    private fun createOverlay() {
        leftView = createDot("L") { x, y ->
            TriggerTouchStorage.save(this, "left", x, y)
        }

        rightView = createDot("R") { x, y ->
            TriggerTouchStorage.save(this, "right", x, y)
        }

        wm.addView(leftView, layoutParams(200, 600))
        wm.addView(rightView, layoutParams(800, 600))
    }

    private fun createDot(label: String, onMove: (Int, Int) -> Unit): View {
        val view = TextView(this).apply {
            text = label
            textSize = 16f
            setBackgroundColor(0x55FF0000)
            setPadding(20, 20, 20, 20)
        }

        view.setOnTouchListener(object : View.OnTouchListener {
            var lastX = 0
            var lastY = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = v.layoutParams as WindowManager.LayoutParams

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX.toInt() - lastX
                        val dy = event.rawY.toInt() - lastY

                        params.x += dx
                        params.y += dy

                        wm.updateViewLayout(v, params)

                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()

                        onMove(params.x, params.y)
                    }
                }
                return true
            }
        })

        return view
    }

    private fun layoutParams(x: Int, y: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }
    }

    override fun onDestroy() {
        leftView?.let { wm.removeView(it) }
        rightView?.let { wm.removeView(it) }
        super.onDestroy()
    }
}

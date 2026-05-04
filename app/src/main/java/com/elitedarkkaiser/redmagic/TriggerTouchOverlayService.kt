package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class TriggerTouchOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var targetPkg: String? = null

    private var leftView: View? = null
    private var rightView: View? = null
    private var controlsView: View? = null

    private var leftX = 200
    private var leftY = 600
    private var rightX = 800
    private var rightY = 600

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pkg = intent?.getStringExtra("pkg")
        if (pkg.isNullOrBlank()) {
            Toast.makeText(this, "Open the selected game first, then setup trigger touch.", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        }

        targetPkg = pkg
        removeOverlay()
        loadSavedOrDefaults(pkg)
        createOverlay(pkg)
        return START_NOT_STICKY
    }

    private fun loadSavedOrDefaults(pkg: String) {
        TriggerTouchStorage.load(this, pkg, "left")?.let {
            leftX = it.first
            leftY = it.second
        }

        TriggerTouchStorage.load(this, pkg, "right")?.let {
            rightX = it.first
            rightY = it.second
        }
    }

    private fun createOverlay(pkg: String) {
        leftView = createDot("L") { x, y ->
            leftX = x
            leftY = y
        }

        rightView = createDot("R") { x, y ->
            rightX = x
            rightY = y
        }

        controlsView = createControls(pkg)

        wm.addView(leftView, layoutParams(leftX, leftY))
        wm.addView(rightView, layoutParams(rightX, rightY))
        wm.addView(controlsView, layoutParams(24, 24))
    }

    private fun createDot(label: String, onMove: (Int, Int) -> Unit): View {
        val view = TextView(this).apply {
            text = label
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xAAE53935.toInt())
            setPadding(26, 22, 26, 22)
        }

        view.setOnTouchListener(object : View.OnTouchListener {
            var lastX = 0
            var lastY = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = v.layoutParams as WindowManager.LayoutParams

                when (event.actionMasked) {
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

    private fun createControls(pkg: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xCC101722.toInt())
            setPadding(12, 10, 12, 10)
        }

        val title = TextView(this).apply {
            text = "Trigger setup: $pkg"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            setPadding(8, 0, 20, 0)
        }

        val confirm = button("CONFIRM") {
            TriggerTouchStorage.save(this, pkg, "left", leftX, leftY)
            TriggerTouchStorage.save(this, pkg, "right", rightX, rightY)
            TriggerTouchStorage.markConfirmed(this, pkg)
            Toast.makeText(this, "Saved trigger points for $pkg", Toast.LENGTH_SHORT).show()
        }

        val enable = button("ENABLE") {
            TriggerTouchStorage.save(this, pkg, "left", leftX, leftY)
            TriggerTouchStorage.save(this, pkg, "right", rightX, rightY)
            TriggerTouchStorage.markConfirmed(this, pkg)
            HardwareController.enableTriggers()
            startService(Intent(this, TriggerRootService::class.java))
            Toast.makeText(this, "Saved and enabled triggers", Toast.LENGTH_SHORT).show()
        }

        val close = button("CLOSE") {
            stopSelf()
        }

        row.addView(title)
        row.addView(confirm)
        row.addView(enable)
        row.addView(close)

        return row
    }

    private fun button(textValue: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = textValue
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            setBackgroundColor(0xAA263447.toInt())
            setPadding(18, 10, 18, 10)
            setOnClickListener { action() }
        }
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

    private fun removeOverlay() {
        leftView?.let { runCatching { wm.removeView(it) } }
        rightView?.let { runCatching { wm.removeView(it) } }
        controlsView?.let { runCatching { wm.removeView(it) } }

        leftView = null
        rightView = null
        controlsView = null
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}

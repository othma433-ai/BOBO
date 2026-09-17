package com.althmany.extractor.runtime.overlay

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.engine.RuntimeOperationCoordinator
import com.althmany.groupmanager.R

/**
 * Active-operation-only floating controls. Overlay is optional; notification actions are always
 * available while a RuntimeOperation owns WhatsApp UI.
 */
class FloatingControlService : Service() {
    private lateinit var wm: WindowManager
    private var root: View? = null
    private var collapsed = false
    private var shownOwner: RuntimeOperation? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, notification(RuntimeOperationCoordinator.current()))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val owner = RuntimeOperationCoordinator.current()
        if (owner == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_PAUSE -> RuntimeControlRouter.pause(this, owner)
            ACTION_RESUME -> RuntimeControlRouter.resume(this, owner)
            ACTION_RETURN -> RuntimeControlRouter.returnToTarget(this, owner)
            ACTION_STOP -> {
                RuntimeControlRouter.stop(this, owner)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val currentOwner = RuntimeOperationCoordinator.current()
        if (currentOwner == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification(currentOwner))

        if (Settings.canDrawOverlays(this)) {
            if (shownOwner != currentOwner) {
                hideOverlay()
                show(currentOwner)
            } else if (root == null) {
                show(currentOwner)
            }
        } else {
            hideOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun show(operation: RuntimeOperation) {
        if (root != null) return
        shownOwner = operation
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(0xEE202124.toInt())
        }
        val title = TextView(this).apply {
            text = "AL-thmany • ${operation.labelAr}"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
        }
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun button(label: String, onClick: () -> Unit) = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
        }
        buttons.addView(button("إيقاف مؤقت") { RuntimeControlRouter.pause(this, operation) })
        buttons.addView(button("استئناف") { RuntimeControlRouter.resume(this, operation) })
        buttons.addView(button("رجوع") { RuntimeControlRouter.returnToTarget(this, operation) })
        buttons.addView(button("إيقاف") {
            RuntimeControlRouter.stop(this, operation)
            stopSelf()
        })
        card.addView(title)
        card.addView(buttons)
        card.setOnLongClickListener {
            collapsed = !collapsed
            buttons.visibility = if (collapsed) View.GONE else View.VISIBLE
            true
        }

        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 220
        }

        card.setOnTouchListener(DragTouchListener(lp))
        wm.addView(card, lp)
        root = card
    }

    private inner class DragTouchListener(private val lp: WindowManager.LayoutParams) : View.OnTouchListener {
        private var startX = 0
        private var startY = 0
        private var downX = 0f
        private var downY = 0f
        override fun onTouch(v: View, event: android.view.MotionEvent): Boolean {
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startX = lp.x
                    startY = lp.y
                    downX = event.rawX
                    downY = event.rawY
                    return false
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    lp.x = (startX - (event.rawX - downX)).toInt()
                    lp.y = (startY + (event.rawY - downY)).toInt()
                    root?.let { wm.updateViewLayout(it, lp) }
                    return true
                }
            }
            return false
        }
    }

    private fun hideOverlay() {
        root?.let { runCatching { wm.removeView(it) } }
        root = null
        shownOwner = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "AL-thmany Runtime Controls",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun pending(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, FloatingControlService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notification(owner: RuntimeOperation?): Notification {
        val label = owner?.labelAr ?: "Runtime"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("AL-thmany 4.0 • $label")
            .setContentText("Smart Runtime controls")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "إيقاف مؤقت", pending(ACTION_PAUSE, 1))
            .addAction(0, "استئناف", pending(ACTION_RESUME, 2))
            .addAction(0, "رجوع", pending(ACTION_RETURN, 3))
            .addAction(0, "إيقاف", pending(ACTION_STOP, 4))
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "althmany_runtime_controls"
        private const val NOTIFICATION_ID = 4040
        private const val ACTION_PAUSE = "com.althmany.runtime.PAUSE"
        private const val ACTION_RESUME = "com.althmany.runtime.RESUME"
        private const val ACTION_RETURN = "com.althmany.runtime.RETURN"
        private const val ACTION_STOP = "com.althmany.runtime.STOP"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FloatingControlService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingControlService::class.java))
        }
    }
}

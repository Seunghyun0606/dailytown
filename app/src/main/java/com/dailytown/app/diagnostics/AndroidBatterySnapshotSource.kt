package com.dailytown.app.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.roundToInt

/** Android adapter for coarse, privacy-safe battery snapshots used by field testing. */
class AndroidBatterySnapshotSource(
    context: Context,
) : BatterySnapshotSource {
    private val appContext = context.applicationContext
    private val batteryManager = appContext.getSystemService(BatteryManager::class.java)

    override fun read(): BatterySnapshot {
        val batteryIntent = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) {
            (level * 100.0 / scale).roundToInt().coerceIn(0, 100)
        } else {
            null
        }
        val status = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN,
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val chargeCounter = batteryManager
            ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            ?.takeUnless { it == Int.MIN_VALUE || it <= 0 }

        return BatterySnapshot(
            levelPercent = percentage,
            chargeCounterMicroAh = chargeCounter,
            charging = charging,
        )
    }
}

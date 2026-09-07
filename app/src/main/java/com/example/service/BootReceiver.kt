package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.DataUsageRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repo = DataUsageRepository(context)
            if (repo.isServiceEnabled() && repo.isStartOnBoot()) {
                SpeedMeterService.start(context)
            }
        }
    }
}

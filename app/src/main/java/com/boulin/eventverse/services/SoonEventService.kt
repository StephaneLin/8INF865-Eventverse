package com.boulin.eventverse.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import com.boulin.eventverse.data.database.dao.EventDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.*

class SoonEventService : Service() {

    private lateinit var serviceHandler: ClosestEventServiceHandler

    @Suppress("DEPRECATION")
    private inner class ClosestEventServiceHandler(val context: Context, looper: Looper): Handler(looper) {
        @ExperimentalCoroutinesApi
        override fun handleMessage(msg: Message) {
            val eventDao: EventDao by inject()

            val name = msg.data.get(NAME_EXTRA) as String
            val surname = msg.data.get(SURNAME_EXTRA) as String

            runBlocking {
                launch {
                    eventDao.getAllEvents().take(1).collect { events ->
                        val now = Calendar.getInstance().time

                        val monthEvents = events.filter { event -> event.startDate.month == now.month && event.startDate.time > now.time }

                        Toast.makeText(context, "Bienvenue $name $surname, il reste ${monthEvents.size} événement${if(monthEvents.size > 1) 's' else ' '}ce mois-ci !", Toast.LENGTH_LONG).show()

                        stopSelf(msg.arg1)
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val handlerThread = HandlerThread("ClosestEventService", Process.THREAD_PRIORITY_BACKGROUND)

        handlerThread.start()

        serviceHandler = ClosestEventServiceHandler(this, handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler.obtainMessage().also { msg ->
            msg.arg1 = startId
            msg.data = intent?.extras
            serviceHandler.sendMessage(msg)
        }

        return START_NOT_STICKY
    }

    companion object {
        const val NAME_EXTRA = "SoonEventService.NAME_EXTRA"
        const val SURNAME_EXTRA = "SoonEventService.SURNAME_EXTRA"
    }
}
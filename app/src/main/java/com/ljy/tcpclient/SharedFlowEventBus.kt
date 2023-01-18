package com.ljy.tcpclient

import android.media.metrics.Event
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Eddy.Liu
 * @Date 2023/1/10
 * @Description
 **/
/**
 * @author Eddy.Liu
 * @Date 2023/1/18
 * @Description
 **/
object ShareFlowEventBus {
    private const val TAG = "EventBus"

    private val busMap = mutableMapOf<String,EventBus<*> >()
    private val stickyBusMap = mutableMapOf<String, EventBus<*> >()

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun<T> with(key: String): EventBus<T> {
        var eventBus = busMap[key]
        if (eventBus == null) {
            eventBus = EventBus<T>(key)
            busMap[key] = eventBus
        }

        return eventBus as EventBus<T>
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun<T> withSticky(key: String): EventBus<T> {
        var eventBus = stickyBusMap[key]
        if (eventBus == null) {
            eventBus = EventBus<T>(key)
            stickyBusMap[key] = eventBus
        }
        return eventBus as EventBus<T>
    }

    class EventBus<T>(private val key: String): LifecycleObserver {

        private val _events = MutableSharedFlow<T>()
        private val events = _events.asSharedFlow()
        private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->

        }

        fun register(lifecycleOwner: LifecycleOwner, action: (t: T) -> Unit) {
            lifecycleOwner.lifecycle.addObserver(this)
            lifecycleOwner.lifecycleScope.launch {
                events.collect {
                    try {
                        action(it)
                    } catch (e: Throwable) {
                        Log.e(TAG, "KEY:%s-----Error:%s".format(key, e.message))
                    }
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun post(event: T) {
            // 利用顶层作用域在UI线程发射事件
            GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
                _events.emit(event)
            }

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            val subscribeCount = _events.subscriptionCount.value
            if (subscribeCount <= 0) busMap.remove(key)
        }
    }
}
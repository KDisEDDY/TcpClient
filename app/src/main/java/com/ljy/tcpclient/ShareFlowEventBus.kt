package com.ljy.tcpclient

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author Eddy.Liu
 * @Date 2023/1/18
 * @Description
 **/
object ShareFlowEventBus {
    private const val TAG = "EventBus"

    private val busMap = mutableMapOf<Any,EventBus<*> >()

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun<T> with(key: Any): EventBus<T> {
        var eventBus = busMap[key]
        if (eventBus == null) {
            eventBus = EventBus<T>(key)
            busMap[key] = eventBus
        }

        return eventBus as EventBus<T>
    }

    class EventBus<T>(private val key: Any): LifecycleObserver {

        private val _events = MutableSharedFlow<T>(replay = 0, extraBufferCapacity = 5, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private val events = _events.asSharedFlow()

        private val _stickyEvent = MutableSharedFlow<T>(replay = 1, extraBufferCapacity = 5, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private val stickyEvent = _stickyEvent.asSharedFlow()
        fun registerSticky(lifecycleOwner: LifecycleOwner, action: (t: T) -> Unit) {
            lifecycleOwner.lifecycle.addObserver(this)
            lifecycleOwner.lifecycleScope.launch {
                stickyEvent.collect {
                    try {
                        action(it)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Emit sticky event KEY:%s-----Error:%s".format(key, e.message))
                    }
                }
            }
        }

        fun register(lifecycleOwner: LifecycleOwner, action: (t: T) -> Unit) {
            lifecycleOwner.lifecycle.addObserver(this)
            lifecycleOwner.lifecycleScope.launch {
                events.collect {
                    try {
                        action(it)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Emit event KEY:%s-----Error:%s".format(key, e.message))
                    }
                }
            }
        }

        fun tryEmit(event: T) {
            // 内部帮我们实现了suspend方法，emit成功返回true，如果发送失败或者发送挂起了会返回false，
            // 所以在背压策略为Suspend时，tryEmit有可能返回false，其他两种直接返回true
            _events.tryEmit(event)
            _stickyEvent.tryEmit(event)
        }

        suspend fun emit(event: T) {
            _events.emit(event)
            _stickyEvent.emit(event)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            val subscribeCount = _events.subscriptionCount.value
            val stickySubscribeCount = _stickyEvent.subscriptionCount.value
            if (subscribeCount <= 0 && stickySubscribeCount <=0) {
                busMap.remove(key)
            }

        }
    }
}


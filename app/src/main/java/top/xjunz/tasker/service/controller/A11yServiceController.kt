package top.xjunz.tasker.service.controller

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.Observer
import top.xjunz.tasker.R
import top.xjunz.tasker.app
import top.xjunz.tasker.ktx.launchIntentSafely
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.service.A11yAutomatorService
import top.xjunz.tasker.service.A11yAutomatorService.Companion.ERROR
import top.xjunz.tasker.service.A11yAutomatorService.Companion.RUNNING

/**
 * @author xjunz 2022/07/23
 */
object A11yServiceController : ServiceController() {

    override val service: A11yAutomatorService? get() = A11yAutomatorService.get()

    private val statusObserver = Observer<Boolean> {
        if (it) {
            listener?.onServiceBound()
        } else {
            listener?.onServiceDisconnected()
        }
    }

    private val errorObserver = Observer<Throwable> {
        if (it != null) {
            listener?.onError(it)
            ERROR.value = null
        }
    }

    override fun bindService() {
        app.launchIntentSafely(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        toast(R.string.pls_start_a11y_service_manually)
    }

    override fun stopService() {
        service?.destroy()
    }

    override fun unbindService() {
        removeStateListener()
    }

    override fun bindExistingServiceIfExists() {
        // do nothing
    }

    override fun setStateListener(listener: ServiceStateListener) {
        super.setStateListener(listener)
        RUNNING.observeForever(statusObserver)
        ERROR.observeForever(errorObserver)
    }

    override fun removeStateListener() {
        super.removeStateListener()
        RUNNING.removeObserver(statusObserver)
        ERROR.removeObserver(errorObserver)
    }

    override fun syncStatus() {
        RUNNING.value = A11yAutomatorService.get() != null
    }
}
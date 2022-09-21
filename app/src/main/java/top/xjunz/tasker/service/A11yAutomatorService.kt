package top.xjunz.tasker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.IAccessibilityServiceClient
import android.app.IUiAutomationConnection
import android.app.UiAutomation
import android.app.UiAutomationHidden
import android.app.accessibilityservice.AccessibilityServiceHidden
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.view.InputEvent
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.MutableLiveData
import androidx.test.uiautomator.GestureController
import androidx.test.uiautomator.InteractionController
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.mock.MockContext
import androidx.test.uiautomator.mock.MockViewConfiguration
import top.xjunz.shared.ktx.unsafeCast
import top.xjunz.tasker.impl.*
import top.xjunz.tasker.ktx.isTrue
import top.xjunz.tasker.util.ReflectionUtil.requireFieldFromSuperClass
import top.xjunz.tasker.util.unsupportedOperation
import java.lang.ref.WeakReference

/**
 * @author xjunz 2022/07/12
 */
class A11yAutomatorService : AccessibilityService(), AutomatorService, IUiAutomationConnection {

    companion object {

        val ERROR = MutableLiveData<Throwable>()

        val RUNNING = MutableLiveData<Boolean>()

        private var instanceRef: WeakReference<A11yAutomatorService>? = null

        fun get() = instanceRef?.get()

        fun require() =
            checkNotNull(get()) { "The A11yAutomatorService is not yet started or is dead!" }
    }

    override val isRunning get() = RUNNING.isTrue

    private var _startTimestamp: Long = -1

    private var callbacks: AccessibilityServiceHidden.Callbacks? = null

    private lateinit var uiAutomationHidden: UiAutomationHidden

    private val _uiAutomation: UiAutomation by lazy {
        uiAutomationHidden.unsafeCast()
    }

    private val interactionController by lazy {
        A11yInteractionController(this)
    }

    private val mockContext by lazy {
        MockContextAdaptor(applicationContext)
    }

    private val mockViewConfig by lazy {
        MockViewConfiguration(ViewConfiguration.get(this).scaledMinimumFlingVelocity)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            uiAutomationHidden = UiAutomationHidden(mainLooper, this)
            uiAutomationHidden.connect()
            instanceRef = WeakReference(this)
            _startTimestamp = System.currentTimeMillis()
            RUNNING.value = true
        } catch (t: Throwable) {
            t.printStackTrace()
            ERROR.value = t
            destroy()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        callbacks?.onAccessibilityEvent(event)
    }

    override fun onInterrupt() {
        TODO("interrupted!")
    }

    override fun onDestroy() {
        super.onDestroy()
        instanceRef?.clear()
        RUNNING.value = false
    }

    /**
     * Connect to the [UiAutomation] with limited features in a delicate way. Should take this
     * really carefully. After connected, we can use [UiAutomation.setOnAccessibilityEventListener],
     * [UiAutomation.waitForIdle] and [UiAutomation.executeAndWaitForEvent].
     *
     * This method will be called on [UiAutomationHidden.connect].
     */
    override fun connect(client: IAccessibilityServiceClient, flags: Int) {
        val windowToken: IBinder = requireFieldFromSuperClass("mWindowToken")
        val connectionId: Int = requireFieldFromSuperClass("mConnectionId")
        callbacks = client.requireFieldFromSuperClass("mCallback")
        callbacks!!.init(connectionId, windowToken)
    }

    override fun disconnect() {
        destroy()
    }

    override fun destroy() {
        if (isRunning) uiAutomationHidden.disconnect()
        disableSelf()
    }

    override fun getStartTimestamp(): Long {
        return _startTimestamp
    }

    override fun createAvailabilityChecker(): IAvailabilityChecker {
        return AvailabilityChecker(this)
    }

    override fun setRotation(rotation: Int): Boolean {
        unsupportedOperation()
    }

    override fun takeScreenshot(crop: Rect, rotation: Int): Bitmap {
        unsupportedOperation()
    }

    override fun executeShellCommand(
        command: String?,
        sink: ParcelFileDescriptor?,
        source: ParcelFileDescriptor?
    ) {
        unsupportedOperation()
    }

    override fun shutdown() {
        destroy()
    }

    override fun getUiAutomation(): UiAutomation {
        return _uiAutomation
    }

    override fun getUiAutomation(flags: Int): UiAutomation {
        return _uiAutomation
    }

    override fun getContext(): MockContext {
        return mockContext
    }

    override fun getInteractionController(): InteractionController {
        return interactionController
    }

    override fun getGestureController(device: UiDevice): GestureController {
        return A11yGestureController(this, device)
    }

    override fun getViewConfiguration(): MockViewConfiguration {
        return mockViewConfig
    }

    override fun asBinder(): IBinder {
        unsupportedOperation()
    }

    override fun injectInputEvent(event: InputEvent?, sync: Boolean): Boolean {
        unsupportedOperation()
    }

    override fun syncInputTransactions() {
        unsupportedOperation()
    }
}
/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import android.view.accessibility.AccessibilityNodeInfo
import top.xjunz.shared.utils.illegalArgument
import top.xjunz.tasker.R
import top.xjunz.tasker.engine.applet.base.*
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.flow.*
import top.xjunz.tasker.task.applet.option.AppletOption
import top.xjunz.tasker.task.applet.value.VariantType

open class FlowOptionRegistry : AppletOptionRegistry(ID_FLOW_OPTION_REGISTRY) {

    companion object {

        private const val ID_FLOW_OPTION_REGISTRY = 0
        const val ID_EVENT_FILTER_REGISTRY: Int = 0xF
        const val ID_APP_OPTION_REGISTRY = 0x10
        const val ID_UI_OBJECT_OPTION_REGISTRY = 0x11
        const val ID_TIME_OPTION_REGISTRY = 0x12
        const val ID_GLOBAL_OPTION_REGISTRY = 0x13
        const val ID_NOTIFICATION_OPTION_REGISTRY = 0x14

        const val ID_GLOBAL_ACTION_REGISTRY = 0x50
        const val ID_UI_OBJECT_ACTION_REGISTRY = 0x51
        const val ID_CONTROL_ACTION_REGISTRY = 0x52
        const val ID_APP_ACTION_REGISTRY = 0x53
        const val ID_TEXT_ACTION_REGISTRY = 0x54
        const val ID_GESTURE_ACTION_REGISTRY = 0x55
    }

    private inline fun <reified F : Flow> flowOptionWithId(
        appletId: Int,
        title: Int
    ): AppletOption {
        return appletOption(title) {
            F::class.java.newInstance()
        }.also {
            it.appletId = appletId
        }
    }

    private inline fun <reified F : Flow> flowOption(title: Int): AppletOption {
        return flowOptionWithId<F>(-1, title)
    }

    fun getPeerOptions(flow: ControlFlow, before: Boolean): Array<AppletOption> {
        // Regex("验证码.*?(\\d+?)")
        return when (flow) {
            is If -> if (before) emptyArray() else arrayOf(doFlow)
            is When -> if (before) emptyArray() else arrayOf(ifFlow, doFlow)
            is Else -> if (before) emptyArray() else arrayOf(ifFlow)
            is Do -> if (before) emptyArray() else arrayOf(ifFlow, elseIfFlow, elseFlow)
            else -> illegalArgument("control flow", flow)
        }
    }

    /**
     * Applet flow is a container flow whose child has the same target.
     */
    val criterionFlowOptions: Array<AppletOption> by lazy {
        arrayOf(appCriteria, uiObjectCriteria, timeCriteria, notificationCriteria, globalCriteria)
    }

    val actionFlowOptions: Array<AppletOption> by lazy {
        arrayOf(
            globalActions,
            uiObjectActions,
            gestureActions,
            textActions,
            appActions,
            controlActions
        )
    }

    @AppletOrdinal(0x0000)
    val rootFlow = flowOption<RootFlow>(AppletOption.TITLE_NONE)

    @AppletOrdinal(0x0001)
    val whenFlow = flowOption<When>(R.string._when)

    @AppletOrdinal(0x0002)
    val ifFlow = flowOption<If>(R.string._if)

    @AppletOrdinal(0x0003)
    val doFlow = flowOption<Do>(R.string._do)

    @AppletOrdinal(0x0004)
    val elseIfFlow = flowOption<ElseIf>(R.string.else_if)

    @AppletOrdinal(0x0005)
    val elseFlow = flowOption<Else>(R.string._else)

    @AppletOrdinal(0x0005)
    val containerFlow = flowOption<ContainerFlow>(AppletOption.TITLE_NONE)

    @AppletOrdinal(0x0006)
    val preloadFlow = flowOption<PreloadFlow>(R.string.global)
        .withResult<ComponentInfoWrapper>(R.string.current_top_app)
        .withResult<AccessibilityNodeInfo>(R.string.current_focus_input)

    @AppletOrdinal(0x000F)
    val eventCriteria = flowOptionWithId<PhantomFlow>(ID_EVENT_FILTER_REGISTRY, R.string.event)

    @AppletOrdinal(0x0010)
    val appCriteria = flowOptionWithId<PhantomFlow>(ID_APP_OPTION_REGISTRY, R.string.app_info)

    @AppletOrdinal(0x0011)
    val uiObjectCriteria =
        flowOptionWithId<UiObjectFlow>(ID_UI_OBJECT_OPTION_REGISTRY, R.string.ui_object_exists)
            .withResult<AccessibilityNodeInfo>(R.string.ui_object)
            .withResult<String>(R.string.matched_ui_object_text)
            .withResult<Int>(R.string.center_coordinate, VariantType.INT_COORDINATE)

    @AppletOrdinal(0x0012)
    val timeCriteria = flowOptionWithId<TimeFlow>(ID_TIME_OPTION_REGISTRY, R.string.current_time)

    @AppletOrdinal(0x0013)
    val globalCriteria =
        flowOptionWithId<PhantomFlow>(ID_GLOBAL_OPTION_REGISTRY, R.string.device_status)

    @AppletOrdinal(0x0014)
    val notificationCriteria = flowOptionWithId<NotificationFlow>(
        ID_NOTIFICATION_OPTION_REGISTRY, R.string.current_notification
    )

    @AppletOrdinal(0x0020)
    val globalActions =
        flowOptionWithId<PhantomFlow>(ID_GLOBAL_ACTION_REGISTRY, R.string.global_actions)

    @AppletOrdinal(0x0021)
    val uiObjectActions =
        flowOptionWithId<PhantomFlow>(ID_UI_OBJECT_ACTION_REGISTRY, R.string.ui_object_operations)

    @AppletOrdinal(0x0022)
    val gestureActions =
        flowOptionWithId<PhantomFlow>(ID_GESTURE_ACTION_REGISTRY, R.string.gesture_operations)

    @AppletOrdinal(0x0023)
    val textActions =
        flowOptionWithId<PhantomFlow>(ID_TEXT_ACTION_REGISTRY, R.string.text_operations)

    @AppletOrdinal(0x0024)
    val appActions =
        flowOptionWithId<PhantomFlow>(ID_APP_ACTION_REGISTRY, R.string.app_operations)

    @AppletOrdinal(0x0025)
    val controlActions =
        flowOptionWithId<PhantomFlow>(ID_CONTROL_ACTION_REGISTRY, R.string.control_actions)

}
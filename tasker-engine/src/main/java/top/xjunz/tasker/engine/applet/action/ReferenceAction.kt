/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.engine.applet.action

import top.xjunz.shared.ktx.casted
import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * @author xjunz 2022/11/15
 */
abstract class ReferenceAction<V>(valueType: Int) : Action<V>(valueType) {

    abstract suspend fun doWithArgs(
        args: Array<Any?>,
        value: V?,
        runtime: TaskRuntime
    ): AppletResult

    final override suspend fun doAction(value: V?, runtime: TaskRuntime): AppletResult {
        val args = runtime.getArguments(this)
        args.forEach {
            runtime.updateFingerprint(it)
        }
        return doWithArgs(args, value?.casted(), runtime)
    }
}

class LambdaReferenceAction<V>(
    valueType: Int,
    private inline val action: suspend (args: Array<Any?>, value: V?, runtime: TaskRuntime) -> Boolean
) : ReferenceAction<V>(valueType) {
    override suspend fun doWithArgs(
        args: Array<Any?>,
        value: V?,
        runtime: TaskRuntime
    ): AppletResult {
        return if (action(args, value, runtime)) AppletResult.SUCCESS else AppletResult.FAILURE
    }
}

inline fun <Arg, reified V> singleArgValueAction(
    crossinline action: (Arg?, V?) -> Boolean
): ReferenceAction<V> {
    return LambdaReferenceAction(Applet.judgeValueType<V>()) { args, v, _ ->
        action(args.single()?.casted(), v)
    }
}

inline fun <Arg> singleArgAction(crossinline action: (Arg?) -> Boolean): ReferenceAction<Unit> {
    return LambdaReferenceAction(Applet.VAL_TYPE_IRRELEVANT) { args, _, _ ->
        action(args.single()?.casted())
    }
}

inline fun <reified ArgOrValue> unaryArgValueAction(
    crossinline action: (ArgOrValue) -> Boolean
): ReferenceAction<ArgOrValue> {
    return LambdaReferenceAction(Applet.judgeValueType<ArgOrValue>()) { args, v, _ ->
        action(requireNotNull(args.singleOrNull()?.casted() ?: v) {
            "Neither ref nor value is not null!"
        })
    }
}

/**
 * An action whose value and reference argument are of different types. Use [mapper] to convert
 * the [Arg] into [Val].
 */
inline fun <reified Val, reified Arg> binaryArgValueAction(
    crossinline mapper: Arg.() -> Val?,
    crossinline action: (Val) -> Boolean
): ReferenceAction<Val> {
    return LambdaReferenceAction(Applet.judgeValueType<Val>()) { args, v, _ ->
        action(requireNotNull(args.singleOrNull()?.casted<Arg>()?.mapper() ?: v) {
            "Neither ref nor value is not null!"
        })
    }
}
/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.engine.applet.base

import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * A [ScopeFlow] initializes its target at [Flow.onPrepare]. The target will be used by all of its
 * elements.
 *
 * @author xjunz 2022/12/04
 */
abstract class ScopeFlow<Target : Any> : Flow() {

    /**
     * The key used to register target to runtime, equivalent to [id] by default, which means
     * all flows with the same id will share an identical target at runtime.
     */
    protected open fun generateTargetKey(): Long {
        return id.toLong()
    }

    private var targetKey: Long = -1

    /**
     * Generate the overall target. The result will be registered to the snapshot in runtime
     * and can be accessed across tasks with a key. This is done in [onPrepare].
     */
    abstract fun initializeTarget(runtime: TaskRuntime): Target

    /**
     * Get the initialized target, which is generated by [initializeTarget].
     */
    protected inline val TaskRuntime.target: Target get() = getTarget()

    /**
     * By default, the target is stored with [id] as the key. If you want to store more
     * variables, you can call this to generate a unique key without conflict with other
     * potential keys.
     */
    fun generateUniqueKey(seed: Int): Long {
        return seed.toLong() shl Int.SIZE_BITS or id.toLong()
    }

    override fun onPrepare(runtime: TaskRuntime) {
        super.onPrepare(runtime)
        if (targetKey == -1L) {
            targetKey = generateTargetKey()
        }
        runtime.setTarget(
            runtime.getGlobal(targetKey) {
                initializeTarget(runtime)
            }
        )
    }
}
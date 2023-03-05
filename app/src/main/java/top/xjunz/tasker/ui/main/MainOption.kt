/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import top.xjunz.tasker.*
import top.xjunz.tasker.service.isPremium

/**
 * @author xjunz 2023/02/27
 */
sealed class MainOption(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    var desc: () -> Any? = { null },
    @StringRes var longDesc: Int = -1
) {
    object PremiumStatus : MainOption(R.string.premium_status, R.drawable.ic_verified_24px, desc = {
        if (isPremium) {
            R.string.activated
        } else {
            R.string.not_activated
        }
    })

    object NightMode : MainOption(R.string.night_mode, R.drawable.ic_nights_stay_24px, desc = {
        when (Preferences.nightMode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.string.follow_system
            AppCompatDelegate.MODE_NIGHT_YES -> R.string.turn_on
            AppCompatDelegate.MODE_NIGHT_NO -> R.string.turn_off
            else -> error("?")
        }
    })


    object Feedback : MainOption(R.string.feedback, R.drawable.ic_chat_24px)

    object VersionInfo : MainOption(R.string.version_info, R.drawable.ic_info_24px, desc = {
        if (app.updateInfo.value?.hasUpdates() == true) {
            R.string.new_version_detected
        } else {
            BuildConfig.VERSION_NAME
        }
    })

    object About : MainOption(
        R.string.about,
        R.drawable.ic_baseline_more_vert_24,
        longDesc = R.string.more_to_say
    )

    companion object {
        val ALL_OPTIONS = if (isShell) {
            arrayOf(NightMode, Feedback, VersionInfo, About)
        } else {
            arrayOf(PremiumStatus, NightMode, Feedback, VersionInfo, About)
        }
    }
}
// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.features.settings.ui

import android.os.Bundle
import android.text.TextUtils
import io.github.mandarine3ds.mandarine.NativeLibrary
import io.github.mandarine3ds.mandarine.features.settings.model.IntSetting
import io.github.mandarine3ds.mandarine.features.settings.model.Settings
import io.github.mandarine3ds.mandarine.utils.SystemSaveGame
import io.github.mandarine3ds.mandarine.utils.DirectoryInitialization
import io.github.mandarine3ds.mandarine.utils.Log

class SettingsActivityPresenter(private val activityView: SettingsActivityView) {
    val settings: Settings get() = activityView.settings

    private var shouldSave = false
    private lateinit var menuTag: String
    private lateinit var gameId: String

    fun onCreate(savedInstanceState: Bundle?, menuTag: String, gameId: String) {
        this.menuTag = menuTag
        this.gameId = gameId
        if (savedInstanceState != null) {
            shouldSave = savedInstanceState.getBoolean(KEY_SHOULD_SAVE)
        }
    }

    fun onStart() {
        SystemSaveGame.load()
        prepareDirectoriesIfNeeded()
    }

    private fun loadSettingsUI() {
        if (!settings.isLoaded) {
            if (!TextUtils.isEmpty(gameId)) {
                settings.loadSettings(gameId, activityView)
            } else {
                settings.loadSettings(activityView)
            }
        }
        activityView.showSettingsFragment(menuTag, false, gameId)
        activityView.onSettingsFileLoaded()
    }

    private fun prepareDirectoriesIfNeeded() {
        if (!DirectoryInitialization.areMandarineDirectoriesReady()) {
            DirectoryInitialization.start()
        }
        loadSettingsUI()
    }

    fun onStop(finishing: Boolean) {
        if (finishing && shouldSave) {
            Log.debug("[SettingsActivity] Settings activity stopping. Saving settings to INI...")
            settings.saveSettings(activityView)
            SystemSaveGame.save()
            // Added to ensure that layout changes take effect as soon as settings window closes
            NativeLibrary.reloadSettings()
            NativeLibrary.updateFramebuffer(NativeLibrary.isPortraitMode)
        }
        NativeLibrary.reloadSettings()
    }

    fun onSettingChanged() {
        shouldSave = true
    }

    fun onSettingsReset() {
        shouldSave = false
    }

    fun saveState(outState: Bundle) {
        outState.putBoolean(KEY_SHOULD_SAVE, shouldSave)
    }

    companion object {
        private const val KEY_SHOULD_SAVE = "should_save"
    }
}

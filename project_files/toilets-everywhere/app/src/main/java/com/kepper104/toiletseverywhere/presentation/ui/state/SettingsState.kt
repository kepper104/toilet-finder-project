package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.presentation.ui.MapStyle

data class SettingsState (
    val selectedDarkModeOption: DarkModeStatus = DarkModeStatus.AUTO,
    val selectedMapStyle: MapStyle = MapStyle.DefaultStyle,
    val mapStyleSelectionExpanded: Boolean = false,
    val nameChangeDialogOpen: Boolean = false,
    val newDisplayName: String = ""



)

enum class DarkModeStatus{
    AUTO, FORCED_ON, FORCED_OFF
}
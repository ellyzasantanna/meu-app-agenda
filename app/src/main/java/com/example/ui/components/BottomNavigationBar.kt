package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodel.AgendaTab

@Composable
fun AgendaBottomNavigationBar(
    currentTab: AgendaTab,
    onTabSelected: (AgendaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar")
    ) {
        NavigationBarItem(
            selected = currentTab == AgendaTab.CALENDAR,
            onClick = { onTabSelected(AgendaTab.CALENDAR) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AgendaTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendário"
                )
            },
            label = { Text("Calendário") },
            modifier = Modifier.testTag("nav_tab_calendar")
        )

        NavigationBarItem(
            selected = currentTab == AgendaTab.STICKY_NOTES,
            onClick = { onTabSelected(AgendaTab.STICKY_NOTES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AgendaTab.STICKY_NOTES) Icons.Filled.StickyNote2 else Icons.Outlined.StickyNote2,
                    contentDescription = "Post-its"
                )
            },
            label = { Text("Post-its") },
            modifier = Modifier.testTag("nav_tab_notes")
        )

        NavigationBarItem(
            selected = currentTab == AgendaTab.IMPORTANT_DATES,
            onClick = { onTabSelected(AgendaTab.IMPORTANT_DATES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AgendaTab.IMPORTANT_DATES) Icons.Filled.EventNote else Icons.Outlined.EventNote,
                    contentDescription = "Datas"
                )
            },
            label = { Text("Datas") },
            modifier = Modifier.testTag("nav_tab_dates")
        )
    }
}

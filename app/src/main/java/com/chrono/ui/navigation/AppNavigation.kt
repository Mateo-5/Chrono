package com.chrono.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chrono.ui.calendar.CalendarScreen
import com.chrono.ui.events.EventsScreen
import com.chrono.ui.exams.ExamsScreen
import com.chrono.ui.focus.FocusScreen
import com.chrono.ui.home.HomeScreen
import com.chrono.ui.notes.NotesScreen
import com.chrono.ui.notifications.NotificationsScreen
import com.chrono.ui.reminders.RemindersScreen
import com.chrono.ui.settings.SettingsScreen
import com.chrono.ui.tasks.TasksScreen
import com.chrono.ui.timetable.TimetableScreen

private const val DURATION = 300

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(DURATION)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(DURATION)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(DURATION)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(DURATION)
            )
        }
    ) {
        composable("home") {
            HomeScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }
        
        composable("calendar") {
            CalendarScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("timetable") {
            TimetableScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("exams") {
            ExamsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("reminders") {
            RemindersScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("tasks") {
            TasksScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("notes") {
            NotesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("events") {
            EventsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("focus") {
            FocusScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("notifications") {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

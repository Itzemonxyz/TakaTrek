package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class GoalCategory(val displayName: String, val icon: ImageVector, val color: Color) {
    HOUSING("Housing", Icons.Default.Home, Color(0xFF3F51B5)),        // Indigo 500
    VEHICLE("Vehicle", Icons.Default.DirectionsCar, Color(0xFF5C6BC0)), // Indigo 400
    ELECTRONICS("Electronics", Icons.Default.LaptopMac, Color(0xFF7986CB)), // Indigo 300
    TRAVEL("Travel", Icons.Default.FlightTakeoff, Color(0xFF3949AB)),   // Indigo 600
    EMERGENCY("Emergency", Icons.Default.MedicalServices, Color(0xFF303F9F)), // Indigo 700
    EDUCATION("Education", Icons.Default.School, Color(0xFF283593)),   // Indigo 800
    OTHERS("Others", Icons.Default.Category, Color(0xFF1A237E));       // Indigo 900

    companion object {
        fun fromString(name: String): GoalCategory {
            return values().find { it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) } ?: OTHERS
        }
    }
}

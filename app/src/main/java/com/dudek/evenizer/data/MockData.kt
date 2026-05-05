package com.dudek.evenizer.data

import androidx.compose.ui.graphics.Color

data class Event(
    val id: Int,
    val title: String,
    val date: String,
    val location: String,
    val organizer: String,
    val category: String,
    val imageUrl: String = ""
)

data class Organizer(
    val id: Int,
    val name: String,
    val availableDates: List<String>,
    val rating: Float,
    val projectsCompleted: Int,
    val color: Color
)

data class Ticket(
    val id: String,
    val eventTitle: String,
    val date: String,
    val location: String,
    val type: String,
    val status: String,
    val price: String
)

object MockData {
    val tickets = listOf(
        Ticket("TKT-001", "Summer Music Festival", "2024-06-15", "Jakarta", "VIP", "Active", "Rp 500.000"),
        Ticket("TKT-002", "Tech Conference 2024", "2024-07-10", "Bandung", "Regular", "Active", "Rp 250.000"),
        Ticket("TKT-003", "Wedding Expo", "2024-06-20", "Surabaya", "VIP", "Used", "Free")
    )
    val events = listOf(
        Event(1, "Summer Music Festival", "2024-06-15", "Jakarta", "Joy Event", "Music"),
        Event(2, "Tech Conference 2024", "2024-07-10", "Bandung", "TechFlow", "Conference"),
        Event(3, "Wedding Expo", "2024-06-20", "Surabaya", "Eternal Love", "Exhibition"),
        Event(4, "Charity Run", "2024-08-05", "Jakarta", "Hope Org", "Sport"),
        Event(5, "Art Exhibition", "2024-06-15", "Bali", "Creative Space", "Art")
    )

    val organizers = listOf(
        Organizer(1, "Joy Event", listOf("2024-06-15", "2024-06-20"), 4.8f, 150, Color(0xFF9C27B0)),
        Organizer(2, "TechFlow", listOf("2024-07-10", "2024-07-11"), 4.5f, 85, Color(0xFF2196F3)),
        Organizer(3, "Eternal Love", listOf("2024-06-20", "2024-06-25"), 4.9f, 200, Color(0xFFE91E63)),
        Organizer(4, "Hope Org", listOf("2024-08-05", "2024-08-06"), 4.2f, 40, Color(0xFF4CAF50)),
        Organizer(5, "Creative Space", listOf("2024-06-15", "2024-07-01"), 4.7f, 120, Color(0xFFFF9800))
    )
}

package com.boulin.eventverse.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents an event in the system
 */
@Entity(tableName = "event")
data class Event(
    @PrimaryKey(autoGenerate = false) val id: String,
    val title: String,
    val cover: String, // link to the event cover image
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val location: Location,
    val target: User.TargetAudience,
    val liked: List<String>, // array of uid for the users that liked the event
    val creator: String, // uid of the user who created the event
    val creationDate: Date,
): Serializable {
    @Ignore
    var distance: Float? = null // distance to the user of the event (useful only in app, so ignore it in the database and api)

    /**
     * Input parameters needed to create or update an event
     */
    data class EventInputParams(
        val title: String,
        val description: String,
        val startDate: Date,
        val endDate: Date,
        val location: Location,
        val target: User.TargetAudience
    )

    @Suppress("DEPRECATION")
    fun getFormattedDates() : String {
        val now = Calendar.getInstance().time

        // if start and end the same day and it is this year : "31 Dec | 12h00 - 14h00"
        if(startDate.date == endDate.date && startDate.month == endDate.month && startDate.year == endDate.year && startDate.year == now.year) {
            return "${dateFormatter.format(startDate)} | ${hourFormatter.format(startDate)} - ${hourFormatter.format(endDate)}"
        }
        // if start and end the same day but not this year : "31 Dec 2025 | 12h00 - 14h00"
        else if(startDate.date == endDate.date && startDate.month == endDate.month && startDate.year == endDate.year) {
            return "${dateWithYearFormatter.format(startDate)} | ${hourFormatter.format(startDate)} - ${hourFormatter.format(endDate)}"
        }
        // if it is not the same date but this year : "31 Dec 12h00 - 20 Fev 14h00"
        else if(startDate.year == now.year && endDate.year == now.year){
            return "${dateFormatter.format(startDate)} ${hourFormatter.format(startDate)} - ${dateFormatter.format(endDate)} ${hourFormatter.format(endDate)}"
        }
        // different date and different year : "31 Dec 2021 12h00 - 20 Fev 2021 14h00"
        else {
            return "${dateWithYearFormatter.format(startDate)} ${hourFormatter.format(startDate)} - ${dateWithYearFormatter.format(endDate)} ${hourFormatter.format(endDate)}"
        }
    }

    companion object {
        val dateFormatter = SimpleDateFormat("dd MMM", Locale("fr"))
        val dateWithYearFormatter = SimpleDateFormat("dd MMM. yyyy", Locale("fr"))
        val hourFormatter = SimpleDateFormat("HH'h'mm", Locale("fr"))
    }
}

/*fun generateEvents(): List<EventInputParams> {
    val paris = Location("Paris (FR)", 2.34, 48.85)
    val newYork = Location("New York, NY (USA)", 40.71, -74.0)
    val quebec = Location("Quebec, QC (CA)", -57.0, 51.46)
    val montreal = Location("Montréal, QC (CA)", -73.55, 45.5)

    return listOf(
        // test dates for all
        EventInputParams("Concert de star", "Un très bel évènement pour tous", getDateByDays(10), getDateByDays(20), paris, User.TargetAudience.ALL),
        EventInputParams("Concert de superstar", "Un très bel évènement pour tous", getDateByDays(14), getDateByDays(14, 5), newYork, User.TargetAudience.ALL),
        EventInputParams("Concert de rockstar", "Un très bel évènement pour tous", getDateByDays(17), getDateByDays(17, 1), quebec, User.TargetAudience.ALL),
        EventInputParams("Concert d'opéra", "Un très bel évènement pour tous", getDateByDays(90), getDateByDays(90, 48), montreal, User.TargetAudience.ALL),
        EventInputParams("Concert de musique", "Un très bel évènement pour tous", getDateByDays(200), getDateByDays(200, 10), paris, User.TargetAudience.ALL),
        EventInputParams("Concert de chanson", "Un très bel évènement pour tous", getDateByDays(500), getDateByDays(500, 2), montreal, User.TargetAudience.ALL),
        EventInputParams("Concert de star", "Un très bel évènement pour tous", getDateByDays(10), getDateByDays(20), paris, User.TargetAudience.ALL),
        EventInputParams("Concert de superstar", "Un très bel évènement pour tous", getDateByDays(17), getDateByDays(17, 5), newYork, User.TargetAudience.ALL),
        EventInputParams("Concert de rockstar", "Un très bel évènement pour tous", getDateByDays(17), getDateByDays(17, 1), quebec, User.TargetAudience.ALL),
        EventInputParams("Concert d'opéra", "Un très bel évènement pour tous", getDateByDays(90), getDateByDays(90, 48), montreal, User.TargetAudience.ALL),
        EventInputParams("Concert de musique", "Un très bel évènement pour tous", getDateByDays(200), getDateByDays(200, 10), paris, User.TargetAudience.ALL),
        EventInputParams("Concert de chanson", "Un très bel évènement pour tous", getDateByDays(500), getDateByDays(502, 2), montreal, User.TargetAudience.ALL),

        // test teenager
        EventInputParams("Rencontre avec ta star tiktok préférée", "Les ados adorent ça !", getDateByDays(5), getDateByDays(5, 4), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Un évènement pour ados", "Un très bel évènement pour les ados", getDateByDays(50), getDateByDays(50, 2), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Rencontre avec ta star tiktok préférée", "Les ados adorent ça !", getDateByDays(0), getDateByDays(0, 4), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Un évènement pour ados", "Un très bel évènement pour les ados", getDateByDays(80), getDateByDays(80, 2), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Rencontre avec ta star tiktok préférée", "Les ados adorent ça !", getDateByDays(5), getDateByDays(5, 4), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Un évènement pour ados", "Un très bel évènement pour les ados", getDateByDays(50), getDateByDays(50, 2), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Rencontre avec ta star tiktok préférée", "Les ados adorent ça !", getDateByDays(0), getDateByDays(0, 4), montreal, User.TargetAudience.TEENAGER),
        EventInputParams("Un évènement pour ados", "Un très bel évènement pour les ados", getDateByDays(80), getDateByDays(80, 2), montreal, User.TargetAudience.TEENAGER),
    )
}

fun getDateByDays(days: Int = 0, hours: Int = 0): Date {
    return getDateByHours(days * 24 + hours)
}

fun getDateByHours(hours: Int = 0): Date {
    return Calendar.getInstance().time.apply {
        time += hours * 60 * 60 * 1000
    }
}*/
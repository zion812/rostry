package com.rio.rostry.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility functions for date and time formatting
 */

/**
 * Format timestamp to readable date string
 */
fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Format timestamp to readable date and time string
 */
fun formatDateTime(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Format duration in milliseconds to readable string
 */
fun formatDuration(durationMs: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays(durationMs)
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

/**
 * Format time ago string (e.g., "2 hours ago")
 */
fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        seconds > 30 -> "${seconds}s ago"
        else -> "Just now"
    }
}

/**
 * Get age in weeks from birth timestamp
 */
fun getAgeInWeeks(birthTimestamp: Long): Int {
    val ageMs = System.currentTimeMillis() - birthTimestamp
    return TimeUnit.MILLISECONDS.toDays(ageMs).toInt() / 7
}

/**
 * Get age in days from birth timestamp
 */
fun getAgeInDays(birthTimestamp: Long): Int {
    val ageMs = System.currentTimeMillis() - birthTimestamp
    return TimeUnit.MILLISECONDS.toDays(ageMs).toInt()
}

/**
 * Check if date is today
 */
fun isToday(timestamp: Long): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

/**
 * Check if date is this week
 */
fun isThisWeek(timestamp: Long): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           today.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR)
}

/**
 * Get start of day timestamp
 */
fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

/**
 * Get end of day timestamp
 */
fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return calendar.timeInMillis
}

/**
 * Get start of week timestamp
 */
fun getStartOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

/**
 * Get start of month timestamp
 */
fun getStartOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

/**
 * Add days to timestamp
 */
fun addDays(timestamp: Long, days: Int): Long {
    return timestamp + (days * 24 * 60 * 60 * 1000L)
}

/**
 * Add weeks to timestamp
 */
fun addWeeks(timestamp: Long, weeks: Int): Long {
    return addDays(timestamp, weeks * 7)
}

/**
 * Add months to timestamp
 */
fun addMonths(timestamp: Long, months: Int): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        add(Calendar.MONTH, months)
    }
    return calendar.timeInMillis
}

/**
 * Format age string based on lifecycle stage
 */
fun formatAge(birthTimestamp: Long): String {
    val ageInDays = getAgeInDays(birthTimestamp)
    val ageInWeeks = getAgeInWeeks(birthTimestamp)
    
    return when {
        ageInDays < 7 -> "${ageInDays}d"
        ageInWeeks < 52 -> "${ageInWeeks}w"
        else -> "${ageInWeeks / 52}y ${ageInWeeks % 52}w"
    }
}

/**
 * Calculate days between two timestamps
 */
fun daysBetween(startTimestamp: Long, endTimestamp: Long): Int {
    val diffMs = kotlin.math.abs(endTimestamp - startTimestamp)
    return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
}

/**
 * Check if timestamp is in the future
 */
fun isFuture(timestamp: Long): Boolean {
    return timestamp > System.currentTimeMillis()
}

/**
 * Check if timestamp is in the past
 */
fun isPast(timestamp: Long): Boolean {
    return timestamp < System.currentTimeMillis()
}

/**
 * Get relative date string (Today, Yesterday, etc.)
 */
fun getRelativeDateString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val daysDiff = daysBetween(timestamp, now)
    
    return when {
        isToday(timestamp) -> "Today"
        daysDiff == 1 && timestamp < now -> "Yesterday"
        daysDiff == 1 && timestamp > now -> "Tomorrow"
        daysDiff < 7 && timestamp < now -> "$daysDiff days ago"
        daysDiff < 7 && timestamp > now -> "In $daysDiff days"
        else -> formatDate(timestamp)
    }
}
package com.rio.rostry.data.model

/**
 * Verification status for farms, fowls, and other entities
 */
enum class VerificationStatus(val displayName: String, val description: String) {
    PENDING("Pending Verification", "Awaiting verification review"),
    UNDER_REVIEW("Under Review", "Currently being reviewed by authorities"),
    VERIFIED("Verified", "Successfully verified and approved"),
    REJECTED("Rejected", "Verification request was rejected"),
    EXPIRED("Expired", "Verification has expired and needs renewal"),
    SUSPENDED("Suspended", "Verification temporarily suspended")
}

/**
 * Performance rating enum for various metrics
 */
enum class PerformanceRating(val displayName: String, val color: String, val score: Int) {
    OUTSTANDING("Outstanding", "#4CAF50", 5),
    EXCELLENT("Excellent", "#8BC34A", 4),
    GOOD("Good", "#CDDC39", 3),
    AVERAGE("Average", "#FFC107", 2),
    BELOW_AVERAGE("Below Average", "#FF5722", 1)
}
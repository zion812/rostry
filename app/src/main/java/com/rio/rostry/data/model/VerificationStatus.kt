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


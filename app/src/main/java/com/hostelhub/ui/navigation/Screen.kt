package com.hostelhub.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{token}") {
        fun createRoute(token: String) = "reset_password/$token"
    }
    
    // Main screens
    object Home : Screen("home")
    object HostelDetail : Screen("hostel/{hostelId}") {
        fun createRoute(hostelId: String) = "hostel/$hostelId"
    }
    object Search : Screen("search")
    object Filters : Screen("filters")
    
    // Booking screens
    object BookingForm : Screen("booking/{hostelId}") {
        fun createRoute(hostelId: String) = "booking/$hostelId"
    }
    object BookingHistory : Screen("booking_history")
    object BookingDetail : Screen("booking_detail/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }
    
    // Profile screens
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object BankDetails : Screen("bank_details")
    object IdentityVerification : Screen("identity_verification")
    
    // Chat screens
    object ChatList : Screen("chat_list")
    object ChatRoom : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    
    // Other screens
    object Notifications : Screen("notifications")
    object Reviews : Screen("reviews/{hostelId}") {
        fun createRoute(hostelId: String) = "reviews/$hostelId"
    }
    object Settings : Screen("settings")
    object Wishlist : Screen("wishlist")
    
    // New screens
    object PropertyListing : Screen("property_listing")
    object RoommateMatching : Screen("roommate_matching")
    object Agreements : Screen("agreements")
    object AgreementDetail : Screen("agreement/{agreementId}") {
        fun createRoute(agreementId: String) = "agreement/$agreementId"
    }
    object MapSearch : Screen("map_search")
    object AISearch : Screen("ai_search")
    object FraudReport : Screen("fraud_report/{hostelId}") {
        fun createRoute(hostelId: String) = "fraud_report/$hostelId"
    }
    object Payment : Screen("payment/{bookingId}") {
        fun createRoute(bookingId: String) = "payment/$bookingId"
    }

    // Admin & Moderation Screens (Module 2)
    object AdminDashboard : Screen("admin_dashboard")
    object UserManagement : Screen("user_management")
    object ModerationQueue : Screen("moderation_queue")

    // Owner Property Management Screens (Module 3)
    object OwnerHostelList : Screen("owner_hostel_list")
    object EditProperty : Screen("edit_property/{hostelId}") {
        fun createRoute(hostelId: String) = "edit_property/$hostelId"
    }

    // Owner Payment Verification Screens (Module 8)
    object BookingVerificationDashboard : Screen("booking_verification_dashboard")

    // Owner Review Management (Module 5)
    object OwnerReviewManagement : Screen("owner_review_management")

    // Module 10: Price Guidance (Fair Rent Estimator)
    object FairRentEstimator : Screen("fair_rent_estimator")

    // Module 11: Trust Score & Fraud Detection
    object AdminFraudDashboard : Screen("admin_fraud_dashboard")
}


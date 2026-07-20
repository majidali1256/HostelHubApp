package com.hostelhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hostelhub.ui.auth.LoginScreen
import com.hostelhub.ui.auth.LoginViewModel
import com.hostelhub.ui.auth.SignupScreen
import com.hostelhub.ui.auth.ForgotPasswordScreen
import com.hostelhub.ui.home.HomeScreen
import com.hostelhub.ui.hostel.HostelDetailScreen
import com.hostelhub.ui.hostel.ReviewsScreen
import com.hostelhub.ui.booking.BookingFormScreen
import com.hostelhub.ui.booking.BookingHistoryScreen
import com.hostelhub.ui.booking.BookingDetailScreen
import com.hostelhub.ui.booking.PaymentInstructionsScreen
import com.hostelhub.ui.profile.ProfileScreen
import com.hostelhub.ui.profile.EditProfileScreen
import com.hostelhub.ui.profile.BankDetailsScreen
import com.hostelhub.ui.profile.IdentityVerificationScreen
import com.hostelhub.ui.profile.SettingsScreen
import com.hostelhub.ui.chat.ChatListScreen
import com.hostelhub.ui.chat.ChatRoomScreen
import com.hostelhub.ui.notifications.NotificationsScreen
import com.hostelhub.ui.search.AISearchScreen
import com.hostelhub.ui.owner.OwnerHostelListScreen
import com.hostelhub.ui.search.FiltersScreen
import com.hostelhub.ui.fraud.FraudReportScreen
import com.hostelhub.ui.admin.AdminDashboardScreen
import com.hostelhub.ui.admin.UserManagementScreen
import com.hostelhub.ui.admin.ModerationQueueScreen
import com.hostelhub.ui.admin.AdminFraudDashboardScreen
import com.hostelhub.ui.fairrent.FairRentEstimatorScreen

@Composable
fun HostelHubNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState(initial = false)
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        // ===== AUTH SCREENS =====
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // ===== MAIN SCREENS =====
        composable(Screen.Home.route) { backStackEntry ->
            val filterOptions = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<com.hostelhub.ui.search.FilterOptions>("filterOptions")
            HomeScreen(
                onNavigateToHostel = { hostelId ->
                    navController.navigate(Screen.HostelDetail.createRoute(hostelId))
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToChat = { navController.navigate(Screen.ChatList.route) },
                onNavigateToBookings = { navController.navigate(Screen.BookingHistory.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToFilters = { navController.navigate(Screen.Filters.route) },
                onNavigateToMap = { navController.navigate(Screen.MapSearch.route) },
                onNavigateRoute = { route -> navController.navigate(route) },
                appliedFilters = filterOptions,
                onClearFilters = {
                    navController.currentBackStackEntry?.savedStateHandle?.remove<com.hostelhub.ui.search.FilterOptions>("filterOptions")
                }
            )
        }
        
        // ===== HOSTEL SCREENS =====
        composable(
            route = Screen.HostelDetail.route,
            arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
            HostelDetailScreen(
                hostelId = hostelId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBooking = {
                    navController.navigate(Screen.BookingForm.createRoute(hostelId))
                },
                onNavigateToReviews = {
                    navController.navigate(Screen.Reviews.createRoute(hostelId))
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.ChatRoom.createRoute(conversationId))
                }
            )
        }
        
        composable(
            route = Screen.Reviews.route,
            arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
            ReviewsScreen(
                hostelId = hostelId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // ===== SEARCH & FILTER SCREENS =====
        composable(Screen.Search.route) {
            AISearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHostel = { hostelId ->
                    navController.navigate(Screen.HostelDetail.createRoute(hostelId))
                }
            )
        }
        
        composable(Screen.Filters.route) {
            FiltersScreen(
                onNavigateBack = { navController.popBackStack() },
                onApplyFilters = { filters ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("filterOptions", filters)
                    navController.popBackStack()
                }
            )
        }
        
        // ===== BOOKING SCREENS =====
        composable(
            route = Screen.BookingForm.route,
            arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
            BookingFormScreen(
                hostelId = hostelId,
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    navController.navigate(Screen.BookingHistory.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        
        composable(Screen.BookingHistory.route) {
            BookingHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBookingDetail = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
        
        composable(
            route = Screen.BookingDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingDetailScreen(
                bookingId = bookingId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPayment = { navController.navigate(Screen.Payment.createRoute(bookingId)) },
                onNavigateToAgreements = { navController.navigate(Screen.Agreements.route) },
                onNavigateToChat = { conversationId -> navController.navigate(Screen.ChatRoom.createRoute(conversationId)) },
                onCancelBooking = { navController.popBackStack() }
            )
        }
        
        // ===== PROFILE SCREENS =====
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToBankDetails = { navController.navigate(Screen.BankDetails.route) },
                onNavigateToIdentityVerification = { navController.navigate(Screen.IdentityVerification.route) },
                onNavigateToAdminDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToAdminFraudDashboard = { navController.navigate(Screen.AdminFraudDashboard.route) },
                onNavigateToOwnerHostelList = { navController.navigate(Screen.OwnerHostelList.route) },
                onNavigateToBookingVerification = { navController.navigate(Screen.BookingVerificationDashboard.route) },
                onNavigateToOwnerReviewManagement = { navController.navigate(Screen.OwnerReviewManagement.route) },
                onNavigateToFairRentEstimator = { navController.navigate(Screen.FairRentEstimator.route) },
                onNavigateToAgreements = { navController.navigate(Screen.Agreements.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BankDetails.route) {
            BankDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.IdentityVerification.route) {
            IdentityVerificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // ===== CHAT SCREENS =====
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.ChatRoom.createRoute(conversationId))
                },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
        
        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatRoomScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // ===== NOTIFICATIONS =====
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBooking = { _ ->
                    navController.navigate(Screen.BookingHistory.route)
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.ChatRoom.createRoute(conversationId))
                }
            )
        }
        
        // ===== FRAUD REPORT =====
        composable("fraud_report/{hostelId}") { backStackEntry ->
            val hostelId = backStackEntry.arguments?.getString("hostelId")
            FraudReportScreen(
                hostelId = hostelId,
                onNavigateBack = { navController.popBackStack() },
                onReportSubmitted = { navController.popBackStack() }
            )
        }
        
        // ===== NEW SCREENS & MODULE 3 OWNER HUB =====
        composable(Screen.OwnerHostelList.route) {
            OwnerHostelListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProperty = { navController.navigate(Screen.PropertyListing.route) },
                onNavigateToEditProperty = { id -> navController.navigate(Screen.EditProperty.createRoute(id)) },
                onNavigateToDetail = { id -> navController.navigate(Screen.HostelDetail.createRoute(id)) },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.PropertyListing.route) {
            com.hostelhub.ui.owner.PropertyListingScreen(
                hostelId = null,
                onNavigateBack = { navController.popBackStack() },
                onListingCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditProperty.route,
            arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hostelId = backStackEntry.arguments?.getString("hostelId")
            com.hostelhub.ui.owner.PropertyListingScreen(
                hostelId = hostelId,
                onNavigateBack = { navController.popBackStack() },
                onListingCreated = { navController.popBackStack() }
            )
        }
        
        composable(Screen.RoommateMatching.route) {
            com.hostelhub.ui.roommate.RoommateMatchingScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewProfile = { /* Navigate to profile */ },
                onSendRequest = { /* Send connection request */ }
            )
        }
        
        composable(Screen.Agreements.route) {
            com.hostelhub.ui.agreements.AgreementsScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewAgreement = { agreementId ->
                    navController.navigate(Screen.AgreementDetail.createRoute(agreementId))
                },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
        
        composable(
            route = Screen.AgreementDetail.route,
            arguments = listOf(navArgument("agreementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val agreementId = backStackEntry.arguments?.getString("agreementId") ?: ""
            com.hostelhub.ui.agreements.AgreementDetailScreen(
                agreementId = agreementId,
                onNavigateBack = { navController.popBackStack() },
                onSign = { navController.popBackStack() }
            )
        }
        
        composable(Screen.MapSearch.route) {
            com.hostelhub.ui.map.MapSearchScreen(
                hostels = emptyList(),
                onNavigateBack = { navController.popBackStack() },
                onHostelClick = { hostelId ->
                    navController.navigate(Screen.HostelDetail.createRoute(hostelId))
                },
                onRequestLocation = { /* Request location permission */ }
            )
        }
        
        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            PaymentInstructionsScreen(
                bookingId = bookingId,
                onNavigateBack = { navController.popBackStack() },
                onPaymentUploaded = { navController.popBackStack() }
            )
        }

        composable(Screen.BookingVerificationDashboard.route) {
            com.hostelhub.ui.owner.BookingVerificationDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OwnerReviewManagement.route) {
            com.hostelhub.ui.reviews.OwnerReviewManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== ADMIN & MODERATION PLATFORM (MODULE 2) =====
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }

        composable(Screen.UserManagement.route) {
            UserManagementScreen(navController = navController)
        }

        composable(Screen.ModerationQueue.route) {
            ModerationQueueScreen(navController = navController)
        }

        // Module 10: Price Guidance (Fair Rent Estimator)
        composable(Screen.FairRentEstimator.route) {
            FairRentEstimatorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }

        // Module 11: Trust Score & Fraud Detection
        composable(Screen.AdminFraudDashboard.route) {
            AdminFraudDashboardScreen(navController = navController)
        }
    }
}


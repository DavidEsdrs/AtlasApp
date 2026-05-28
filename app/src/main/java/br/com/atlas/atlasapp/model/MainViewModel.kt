package br.com.atlas.atlasapp.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    // =========================
    // USER
    // =========================

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // =========================
    // ROUTES
    // =========================

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute.asStateFlow()

    // =========================
    // REVIEWS
    // =========================

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    // =========================
    // CHECK-INS
    // =========================

    private val _checkIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val checkIns: StateFlow<List<CheckIn>> = _checkIns.asStateFlow()

    // =========================
    // BADGES
    // =========================

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    // =========================
    // USER ACTIONS
    // =========================

    fun setUser(user: User) {
        _currentUser.value = user
    }

    // =========================
    // ROUTE ACTIONS
    // =========================

    fun setRoutes(routeList: List<Route>) {
        _routes.value = routeList
    }

    fun selectRoute(route: Route) {
        _selectedRoute.value = route
    }

    fun addRoute(route: Route) {
        _routes.value += route
    }

    fun removeRoute(routeId: String) {
        _routes.value = _routes.value.filterNot { it.id == routeId }
    }

    // =========================
    // REVIEW ACTIONS
    // =========================

    fun addReview(review: Review) {
        _reviews.value += review
    }

    // =========================
    // CHECK-IN ACTIONS
    // =========================

    fun addCheckIn(checkIn: CheckIn) {
        _checkIns.value += checkIn
    }

    // =========================
    // BADGE ACTIONS
    // =========================

    fun addBadge(badge: Badge) {
        _badges.value += badge
    }

    // =========================
    // CLEAR
    // =========================

    fun logout() {
        _currentUser.value = null
        _selectedRoute.value = null
        _reviews.value = emptyList()
        _checkIns.value = emptyList()
        _badges.value = emptyList()
    }
}
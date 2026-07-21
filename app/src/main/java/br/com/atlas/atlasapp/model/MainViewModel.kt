package br.com.atlas.atlasapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.atlas.atlasapp.data.repository.RouteRepository
import br.com.atlas.atlasapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val routeRepository = RouteRepository()
    private val userRepository = UserRepository()
    private var hasAttemptedSeed = false

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

    private val _routesLoading = MutableStateFlow(false)
    val routesLoading: StateFlow<Boolean> = _routesLoading.asStateFlow()

    private val _routesError = MutableStateFlow<String?>(null)
    val routesError: StateFlow<String?> = _routesError.asStateFlow()

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute.asStateFlow()

    private val _routeCreator = MutableStateFlow<User?>(null)
    val routeCreator: StateFlow<User?> = _routeCreator.asStateFlow()

    private val _detailsLoading = MutableStateFlow(false)
    val detailsLoading: StateFlow<Boolean> = _detailsLoading.asStateFlow()

    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError: StateFlow<String?> = _detailsError.asStateFlow()

    private val _exploreRoutes = MutableStateFlow<List<Route>>(emptyList())
    val exploreRoutes: StateFlow<List<Route>> = _exploreRoutes.asStateFlow()

    private val _exploreLoading = MutableStateFlow(false)
    val exploreLoading: StateFlow<Boolean> = _exploreLoading.asStateFlow()

    private val _exploreError = MutableStateFlow<String?>(null)
    val exploreError: StateFlow<String?> = _exploreError.asStateFlow()

    private val _selectedExploreCategory = MutableStateFlow<RouteCategory?>(null)
    val selectedExploreCategory: StateFlow<RouteCategory?> = _selectedExploreCategory.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    private val _createdRoutes = MutableStateFlow<List<Route>>(emptyList())
    val createdRoutes: StateFlow<List<Route>> = _createdRoutes.asStateFlow()

    private val _completedRoutes = MutableStateFlow<List<Route>>(emptyList())
    val completedRoutes: StateFlow<List<Route>> = _completedRoutes.asStateFlow()

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

    fun loadRoutes() {
        if (_routesLoading.value) return

        viewModelScope.launch {
            _routesLoading.value = true
            _routesError.value = null

            routeRepository.getAllRoutes()
                .onSuccess { loadedRoutes ->
                    if (loadedRoutes.isEmpty()) {
                        seedInitialRoutesIfEmpty()
                        routeRepository.getAllRoutes()
                            .onSuccess { _routes.value = it }
                            .onFailure { _routesError.value = it.message ?: "Erro ao carregar rotas" }
                    } else {
                        _routes.value = loadedRoutes
                    }
                }
                .onFailure { _routesError.value = it.message ?: "Erro ao carregar rotas" }

            _routesLoading.value = false
        }
    }

    fun loadRouteDetails(routeId: String?) {
        if (_detailsLoading.value) return

        if (routeId.isNullOrBlank()) {
            _detailsError.value = "Rota invalida"
            _selectedRoute.value = null
            _routeCreator.value = null
            return
        }

        viewModelScope.launch {
            _detailsLoading.value = true
            _detailsError.value = null
            _routeCreator.value = null

            routeRepository.getRouteById(routeId)
                .onSuccess { route ->
                    _selectedRoute.value = route

                    if (route.creatorId.isNotBlank()) {
                        userRepository.getUserById(route.creatorId)
                            .onSuccess { creator -> _routeCreator.value = creator }
                    }
                }
                .onFailure {
                    _selectedRoute.value = null
                    _detailsError.value = it.message ?: "Erro ao carregar detalhes"
                }

            _detailsLoading.value = false
        }
    }

    fun clearRouteErrors() {
        _routesError.value = null
        _detailsError.value = null
        _exploreError.value = null
        _profileError.value = null
    }

    fun loadExploreRoutes(category: RouteCategory? = _selectedExploreCategory.value) {
        if (_exploreLoading.value) return

        viewModelScope.launch {
            _exploreLoading.value = true
            _exploreError.value = null
            _selectedExploreCategory.value = category

            val result = if (category == null) {
                routeRepository.getAllRoutes()
            } else {
                routeRepository.getRoutesByCategory(category.name)
            }

            result
                .onSuccess { _exploreRoutes.value = it }
                .onFailure {
                    _exploreRoutes.value = emptyList()
                    _exploreError.value = it.message ?: "Erro ao carregar rotas de exploracao"
                }

            _exploreLoading.value = false
        }
    }

    fun loadProfileData() {
        if (_profileLoading.value) return

        val uid = _currentUser.value?.id
        if (uid.isNullOrBlank()) {
            _profileError.value = "Usuario nao autenticado"
            _createdRoutes.value = emptyList()
            _completedRoutes.value = emptyList()
            return
        }

        viewModelScope.launch {
            _profileLoading.value = true
            _profileError.value = null

            userRepository.getUserById(uid)
                .onSuccess { refreshedUser ->
                    _currentUser.value = refreshedUser

                    routeRepository.getRoutesByCreator(uid)
                        .onSuccess { _createdRoutes.value = it }
                        .onFailure {
                            _createdRoutes.value = emptyList()
                            _profileError.value = it.message ?: "Erro ao carregar rotas criadas"
                        }

                    val completedById = refreshedUser.completedRoutes
                        .distinct()
                        .mapNotNull { completedRouteId ->
                            routeRepository.getRouteById(completedRouteId).getOrNull()
                        }
                    _completedRoutes.value = completedById
                }
                .onFailure {
                    _createdRoutes.value = emptyList()
                    _completedRoutes.value = emptyList()
                    _profileError.value = it.message ?: "Erro ao carregar perfil"
                }

            _profileLoading.value = false
        }
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
        _routes.value = emptyList()
        _routesError.value = null
        _selectedRoute.value = null
        _routeCreator.value = null
        _detailsError.value = null
        _detailsLoading.value = false
        _routesLoading.value = false
        _exploreRoutes.value = emptyList()
        _exploreLoading.value = false
        _exploreError.value = null
        _selectedExploreCategory.value = null
        _profileLoading.value = false
        _profileError.value = null
        _createdRoutes.value = emptyList()
        _completedRoutes.value = emptyList()
        hasAttemptedSeed = false
        _reviews.value = emptyList()
        _checkIns.value = emptyList()
        _badges.value = emptyList()
    }

    private suspend fun seedInitialRoutesIfEmpty() {
        if (hasAttemptedSeed) return
        hasAttemptedSeed = true

        val creatorId = _currentUser.value?.id ?: "seed-system"
        val seedRoutes = listOf(
            Route(
                creatorId = creatorId,
                title = "Sabores escondidos do centro",
                description = "Padarias e lanchonetes que resistem ao tempo.",
                category = RouteCategory.GASTRONOMIC,
                points = listOf(
                    RoutePoint(
                        id = "p1",
                        title = "Padaria Central",
                        description = "Parada para cafe e pao na chapa.",
                        latitude = -23.55052,
                        longitude = -46.63331,
                        order = 1
                    ),
                    RoutePoint(
                        id = "p2",
                        title = "Lanchonete da Esquina",
                        description = "Salgados classicos do centro.",
                        latitude = -23.5489,
                        longitude = -46.6388,
                        order = 2
                    )
                ),
                estimatedDurationMinutes = 150,
                rating = 4.8,
                totalRatings = 156
            ),
            Route(
                creatorId = creatorId,
                title = "Oasis urbanos secretos",
                description = "Pracas e jardins para respirar no meio da cidade.",
                category = RouteCategory.NATURE,
                points = listOf(
                    RoutePoint(
                        id = "p1",
                        title = "Praca das Flores",
                        description = "Area arborizada com bancos e sombra.",
                        latitude = -23.5614,
                        longitude = -46.6559,
                        order = 1
                    ),
                    RoutePoint(
                        id = "p2",
                        title = "Jardim da Colina",
                        description = "Vista elevada para o bairro historico.",
                        latitude = -23.5583,
                        longitude = -46.6496,
                        order = 2
                    )
                ),
                estimatedDurationMinutes = 90,
                rating = 4.6,
                totalRatings = 89
            ),
            Route(
                creatorId = creatorId,
                title = "Arte urbana em cada esquina",
                description = "Murais e grafites que contam historias locais.",
                category = RouteCategory.URBAN_ART,
                points = listOf(
                    RoutePoint(
                        id = "p1",
                        title = "Mural do Viaduto",
                        description = "Painel colorido com artistas da regiao.",
                        latitude = -23.5536,
                        longitude = -46.6411,
                        order = 1
                    ),
                    RoutePoint(
                        id = "p2",
                        title = "Beco Criativo",
                        description = "Galeria a ceu aberto com rotacao de obras.",
                        latitude = -23.5562,
                        longitude = -46.6445,
                        order = 2
                    )
                ),
                estimatedDurationMinutes = 120,
                rating = 4.7,
                totalRatings = 102
            )
        )

        seedRoutes.forEach { routeRepository.createRoute(it) }
    }
}
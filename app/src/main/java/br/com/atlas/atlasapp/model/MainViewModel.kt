package br.com.atlas.atlasapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.atlas.atlasapp.data.repository.CheckInRepository
import br.com.atlas.atlasapp.data.repository.RouteRepository
import br.com.atlas.atlasapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val routeRepository = RouteRepository()
    private val userRepository = UserRepository()
    private val checkInRepository = CheckInRepository()
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

    private val _createRouteLoading = MutableStateFlow(false)
    val createRouteLoading: StateFlow<Boolean> = _createRouteLoading.asStateFlow()

    private val _createRouteError = MutableStateFlow<String?>(null)
    val createRouteError: StateFlow<String?> = _createRouteError.asStateFlow()

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute.asStateFlow()

    private val _routeCreator = MutableStateFlow<User?>(null)
    val routeCreator: StateFlow<User?> = _routeCreator.asStateFlow()

    private val _detailsLoading = MutableStateFlow(false)
    val detailsLoading: StateFlow<Boolean> = _detailsLoading.asStateFlow()

    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError: StateFlow<String?> = _detailsError.asStateFlow()

    private val _routeCompletionCount = MutableStateFlow(0)
    val routeCompletionCount: StateFlow<Int> = _routeCompletionCount.asStateFlow()

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

    private val _currentRoutePointIndex = MutableStateFlow(0)
    val currentRoutePointIndex: StateFlow<Int> = _currentRoutePointIndex.asStateFlow()

    private val _checkInLoading = MutableStateFlow(false)
    val checkInLoading: StateFlow<Boolean> = _checkInLoading.asStateFlow()

    private val _checkInError = MutableStateFlow<String?>(null)
    val checkInError: StateFlow<String?> = _checkInError.asStateFlow()

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
                    ensureDefaultRoutesExist(loadedRoutes)

                    routeRepository.getAllRoutes()
                        .onSuccess { _routes.value = it }
                        .onFailure { _routesError.value = it.message ?: "Erro ao carregar rotas" }
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
            _routeCompletionCount.value = 0
            return
        }

        viewModelScope.launch {
            _detailsLoading.value = true
            _detailsError.value = null
            _routeCreator.value = null

            routeRepository.getRouteById(routeId)
                .onSuccess { route ->
                    _selectedRoute.value = route

                    val userId = _currentUser.value?.id
                    if (!userId.isNullOrBlank() && route.points.isNotEmpty()) {
                        checkInRepository.getCheckInsByUserAndRoute(userId, route.id)
                            .onSuccess { userRouteCheckIns ->
                                _routeCompletionCount.value = userRouteCheckIns.size / route.points.size
                            }
                            .onFailure {
                                _routeCompletionCount.value = 0
                            }
                    } else {
                        _routeCompletionCount.value = 0
                    }

                    if (route.creatorId.isNotBlank()) {
                        userRepository.getUserById(route.creatorId)
                            .onSuccess { creator -> _routeCreator.value = creator }
                    }
                }
                .onFailure {
                    _selectedRoute.value = null
                    _routeCompletionCount.value = 0
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
        _createRouteError.value = null
        _checkInError.value = null
    }

    fun createRoute(
        title: String,
        description: String,
        category: RouteCategory,
        points: List<RoutePoint>,
        onSuccess: () -> Unit = {}
    ) {
        if (_createRouteLoading.value) return

        val creatorId = _currentUser.value?.id
        if (creatorId.isNullOrBlank()) {
            _createRouteError.value = "Usuario nao autenticado"
            return
        }

        val sanitizedTitle = title.trim()
        val sanitizedDescription = description.trim()
        if (sanitizedTitle.isBlank() || sanitizedDescription.isBlank()) {
            _createRouteError.value = "Preencha titulo e descricao"
            return
        }

        if (points.isEmpty()) {
            _createRouteError.value = "Adicione pelo menos um ponto"
            return
        }

        val sanitizedPoints = points.mapIndexed { index, point ->
            RoutePoint(
                id = point.id.ifBlank { "p${index + 1}" },
                title = point.title.trim(),
                description = point.description.trim(),
                latitude = point.latitude,
                longitude = point.longitude,
                imageUrl = point.imageUrl,
                order = index + 1
            )
        }

        val hasBlankPointTitle = sanitizedPoints.any { it.title.isBlank() }
        if (hasBlankPointTitle) {
            _createRouteError.value = "Preencha o titulo de todos os pontos"
            return
        }

        val hasInvalidCoordinates = sanitizedPoints.any {
            it.latitude !in -90.0..90.0 || it.longitude !in -180.0..180.0
        }
        if (hasInvalidCoordinates) {
            _createRouteError.value = "Coordenadas invalidas nos pontos"
            return
        }

        viewModelScope.launch {
            _createRouteLoading.value = true
            _createRouteError.value = null

            val newRoute = Route(
                creatorId = creatorId,
                title = sanitizedTitle,
                description = sanitizedDescription,
                category = category,
                points = sanitizedPoints,
                estimatedDurationMinutes = 90,
                rating = 0.0,
                totalRatings = 0
            )

            routeRepository.createRoute(newRoute)
                .onSuccess { routeId ->
                    val createdRoute = newRoute.copy(id = routeId)

                    _routes.value = listOf(createdRoute) + _routes.value

                    val selectedCategory = _selectedExploreCategory.value
                    if (selectedCategory == null || selectedCategory == category) {
                        _exploreRoutes.value = listOf(createdRoute) + _exploreRoutes.value
                    }

                    _createdRoutes.value = listOf(createdRoute) + _createdRoutes.value

                    _currentUser.value = _currentUser.value?.let { user ->
                        if (user.createdRoutes.contains(routeId)) user else user.copy(createdRoutes = user.createdRoutes + routeId)
                    }

                    onSuccess()
                }
                .onFailure {
                    _createRouteError.value = it.message ?: "Erro ao criar rota"
                }

            _createRouteLoading.value = false
        }
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

    fun startSelectedRouteProgress() {
        _currentRoutePointIndex.value = 0
        _checkInError.value = null
    }

    fun registerCheckInForCurrentPoint(onRouteCompleted: () -> Unit = {}) {
        if (_checkInLoading.value) return

        val userId = _currentUser.value?.id
        if (userId.isNullOrBlank()) {
            _checkInError.value = "Usuario nao autenticado"
            return
        }

        val route = _selectedRoute.value
        if (route == null || route.id.isBlank()) {
            _checkInError.value = "Rota invalida para check-in"
            return
        }

        if (route.points.isEmpty()) {
            _checkInError.value = "Rota sem pontos para check-in"
            return
        }

        val currentIndex = _currentRoutePointIndex.value
        if (currentIndex >= route.points.size) {
            _checkInError.value = "Rota ja concluida"
            return
        }

        val currentPoint = route.points[currentIndex]

        viewModelScope.launch {
            _checkInLoading.value = true
            _checkInError.value = null

            val checkIn = CheckIn(
                userId = userId,
                routeId = route.id,
                pointId = currentPoint.id
            )

            checkInRepository.createCheckIn(checkIn)
                .onSuccess { createdCheckInId ->
                    _checkIns.value += checkIn.copy(id = createdCheckInId)

                    val nextIndex = currentIndex + 1
                    _currentRoutePointIndex.value = nextIndex

                    if (nextIndex >= route.points.size) {
                        val earnedPoints = route.points.size * 10

                        userRepository.addCompletedRoute(userId, route.id)
                        userRepository.addPointsToUser(userId, earnedPoints)

                        _currentUser.value = _currentUser.value?.let { user ->
                            val updatedCompletedRoutes = if (user.completedRoutes.contains(route.id)) {
                                user.completedRoutes
                            } else {
                                user.completedRoutes + route.id
                            }
                            user.copy(
                                points = user.points + earnedPoints,
                                completedRoutes = updatedCompletedRoutes
                            )
                        }

                        if (_completedRoutes.value.none { it.id == route.id }) {
                            _completedRoutes.value = listOf(route) + _completedRoutes.value
                        }

                        _routeCompletionCount.value = _routeCompletionCount.value + 1

                        onRouteCompleted()
                    }
                }
                .onFailure {
                    _checkInError.value = it.message ?: "Erro ao registrar check-in"
                }

            _checkInLoading.value = false
        }
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
        _routeCompletionCount.value = 0
        _detailsError.value = null
        _detailsLoading.value = false
        _currentRoutePointIndex.value = 0
        _checkInLoading.value = false
        _checkInError.value = null
        _routesLoading.value = false
        _createRouteLoading.value = false
        _createRouteError.value = null
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

    private fun buildSeedRoutes(creatorId: String): List<Route> {
        return listOf(
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
            ),
            Route(
                creatorId = creatorId,
                title = "Recife historico a pe",
                description = "Percurso curto para testar o mapa com pontos proximos e bem visiveis.",
                category = RouteCategory.HISTORICAL,
                points = listOf(
                    RoutePoint(
                        id = "p1",
                        title = "Marco Zero",
                        description = "Ponto de partida para o centro historico.",
                        latitude = -8.0632,
                        longitude = -34.8711,
                        order = 1
                    ),
                    RoutePoint(
                        id = "p2",
                        title = "Rua do Bom Jesus",
                        description = "Uma das ruas mais fotografadas do Recife Antigo.",
                        latitude = -8.0616,
                        longitude = -34.8700,
                        order = 2
                    ),
                    RoutePoint(
                        id = "p3",
                        title = "Paço do Frevo",
                        description = "Parada cultural para fechar o percurso.",
                        latitude = -8.0610,
                        longitude = -34.8708,
                        order = 3
                    )
                ),
                estimatedDurationMinutes = 45,
                rating = 4.9,
                totalRatings = 28
            ),
            Route(
                creatorId = creatorId,
                title = "Recife antigo completo",
                description = "Roteiro mais longo com varios pontos para testar o mapa e a linha da rota.",
                category = RouteCategory.HISTORICAL,
                points = listOf(
                    RoutePoint(
                        id = "p1",
                        title = "Marco Zero",
                        description = "Comeco do passeio no Recife Antigo.",
                        latitude = -8.0632,
                        longitude = -34.8711,
                        order = 1
                    ),
                    RoutePoint(
                        id = "p2",
                        title = "Cais do Sertao",
                        description = "Polo cultural e turistico ao lado do porto.",
                        latitude = -8.0638,
                        longitude = -34.8702,
                        order = 2
                    ),
                    RoutePoint(
                        id = "p3",
                        title = "Rua do Bom Jesus",
                        description = "Rua historica com casario colorido.",
                        latitude = -8.0616,
                        longitude = -34.8700,
                        order = 3
                    ),
                    RoutePoint(
                        id = "p4",
                        title = "Sinagoga Kahal Zur Israel",
                        description = "Um dos pontos historicos mais conhecidos da cidade.",
                        latitude = -8.0620,
                        longitude = -34.8718,
                        order = 4
                    ),
                    RoutePoint(
                        id = "p5",
                        title = "Paço do Frevo",
                        description = "Fechamento cultural do percurso.",
                        latitude = -8.0610,
                        longitude = -34.8708,
                        order = 5
                    )
                ),
                estimatedDurationMinutes = 75,
                rating = 4.8,
                totalRatings = 41
            )
        )
    }

    private suspend fun ensureDefaultRoutesExist(existingRoutes: List<Route>) {
        val existingTitles = existingRoutes.map { it.title }.toSet()
        val seedRoutes = buildSeedRoutes(_currentUser.value?.id ?: "seed-system")

        val missingSeedRoutes = seedRoutes.filterNot { it.title in existingTitles }
        if (missingSeedRoutes.isEmpty()) return

        missingSeedRoutes.forEach { routeRepository.createRoute(it) }
    }
}
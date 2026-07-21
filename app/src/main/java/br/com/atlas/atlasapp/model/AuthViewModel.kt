package br.com.atlas.atlasapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.atlas.atlasapp.data.repository.UserRepository
import br.com.atlas.atlasapp.services.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authService = AuthService()
    private val userRepository = UserRepository()

    private val _isLoggedIn = MutableStateFlow(authService.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCurrentUser(mainViewModel: MainViewModel? = null) {
        val uid = authService.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid)
                .onSuccess { mainViewModel?.setUser(it) }
        }
    }

    fun login(email: String, password: String, mainViewModel: MainViewModel) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Preencha e-mail e senha"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authService.loginWithEmail(email.trim(), password)
                .onSuccess { uid ->
                    userRepository.getUserById(uid)
                        .onSuccess { mainViewModel.setUser(it) }
                        .onFailure {
                            mainViewModel.setUser(User(id = uid, email = email.trim()))
                        }
                    _isLoggedIn.value = true
                }
                .onFailure { _error.value = it.message ?: "Erro ao entrar" }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, mainViewModel: MainViewModel) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Preencha todos os campos"
            return
        }
        if (password.length < 6) {
            _error.value = "Senha deve ter no mínimo 6 caracteres"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authService.registerWithEmail(email.trim(), password)
                .onSuccess { uid ->
                    val user = User(id = uid, name = name.trim(), email = email.trim())
                    userRepository.createUser(user)
                        .onSuccess {
                            mainViewModel.setUser(user)
                            _isLoggedIn.value = true
                        }
                        .onFailure { _error.value = it.message ?: "Erro ao criar perfil" }
                }
                .onFailure { _error.value = it.message ?: "Erro ao cadastrar" }
            _isLoading.value = false
        }
    }

    fun logout(mainViewModel: MainViewModel) {
        viewModelScope.launch {
            authService.logout()
            mainViewModel.logout()
            _isLoggedIn.value = false
            _error.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}

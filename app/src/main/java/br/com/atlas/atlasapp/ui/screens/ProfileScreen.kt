package br.com.atlas.atlasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.atlas.atlasapp.model.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val profileLoading by viewModel.profileLoading.collectAsState()
    val profileError by viewModel.profileError.collectAsState()
    val createdRoutes by viewModel.createdRoutes.collectAsState()
    val completedRoutes by viewModel.completedRoutes.collectAsState()

    LaunchedEffect(user?.id) {
        if (!user?.id.isNullOrBlank()) {
            viewModel.loadProfileData()
        }
    }

    val initials = user?.name
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.take(2)
        ?.joinToString("")
        ?: "?"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Perfil", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFFDAE9FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(user?.name ?: "Usuário", fontWeight = FontWeight.Bold)
            Text(
                user?.email ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("${user?.completedRoutes?.size ?: 0}", "Rotas\ncompletadas")
                StatCard("${user?.createdRoutes?.size ?: 0}", "Rotas criadas")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("${user?.points ?: 0}", "Pontos")
                StatCard("${user?.badges?.size ?: 0}", "Badges")
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text("Resumo de rotas", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Criadas: ${createdRoutes.size}  Completadas: ${completedRoutes.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (profileLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (!profileLoading && profileError != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(profileError ?: "Erro ao carregar perfil", color = MaterialTheme.colorScheme.error)
                    FilledTonalButton(onClick = {
                        viewModel.clearRouteErrors()
                        viewModel.loadProfileData()
                    }) {
                        Text("Tentar novamente")
                    }
                }
            }
        }
        item {
            Text(
                "Rotas criadas",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold
            )
        }
        if (!profileLoading && createdRoutes.isEmpty()) {
            item {
                Text(
                    "Nenhuma rota criada ainda",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(createdRoutes, key = { it.id }) { route ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(route.title, fontWeight = FontWeight.SemiBold)
                    Text(
                        route.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Text(
                "Rotas completadas",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold
            )
        }
        if (!profileLoading && completedRoutes.isEmpty()) {
            item {
                Text(
                    "Nenhuma rota completada ainda",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(completedRoutes, key = { it.id }) { route ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(route.title, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${route.points.size} pontos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null)
                Icon(Icons.Default.Map, contentDescription = null)
                Icon(Icons.Default.Search, contentDescription = null)
                Icon(Icons.Default.AccountCircle, contentDescription = null)
            }
        }
        item {
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Sair")
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

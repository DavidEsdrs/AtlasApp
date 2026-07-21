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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import br.com.atlas.atlasapp.model.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsScreen(
    viewModel: MainViewModel,
    routeId: String?,
    onBack: () -> Unit,
    onStartRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val route by viewModel.selectedRoute.collectAsState()
    val creator by viewModel.routeCreator.collectAsState()
    val completionCount by viewModel.routeCompletionCount.collectAsState()
    val isLoading by viewModel.detailsLoading.collectAsState()
    val error by viewModel.detailsError.collectAsState()

    LaunchedEffect(routeId) {
        viewModel.loadRouteDetails(routeId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (error != null) {
                Text(error ?: "Erro ao carregar detalhes", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            val currentRoute = route
            if (currentRoute == null) {
                Text(
                    "Rota nao encontrada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFEFEFE8), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.DarkGray)
                }
                Text(currentRoute.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                if (completionCount > 0) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (completionCount == 1) "Rota concluida 1 vez" else "Rota concluida $completionCount vezes"
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    )
                }

                Text(
                    currentRoute.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Criado por ${creator?.name?.ifBlank { "Criador desconhecido" } ?: "Criador desconhecido"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("${currentRoute.points.size}", "Pontos de parada", Modifier.weight(1f))
                    StatCard(formatDuration(currentRoute.estimatedDurationMinutes), "Duracao", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(String.format("%.1f", currentRoute.rating), "Avaliacao", Modifier.weight(1f))
                    StatCard("${currentRoute.totalRatings}", "Avaliacoes", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(onClick = onStartRoute, modifier = Modifier.fillMaxWidth()) {
                Text("Iniciar rota")
            }
        }
    }
}

private fun formatDuration(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "-"

    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        if (minutes > 0) "${hours}h${minutes}m" else "${hours}h"
    } else {
        "${minutes}m"
    }
}

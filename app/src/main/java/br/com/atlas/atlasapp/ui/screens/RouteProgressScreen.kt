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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import br.com.atlas.atlasapp.model.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteProgressScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRouteCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val route by viewModel.selectedRoute.collectAsState()
    val currentPointIndex by viewModel.currentRoutePointIndex.collectAsState()
    val isLoading by viewModel.checkInLoading.collectAsState()
    val error by viewModel.checkInError.collectAsState()

    LaunchedEffect(route?.id) {
        if (route != null) {
            viewModel.startSelectedRouteProgress()
        }
    }

    val currentRoute = route
    val totalPoints = currentRoute?.points?.size ?: 0
    val checkedPoints = currentPointIndex.coerceAtMost(totalPoints)
    val progressPercent = if (totalPoints > 0) (checkedPoints * 100) / totalPoints else 0
    val nextPoint = if (currentRoute != null && checkedPoints < totalPoints) {
        currentRoute.points[checkedPoints]
    } else {
        null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Em andamento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (currentRoute == null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nenhuma rota em andamento", fontWeight = FontWeight.SemiBold)
                Text(
                    "Volte aos detalhes para iniciar uma rota.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onBack) { Text("Voltar") }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFEFEFE8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Text("Mapa de navegacao")
                    AssistChip(onClick = {}, label = { Text("Use o mapa para seguir ao proximo ponto") })
                }
            }

            Text(
                "$checkedPoints de $totalPoints pontos",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("$progressPercent% completo", fontWeight = FontWeight.Bold)

            if (nextPoint != null) {
                Text("Proximo: ${nextPoint.title}", fontWeight = FontWeight.SemiBold)
                Text(
                    if (nextPoint.description.isBlank()) "Sem descricao para este ponto" else nextPoint.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Rota concluida", fontWeight = FontWeight.SemiBold)
                Text(
                    "Todos os pontos foram registrados. Boa exploracao!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dica: cada check-in soma pontos no seu perfil.", Modifier.padding(12.dp))
            }

            if (error != null) {
                Text(error ?: "Erro ao registrar check-in", color = MaterialTheme.colorScheme.error)
            }

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Voltar") }
                if (nextPoint != null) {
                    Button(
                        onClick = {
                            viewModel.registerCheckInForCurrentPoint()
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isLoading) "Registrando..." else "Fazer check-in")
                    }
                } else {
                    Button(onClick = onRouteCompleted, modifier = Modifier.weight(1f)) {
                        Text("Finalizar")
                    }
                }
            }
        }
    }
}

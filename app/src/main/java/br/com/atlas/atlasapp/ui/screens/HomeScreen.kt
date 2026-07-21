package br.com.atlas.atlasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.atlas.atlasapp.model.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val routes by viewModel.routes.collectAsState()
    val isLoading by viewModel.routesLoading.collectAsState()
    val error by viewModel.routesError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRoutes()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Atlas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rotas proximas", fontWeight = FontWeight.SemiBold)
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
            }
        }

        if (!isLoading && error != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(error ?: "Erro ao carregar rotas", color = MaterialTheme.colorScheme.error)
                    FilledTonalButton(onClick = {
                        viewModel.clearRouteErrors()
                        viewModel.loadRoutes()
                    }) {
                        Text("Tentar novamente")
                    }
                }
            }
        }

        items(routes, key = { it.id }) { route ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetails(route.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFFECEFF4), RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(route.title, fontWeight = FontWeight.Bold)
                    Text(
                        route.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isLoading && error == null && routes.isEmpty()) {
            item {
                Text(
                    "Nenhuma rota encontrada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

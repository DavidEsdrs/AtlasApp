package br.com.atlas.atlasapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.atlas.atlasapp.model.MainViewModel
import br.com.atlas.atlasapp.model.RouteCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: MainViewModel,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val routes by viewModel.exploreRoutes.collectAsState()
    val selectedCategory by viewModel.selectedExploreCategory.collectAsState()
    val isLoading by viewModel.exploreLoading.collectAsState()
    val error by viewModel.exploreError.collectAsState()
    var query by remember { mutableStateOf("") }

    val filteredRoutes = remember(routes, query) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            routes
        } else {
            routes.filter {
                it.title.lowercase().contains(normalizedQuery) ||
                    it.description.lowercase().contains(normalizedQuery)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadExploreRoutes()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Explorar") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar rotas...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Text("Categorias populares", fontWeight = FontWeight.SemiBold) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    RouteCategory.values().take(2).forEach { category ->
                        FilterChip(
                            onClick = {
                                val nextCategory = if (selectedCategory == category) null else category
                                viewModel.loadExploreRoutes(nextCategory)
                            },
                            selected = selectedCategory == category,
                            label = { Text(category.toDisplayName()) }
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    RouteCategory.values().drop(2).forEach { category ->
                        FilterChip(
                            onClick = {
                                val nextCategory = if (selectedCategory == category) null else category
                                viewModel.loadExploreRoutes(nextCategory)
                            },
                            selected = selectedCategory == category,
                            label = { Text(category.toDisplayName()) }
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!isLoading && error != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(error ?: "Erro ao carregar exploracao", color = MaterialTheme.colorScheme.error)
                        FilledTonalButton(onClick = {
                            viewModel.clearRouteErrors()
                            viewModel.loadExploreRoutes(selectedCategory)
                        }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            if (!isLoading && error == null) {
                item { Text("Mais exploradas", fontWeight = FontWeight.SemiBold) }
            }

            items(filteredRoutes, key = { it.id }) { route ->
                ElevatedCard(modifier = Modifier.fillMaxWidth().clickable { onOpenDetails(route.id) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(route.title, fontWeight = FontWeight.Medium)
                            Text(
                                "${route.points.size} pontos - ${route.estimatedDurationMinutes} min",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (!isLoading && error == null && filteredRoutes.isEmpty()) {
                item {
                    Text(
                        "Nenhuma rota encontrada para os filtros atuais",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

private fun RouteCategory.toDisplayName(): String {
    return when (this) {
        RouteCategory.GASTRONOMIC -> "Gastronomica"
        RouteCategory.HISTORICAL -> "Historica"
        RouteCategory.URBAN_ART -> "Arte urbana"
        RouteCategory.NATURE -> "Natureza"
        RouteCategory.ARCHITECTURE -> "Arquitetura"
    }
}

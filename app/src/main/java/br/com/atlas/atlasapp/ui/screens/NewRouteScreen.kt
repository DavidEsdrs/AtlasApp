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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
fun NewRouteScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(RouteCategory.HISTORICAL.name) }

    val isLoading by viewModel.createRouteLoading.collectAsState()
    val error by viewModel.createRouteError.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Nova rota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = {
                                val category = RouteCategory.valueOf(selectedCategory)
                                viewModel.createRoute(
                                    title = title,
                                    description = description,
                                    category = category,
                                    onSuccess = onCreated
                                )
                            }
                        ) {
                            Text("Salvar")
                        }
                    }
                }
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
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (error != null) viewModel.clearRouteErrors()
                },
                label = { Text("Nome da rota") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    if (error != null) viewModel.clearRouteErrors()
                },
                label = { Text("Descricao") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Categoria", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouteCategory.values().take(2).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                        label = { Text(category.toDisplayName()) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouteCategory.values().drop(2).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                        label = { Text(category.toDisplayName()) }
                    )
                }
            }

            if (error != null) {
                Text(error ?: "Erro ao criar rota", color = MaterialTheme.colorScheme.error)
            }

            Text("Pontos da rota", fontWeight = FontWeight.Bold)
            Text(
                "Neste MVP, os pontos serao adicionados em uma proxima etapa.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFEFEFE8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Text("Previa do mapa")
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
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

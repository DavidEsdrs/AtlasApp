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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import br.com.atlas.atlasapp.model.RoutePoint
import br.com.atlas.atlasapp.ui.components.RouteMapView
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

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
    var pointTitle by rememberSaveable { mutableStateOf("") }
    var pointDescription by rememberSaveable { mutableStateOf("") }
    var pendingLatLng by remember { mutableStateOf<LatLng?>(null) }
    var pointFormError by rememberSaveable { mutableStateOf<String?>(null) }
    var points by remember { mutableStateOf<List<RoutePoint>>(emptyList()) }

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
                                    points = points,
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
                .verticalScroll(rememberScrollState())
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
                "Adicione os pontos na ordem da rota. Cada check-in vai seguir essa sequencia.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            OutlinedTextField(
                value = pointTitle,
                onValueChange = {
                    pointTitle = it
                    pointFormError = null
                },
                label = { Text("Nome do ponto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pointDescription,
                onValueChange = {
                    pointDescription = it
                    pointFormError = null
                },
                label = { Text("Descricao do ponto (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Text(
                text = if (pendingLatLng == null)
                    "Toque no mapa para selecionar a localizacao do ponto"
                else
                    "Local selecionado: ${"%,.6f".format(pendingLatLng!!.latitude)}, ${"%,.6f".format(pendingLatLng!!.longitude)}",
                fontSize = 12.sp,
                color = if (pendingLatLng == null)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.primary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFEFEFE8), RoundedCornerShape(8.dp))
            ) {
                RouteMapView(
                    points = points,
                    pendingPoint = pendingLatLng,
                    onMapClick = { latLng ->
                        pendingLatLng = latLng
                        pointFormError = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (pointFormError != null) {
                Text(pointFormError ?: "Erro no ponto", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    when {
                        pointTitle.trim().isBlank() -> pointFormError = "Informe o nome do ponto"
                        pendingLatLng == null -> pointFormError = "Toque no mapa para definir a localizacao"
                        else -> {
                            points = points + RoutePoint(
                                id = UUID.randomUUID().toString(),
                                title = pointTitle.trim(),
                                description = pointDescription.trim(),
                                latitude = pendingLatLng!!.latitude,
                                longitude = pendingLatLng!!.longitude,
                                order = points.size + 1
                            )

                            pointTitle = ""
                            pointDescription = ""
                            pendingLatLng = null
                            pointFormError = null
                            if (error != null) viewModel.clearRouteErrors()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar ponto")
            }

            Text(
                if (points.isEmpty()) "Nenhum ponto adicionado" else "${points.size} ponto(s) adicionados",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            points.forEachIndexed { index, point ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${index + 1}. ${point.title}", fontWeight = FontWeight.SemiBold)
                        if (point.description.isNotBlank()) {
                            Text(point.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "Lat ${point.latitude}, Lng ${point.longitude}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                points = points
                                    .filterNot { it.id == point.id }
                                    .mapIndexed { reorderedIndex, existingPoint ->
                                        existingPoint.copy(order = reorderedIndex + 1)
                                    }
                            }
                        ) {
                            Text("Remover")
                        }
                    }
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

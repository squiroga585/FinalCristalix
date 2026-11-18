package me.mariahernandez.cristalia.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.mariahernandez.cristalia.celdaunitaria.Paleta

@Composable
fun PerfilScreen(
    usuario: String? = null,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuario) {
        viewModel.inicializarUsuario(usuario)
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                PerfilEventoUi.Volver -> onVolver()
                PerfilEventoUi.CerrarSesion -> onCerrarSesion()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Fondo)
            .drawBehind {
                val step = 28.dp.toPx()
                val strokeWidth = 0.5.dp.toPx()
                val gridColor = Color(0xFF182030).copy(alpha = 0.35f)

                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = strokeWidth)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = strokeWidth)
                    y += step
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Botón atrás y Título (Sin botón de configuración como se solicitó)
            HeaderBar(onVolver = viewModel::onVolverClick)

            // Tarjeta de perfil (Sin foto de perfil como se solicitó)
            PerfilUserCard(
                nombreUsuario = state.nombreUsuario,
                totalCalculos = state.totalCalculos,
                totalDescargas = state.totalDescargas
            )

            // Selector de Tabs: Descargas vs Historial
            TabSelectorRow(
                tabSeleccionado = state.tabSeleccionado,
                onSelectTab = viewModel::seleccionarTab
            )

            // Título de la sección
            val listaAMostrar = if (state.tabSeleccionado == TabPerfil.DESCARGAS) {
                state.descargasFiltradas
            } else {
                state.historial
            }

            val tituloSeccion = if (state.tabSeleccionado == TabPerfil.DESCARGAS) {
                "DESCARGAS GUARDADAS (${listaAMostrar.size})"
            } else {
                "HISTORIAL DE OPERACIONES (${listaAMostrar.size})"
            }

            Text(
                text = tituloSeccion,
                color = Color(0xFF6B7B96),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            // Lista de operaciones
            if (listaAMostrar.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Paleta.Tarjeta,
                    border = BorderStroke(1.dp, Color(0xFF1E2638)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.tabSeleccionado == TabPerfil.DESCARGAS)
                                "No tienes elementos guardados en descargas."
                            else "No hay operaciones registradas en el historial.",
                            color = Paleta.TextoSecundario,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listaAMostrar.forEach { item ->
                        CardOperacionItem(
                            item = item,
                            onToggleFavorito = { viewModel.toggleFavorito(item.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón: Cerrar sesión de investigador
            Surface(
                onClick = viewModel::onCerrarSesionClick,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF131825),
                border = BorderStroke(1.dp, Color(0xFF263044)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFF9AA3B5),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión de investigador",
                        color = Color(0xFF9AA3B5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(onVolver: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón atrás
        Surface(
            onClick = onVolver,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF131825),
            border = BorderStroke(1.dp, Color(0xFF263044))
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Título "Mi Perfil"
        Text(
            text = "Mi Perfil",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Spacer para balancear el header sin mostrar la opción de configuración
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun PerfilUserCard(
    nombreUsuario: String,
    totalCalculos: Int,
    totalDescargas: Int
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Paleta.Tarjeta,
        border = BorderStroke(1.dp, Color(0xFF1E2638)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de usuario (sin foto de perfil)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = nombreUsuario,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Paleta.Cian)
                    )
                    Text(
                        text = "Investigador Cristalográfico",
                        color = Paleta.TextoSecundario,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    numero = "$totalCalculos",
                    etiqueta = "Cálculos",
                    modifier = Modifier.weight(1f)
                )

                StatBox(
                    numero = "$totalDescargas",
                    etiqueta = "Descargas",
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatBox(
    numero: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F1422),
        border = BorderStroke(1.dp, Color(0xFF1C2436)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = numero,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                color = Paleta.TextoSecundario,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TabSelectorRow(
    tabSeleccionado: TabPerfil,
    onSelectTab: (TabPerfil) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF111624),
        border = BorderStroke(1.dp, Color(0xFF1E2638)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val esDescargasActivo = tabSeleccionado == TabPerfil.DESCARGAS
            Surface(
                onClick = { onSelectTab(TabPerfil.DESCARGAS) },
                shape = RoundedCornerShape(10.dp),
                color = if (esDescargasActivo) Color(0xFF182236) else Color.Transparent,
                border = if (esDescargasActivo) BorderStroke(1.dp, Paleta.Cian.copy(alpha = 0.4f)) else null,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = if (esDescargasActivo) Paleta.Cian else Paleta.TextoSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Descargas",
                        color = if (esDescargasActivo) Color.White else Paleta.TextoSecundario,
                        fontSize = 13.sp,
                        fontWeight = if (esDescargasActivo) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            val esHistorialActivo = tabSeleccionado == TabPerfil.HISTORIAL
            Surface(
                onClick = { onSelectTab(TabPerfil.HISTORIAL) },
                shape = RoundedCornerShape(10.dp),
                color = if (esHistorialActivo) Color(0xFF182236) else Color.Transparent,
                border = if (esHistorialActivo) BorderStroke(1.dp, Paleta.Cian.copy(alpha = 0.4f)) else null,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = if (esHistorialActivo) Paleta.Cian else Paleta.TextoSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Historial",
                        color = if (esHistorialActivo) Color.White else Paleta.TextoSecundario,
                        fontSize = 13.sp,
                        fontWeight = if (esHistorialActivo) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CardOperacionItem(
    item: OperacionHistorial,
    onToggleFavorito: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Paleta.Tarjeta,
        border = BorderStroke(1.dp, Color(0xFF1E2638)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono / Gráfico mini
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1422))
                    .border(BorderStroke(1.dp, Color(0xFF222C40)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconoEstructuraMini(tipo = item.tipoEstructura)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información central
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.titulo,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.descripcion,
                    color = Paleta.TextoSecundario,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.badges.forEach { badge ->
                            BadgeValorMini(etiqueta = badge.etiqueta, valor = badge.valor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de Favorito (Corazón)
            IconButton(
                onClick = onToggleFavorito,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (item.esFavorito) Color(0xFF331525) else Color(0xFF141B28))
            ) {
                Icon(
                    imageVector = if (item.esFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (item.esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                    tint = if (item.esFavorito) Color(0xFFFF3366) else Color(0xFF6B7B96),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun BadgeValorMini(etiqueta: String, valor: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF131A28))
            .border(BorderStroke(1.dp, Color(0xFF202B3F)), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = etiqueta,
                color = Paleta.Cian,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = valor,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun IconoEstructuraMini(tipo: String) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (tipo.uppercase()) {
            "FCC" -> {
                val r = w * 0.35f
                val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                val colorCube = Color(0xFF00F0FF)
                drawRect(
                    color = colorCube,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = stroke
                )
                drawCircle(Color(0xFFFF3366), radius = 3.dp.toPx(), center = Offset(cx, cy))
            }
            "BCC" -> {
                val r = w * 0.35f
                val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                drawRect(
                    color = Color(0xFFC9A0FF),
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = stroke
                )
                drawCircle(Color(0xFF00F0FF), radius = 3.5.dp.toPx(), center = Offset(cx, cy))
            }
            "TETR" -> {
                val r = w * 0.35f
                val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                drawRect(
                    color = Color(0xFFE91E8C),
                    topLeft = Offset(cx - r * 0.7f, cy - r),
                    size = Size(r * 1.4f, r * 2f),
                    style = stroke
                )
                drawCircle(Color(0xFF00F0FF), radius = 2.5.dp.toPx(), center = Offset(cx, cy))
            }
            "HCP" -> {
                val r = w * 0.35f
                val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r * 0.866f, cy - r * 0.5f)
                    lineTo(cx + r * 0.866f, cy + r * 0.5f)
                    lineTo(cx, cy + r)
                    lineTo(cx - r * 0.866f, cy + r * 0.5f)
                    lineTo(cx - r * 0.866f, cy - r * 0.5f)
                    close()
                }
                drawPath(path, color = Color(0xFF00F0FF), style = stroke)
            }
            "FE" -> {
                val r = w * 0.35f
                drawCircle(color = Color(0xFF00F0FF).copy(alpha = 0.3f), radius = r, center = Offset(cx, cy))
                drawCircle(color = Color(0xFF00F0FF), radius = r, center = Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
            }
            "HUECO" -> {
                drawCircle(color = Color(0xFFE91E8C), radius = 5.dp.toPx(), center = Offset(cx - 4.dp.toPx(), cy - 2.dp.toPx()))
                drawCircle(color = Color(0xFFE91E8C), radius = 5.dp.toPx(), center = Offset(cx + 4.dp.toPx(), cy - 2.dp.toPx()))
                drawCircle(color = Color(0xFF00F0FF), radius = 3.dp.toPx(), center = Offset(cx, cy + 3.dp.toPx()))
            }
            else -> { // SC
                val r = w * 0.35f
                val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                drawRect(
                    color = Color.White.copy(alpha = 0.8f),
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = stroke
                )
            }
        }
    }
}

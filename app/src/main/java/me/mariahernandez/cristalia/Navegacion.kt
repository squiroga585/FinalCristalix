package me.mariahernandez.cristalia

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cristalia.model.EstructuraCristalina
import com.cristalia.ui.ingresardatos.IngresarDatosScreen
import com.cristalia.ui.ingresardatos.IngresarDatosViewModel
import com.cristalia.ui.ingresardatos.ModoEntrada
import me.mariahernandez.cristalia.celdaunitaria.CeldaUnitariaScreen
import me.mariahernandez.cristalia.densidad.DensidadScreen
import me.mariahernandez.cristalia.densidad.parametroa.ResultadoDensidadParametroAScreen
import me.mariahernandez.cristalia.densidad.radio.ResultadoDensidadRadioScreen
import me.mariahernandez.cristalia.densidad.volumen.ResultadoDensidadVolumenScreen
import me.mariahernandez.cristalia.empaquetamiento.EmpaquetamientoScreen
import me.mariahernandez.cristalia.huecos.HuecosScreen
import me.mariahernandez.cristalia.huecos.detalle.DetalleHuecosScreen
import me.mariahernandez.cristalia.inicio.CristaliaSplashScreen
import me.mariahernandez.cristalia.login.LoginScreen
import me.mariahernandez.cristalia.login.RegistroScreen
import me.mariahernandez.cristalia.menu.MenuScreen
import me.mariahernandez.cristalia.perfil.PerfilScreen

object Rutas {

    const val INICIO = "inicio"
    const val MENU = "menu"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val PERFIL = "perfil/{usuario}"

    const val CELDA_UNITARIA = "celda_unitaria"
    const val EMPAQUETAMIENTO = "factor-empaquetamiento"
    const val DENSIDAD = "densidad"
    const val HUECOS = "huecos"
    const val INGRESAR_DATOS = "ingresar_datos/{opcionId}"
    const val RESULTADO_DENSIDAD_RADIO = "resultado_densidad_radio/{opcionId}/{masaMolar}/{radioCation}/{radioAnion}"
    const val RESULTADO_DENSIDAD_VOLUMEN = "resultado_densidad_volumen/{opcionId}/{masaMolar}/{volumenCelda}"
    const val RESULTADO_DENSIDAD_PARAMETRO_A = "resultado_densidad_parametro_a/{opcionId}/{masaMolar}/{parametroA}/{parametroC}"
    const val HUECOS_DETALLE = "huecos_detalle/{opcionId}"
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val abrirLoginOPerfil = {
        if (SessionRepositoryImpl.estaLogueado()) {
            val usuario = SessionRepositoryImpl.obtenerNombreUsuario() ?: "Usuario"
            val encodedUser = Uri.encode(usuario.ifBlank { "Usuario" })
            navController.navigate("perfil/$encodedUser")
        } else {
            navController.navigate(Rutas.LOGIN)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO
    ) {

        composable(Rutas.INICIO) {
            CristaliaSplashScreen(
                onEnter = {
                    navController.navigate(Rutas.MENU)
                }
            )
        }

        composable(Rutas.MENU) {
            MenuScreen(
                onCeldaUnitariaClick = {
                    navController.navigate(Rutas.CELDA_UNITARIA)
                },
                onFactorEmpaquetamientoClick = {
                    navController.navigate(route = Rutas.EMPAQUETAMIENTO)
                },
                onDensidadClick = {
                    navController.navigate(route = Rutas.DENSIDAD)
                },
                onHuecosClick = {
                    navController.navigate(route = Rutas.HUECOS)
                },
                onIrALogin = abrirLoginOPerfil,
                onIrAPerfil = { usuario ->
                    val encodedUser = Uri.encode(usuario.ifBlank { "Usuario" })
                    navController.navigate("perfil/$encodedUser")
                }
            )
        }

        composable(Rutas.LOGIN) {
            LoginScreen(
                onVolver = { navController.popBackStack() },
                onLoginExitoso = { usuario ->
                    SessionRepositoryImpl.iniciarSesion(usuario)
                    val encodedUser = Uri.encode(usuario.ifBlank { "Laura Martínez" })
                    navController.navigate("perfil/$encodedUser") {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onCrearCuenta = {
                    navController.navigate(Rutas.REGISTRO)
                }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }

        composable(Rutas.PERFIL) { backStackEntry ->
            val usuarioRaw = backStackEntry.arguments?.getString("usuario")
            val usuarioDecoded = usuarioRaw?.let { Uri.decode(it) } ?: SessionRepositoryImpl.obtenerNombreUsuario() ?: "Laura Martínez"

            PerfilScreen(
                usuario = usuarioDecoded,
                onVolver = { navController.popBackStack() },
                onCerrarSesion = {
                    SessionRepositoryImpl.cerrarSesion()
                    navController.navigate(Rutas.MENU) {
                        popUpTo(Rutas.MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.CELDA_UNITARIA) {
            CeldaUnitariaScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }

        composable(route = Rutas.EMPAQUETAMIENTO) {
            EmpaquetamientoScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }

        composable(route = Rutas.DENSIDAD) {
            DensidadScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil,
                onOpcionClick = { _, opcionId ->
                    navController.navigate("ingresar_datos/$opcionId")
                }
            )
        }

        composable(route = Rutas.INGRESAR_DATOS) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "nacl"
            val estructura = EstructuraCristalina.catalogoEjemplo()
                .firstOrNull { it.id == opcionId }
                ?: EstructuraCristalina.catalogoEjemplo().first()

            val viewModel: IngresarDatosViewModel = viewModel()

            IngresarDatosScreen(
                estructura = estructura,
                onBack = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil,
                onNavegarAResultado = { _ ->
                    val estado = viewModel.uiState.value
                    val masaMolar = estado.masaMolar

                    when (estado.modoEntrada) {
                        ModoEntrada.RADIO -> {
                            val rCat = estado.radioCation
                            val rAn = estado.radioAnion
                            navController.navigate("resultado_densidad_radio/$opcionId/$masaMolar/$rCat/$rAn")
                        }

                        ModoEntrada.VOLUMEN -> {
                            val volumen = estado.volumenCm3
                            if (volumen != null) {
                                navController.navigate("resultado_densidad_volumen/$opcionId/$masaMolar/$volumen")
                            }
                        }

                        ModoEntrada.PARAMETRO_A -> {
                            val a = estado.parametroA.toDoubleOrNull()
                            val c = if (estructura.requiereParametroC) estado.parametroC.toDoubleOrNull() else 0.0
                            if (a != null && c != null) {
                                navController.navigate("resultado_densidad_parametro_a/$opcionId/$masaMolar/$a/$c")
                            }
                        }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(route = Rutas.RESULTADO_DENSIDAD_RADIO) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "nacl"
            val masaMolar = backStackEntry.arguments?.getString("masaMolar")?.toDoubleOrNull() ?: 0.0
            val rCat = backStackEntry.arguments?.getString("radioCation")?.toDoubleOrNull() ?: 0.0
            val rAn = backStackEntry.arguments?.getString("radioAnion")?.toDoubleOrNull() ?: 0.0

            val estructura = EstructuraCristalina.catalogoEjemplo()
                .firstOrNull { it.id == opcionId }
                ?: EstructuraCristalina.catalogoEjemplo().first()

            ResultadoDensidadRadioScreen(
                estructura = estructura,
                masaMolar = masaMolar,
                radioCation = rCat,
                radioAnion = rAn,
                onBack = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }

        composable(route = Rutas.RESULTADO_DENSIDAD_VOLUMEN) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "nacl"
            val masaMolar = backStackEntry.arguments?.getString("masaMolar")?.toDoubleOrNull() ?: 0.0
            val volumenCm3 = backStackEntry.arguments?.getString("volumenCelda")?.toDoubleOrNull() ?: 0.0

            val estructura = EstructuraCristalina.catalogoEjemplo()
                .firstOrNull { it.id == opcionId }
                ?: EstructuraCristalina.catalogoEjemplo().first()

            ResultadoDensidadVolumenScreen(
                estructura = estructura,
                masaMolar = masaMolar,
                volumenCm3 = volumenCm3,
                onBack = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }

        composable(route = Rutas.RESULTADO_DENSIDAD_PARAMETRO_A) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "nacl"
            val masaMolar = backStackEntry.arguments?.getString("masaMolar")?.toDoubleOrNull() ?: 0.0
            val aPm = backStackEntry.arguments?.getString("parametroA")?.toDoubleOrNull() ?: 0.0
            val cPm = backStackEntry.arguments?.getString("parametroC")?.toDoubleOrNull()?.takeIf { it > 0.0 }

            val estructura = EstructuraCristalina.catalogoEjemplo()
                .firstOrNull { it.id == opcionId }
                ?: EstructuraCristalina.catalogoEjemplo().first()

            ResultadoDensidadParametroAScreen(
                estructura = estructura,
                parametroAPm = aPm,
                parametroCPm = cPm,
                masaMolar = masaMolar,
                onBack = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }

        composable(route = Rutas.HUECOS) {
            HuecosScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil,
                onOpcionClick = { _, opcionId ->
                    navController.navigate("huecos_detalle/$opcionId")
                }
            )
        }

        composable(route = Rutas.HUECOS_DETALLE) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "fcc"

            DetalleHuecosScreen(
                opcionId = opcionId,
                onVolver = { navController.popBackStack() },
                onIrALogin = abrirLoginOPerfil
            )
        }
    }
}

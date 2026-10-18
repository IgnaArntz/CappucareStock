
package com.cappucare.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cappucare.app.data.CappucareDatabase
import com.cappucare.app.data.InventarioRepository
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventarioViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = InventarioRepository(
        CappucareDatabase.obtener(application).inventarioDao()
    )

    val productos: StateFlow<List<Producto>> =
        repository.productos.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val movimientos: StateFlow<List<Movimiento>> =
        repository.movimientos.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val cantidadStockBajo: StateFlow<Int> =
        repository.cantidadStockBajo.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun registrarIngreso(
        codigo: String,
        cantidad: Int,
        responsable: String
    ) {
        viewModelScope.launch {
            _mensaje.value =
                if (repository.registrarIngreso(codigo, cantidad, responsable)) {
                    "Ingreso registrado correctamente"
                } else {
                    "No se pudo registrar el ingreso. Verifique el código y los datos."
                }
        }
    }

    fun registrarSalida(
        codigo: String,
        cantidad: Int,
        motivo: String,
        responsable: String
    ) {
        viewModelScope.launch {
            _mensaje.value =
                if (repository.registrarSalida(
                        codigo,
                        cantidad,
                        motivo,
                        responsable
                    )
                ) {
                    "Salida registrada correctamente"
                } else {
                    "No se pudo registrar la salida. Verifique el código y el stock disponible."
                }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}


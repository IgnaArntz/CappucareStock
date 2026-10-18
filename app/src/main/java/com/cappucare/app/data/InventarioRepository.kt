package com.cappucare.app.data

import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Producto
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventarioRepository(private val dao: InventarioDao) {
    val productos: Flow<List<Producto>> = dao.observarProductos()
    val movimientos: Flow<List<Movimiento>> = dao.observarMovimientos()
    val cantidadStockBajo: Flow<Int> = dao.observarCantidadStockBajo()
    fun movimientosPorProducto(codigo: String): Flow<List<Movimiento>> = dao.observarMovimientosPorProducto(codigo)

    suspend fun registrarIngreso(codigo: String, cantidad: Int, responsable: String): Boolean =
        dao.registrarIngreso(codigo.trim(), cantidad, responsable.trim(), fechaActual())

    suspend fun registrarSalida(codigo: String, cantidad: Int, motivo: String, responsable: String): Boolean =
        dao.registrarSalida(codigo.trim(), cantidad, motivo.trim(), responsable.trim(), fechaActual())

    private fun fechaActual(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
}

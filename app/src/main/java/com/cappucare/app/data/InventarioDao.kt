
package com.cappucare.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
abstract class InventarioDao {

    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE")
    abstract fun observarProductos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE")
    abstract suspend fun obtenerProductos(): List<Producto>

    @Query("SELECT * FROM movimientos ORDER BY id DESC")
    abstract fun observarMovimientos(): Flow<List<Movimiento>>

    @Query("SELECT * FROM movimientos WHERE codigoProducto = :codigo ORDER BY id DESC")
    abstract fun observarMovimientosPorProducto(codigo: String): Flow<List<Movimiento>>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    abstract suspend fun obtenerProducto(codigo: String): Producto?

    @Query("UPDATE productos SET cantidad = :cantidad, estado = :estado WHERE codigo = :codigo")
    abstract suspend fun actualizarStock(
        codigo: String,
        cantidad: Int,
        estado: String
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertarProducto(producto: Producto)

    @Insert
    abstract suspend fun insertarMovimiento(movimiento: Movimiento)
    @Query("""
    INSERT OR IGNORE INTO productos
    (codigo, nombre, categoria, cantidad, unidad, nivelMinimo, ubicacion, estado)
    VALUES
    ('JER001', 'Jeringas', 'Insumos', 100, 'Unidades', 20, 'Bodega de insumos', 'Disponible')
""")
    abstract suspend fun insertarJeringasSiNoExisten()

    @Transaction
    open suspend fun registrarIngreso(
        codigo: String,
        cantidad: Int,
        responsable: String,
        fecha: String
    ): Boolean {
        if (cantidad <= 0 || responsable.isBlank()) return false

        val producto = obtenerProducto(codigo) ?: return false
        val stock = producto.cantidad + cantidad
        val estado = if (stock <= producto.nivelMinimo) {
            "Stock bajo"
        } else {
            "Disponible"
        }

        if (actualizarStock(codigo, stock, estado) == 0) return false

        insertarMovimiento(
            Movimiento(
                codigoProducto = codigo,
                tipo = "INGRESO",
                cantidad = cantidad,
                motivo = "Ingreso de mercadería",
                responsable = responsable,
                fecha = fecha
            )
        )

        return true
    }

    @Transaction
    open suspend fun registrarSalida(
        codigo: String,
        cantidad: Int,
        motivo: String,
        responsable: String,
        fecha: String
    ): Boolean {
        if (cantidad <= 0 || responsable.isBlank() || motivo.isBlank()) {
            return false
        }

        val producto = obtenerProducto(codigo) ?: return false
        if (producto.cantidad < cantidad) return false

        val stock = producto.cantidad - cantidad
        val estado = if (stock <= producto.nivelMinimo) {
            "Stock bajo"
        } else {
            "Disponible"
        }

        if (actualizarStock(codigo, stock, estado) == 0) return false

        insertarMovimiento(
            Movimiento(
                codigoProducto = codigo,
                tipo = "SALIDA",
                cantidad = cantidad,
                motivo = motivo,
                responsable = responsable,
                fecha = fecha
            )
        )

        return true
    }

    @Query("SELECT COUNT(*) FROM productos WHERE cantidad <= nivelMinimo")
    abstract fun observarCantidadStockBajo(): Flow<Int>
}

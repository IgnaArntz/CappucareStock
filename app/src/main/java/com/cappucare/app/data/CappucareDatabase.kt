
package com.cappucare.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Producto

@Database(
    entities = [Producto::class, Movimiento::class],
    version = 1,
    exportSchema = false
)
abstract class CappucareDatabase : RoomDatabase() {

    abstract fun inventarioDao(): InventarioDao

    companion object {
        @Volatile
        private var INSTANCE: CappucareDatabase? = null

        fun obtener(context: Context): CappucareDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CappucareDatabase::class.java,
                    "cappucare_room.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            // Productos iniciales: solo se insertan al crear la base de datos.
                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO productos
                                (codigo, nombre, categoria, cantidad, unidad, nivelMinimo, ubicacion, estado)
                                VALUES
                                ('MED001', 'Paracetamol 500 mg', 'Medicamentos', 30, 'Cajas', 20, 'Estante A1', 'Disponible')
                                """.trimIndent()
                            )

                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO productos
                                (codigo, nombre, categoria, cantidad, unidad, nivelMinimo, ubicacion, estado)
                                VALUES
                                ('INS002', 'Guantes médicos', 'Insumos', 40, 'Cajas', 20, 'Estante B2', 'Disponible')
                                """.trimIndent()
                            )

                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO productos
                                (codigo, nombre, categoria, cantidad, unidad, nivelMinimo, ubicacion, estado)
                                VALUES
                                ('INS003', 'Mascarillas quirúrgicas', 'Insumos', 45, 'Cajas', 30, 'Estante B3', 'Disponible')
                                """.trimIndent()
                            )

                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO productos
                                (codigo, nombre, categoria, cantidad, unidad, nivelMinimo, ubicacion, estado)
                                VALUES
                                ('JER001', 'Jeringas', 'Insumos', 100, 'Unidades', 20, 'Bodega de insumos', 'Disponible')
                                """.trimIndent()
                            )
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

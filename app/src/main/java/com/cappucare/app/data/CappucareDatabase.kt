package com.cappucare.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Producto
import java.io.File

@Database(entities = [Producto::class, Movimiento::class], version = 1, exportSchema = false)
abstract class CappucareDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao

    companion object {
        @Volatile private var INSTANCE: CappucareDatabase? = null

        fun obtener(context: Context): CappucareDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, CappucareDatabase::class.java, "cappucare_room.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Importa los datos de la versión SQLiteOpenHelper si existe, para no perder stock ni historial.
                        val legacyFile: File = context.applicationContext.getDatabasePath("cappucare.db")
                        if (legacyFile.exists()) {
                            try {
                                SQLiteDatabase.openDatabase(legacyFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { oldDb ->
                                    oldDb.rawQuery("SELECT codigo,nombre,categoria,cantidad,unidad,nivelMinimo,ubicacion,estado FROM productos", null).use { cursor ->
                                        while (cursor.moveToNext()) {
                                            db.execSQL("INSERT OR IGNORE INTO productos (codigo,nombre,categoria,cantidad,unidad,nivelMinimo,ubicacion,estado) VALUES (?,?,?,?,?,?,?,?)", arrayOf(
                                                cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getString(7)
                                            ))
                                        }
                                    }
                                    oldDb.rawQuery("SELECT id,codigoProducto,tipo,cantidad,motivo,responsable,fecha FROM movimientos ORDER BY id", null).use { cursor ->
                                        while (cursor.moveToNext()) {
                                            db.execSQL("INSERT OR IGNORE INTO movimientos (id,codigoProducto,tipo,cantidad,motivo,responsable,fecha) VALUES (?,?,?,?,?,?,?)", arrayOf(
                                                cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)
                                            ))
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                                // Si la base antigua no tiene el esquema esperado, la app continúa con datos ficticios.
                            }
                        }
                        db.execSQL("INSERT OR IGNORE INTO productos (codigo,nombre,categoria,cantidad,unidad,nivelMinimo,ubicacion,estado) VALUES ('MED001','Paracetamol 500 mg','Medicamentos',50,'Cajas',20,'Estante A1','Disponible')")
                        db.execSQL("INSERT OR IGNORE INTO productos (codigo,nombre,categoria,cantidad,unidad,nivelMinimo,ubicacion,estado) VALUES ('INS002','Guantes médicos','Insumos',15,'Cajas',20,'Estante B2','Stock bajo')")
                        db.execSQL("INSERT OR IGNORE INTO productos (codigo,nombre,categoria,cantidad,unidad,nivelMinimo,ubicacion,estado) VALUES ('INS003','Mascarillas quirúrgicas','Insumos',80,'Cajas',30,'Estante B3','Disponible')")
                    }
                }).build().also { INSTANCE = it }
        }
    }
}

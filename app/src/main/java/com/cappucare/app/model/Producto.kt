package com.cappucare.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val categoria: String,
    val cantidad: Int,
    val unidad: String,
    val nivelMinimo: Int,
    val ubicacion: String,
    val estado: String
)

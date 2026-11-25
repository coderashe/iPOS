package com.ipos.data.domain

import androidx.room.Embedded
import androidx.room.Relation
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeBrand

data class ProductWithBrand(
    @Embedded val product: Products,
    @Relation(
        parentColumn = "brandID",
        entityColumn = "brandID"
    )
    val brand: ShoeBrand? = null
)

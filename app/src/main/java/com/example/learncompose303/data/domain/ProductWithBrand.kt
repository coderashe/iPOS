package com.example.learncompose303.data.domain

import androidx.room.Embedded
import androidx.room.Relation
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ShoeBrand

data class ProductWithBrand(
    @Embedded val product: Products,
    @Relation(
        parentColumn = "brandID",
        entityColumn = "brandID"
    )
    val brand: ShoeBrand
)

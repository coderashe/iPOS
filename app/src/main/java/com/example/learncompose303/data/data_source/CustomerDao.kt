package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Customer
import kotlinx.coroutines.flow.Flow


@Dao
interface CustomerDao {

    // Fetch customer by ID
    @Query("SELECT * FROM Customer WHERE customerId = :id")
    suspend fun getCustomerById(id: Long): Customer?

    // Fetch all customers as a Flow
    @Query("SELECT * FROM Customer")
    fun getAllCustomers(): Flow<List<Customer>>

    // Fetch all customers once (without Flow)
    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomersOnce(): List<Customer>

    // Insert a single customer
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    // Insert multiple customers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    // Update a customer
    @Update
    suspend fun updateCustomer(customer: Customer)

    // Delete a customer
    @Delete
    suspend fun deleteCustomer(customer: Customer)
}

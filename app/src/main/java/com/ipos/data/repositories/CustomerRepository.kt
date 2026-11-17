package com.ipos.data.repositories

import com.ipos.data.data_source.CustomerDao
import com.ipos.data.entities.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CustomerRepository {

    // Fetch customer by ID
    suspend fun getCustomerById(id: Long): Customer?
    suspend fun getWalkinCustomer(name: String): Customer?

    // Insert a single customer
    suspend fun insertCustomer(customer: Customer): Long

    // Update a customer
    suspend fun updateCustomer(customer: Customer)

    // Delete a customer
    suspend fun deleteCustomer(customer: Customer)

    // Fetch all customers as a Flow
    suspend fun getAllCustomers(): Flow<List<Customer>>

    // Insert a list of customers
    suspend fun insertCustomers(customers: List<Customer>)

    // Fetch all customers once (without Flow)
    suspend fun getAllCustomersOnce(): List<Customer>
}

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override suspend fun getCustomerById(id: Long): Customer? {
        return withContext(Dispatchers.IO) {
            customerDao.getCustomerById(id)
        }
    }

    override suspend fun getWalkinCustomer(name: String): Customer? {
        return withContext(Dispatchers.IO) {
            customerDao.getWalkinCustomer(name)
        }
    }

    override suspend fun insertCustomer(customer: Customer): Long {
        return withContext(Dispatchers.IO) {
            customerDao.insertCustomer(customer)
        }
    }

    override suspend fun updateCustomer(customer: Customer) {
        return withContext(Dispatchers.IO) {
            customerDao.updateCustomer(customer)
        }
    }

    override suspend fun deleteCustomer(customer: Customer) {
        return withContext(Dispatchers.IO) {
            customerDao.deleteCustomer(customer)
        }
    }

    override suspend fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers() // No need for Dispatchers.IO, Flow handles threading
    }

    override suspend fun insertCustomers(customers: List<Customer>) {
        return withContext(Dispatchers.IO) {
            customerDao.insertCustomers(customers)
        }
    }

    override suspend fun getAllCustomersOnce(): List<Customer> {
        return withContext(Dispatchers.IO) {
            customerDao.getAllCustomersOnce()
        }
    }
}

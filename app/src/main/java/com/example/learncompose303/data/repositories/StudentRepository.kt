package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.StudentEntity
import com.example.learncompose303.data.data_source.StudentDao
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface StudentRepository {
    suspend fun insert(studentEntity: StudentEntity)

    suspend fun delete(studentEntity: StudentEntity)

    suspend fun update(studentEntity: StudentEntity)

    suspend fun getAllStudents(): Flow<List<StudentEntity>>

    suspend fun getStudentByNameAndRollNo(name: String, rollNo: String): StudentEntity?
}

class RepositoryImpl @Inject constructor(
    private val dao: StudentDao,
) : StudentRepository {

    override suspend fun insert(studentEntity: StudentEntity) {
        withContext(IO) {
            dao.insert(studentEntity)
        }
    }

    override suspend fun delete(studentEntity: StudentEntity) {
        withContext(IO) {
            dao.delete(studentEntity)
        }
    }

    override suspend fun update(studentEntity: StudentEntity) {
        withContext(IO) {
            dao.update(studentEntity)
        }
    }

    override suspend fun getAllStudents(): Flow<List<StudentEntity>> {
        return withContext(IO) {
            dao.getAllStudents()
        }
    }

    // Override the method to query the student by name and roll number
    override suspend fun getStudentByNameAndRollNo(name: String, rollNo: String): StudentEntity? {
        return withContext(IO) {
            dao.findStudentByNameAndRollNo(name, rollNo)
        }
    }
}

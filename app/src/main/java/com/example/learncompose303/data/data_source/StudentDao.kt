package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentEntity: StudentEntity)

    @Delete
    suspend fun delete(studentEntity: StudentEntity)

    @Update
    suspend fun update(studentEntity: StudentEntity)

    @Query("SELECT * FROM student")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM student WHERE name = :name AND studentRollNo = :rollNo LIMIT 1")
    suspend fun findStudentByNameAndRollNo(name: String, rollNo: String): StudentEntity?
}
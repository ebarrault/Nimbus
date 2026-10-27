package com.example.nimbus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.nimbus.data.local.entity.FavoriteCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {

    /** Room réémet automatiquement la liste à chaque modification de la table. */
    @Query("SELECT * FROM favorite_cities ORDER BY position")
    fun observeAll(): Flow<List<FavoriteCityEntity>>

    @Query("SELECT MAX(position) FROM favorite_cities")
    suspend fun maxPosition(): Int?

    /** IGNORE : si la ville est déjà en favori, on ne change rien. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(city: FavoriteCityEntity)

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE favorite_cities SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    /** Ajoute la ville à la fin de la liste. */
    @Transaction
    suspend fun insertAtEnd(city: FavoriteCityEntity) {
        val next = (maxPosition() ?: -1) + 1
        insert(city.copy(position = next))
    }

    /** Réécrit toutes les positions d'un coup (tout ou rien grâce à @Transaction). */
    @Transaction
    suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updatePosition(id, index) }
    }
}

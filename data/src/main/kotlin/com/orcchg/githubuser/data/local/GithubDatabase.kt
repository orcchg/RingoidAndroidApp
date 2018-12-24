package com.orcchg.githubuser.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.orcchg.githubuser.domain.model.GithubUser

@Database(entities = [GithubUser::class], version = 1)
abstract class GithubDatabase : RoomDatabase() {

    abstract fun userDao(): GithubUserDao
}

package com.quantumresearch.mycel.app.headless

import com.quantumresearch.mycel.spore.api.crypto.KeyStrengthener
import com.quantumresearch.mycel.spore.api.db.DatabaseConfig
import java.io.File

internal class HeadlessDatabaseConfig(private val dbDir: File, private val keyDir: File) :
    DatabaseConfig {

    override fun getDatabaseDirectory() = dbDir

    override fun getDatabaseKeyDirectory() = keyDir

    override fun getKeyStrengthener(): KeyStrengthener? = null
}

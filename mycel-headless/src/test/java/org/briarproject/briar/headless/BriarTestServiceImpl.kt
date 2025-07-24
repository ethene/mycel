package com.quantumresearch.mycel.app.headless

import com.quantumresearch.mycel.spore.api.account.AccountManager
import com.quantumresearch.mycel.spore.api.crypto.DecryptionException
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Singleton

const val user = "user"
const val pass = "pass"

@Immutable
@Singleton
internal class BriarTestServiceImpl
@Inject
constructor(
    private val accountManager: AccountManager,
    private val lifecycleManager: LifecycleManager
) : BriarService {

    override fun start() {
        if (accountManager.accountExists()) {
            accountManager.deleteAccount()
        }
        accountManager.createAccount(user, pass)
        try {
            accountManager.signIn(pass)
        } catch (e: DecryptionException) {
            throw AssertionError("Password invalid")
        }
        val dbKey = accountManager.databaseKey ?: throw AssertionError()
        lifecycleManager.startServices(dbKey)
        lifecycleManager.waitForStartup()
    }

    override fun stop() {
        lifecycleManager.stopServices()
        lifecycleManager.waitForShutdown()
    }

}

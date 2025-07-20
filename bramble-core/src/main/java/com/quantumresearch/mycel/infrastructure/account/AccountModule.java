package com.quantumresearch.mycel.infrastructure.account;

import com.quantumresearch.mycel.infrastructure.api.account.AccountManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AccountModule {

	@Provides
	@Singleton
	AccountManager provideAccountManager(AccountManagerImpl accountManager) {
		return accountManager;
	}
}

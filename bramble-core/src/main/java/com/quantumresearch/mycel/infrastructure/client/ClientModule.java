package com.quantumresearch.mycel.infrastructure.client;

import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.client.ContactGroupFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ClientModule {

	@Provides
	ClientHelper provideClientHelper(ClientHelperImpl clientHelper) {
		return clientHelper;
	}

	@Provides
	ContactGroupFactory provideContactGroupFactory(
			ContactGroupFactoryImpl contactGroupFactory) {
		return contactGroupFactory;
	}

}

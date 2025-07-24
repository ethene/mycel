package com.quantumresearch.mycel.spore.client;

import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.client.ContactGroupFactory;

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

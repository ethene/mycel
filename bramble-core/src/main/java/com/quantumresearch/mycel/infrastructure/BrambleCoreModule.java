package com.quantumresearch.mycel.infrastructure;

import com.quantumresearch.mycel.infrastructure.cleanup.CleanupModule;
import com.quantumresearch.mycel.infrastructure.client.ClientModule;
import com.quantumresearch.mycel.infrastructure.connection.ConnectionModule;
import com.quantumresearch.mycel.infrastructure.contact.ContactModule;
import com.quantumresearch.mycel.infrastructure.crypto.CryptoExecutorModule;
import com.quantumresearch.mycel.infrastructure.crypto.CryptoModule;
import com.quantumresearch.mycel.infrastructure.data.DataModule;
import com.quantumresearch.mycel.infrastructure.db.DatabaseExecutorModule;
import com.quantumresearch.mycel.infrastructure.db.DatabaseModule;
import com.quantumresearch.mycel.infrastructure.event.EventModule;
import com.quantumresearch.mycel.infrastructure.identity.IdentityModule;
import com.quantumresearch.mycel.infrastructure.io.IoModule;
import com.quantumresearch.mycel.infrastructure.keyagreement.KeyAgreementModule;
import com.quantumresearch.mycel.infrastructure.lifecycle.LifecycleModule;
import com.quantumresearch.mycel.infrastructure.mailbox.MailboxModule;
import com.quantumresearch.mycel.infrastructure.plugin.PluginModule;
import com.quantumresearch.mycel.infrastructure.properties.PropertiesModule;
import com.quantumresearch.mycel.infrastructure.qrcode.QrCodeModule;
import com.quantumresearch.mycel.infrastructure.record.RecordModule;
import com.quantumresearch.mycel.infrastructure.reliability.ReliabilityModule;
import com.quantumresearch.mycel.infrastructure.rendezvous.RendezvousModule;
import com.quantumresearch.mycel.infrastructure.settings.SettingsModule;
import com.quantumresearch.mycel.infrastructure.sync.SyncModule;
import com.quantumresearch.mycel.infrastructure.sync.validation.ValidationModule;
import com.quantumresearch.mycel.infrastructure.transport.TransportModule;
import com.quantumresearch.mycel.infrastructure.transport.agreement.TransportKeyAgreementModule;
import com.quantumresearch.mycel.infrastructure.versioning.VersioningModule;

import dagger.Module;

@Module(includes = {
		CleanupModule.class,
		ClientModule.class,
		ConnectionModule.class,
		ContactModule.class,
		CryptoModule.class,
		CryptoExecutorModule.class,
		DataModule.class,
		DatabaseModule.class,
		DatabaseExecutorModule.class,
		EventModule.class,
		IdentityModule.class,
		IoModule.class,
		KeyAgreementModule.class,
		LifecycleModule.class,
		MailboxModule.class,
		PluginModule.class,
		PropertiesModule.class,
		QrCodeModule.class,
		RecordModule.class,
		ReliabilityModule.class,
		RendezvousModule.class,
		SettingsModule.class,
		SyncModule.class,
		TransportKeyAgreementModule.class,
		TransportModule.class,
		ValidationModule.class,
		VersioningModule.class
})
public class BrambleCoreModule {
}

package com.quantumresearch.mycel.spore;

import com.quantumresearch.mycel.spore.cleanup.CleanupModule;
import com.quantumresearch.mycel.spore.client.ClientModule;
import com.quantumresearch.mycel.spore.connection.ConnectionModule;
import com.quantumresearch.mycel.spore.contact.ContactModule;
import com.quantumresearch.mycel.spore.crypto.CryptoExecutorModule;
import com.quantumresearch.mycel.spore.crypto.CryptoModule;
import com.quantumresearch.mycel.spore.data.DataModule;
import com.quantumresearch.mycel.spore.db.DatabaseExecutorModule;
import com.quantumresearch.mycel.spore.db.DatabaseModule;
import com.quantumresearch.mycel.spore.event.EventModule;
import com.quantumresearch.mycel.spore.identity.IdentityModule;
import com.quantumresearch.mycel.spore.io.IoModule;
import com.quantumresearch.mycel.spore.keyagreement.KeyAgreementModule;
import com.quantumresearch.mycel.spore.lifecycle.LifecycleModule;
import com.quantumresearch.mycel.spore.mailbox.MailboxModule;
import com.quantumresearch.mycel.spore.plugin.PluginModule;
import com.quantumresearch.mycel.spore.properties.PropertiesModule;
import com.quantumresearch.mycel.spore.qrcode.QrCodeModule;
import com.quantumresearch.mycel.spore.record.RecordModule;
import com.quantumresearch.mycel.spore.reliability.ReliabilityModule;
import com.quantumresearch.mycel.spore.rendezvous.RendezvousModule;
import com.quantumresearch.mycel.spore.settings.SettingsModule;
import com.quantumresearch.mycel.spore.sync.SyncModule;
import com.quantumresearch.mycel.spore.sync.validation.ValidationModule;
import com.quantumresearch.mycel.spore.transport.TransportModule;
import com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementModule;
import com.quantumresearch.mycel.spore.versioning.VersioningModule;

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
public class SporeCoreModule {
}

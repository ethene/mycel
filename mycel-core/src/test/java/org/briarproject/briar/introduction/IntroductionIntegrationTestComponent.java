package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.SporeCoreModule;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;
import com.quantumresearch.mycel.app.attachment.AttachmentModule;
import com.quantumresearch.mycel.app.autodelete.AutoDeleteModule;
import com.quantumresearch.mycel.app.avatar.AvatarModule;
import com.quantumresearch.mycel.app.blog.BlogModule;
import com.quantumresearch.mycel.app.client.MycelClientModule;
import com.quantumresearch.mycel.app.conversation.ConversationModule;
import com.quantumresearch.mycel.app.forum.ForumModule;
import com.quantumresearch.mycel.app.identity.IdentityModule;
import com.quantumresearch.mycel.app.messaging.MessagingModule;
import com.quantumresearch.mycel.app.privategroup.PrivateGroupModule;
import com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationModule;
import com.quantumresearch.mycel.app.sharing.SharingModule;
import com.quantumresearch.mycel.app.test.MycelIntegrationTestComponent;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		SporeCoreModule.class,
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		MycelClientModule.class,
		ConversationModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface IntroductionIntegrationTestComponent
		extends MycelIntegrationTestComponent {

	void inject(IntroductionIntegrationTest init);

	void inject(MessageEncoderParserIntegrationTest init);

	void inject(SessionEncoderParserIntegrationTest init);

	void inject(IntroductionCryptoIntegrationTest init);

	void inject(AutoDeleteIntegrationTest init);

	MessageEncoder getMessageEncoder();

	MessageParser getMessageParser();

	SessionParser getSessionParser();

	IntroductionCrypto getIntroductionCrypto();

}

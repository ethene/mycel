package com.quantumresearch.mycel.app;

import com.quantumresearch.mycel.app.attachment.AttachmentModule;
import com.quantumresearch.mycel.app.autodelete.AutoDeleteModule;
import com.quantumresearch.mycel.app.avatar.AvatarModule;
import com.quantumresearch.mycel.app.blog.BlogModule;
import com.quantumresearch.mycel.app.client.BriarClientModule;
import com.quantumresearch.mycel.app.conversation.ConversationModule;
import com.quantumresearch.mycel.app.feed.FeedModule;
import com.quantumresearch.mycel.app.forum.ForumModule;
import com.quantumresearch.mycel.app.identity.IdentityModule;
import com.quantumresearch.mycel.app.introduction.IntroductionModule;
import com.quantumresearch.mycel.app.messaging.MessagingModule;
import com.quantumresearch.mycel.app.privategroup.PrivateGroupModule;
import com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationModule;
import com.quantumresearch.mycel.app.sharing.SharingModule;
import com.quantumresearch.mycel.app.test.TestModule;

import dagger.Module;

@Module(includes = {
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		BriarClientModule.class,
		ConversationModule.class,
		FeedModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		TestModule.class
})
public class BriarCoreModule {
}

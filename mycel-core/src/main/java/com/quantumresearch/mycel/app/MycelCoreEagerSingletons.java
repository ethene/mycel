package com.quantumresearch.mycel.app;

import com.quantumresearch.mycel.app.autodelete.AutoDeleteModule;
import com.quantumresearch.mycel.app.avatar.AvatarModule;
import com.quantumresearch.mycel.app.blog.BlogModule;
import com.quantumresearch.mycel.app.conversation.ConversationModule;
import com.quantumresearch.mycel.app.feed.FeedModule;
import com.quantumresearch.mycel.app.forum.ForumModule;
import com.quantumresearch.mycel.app.identity.IdentityModule;
import com.quantumresearch.mycel.app.introduction.IntroductionModule;
import com.quantumresearch.mycel.app.messaging.MessagingModule;
import com.quantumresearch.mycel.app.privategroup.PrivateGroupModule;
import com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationModule;
import com.quantumresearch.mycel.app.sharing.SharingModule;

public interface MycelCoreEagerSingletons {

	void inject(AutoDeleteModule.EagerSingletons init);

	void inject(AvatarModule.EagerSingletons init);

	void inject(BlogModule.EagerSingletons init);

	void inject(ConversationModule.EagerSingletons init);

	void inject(FeedModule.EagerSingletons init);

	void inject(ForumModule.EagerSingletons init);

	void inject(GroupInvitationModule.EagerSingletons init);

	void inject(IdentityModule.EagerSingletons init);

	void inject(IntroductionModule.EagerSingletons init);

	void inject(MessagingModule.EagerSingletons init);

	void inject(PrivateGroupModule.EagerSingletons init);

	void inject(SharingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(MycelCoreEagerSingletons c) {
			c.inject(new AutoDeleteModule.EagerSingletons());
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new BlogModule.EagerSingletons());
			c.inject(new ConversationModule.EagerSingletons());
			c.inject(new FeedModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new GroupInvitationModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
			c.inject(new PrivateGroupModule.EagerSingletons());
			c.inject(new SharingModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new IntroductionModule.EagerSingletons());
		}
	}
}

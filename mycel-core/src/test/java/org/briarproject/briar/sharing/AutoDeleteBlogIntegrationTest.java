package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.app.api.blog.Blog;
import com.quantumresearch.mycel.app.api.blog.BlogManager;
import com.quantumresearch.mycel.app.api.blog.event.BlogInvitationResponseReceivedEvent;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager.ConversationClient;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import com.quantumresearch.mycel.app.api.sharing.InvitationResponse;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import com.quantumresearch.mycel.app.api.sharing.SharingManager;
import com.quantumresearch.mycel.app.test.MycelIntegrationTestComponent;
import org.junit.Before;

import java.util.Collection;

public class AutoDeleteBlogIntegrationTest
		extends AbstractAutoDeleteIntegrationTest {

	private SharingManager<Blog> sharingManager0;
	private SharingManager<Blog> sharingManager1;
	private Blog shareable;
	private BlogManager manager0;
	private BlogManager manager1;
	private Class<BlogInvitationResponseReceivedEvent>
			responseReceivedEventClass;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		manager0 = c0.getBlogManager();
		manager1 = c1.getBlogManager();
		// personalBlog(author0) is already shared with c1
		shareable = manager0.getPersonalBlog(author2);
		sharingManager0 = c0.getBlogSharingManager();
		sharingManager1 = c1.getBlogSharingManager();
		responseReceivedEventClass = BlogInvitationResponseReceivedEvent.class;
	}

	@Override
	protected ConversationClient getConversationClient(
			MycelIntegrationTestComponent component) {
		return component.getBlogSharingManager();
	}

	@Override
	protected SharingManager<? extends Shareable> getSharingManager0() {
		return sharingManager0;
	}

	@Override
	protected SharingManager<? extends Shareable> getSharingManager1() {
		return sharingManager1;
	}

	@Override
	protected Shareable getShareable() {
		return shareable;
	}

	@Override
	protected Collection<Blog> subscriptions0() throws DbException {
		return manager0.getBlogs();
	}

	@Override
	protected Collection<Blog> subscriptions1() throws DbException {
		return manager1.getBlogs();
	}

	@Override
	protected Class<? extends ConversationMessageReceivedEvent<? extends InvitationResponse>> getResponseReceivedEventClass() {
		return responseReceivedEventClass;
	}
}

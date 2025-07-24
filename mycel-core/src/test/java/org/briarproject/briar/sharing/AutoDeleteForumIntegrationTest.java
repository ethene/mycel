package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager.ConversationClient;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import com.quantumresearch.mycel.app.api.forum.Forum;
import com.quantumresearch.mycel.app.api.forum.ForumManager;
import com.quantumresearch.mycel.app.api.forum.event.ForumInvitationResponseReceivedEvent;
import com.quantumresearch.mycel.app.api.sharing.InvitationResponse;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import com.quantumresearch.mycel.app.api.sharing.SharingManager;
import com.quantumresearch.mycel.app.test.MycelIntegrationTestComponent;
import org.junit.Before;

import java.util.Collection;

public class AutoDeleteForumIntegrationTest
		extends AbstractAutoDeleteIntegrationTest {

	private SharingManager<Forum> sharingManager0;
	private SharingManager<Forum> sharingManager1;
	private Forum shareable;
	private ForumManager manager0;
	private ForumManager manager1;
	private Class<ForumInvitationResponseReceivedEvent>
			responseReceivedEventClass;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		manager0 = c0.getForumManager();
		manager1 = c1.getForumManager();
		shareable = manager0.addForum("Test Forum");
		sharingManager0 = c0.getForumSharingManager();
		sharingManager1 = c1.getForumSharingManager();
		responseReceivedEventClass = ForumInvitationResponseReceivedEvent.class;
	}

	@Override
	protected ConversationClient getConversationClient(
			MycelIntegrationTestComponent component) {
		return component.getForumSharingManager();
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
	protected Collection<Forum> subscriptions0() throws DbException {
		return manager0.getForums();
	}

	@Override
	protected Collection<Forum> subscriptions1() throws DbException {
		return manager1.getForums();
	}

	@Override
	protected Class<? extends ConversationMessageReceivedEvent<? extends InvitationResponse>> getResponseReceivedEventClass() {
		return responseReceivedEventClass;
	}
}

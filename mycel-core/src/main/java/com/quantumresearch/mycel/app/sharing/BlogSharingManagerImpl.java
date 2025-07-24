package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.app.api.blog.Blog;
import com.quantumresearch.mycel.app.api.blog.BlogInvitationResponse;
import com.quantumresearch.mycel.app.api.blog.BlogManager;
import com.quantumresearch.mycel.app.api.blog.BlogManager.RemoveBlogHook;
import com.quantumresearch.mycel.app.api.blog.BlogSharingManager;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class BlogSharingManagerImpl extends SharingManagerImpl<Blog>
		implements BlogSharingManager, RemoveBlogHook {

	private final IdentityManager identityManager;
	private final BlogManager blogManager;

	@Inject
	BlogSharingManagerImpl(DatabaseComponent db, ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser, MessageParser<Blog> messageParser,
			SessionEncoder sessionEncoder, SessionParser sessionParser,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			ProtocolEngine<Blog> engine,
			InvitationFactory<Blog, BlogInvitationResponse> invitationFactory,
			IdentityManager identityManager, BlogManager blogManager) {
		super(db, clientHelper, clientVersioningManager, metadataParser,
				messageParser, sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory);
		this.identityManager = identityManager;
		this.blogManager = blogManager;
	}

	@Override
	protected ClientId getClientId() {
		return CLIENT_ID;
	}

	@Override
	protected int getMajorVersion() {
		return MAJOR_VERSION;
	}

	@Override
	protected ClientId getShareableClientId() {
		return BlogManager.CLIENT_ID;
	}

	@Override
	protected int getShareableMajorVersion() {
		return BlogManager.MAJOR_VERSION;
	}

	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		// Create a group to share with the contact
		super.addingContact(txn, c);

		// Get our blog and that of the contact
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		Blog ourBlog = blogManager.getPersonalBlog(localAuthor);
		Blog theirBlog = blogManager.getPersonalBlog(c.getAuthor());

		// Pre-share both blogs, if they have not been shared already
		try {
			preShareGroup(txn, c, ourBlog.getGroup());
			preShareGroup(txn, c, theirBlog.getGroup());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public void removingBlog(Transaction txn, Blog b) throws DbException {
		removingShareable(txn, b);
	}

}

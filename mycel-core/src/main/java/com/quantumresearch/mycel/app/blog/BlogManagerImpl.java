package com.quantumresearch.mycel.app.blog;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.BdfIncomingMessageHook;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactManager.ContactHook;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.blog.Blog;
import com.quantumresearch.mycel.app.api.blog.BlogCommentHeader;
import com.quantumresearch.mycel.app.api.blog.BlogFactory;
import com.quantumresearch.mycel.app.api.blog.BlogManager;
import com.quantumresearch.mycel.app.api.blog.BlogPost;
import com.quantumresearch.mycel.app.api.blog.BlogPostFactory;
import com.quantumresearch.mycel.app.api.blog.BlogPostHeader;
import com.quantumresearch.mycel.app.api.blog.MessageType;
import com.quantumresearch.mycel.app.api.blog.event.BlogPostAddedEvent;
import com.quantumresearch.mycel.app.api.identity.AuthorInfo;
import com.quantumresearch.mycel.app.api.identity.AuthorManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_SHARE;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_AUTHOR;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_COMMENT;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_ORIGINAL_MSG_ID;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_ORIGINAL_PARENT_MSG_ID;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_PARENT_MSG_ID;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_READ;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_RSS_FEED;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_TIMESTAMP;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_TIME_RECEIVED;
import static com.quantumresearch.mycel.app.api.blog.BlogConstants.KEY_TYPE;
import static com.quantumresearch.mycel.app.api.blog.MessageType.COMMENT;
import static com.quantumresearch.mycel.app.api.blog.MessageType.POST;
import static com.quantumresearch.mycel.app.api.blog.MessageType.WRAPPED_COMMENT;
import static com.quantumresearch.mycel.app.api.blog.MessageType.WRAPPED_POST;
import static com.quantumresearch.mycel.app.api.identity.AuthorInfo.Status.NONE;

@NotNullByDefault
class BlogManagerImpl extends BdfIncomingMessageHook implements BlogManager,
		OpenDatabaseHook, ContactHook {

	private final IdentityManager identityManager;
	private final AuthorManager authorManager;
	private final BlogFactory blogFactory;
	private final BlogPostFactory blogPostFactory;
	private final List<RemoveBlogHook> removeHooks;

	@Inject
	BlogManagerImpl(DatabaseComponent db, IdentityManager identityManager,
			AuthorManager authorManager, ClientHelper clientHelper,
			MetadataParser metadataParser, BlogFactory blogFactory,
			BlogPostFactory blogPostFactory) {
		super(db, clientHelper, metadataParser);
		this.identityManager = identityManager;
		this.authorManager = authorManager;
		this.blogFactory = blogFactory;
		this.blogPostFactory = blogPostFactory;
		removeHooks = new CopyOnWriteArrayList<>();
	}

	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		// Create our personal blog if necessary
		LocalAuthor a = identityManager.getLocalAuthor(txn);
		Blog b = blogFactory.createBlog(a);
		db.addGroup(txn, b.getGroup());  // does nothing, if group exists
	}

	@Override
	public void addingContact(Transaction txn, Contact c) {
	}

	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		Blog b = blogFactory.createBlog(c.getAuthor());
		// TODO we might want to reconsider removing b, if otherwise shared
		if (db.containsGroup(txn, b.getId())) removeBlog(txn, b);
	}

	@Override
	protected DeliveryAction incomingMessage(Transaction txn, Message m,
			BdfList list, BdfDictionary meta)
			throws DbException, FormatException {

		GroupId groupId = m.getGroupId();
		MessageType type = getMessageType(meta);

		if (type == POST || type == COMMENT) {
			BlogPostHeader h =
					getPostHeaderFromMetadata(txn, groupId, m.getId(), meta);

			// check that original message IDs match
			if (type == COMMENT) {
				MessageId parentId = h.getParentId();
				if (parentId == null) throw new FormatException();
				BdfDictionary d = clientHelper
						.getMessageMetadataAsDictionary(txn, parentId);
				byte[] original1 = d.getRaw(KEY_ORIGINAL_MSG_ID);
				byte[] original2 = meta.getRaw(KEY_ORIGINAL_PARENT_MSG_ID);
				if (!Arrays.equals(original1, original2)) {
					throw new FormatException();
				}
			}

			// broadcast event about new post or comment
			BlogPostAddedEvent event =
					new BlogPostAddedEvent(groupId, h, false);
			txn.attach(event);

			// shares message and its dependencies
			return ACCEPT_SHARE;
		} else if (type == WRAPPED_COMMENT) {
			// Check that the original message ID in the dependency's metadata
			// matches the original parent ID of the wrapped comment
			MessageId dependencyId =
					new MessageId(meta.getRaw(KEY_PARENT_MSG_ID));
			BdfDictionary d = clientHelper
					.getMessageMetadataAsDictionary(txn, dependencyId);
			byte[] original1 = d.getRaw(KEY_ORIGINAL_MSG_ID);
			byte[] original2 = meta.getRaw(KEY_ORIGINAL_PARENT_MSG_ID);
			if (!Arrays.equals(original1, original2)) {
				throw new FormatException();
			}
		}
		// don't share message until parent arrives
		return ACCEPT_DO_NOT_SHARE;
	}

	@Override
	public void addBlog(Blog b) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			db.addGroup(txn, b.getGroup());
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public void addBlog(Transaction txn, Blog b) throws DbException {
		db.addGroup(txn, b.getGroup());
	}

	@Override
	public boolean canBeRemoved(Blog b) throws DbException {
		Transaction txn = db.startTransaction(true);
		try {
			boolean canBeRemoved = canBeRemoved(txn, b);
			db.commitTransaction(txn);
			return canBeRemoved;
		} finally {
			db.endTransaction(txn);
		}
	}

	private boolean canBeRemoved(Transaction txn, Blog b)
			throws DbException {
		AuthorId authorId = b.getAuthor().getId();
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		return !localAuthor.getId().equals(authorId);
	}

	@Override
	public void removeBlog(Blog b) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			removeBlog(txn, b);
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public void removeBlog(Transaction txn, Blog b) throws DbException {
		if (!canBeRemoved(txn, b))
			throw new IllegalArgumentException();
		for (RemoveBlogHook hook : removeHooks)
			hook.removingBlog(txn, b);
		db.removeGroup(txn, b.getGroup());
	}

	@Override
	public void addLocalPost(BlogPost p) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			addLocalPost(txn, p);
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public void addLocalPost(Transaction txn, BlogPost p) throws DbException {
		try {
			GroupId groupId = p.getMessage().getGroupId();
			Blog b = getBlog(txn, groupId);

			BdfDictionary meta = new BdfDictionary();
			meta.put(KEY_TYPE, POST.getInt());
			meta.put(KEY_TIMESTAMP, p.getMessage().getTimestamp());
			meta.put(KEY_AUTHOR, clientHelper.toList(p.getAuthor()));
			meta.put(KEY_READ, true);
			meta.put(KEY_RSS_FEED, b.isRssFeed());
			clientHelper.addLocalMessage(txn, p.getMessage(), meta, true,
					false);

			// broadcast event about new post
			MessageId postId = p.getMessage().getId();
			BlogPostHeader h =
					getPostHeaderFromMetadata(txn, groupId, postId, meta);
			boolean local = !b.isRssFeed();
			BlogPostAddedEvent event =
					new BlogPostAddedEvent(groupId, h, local);
			txn.attach(event);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public void addLocalComment(LocalAuthor author, GroupId groupId,
			@Nullable String comment, BlogPostHeader parentHeader)
			throws DbException {
		db.transaction(false, txn -> {
			addLocalComment(txn, author, groupId, comment, parentHeader);
		});
	}

	@Override
	public void addLocalComment(Transaction txn, LocalAuthor author,
			GroupId groupId, @Nullable String comment,
			BlogPostHeader parentHeader) throws DbException {
		MessageType type = parentHeader.getType();
		if (type != POST && type != COMMENT)
			throw new IllegalArgumentException("Comment on unknown type!");

		try {
			// Wrap post that we are commenting on
			MessageId parentOriginalId =
					getOriginalMessageId(txn, parentHeader);
			MessageId parentCurrentId =
					wrapMessage(txn, groupId, parentHeader, parentOriginalId);

			// Create actual comment
			Message message = blogPostFactory.createBlogComment(groupId, author,
					comment, parentOriginalId, parentCurrentId);
			BdfDictionary meta = new BdfDictionary();
			meta.put(KEY_TYPE, COMMENT.getInt());
			if (comment != null) meta.put(KEY_COMMENT, comment);
			meta.put(KEY_TIMESTAMP, message.getTimestamp());
			meta.put(KEY_ORIGINAL_MSG_ID, message.getId());
			meta.put(KEY_ORIGINAL_PARENT_MSG_ID, parentOriginalId);
			meta.put(KEY_PARENT_MSG_ID, parentCurrentId);
			meta.put(KEY_AUTHOR, clientHelper.toList(author));
			meta.put(KEY_READ, true);

			// Send comment
			clientHelper.addLocalMessage(txn, message, meta, true, false);

			// broadcast event
			BlogPostHeader h = getPostHeaderFromMetadata(txn, groupId,
					message.getId(), meta);
			BlogPostAddedEvent event = new BlogPostAddedEvent(groupId, h, true);
			txn.attach(event);
		} catch (FormatException e) {
			throw new DbException(e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Invalid key of author", e);
		}
	}

	private MessageId getOriginalMessageId(Transaction txn, BlogPostHeader h)
			throws DbException, FormatException {
		MessageType type = h.getType();
		if (type == POST || type == COMMENT) return h.getId();
		BdfDictionary meta = clientHelper.getMessageMetadataAsDictionary(txn,
				h.getId());
		return new MessageId(meta.getRaw(KEY_ORIGINAL_MSG_ID));
	}

	private MessageId wrapMessage(Transaction txn, GroupId groupId,
			BlogPostHeader header, MessageId originalId)
			throws DbException, FormatException {

		if (groupId.equals(header.getGroupId())) {
			// We are trying to wrap a post that is already in our group.
			// This is unnecessary, so just return the post's MessageId
			return header.getId();
		}

		// Get body of message to be wrapped
		BdfList body = clientHelper.getMessageAsList(txn, header.getId());
		long timestamp = header.getTimestamp();
		Message wrappedMessage;

		BdfDictionary meta = new BdfDictionary();
		MessageType type = header.getType();
		if (type == POST) {
			// Wrap post
			Group group = db.getGroup(txn, header.getGroupId());
			byte[] descriptor = group.getDescriptor();
			wrappedMessage = blogPostFactory.wrapPost(groupId, descriptor,
					timestamp, body);
			meta.put(KEY_TYPE, WRAPPED_POST.getInt());
			meta.put(KEY_RSS_FEED, header.isRssFeed());
		} else if (type == COMMENT) {
			// Recursively wrap parent
			BlogCommentHeader commentHeader = (BlogCommentHeader) header;
			BlogPostHeader parentHeader = commentHeader.getParent();
			MessageId parentOriginalId =
					getOriginalMessageId(txn, parentHeader);
			MessageId parentCurrentId =
					wrapMessage(txn, groupId, parentHeader, parentOriginalId);
			// Wrap comment
			Group group = db.getGroup(txn, header.getGroupId());
			byte[] descriptor = group.getDescriptor();
			wrappedMessage = blogPostFactory.wrapComment(groupId, descriptor,
					timestamp, body, parentCurrentId);
			meta.put(KEY_TYPE, WRAPPED_COMMENT.getInt());
			if (commentHeader.getComment() != null)
				meta.put(KEY_COMMENT, commentHeader.getComment());
			meta.put(KEY_PARENT_MSG_ID, parentCurrentId);
		} else if (type == WRAPPED_POST) {
			// Re-wrap wrapped post without adding another wrapping layer
			wrappedMessage = blogPostFactory.rewrapWrappedPost(groupId, body);
			meta.put(KEY_TYPE, WRAPPED_POST.getInt());
			meta.put(KEY_RSS_FEED, header.isRssFeed());
		} else if (type == WRAPPED_COMMENT) {
			// Recursively wrap parent
			BlogCommentHeader commentHeader = (BlogCommentHeader) header;
			BlogPostHeader parentHeader = commentHeader.getParent();
			MessageId parentOriginalId =
					getOriginalMessageId(txn, parentHeader);
			MessageId parentCurrentId =
					wrapMessage(txn, groupId, parentHeader, parentOriginalId);
			// Re-wrap wrapped comment
			wrappedMessage = blogPostFactory.rewrapWrappedComment(groupId, body,
					parentCurrentId);
			meta.put(KEY_TYPE, WRAPPED_COMMENT.getInt());
			if (commentHeader.getComment() != null)
				meta.put(KEY_COMMENT, commentHeader.getComment());
			meta.put(KEY_PARENT_MSG_ID, parentCurrentId);
		} else {
			throw new IllegalArgumentException(
					"Unknown Message Type: " + type);
		}
		meta.put(KEY_ORIGINAL_MSG_ID, originalId);
		meta.put(KEY_AUTHOR, clientHelper.toList(header.getAuthor()));
		meta.put(KEY_TIMESTAMP, header.getTimestamp());
		meta.put(KEY_TIME_RECEIVED, header.getTimeReceived());

		// Send wrapped message and store metadata
		clientHelper.addLocalMessage(txn, wrappedMessage, meta, true, false);
		return wrappedMessage.getId();
	}

	@Override
	public Blog getBlog(GroupId g) throws DbException {
		Blog blog;
		Transaction txn = db.startTransaction(true);
		try {
			blog = getBlog(txn, g);
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
		return blog;
	}

	@Override
	public Blog getBlog(Transaction txn, GroupId g) throws DbException {
		try {
			Group group = db.getGroup(txn, g);
			return blogFactory.parseBlog(group);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public Collection<Blog> getBlogs(LocalAuthor localAuthor)
			throws DbException {

		Collection<Blog> allBlogs = getBlogs();
		List<Blog> blogs = new ArrayList<>();
		for (Blog b : allBlogs) {
			if (b.getAuthor().equals(localAuthor)) {
				blogs.add(b);
			}
		}
		return blogs;
	}

	@Override
	public Blog getPersonalBlog(Author author) {
		return blogFactory.createBlog(author);
	}

	@Override
	public Collection<Blog> getBlogs() throws DbException {
		return db.transactionWithResult(true, this::getBlogs);
	}

	@Override
	public Collection<Blog> getBlogs(Transaction txn) throws DbException {
		try {
			List<Blog> blogs = new ArrayList<>();
			Collection<Group> groups =
					db.getGroups(txn, CLIENT_ID, MAJOR_VERSION);
			for (Group g : groups) {
				blogs.add(blogFactory.parseBlog(g));
			}
			return blogs;
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public Collection<GroupId> getBlogIds(Transaction txn) throws DbException {
		List<GroupId> groupIds = new ArrayList<>();
		Collection<Group> groups = db.getGroups(txn, CLIENT_ID, MAJOR_VERSION);
		for (Group g : groups) groupIds.add(g.getId());
		return groupIds;
	}

	@Override
	public BlogPostHeader getPostHeader(Transaction txn, GroupId g, MessageId m)
			throws DbException {
		try {
			BdfDictionary meta =
					clientHelper.getMessageMetadataAsDictionary(txn, m);
			return getPostHeaderFromMetadata(txn, g, m, meta);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public String getPostText(MessageId m) throws DbException {
		try {
			return getPostText(clientHelper.getMessageAsList(m));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public String getPostText(Transaction txn, MessageId m) throws DbException {
		try {
			return getPostText(clientHelper.getMessageAsList(txn, m));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private String getPostText(BdfList message) throws FormatException {
		MessageType type = MessageType.valueOf(message.getInt(0));
		if (type == POST) {
			// Type, text, signature
			return message.getString(1);
		} else if (type == WRAPPED_POST) {
			// Type, copied group descriptor, copied timestamp, copied text,
			// copied signature
			return message.getString(3);
		} else {
			throw new FormatException();
		}
	}

	@Override
	public Collection<BlogPostHeader> getPostHeaders(GroupId g)
			throws DbException {
		return db.transactionWithResult(true, txn -> getPostHeaders(txn, g));
	}

	@Override
	public List<BlogPostHeader> getPostHeaders(Transaction txn, GroupId g)
			throws DbException {
		// Query for posts and comments only
		BdfDictionary query1 = BdfDictionary.of(
				new BdfEntry(KEY_TYPE, POST.getInt())
		);
		BdfDictionary query2 = BdfDictionary.of(
				new BdfEntry(KEY_TYPE, COMMENT.getInt())
		);

		List<BlogPostHeader> headers = new ArrayList<>();
		try {
			Map<MessageId, BdfDictionary> metadata1 =
					clientHelper.getMessageMetadataAsDictionary(txn, g, query1);
			Map<MessageId, BdfDictionary> metadata2 =
					clientHelper.getMessageMetadataAsDictionary(txn, g, query2);
			Map<MessageId, BdfDictionary> metadata =
					new HashMap<>(metadata1.size() + metadata2.size());
			metadata.putAll(metadata1);
			metadata.putAll(metadata2);
			// get all authors we need to get the information for
			Set<AuthorId> authors = new HashSet<>();
			for (Entry<MessageId, BdfDictionary> entry : metadata.entrySet()) {
				BdfList authorList = entry.getValue().getList(KEY_AUTHOR);
				Author a = clientHelper.parseAndValidateAuthor(authorList);
				authors.add(a.getId());
			}
			// get information for all authors
			Map<AuthorId, AuthorInfo> authorInfos = new HashMap<>();
			for (AuthorId authorId : authors) {
				authorInfos.put(authorId,
						authorManager.getAuthorInfo(txn, authorId));
			}
			// get post headers
			for (Entry<MessageId, BdfDictionary> entry : metadata.entrySet()) {
				BdfDictionary meta = entry.getValue();
				BlogPostHeader h = getPostHeaderFromMetadata(txn, g,
						entry.getKey(), meta, authorInfos);
				headers.add(h);
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return headers;
	}

	@Override
	public void setReadFlag(MessageId m, boolean read) throws DbException {
		db.transaction(true, txn -> {
			setReadFlag(txn, m, read);
		});
	}

	@Override
	public void setReadFlag(Transaction txn, MessageId m, boolean read)
			throws DbException {
		try {
			BdfDictionary meta = new BdfDictionary();
			meta.put(KEY_READ, read);
			clientHelper.mergeMessageMetadata(txn, m, meta);
		} catch (FormatException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void registerRemoveBlogHook(RemoveBlogHook hook) {
		removeHooks.add(hook);
	}

	private BlogPostHeader getPostHeaderFromMetadata(Transaction txn,
			GroupId groupId, MessageId id) throws DbException, FormatException {
		BdfDictionary meta =
				clientHelper.getMessageMetadataAsDictionary(txn, id);
		return getPostHeaderFromMetadata(txn, groupId, id, meta);
	}

	private BlogPostHeader getPostHeaderFromMetadata(Transaction txn,
			GroupId groupId, MessageId id, BdfDictionary meta)
			throws DbException, FormatException {
		return getPostHeaderFromMetadata(txn, groupId, id, meta,
				Collections.emptyMap());
	}

	private BlogPostHeader getPostHeaderFromMetadata(Transaction txn,
			GroupId groupId, MessageId id, BdfDictionary meta,
			Map<AuthorId, AuthorInfo> authorInfos)
			throws DbException, FormatException {

		MessageType type = getMessageType(meta);

		long timestamp = meta.getLong(KEY_TIMESTAMP);
		long timeReceived = meta.getLong(KEY_TIME_RECEIVED, timestamp);

		BdfList authorList = meta.getList(KEY_AUTHOR);
		Author author = clientHelper.parseAndValidateAuthor(authorList);
		boolean isFeedPost = meta.getBoolean(KEY_RSS_FEED, false);
		AuthorInfo authorInfo;
		if (isFeedPost) {
			authorInfo = new AuthorInfo(NONE);
		} else if (authorInfos.containsKey(author.getId())) {
			authorInfo = authorInfos.get(author.getId());
		} else {
			authorInfo = authorManager.getAuthorInfo(txn, author.getId());
		}

		boolean read = meta.getBoolean(KEY_READ, false);

		if (type == COMMENT || type == WRAPPED_COMMENT) {
			String comment = meta.getOptionalString(KEY_COMMENT);
			MessageId parentId = new MessageId(meta.getRaw(KEY_PARENT_MSG_ID));
			BlogPostHeader parent =
					getPostHeaderFromMetadata(txn, groupId, parentId);
			return new BlogCommentHeader(type, groupId, comment, parent, id,
					timestamp, timeReceived, author, authorInfo, read);
		} else {
			return new BlogPostHeader(type, groupId, id, timestamp,
					timeReceived, author, authorInfo, isFeedPost, read);
		}
	}

	private MessageType getMessageType(BdfDictionary d) throws FormatException {
		return MessageType.valueOf(d.getInt(KEY_TYPE));
	}
}

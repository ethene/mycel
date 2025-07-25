package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@NotNullByDefault
public interface BlogManager {

	/**
	 * The unique ID of the blog client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.blog");

	/**
	 * The current major version of the blog client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the blog client.
	 */
	int MINOR_VERSION = 0;

	/**
	 * Adds the given {@link Blog}.
	 */
	void addBlog(Blog b) throws DbException;

	/**
	 * Adds the given {@link Blog} within the given {@link Transaction}.
	 */
	void addBlog(Transaction txn, Blog b) throws DbException;

	/**
	 * Returns true if a blog can be removed.
	 */
	boolean canBeRemoved(Blog b) throws DbException;

	/**
	 * Removes and deletes a blog.
	 */
	void removeBlog(Blog b) throws DbException;

	/**
	 * Removes and deletes a blog with the given {@link Transaction}.
	 */
	void removeBlog(Transaction txn, Blog b) throws DbException;

	/**
	 * Stores a local blog post.
	 */
	void addLocalPost(BlogPost p) throws DbException;

	/**
	 * Stores a local blog post.
	 */
	void addLocalPost(Transaction txn, BlogPost p) throws DbException;

	/**
	 * Adds a comment to an existing blog post or reblogs it.
	 */
	void addLocalComment(LocalAuthor author, GroupId groupId,
			@Nullable String comment, BlogPostHeader parentHeader)
			throws DbException;

	/**
	 * Adds a comment to an existing blog post or reblogs it.
	 */
	void addLocalComment(Transaction txn, LocalAuthor author,
			GroupId groupId, @Nullable String comment,
			BlogPostHeader parentHeader) throws DbException;

	/**
	 * Returns the blog with the given ID.
	 */
	Blog getBlog(GroupId g) throws DbException;

	/**
	 * Returns the blog with the given ID.
	 */
	Blog getBlog(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns all blogs owned by the given localAuthor.
	 */
	Collection<Blog> getBlogs(LocalAuthor localAuthor) throws DbException;

	/**
	 * Returns only the personal blog of the given author.
	 */
	Blog getPersonalBlog(Author author);

	/**
	 * Returns all blogs to which the user subscribes.
	 */
	Collection<Blog> getBlogs() throws DbException;

	/**
	 * Returns all blogs to which the user subscribes.
	 */
	Collection<Blog> getBlogs(Transaction txn) throws DbException;

	/**
	 * Returns the group IDs of all blogs to which the user subscribes.
	 */
	Collection<GroupId> getBlogIds(Transaction txn) throws DbException;

	/**
	 * Returns the header of the blog post with the given ID.
	 */
	BlogPostHeader getPostHeader(Transaction txn, GroupId g, MessageId m)
			throws DbException;

	/**
	 * Returns the text of the blog post with the given ID.
	 */
	String getPostText(MessageId m) throws DbException;

	/**
	 * Returns the text of the blog post with the given ID.
	 */
	String getPostText(Transaction txn, MessageId m) throws DbException;

	/**
	 * Returns the headers of all posts in the given blog.
	 */
	Collection<BlogPostHeader> getPostHeaders(GroupId g) throws DbException;

	/**
	 * Returns the headers of all posts in the given blog.
	 */
	List<BlogPostHeader> getPostHeaders(Transaction txn, GroupId g)
			throws DbException;

	/**
	 * Marks a blog post as read or unread.
	 */
	void setReadFlag(MessageId m, boolean read) throws DbException;

	/**
	 * Marks a blog post as read or unread.
	 */
	void setReadFlag(Transaction txn, MessageId m, boolean read) throws DbException;

	/**
	 * Registers a hook to be called whenever a blog is removed.
	 */
	void registerRemoveBlogHook(RemoveBlogHook hook);

	interface RemoveBlogHook {
		/**
		 * Called when a blog is being removed.
		 *
		 * @param txn A read-write transaction
		 * @param b The blog that is being removed
		 */
		void removingBlog(Transaction txn, Blog b) throws DbException;
	}

}

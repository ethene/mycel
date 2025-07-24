package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.app.api.client.BaseGroup;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class Blog extends BaseGroup implements Shareable {

	private final Author author;
	private final boolean rssFeed;

	public Blog(Group group, Author author, boolean rssFeed) {
		super(group);
		this.author = author;
		this.rssFeed = rssFeed;
	}

	public Author getAuthor() {
		return author;
	}

	public boolean isRssFeed() {
		return rssFeed;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Blog && super.equals(o);
	}

	/**
	 * Returns the blog's author's name, not the name as shown in the UI.
	 */
	@Override
	public String getName() {
		return author.getName();
	}

}

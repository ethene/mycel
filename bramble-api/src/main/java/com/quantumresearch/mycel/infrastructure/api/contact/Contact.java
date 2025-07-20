package com.quantumresearch.mycel.infrastructure.api.contact;

import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import com.quantumresearch.mycel.infrastructure.api.identity.Author;
import com.quantumresearch.mycel.infrastructure.api.identity.AuthorId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.infrastructure.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.toUtf8;

@Immutable
@NotNullByDefault
public class Contact {

	private final ContactId id;
	private final Author author;
	private final AuthorId localAuthorId;
	@Nullable
	private final String alias;
	@Nullable
	private final PublicKey handshakePublicKey;
	private final boolean verified;

	public Contact(ContactId id, Author author, AuthorId localAuthorId,
			@Nullable String alias, @Nullable PublicKey handshakePublicKey,
			boolean verified) {
		if (alias != null) {
			int aliasLength = toUtf8(alias).length;
			if (aliasLength == 0 || aliasLength > MAX_AUTHOR_NAME_LENGTH)
				throw new IllegalArgumentException();
		}
		this.id = id;
		this.author = author;
		this.localAuthorId = localAuthorId;
		this.alias = alias;
		this.handshakePublicKey = handshakePublicKey;
		this.verified = verified;
	}

	public ContactId getId() {
		return id;
	}

	public Author getAuthor() {
		return author;
	}

	public AuthorId getLocalAuthorId() {
		return localAuthorId;
	}

	@Nullable
	public String getAlias() {
		return alias;
	}

	@Nullable
	public PublicKey getHandshakePublicKey() {
		return handshakePublicKey;
	}

	public boolean isVerified() {
		return verified;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Contact && id.equals(((Contact) o).id);
	}
}

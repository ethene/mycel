package com.quantumresearch.mycel.spore.contact;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.contact.PendingContact;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContactState;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactStateChangedEvent;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.NoSuchContactException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.transport.KeyManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.contact.PendingContactState.WAITING_FOR_CONNECTION;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.spore.util.StringUtils.toUtf8;

@ThreadSafe
@NotNullByDefault
class ContactManagerImpl implements ContactManager, EventListener {

	private final DatabaseComponent db;
	private final KeyManager keyManager;
	private final IdentityManager identityManager;
	private final PendingContactFactory pendingContactFactory;

	private final List<ContactHook> hooks = new CopyOnWriteArrayList<>();
	private final Map<PendingContactId, PendingContactState> states =
			new ConcurrentHashMap<>();

	@Inject
	ContactManagerImpl(DatabaseComponent db,
			KeyManager keyManager,
			IdentityManager identityManager,
			PendingContactFactory pendingContactFactory) {
		this.db = db;
		this.keyManager = keyManager;
		this.identityManager = identityManager;
		this.pendingContactFactory = pendingContactFactory;
	}

	@Override
	public void registerContactHook(ContactHook hook) {
		hooks.add(hook);
	}

	@Override
	public ContactId addContact(Transaction txn, Author remote, AuthorId local,
			SecretKey rootKey, long timestamp, boolean alice, boolean verified,
			boolean active) throws DbException {
		ContactId c = db.addContact(txn, remote, local, null, verified);
		keyManager.addRotationKeys(txn, c, rootKey, timestamp, alice, active);
		Contact contact = db.getContact(txn, c);
		for (ContactHook hook : hooks) hook.addingContact(txn, contact);
		return c;
	}

	@Override
	public ContactId addContact(Transaction txn, PendingContactId p,
			Author remote, AuthorId local, SecretKey rootKey, long timestamp,
			boolean alice, boolean verified, boolean active)
			throws DbException, GeneralSecurityException {
		PendingContact pendingContact = db.getPendingContact(txn, p);
		db.removePendingContact(txn, p);
		states.remove(p);
		PublicKey theirPublicKey = pendingContact.getPublicKey();
		ContactId c =
				db.addContact(txn, remote, local, theirPublicKey, verified);
		String alias = pendingContact.getAlias();
		if (!alias.equals(remote.getName())) db.setContactAlias(txn, c, alias);
		KeyPair ourKeyPair = identityManager.getHandshakeKeys(txn);
		keyManager.addContact(txn, c, theirPublicKey, ourKeyPair);
		keyManager.addRotationKeys(txn, c, rootKey, timestamp, alice, active);
		Contact contact = db.getContact(txn, c);
		for (ContactHook hook : hooks) hook.addingContact(txn, contact);
		return c;
	}

	@Override
	public ContactId addContact(Transaction txn, Author remote, AuthorId local,
			boolean verified) throws DbException {
		ContactId c = db.addContact(txn, remote, local, null, verified);
		Contact contact = db.getContact(txn, c);
		for (ContactHook hook : hooks) hook.addingContact(txn, contact);
		return c;
	}

	@Override
	public ContactId addContact(Author remote, AuthorId local,
			SecretKey rootKey, long timestamp, boolean alice, boolean verified,
			boolean active) throws DbException {
		return db.transactionWithResult(false, txn ->
				addContact(txn, remote, local, rootKey, timestamp, alice,
						verified, active));
	}

	@Override
	public String getHandshakeLink() throws DbException {
		return db.transactionWithResult(true, this::getHandshakeLink);
	}

	@Override
	public String getHandshakeLink(Transaction txn) throws DbException {
		KeyPair keyPair = identityManager.getHandshakeKeys(txn);
		return pendingContactFactory.createHandshakeLink(keyPair.getPublic());
	}

	@Override
	public PendingContact addPendingContact(Transaction txn, String link,
			String alias)
			throws DbException, FormatException, GeneralSecurityException {
		PendingContact p =
				pendingContactFactory.createPendingContact(link, alias);
		AuthorId local = identityManager.getLocalAuthor(txn).getId();
		db.addPendingContact(txn, p, local);
		KeyPair ourKeyPair = identityManager.getHandshakeKeys(txn);
		keyManager.addPendingContact(txn, p.getId(), p.getPublicKey(),
				ourKeyPair);
		return p;
	}

	@Override
	public PendingContact addPendingContact(String link, String alias)
			throws DbException, FormatException, GeneralSecurityException {
		Transaction txn = db.startTransaction(false);
		try {
			PendingContact p = addPendingContact(txn, link, alias);
			db.commitTransaction(txn);
			return p;
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public PendingContact getPendingContact(Transaction txn, PendingContactId p)
			throws DbException {
		return db.getPendingContact(txn, p);
	}

	@Override
	public Collection<Pair<PendingContact, PendingContactState>> getPendingContacts()
			throws DbException {
		return db.transactionWithResult(true, this::getPendingContacts);
	}

	@Override
	public Collection<Pair<PendingContact, PendingContactState>> getPendingContacts(
			Transaction txn)
			throws DbException {
		Collection<PendingContact> pendingContacts = db.getPendingContacts(txn);
		List<Pair<PendingContact, PendingContactState>> pairs =
				new ArrayList<>(pendingContacts.size());
		for (PendingContact p : pendingContacts) {
			PendingContactState state = states.get(p.getId());
			if (state == null) state = WAITING_FOR_CONNECTION;
			pairs.add(new Pair<>(p, state));
		}
		return pairs;
	}

	@Override
	public void removePendingContact(PendingContactId p) throws DbException {
		db.transaction(false, txn -> removePendingContact(txn, p));
	}

	@Override
	public void removePendingContact(Transaction txn, PendingContactId p)
			throws DbException {
		db.removePendingContact(txn, p);
		states.remove(p);
	}

	@Override
	public Contact getContact(ContactId c) throws DbException {
		return db.transactionWithResult(true, txn -> db.getContact(txn, c));
	}

	@Override
	public Contact getContact(Transaction txn, ContactId c) throws DbException {
		return db.getContact(txn, c);
	}

	@Override
	public Contact getContact(AuthorId remoteAuthorId, AuthorId localAuthorId)
			throws DbException {
		return db.transactionWithResult(true, txn ->
				getContact(txn, remoteAuthorId, localAuthorId));
	}

	@Override
	public Contact getContact(Transaction txn, AuthorId remoteAuthorId,
			AuthorId localAuthorId) throws DbException {
		Collection<Contact> contacts =
				db.getContactsByAuthorId(txn, remoteAuthorId);
		for (Contact c : contacts) {
			if (c.getLocalAuthorId().equals(localAuthorId)) {
				return c;
			}
		}
		throw new NoSuchContactException();
	}

	@Override
	public Collection<Contact> getContacts() throws DbException {
		return db.transactionWithResult(true, db::getContacts);
	}

	@Override
	public Collection<Contact> getContacts(Transaction txn) throws DbException {
		return db.getContacts(txn);
	}

	@Override
	public void removeContact(ContactId c) throws DbException {
		db.transaction(false, txn -> removeContact(txn, c));
	}

	@Override
	public void setContactAlias(Transaction txn, ContactId c,
			@Nullable String alias) throws DbException {
		if (alias != null) {
			int aliasLength = toUtf8(alias).length;
			if (aliasLength == 0 || aliasLength > MAX_AUTHOR_NAME_LENGTH)
				throw new IllegalArgumentException();
		}
		db.setContactAlias(txn, c, alias);
	}

	@Override
	public void setContactAlias(ContactId c, @Nullable String alias)
			throws DbException {
		db.transaction(false, txn -> setContactAlias(txn, c, alias));
	}

	@Override
	public boolean contactExists(Transaction txn, AuthorId remoteAuthorId,
			AuthorId localAuthorId) throws DbException {
		return db.containsContact(txn, remoteAuthorId, localAuthorId);
	}

	@Override
	public boolean contactExists(AuthorId remoteAuthorId,
			AuthorId localAuthorId) throws DbException {
		return db.transactionWithResult(true, txn ->
				contactExists(txn, remoteAuthorId, localAuthorId));
	}

	@Override
	public void removeContact(Transaction txn, ContactId c)
			throws DbException {
		Contact contact = db.getContact(txn, c);
		for (ContactHook hook : hooks) hook.removingContact(txn, contact);
		db.removeContact(txn, c);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof PendingContactStateChangedEvent) {
			PendingContactStateChangedEvent p =
					(PendingContactStateChangedEvent) e;
			states.put(p.getId(), p.getPendingContactState());
		}
	}
}

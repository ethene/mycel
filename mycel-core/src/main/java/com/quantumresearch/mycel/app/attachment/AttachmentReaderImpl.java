package com.quantumresearch.mycel.app.attachment;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.NoSuchMessageException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.db.TransactionManager;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.attachment.Attachment;
import com.quantumresearch.mycel.app.api.attachment.AttachmentHeader;
import com.quantumresearch.mycel.app.api.attachment.AttachmentReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.inject.Inject;

import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;

public class AttachmentReaderImpl implements AttachmentReader {

	private final TransactionManager db;
	private final ClientHelper clientHelper;

	@Inject
	public AttachmentReaderImpl(TransactionManager db,
			ClientHelper clientHelper) {
		this.db = db;
		this.clientHelper = clientHelper;
	}

	@Override
	public Attachment getAttachment(AttachmentHeader h) throws DbException {
		return db.transactionWithResult(true, txn -> getAttachment(txn, h));
	}

	@Override
	public Attachment getAttachment(Transaction txn, AttachmentHeader h)
			throws DbException {
		// TODO: Support large messages
		MessageId m = h.getMessageId();
		Message message = clientHelper.getMessage(txn, m);
		// Check that the message is in the expected group, to prevent it from
		// being loaded in the context of a different group
		if (!message.getGroupId().equals(h.getGroupId())) {
			throw new NoSuchMessageException();
		}
		byte[] body = message.getBody();
		try {
			BdfDictionary meta =
					clientHelper.getMessageMetadataAsDictionary(txn, m);
			String contentType = meta.getString(MSG_KEY_CONTENT_TYPE);
			if (!contentType.equals(h.getContentType()))
				throw new NoSuchMessageException();
			int offset = meta.getInt(MSG_KEY_DESCRIPTOR_LENGTH);
			InputStream stream = new ByteArrayInputStream(body, offset,
					body.length - offset);
			return new Attachment(h, stream);
		} catch (FormatException e) {
			throw new NoSuchMessageException();
		}
	}

}

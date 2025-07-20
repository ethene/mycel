package com.quantumresearch.mycel.infrastructure.keyagreement;

import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.data.BdfWriter;
import com.quantumresearch.mycel.infrastructure.api.data.BdfWriterFactory;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.Payload;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.PayloadEncoder;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.TransportDescriptor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConstants.QR_FORMAT_ID;
import static com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConstants.QR_FORMAT_VERSION;

@Immutable
@NotNullByDefault
class PayloadEncoderImpl implements PayloadEncoder {

	private final BdfWriterFactory bdfWriterFactory;

	@Inject
	PayloadEncoderImpl(BdfWriterFactory bdfWriterFactory) {
		this.bdfWriterFactory = bdfWriterFactory;
	}

	@Override
	public byte[] encode(Payload p) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int formatIdAndVersion = (QR_FORMAT_ID << 5) | QR_FORMAT_VERSION;
		out.write(formatIdAndVersion);
		BdfList payload = new BdfList();
		payload.add(p.getCommitment());
		for (TransportDescriptor d : p.getTransportDescriptors()) {
			payload.add(d.getDescriptor());
		}
		BdfWriter w = bdfWriterFactory.createWriter(out);
		try {
			w.writeList(payload);
		} catch (IOException e) {
			// Shouldn't happen with ByteArrayOutputStream
			throw new AssertionError(e);
		}
		return out.toByteArray();
	}
}

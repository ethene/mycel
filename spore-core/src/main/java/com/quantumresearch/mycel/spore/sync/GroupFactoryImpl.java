package com.quantumresearch.mycel.spore.sync;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupFactory;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.util.ByteUtils;
import com.quantumresearch.mycel.spore.util.StringUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.sync.Group.FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.api.sync.GroupId.LABEL;
import static com.quantumresearch.mycel.spore.util.ByteUtils.INT_32_BYTES;

@Immutable
@NotNullByDefault
class GroupFactoryImpl implements GroupFactory {

	private static final byte[] FORMAT_VERSION_BYTES =
			new byte[] {FORMAT_VERSION};

	private final CryptoComponent crypto;

	@Inject
	GroupFactoryImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public Group createGroup(ClientId c, int majorVersion, byte[] descriptor) {
		byte[] majorVersionBytes = new byte[INT_32_BYTES];
		ByteUtils.writeUint32(majorVersion, majorVersionBytes, 0);
		byte[] hash = crypto.hash(LABEL, FORMAT_VERSION_BYTES,
				StringUtils.toUtf8(c.getString()), majorVersionBytes,
				descriptor);
		return new Group(new GroupId(hash), c, majorVersion, descriptor);
	}
}

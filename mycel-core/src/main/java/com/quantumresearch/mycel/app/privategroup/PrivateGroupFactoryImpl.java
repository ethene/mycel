package com.quantumresearch.mycel.app.privategroup;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupFactory;
import com.quantumresearch.mycel.spore.util.StringUtils;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.SecureRandom;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkSize;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupConstants.GROUP_SALT_LENGTH;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupConstants.MAX_GROUP_NAME_LENGTH;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager.CLIENT_ID;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager.MAJOR_VERSION;

@Immutable
@NotNullByDefault
class PrivateGroupFactoryImpl implements PrivateGroupFactory {

	private final GroupFactory groupFactory;
	private final ClientHelper clientHelper;
	private final SecureRandom random;

	@Inject
	PrivateGroupFactoryImpl(GroupFactory groupFactory,
			ClientHelper clientHelper, SecureRandom random) {

		this.groupFactory = groupFactory;
		this.clientHelper = clientHelper;
		this.random = random;
	}

	@Override
	public PrivateGroup createPrivateGroup(String name, Author creator) {
		int length = StringUtils.toUtf8(name).length;
		if (length == 0 || length > MAX_GROUP_NAME_LENGTH)
			throw new IllegalArgumentException();
		byte[] salt = new byte[GROUP_SALT_LENGTH];
		random.nextBytes(salt);
		return createPrivateGroup(name, creator, salt);
	}

	@Override
	public PrivateGroup createPrivateGroup(String name, Author creator,
			byte[] salt) {
		try {
			BdfList creatorList = clientHelper.toList(creator);
			BdfList group = BdfList.of(creatorList, name, salt);
			byte[] descriptor = clientHelper.toByteArray(group);
			Group g = groupFactory.createGroup(CLIENT_ID, MAJOR_VERSION,
					descriptor);
			return new PrivateGroup(g, name, creator, salt);
		} catch (FormatException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PrivateGroup parsePrivateGroup(Group g) throws FormatException {
		// Creator, group name, salt
		BdfList descriptor = clientHelper.toList(g.getDescriptor());
		checkSize(descriptor, 3);
		BdfList creatorList = descriptor.getList(0);
		String groupName = descriptor.getString(1);
		checkLength(groupName, 1, MAX_GROUP_NAME_LENGTH);
		byte[] salt = descriptor.getRaw(2);
		checkLength(salt, GROUP_SALT_LENGTH);

		Author creator = clientHelper.parseAndValidateAuthor(creatorList);
		return new PrivateGroup(g, groupName, creator, salt);
	}

}

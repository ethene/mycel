package com.quantumresearch.mycel.spore.mailbox;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.quantumresearch.mycel.spore.api.WeakSingletonProvider;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.mailbox.InvalidMailboxIdException;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxAuthToken;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFileId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.fasterxml.jackson.databind.MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static okhttp3.internal.Util.EMPTY_REQUEST;
import static com.quantumresearch.mycel.spore.util.IoUtils.copyAndClose;

@NotNullByDefault
class MailboxApiImpl implements MailboxApi {

	private static final MediaType JSON =
			requireNonNull(MediaType.parse("application/json; charset=utf-8"));
	private static final MediaType FILE =
			requireNonNull(MediaType.parse("application/octet-stream"));

	private final WeakSingletonProvider<OkHttpClient> httpClientProvider;
	private final JsonMapper mapper = JsonMapper.builder()
			.enable(BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
			.build();
	private final UrlConverter urlConverter;

	@Inject
	MailboxApiImpl(WeakSingletonProvider<OkHttpClient> httpClientProvider,
			UrlConverter urlConverter) {
		this.httpClientProvider = httpClientProvider;
		this.urlConverter = urlConverter;
	}

	@Override
	public List<MailboxVersion> getServerSupports(MailboxProperties properties)
			throws IOException, ApiException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		Response response = sendGetRequest(properties, "/versions");
		if (response.code() != 200) throw new ApiException();

		ResponseBody body = response.body();
		if (body == null) throw new ApiException();
		try {
			JsonNode node = mapper.readTree(body.string());
			return parseServerSupports(node);
		} catch (JacksonException e) {
			throw new ApiException();
		}
	}

	@Override
	public MailboxProperties setup(MailboxProperties properties)
			throws IOException, ApiException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		Request request = getRequestBuilder(properties.getAuthToken())
				.url(getBaseUrl(properties) + "/setup")
				.put(EMPTY_REQUEST)
				.build();
		OkHttpClient client = httpClientProvider.get();
		Response response = client.newCall(request).execute();
		if (response.code() == 401) throw new MailboxAlreadyPairedException();
		if (!response.isSuccessful()) throw new ApiException();
		ResponseBody body = response.body();
		if (body == null) throw new ApiException();
		try {
			JsonNode node = mapper.readTree(body.string());
			JsonNode tokenNode = node.get("token");
			if (tokenNode == null) {
				throw new ApiException();
			}
			return new MailboxProperties(properties.getOnion(),
					MailboxAuthToken.fromString(tokenNode.textValue()),
					parseServerSupports(node));
		} catch (JacksonException | InvalidMailboxIdException e) {
			throw new ApiException();
		}
	}

	private List<MailboxVersion> parseServerSupports(JsonNode node)
			throws ApiException {
		List<MailboxVersion> serverSupports = new ArrayList<>();
		ArrayNode serverSupportsNode = getArray(node, "serverSupports");
		for (JsonNode versionNode : serverSupportsNode) {
			if (!versionNode.isObject()) throw new ApiException();
			ObjectNode objectNode = (ObjectNode) versionNode;
			JsonNode majorNode = objectNode.get("major");
			JsonNode minorNode = objectNode.get("minor");
			if (majorNode == null || !majorNode.isNumber()) {
				throw new ApiException();
			}
			if (minorNode == null || !minorNode.isNumber()) {
				throw new ApiException();
			}
			int major = majorNode.asInt();
			int minor = minorNode.asInt();
			if (major < 0 || minor < 0) throw new ApiException();
			serverSupports.add(new MailboxVersion(major, minor));
		}
		// Sort the list of versions for easier comparison
		sort(serverSupports);
		return serverSupports;
	}

	@Override
	public boolean checkStatus(MailboxProperties properties)
			throws IOException, ApiException {
		Response response = sendGetRequest(properties, "/status");
		if (response.code() == 401) throw new ApiException();
		return response.isSuccessful();
	}

	@Override
	public void wipeMailbox(MailboxProperties properties)
			throws IOException, ApiException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		Request request = getRequestBuilder(properties.getAuthToken())
				.url(getBaseUrl(properties) + "/")
				.delete()
				.build();
		OkHttpClient client = httpClientProvider.get();
		Response response = client.newCall(request).execute();
		if (response.code() != 204) throw new ApiException();
	}

	/* Contact Management API (owner only) */

	@Override
	public void addContact(MailboxProperties properties, MailboxContact contact)
			throws IOException, ApiException, TolerableFailureException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		byte[] bodyBytes = mapper.writeValueAsBytes(contact);
		RequestBody body = RequestBody.create(JSON, bodyBytes);
		Response response = sendPostRequest(properties, "/contacts", body);
		if (response.code() == 409) throw new TolerableFailureException();
		if (!response.isSuccessful()) throw new ApiException();
	}

	@Override
	public void deleteContact(MailboxProperties properties, ContactId contactId)
			throws IOException, ApiException, TolerableFailureException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		String url = getBaseUrl(properties) + "/contacts/" +
				contactId.getInt();
		Request request = getRequestBuilder(properties.getAuthToken())
				.delete()
				.url(url)
				.build();
		OkHttpClient client = httpClientProvider.get();
		Response response = client.newCall(request).execute();
		if (response.code() == 404) throw new TolerableFailureException();
		if (response.code() != 200) throw new ApiException();
	}

	@Override
	public Collection<ContactId> getContacts(MailboxProperties properties)
			throws IOException, ApiException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		Response response = sendGetRequest(properties, "/contacts");
		if (response.code() != 200) throw new ApiException();

		ResponseBody body = response.body();
		if (body == null) throw new ApiException();
		try {
			JsonNode node = mapper.readTree(body.string());
			ArrayNode contactsNode = getArray(node, "contacts");
			List<ContactId> list = new ArrayList<>();
			for (JsonNode contactNode : contactsNode) {
				if (!contactNode.isNumber()) throw new ApiException();
				int id = contactNode.intValue();
				if (id < 1) throw new ApiException();
				list.add(new ContactId(id));
			}
			return list;
		} catch (JacksonException e) {
			throw new ApiException();
		}
	}

	/* File Management (owner and contacts) */

	@Override
	public void addFile(MailboxProperties properties, MailboxFolderId folderId,
			File file) throws IOException, ApiException {
		String path = "/files/" + folderId;
		RequestBody body = RequestBody.create(FILE, file);
		Response response = sendPostRequest(properties, path, body);
		if (response.code() != 200) throw new ApiException();
	}

	@Override
	public List<MailboxFile> getFiles(MailboxProperties properties,
			MailboxFolderId folderId)
			throws IOException, ApiException, TolerableFailureException {
		String path = "/files/" + folderId;
		Response response = sendGetRequest(properties, path);
		if (response.code() == 404) throw new TolerableFailureException();
		if (response.code() != 200) throw new ApiException();

		ResponseBody body = response.body();
		if (body == null) throw new ApiException();
		try {
			JsonNode node = mapper.readTree(body.string());
			ArrayNode filesNode = getArray(node, "files");
			List<MailboxFile> list = new ArrayList<>();
			for (JsonNode fileNode : filesNode) {
				if (!fileNode.isObject()) throw new ApiException();
				ObjectNode objectNode = (ObjectNode) fileNode;
				JsonNode nameNode = objectNode.get("name");
				JsonNode timeNode = objectNode.get("time");
				if (nameNode == null || !nameNode.isTextual()) {
					throw new ApiException();
				}
				if (timeNode == null || !timeNode.isNumber()) {
					throw new ApiException();
				}
				String name = nameNode.asText();
				long time = timeNode.asLong();
				if (time < 1) throw new ApiException();
				list.add(new MailboxFile(MailboxFileId.fromString(name), time));
			}
			sort(list);
			return list;
		} catch (JacksonException | InvalidMailboxIdException e) {
			throw new ApiException();
		}
	}

	@Override
	public void getFile(MailboxProperties properties, MailboxFolderId folderId,
			MailboxFileId fileId, File file)
			throws IOException, ApiException, TolerableFailureException {
		String path = "/files/" + folderId + "/" + fileId;
		Response response = sendGetRequest(properties, path);
		if (response.code() == 404) throw new TolerableFailureException();
		if (response.code() != 200) throw new ApiException();

		ResponseBody body = response.body();
		if (body == null) throw new ApiException();
		FileOutputStream outputStream = new FileOutputStream(file);
		copyAndClose(body.byteStream(), outputStream);
	}

	@Override
	public void deleteFile(MailboxProperties properties,
			MailboxFolderId folderId, MailboxFileId fileId)
			throws IOException, ApiException, TolerableFailureException {
		String path = "/files/" + folderId + "/" + fileId;
		Request request = getRequestBuilder(properties.getAuthToken())
				.delete()
				.url(getBaseUrl(properties) + path)
				.build();
		OkHttpClient client = httpClientProvider.get();
		Response response = client.newCall(request).execute();
		if (response.code() == 404) throw new TolerableFailureException();
		if (response.code() != 200) throw new ApiException();
	}

	@Override
	public List<MailboxFolderId> getFolders(MailboxProperties properties)
			throws IOException, ApiException {
		if (!properties.isOwner()) throw new IllegalArgumentException();
		Response response = sendGetRequest(properties, "/folders");
		if (response.code() != 200) throw new ApiException();

		ResponseBody body = response.body();
		if (body == null) throw new ApiException();
		try {
			JsonNode node = mapper.readTree(body.string());
			ArrayNode filesNode = getArray(node, "folders");
			List<MailboxFolderId> list = new ArrayList<>();
			for (JsonNode fileNode : filesNode) {
				if (!fileNode.isObject()) throw new ApiException();
				ObjectNode objectNode = (ObjectNode) fileNode;
				JsonNode idNode = objectNode.get("id");
				if (idNode == null || !idNode.isTextual()) {
					throw new ApiException();
				}
				String id = idNode.asText();
				list.add(MailboxFolderId.fromString(id));
			}
			return list;
		} catch (JacksonException | InvalidMailboxIdException e) {
			throw new ApiException();
		}
	}

	/* Helper Functions */

	private Response sendGetRequest(MailboxProperties properties, String path)
			throws IOException {
		Request request = getRequestBuilder(properties.getAuthToken())
				.url(getBaseUrl(properties) + path)
				.build();
		OkHttpClient client = httpClientProvider.get();
		return client.newCall(request).execute();
	}

	private Response sendPostRequest(MailboxProperties properties, String path,
			RequestBody body) throws IOException {
		Request request = getRequestBuilder(properties.getAuthToken())
				.url(getBaseUrl(properties) + path)
				.post(body)
				.build();
		OkHttpClient client = httpClientProvider.get();
		return client.newCall(request).execute();
	}

	private Request.Builder getRequestBuilder(MailboxId token) {
		return new Request.Builder()
				.addHeader("Authorization", "Bearer " + token);
	}

	/* JSON helpers */

	private ArrayNode getArray(JsonNode node, String name) throws ApiException {
		JsonNode arrayNode = node.get(name);
		if (arrayNode == null || !arrayNode.isArray()) {
			throw new ApiException();
		}
		return (ArrayNode) arrayNode;
	}

	private String getBaseUrl(MailboxProperties properties) {
		return urlConverter.convertOnionToBaseUrl(properties.getOnion());
	}
}

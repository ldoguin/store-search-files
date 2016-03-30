package org.couchbase.devex.domain;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

public class StoredFileDocument {

	public static String BINARY_STORE_DIGEST_PROPERTY = "binaryStoreDigest";

	public static String BINARY_STORE_LOCATION_PROPERTY = "binaryStoreLocation";

	public static String BINARY_STORE_FILENAME_PROPERTY = "binaryStoreFilename";

	public static String BINARY_STORE_METADATA_SIZE_PROPERTY = "FileSize";

	public static String BINARY_STORE_METADATA_MIMETYPE_PROPERTY = "MIMEType";

	public static String BINARY_STORE_METADATA_FULLTEXT_PROPERTY = "fulltext";

	public static String COUCHBASE_STORED_FILE_DOCUMENT_TYPE = "file";

	private JsonDocument jsonDocument;

	private JsonObject content;

	public StoredFileDocument(JsonDocument jsonDocument) {
		this.jsonDocument = jsonDocument;
		this.content = jsonDocument.content();
	}

	public String getMimeType() {
		String mimeType = content.getString(BINARY_STORE_METADATA_MIMETYPE_PROPERTY);
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		return mimeType;
	}

	public Integer getSize() {
		return content.getInt(BINARY_STORE_METADATA_SIZE_PROPERTY);
	}

	public String getBinaryStoreLocation() {
		return content.getString(BINARY_STORE_LOCATION_PROPERTY);
	}

	public String getBinaryStoreFilename() {
		return content.getString(BINARY_STORE_FILENAME_PROPERTY);
	}

	public String getBinaryStoreDigest() {
		return content.getString(BINARY_STORE_DIGEST_PROPERTY);
	}

	public String getType() {
		return COUCHBASE_STORED_FILE_DOCUMENT_TYPE;
	}

	public JsonDocument getJsonDocument() {
		return jsonDocument;
	}
}

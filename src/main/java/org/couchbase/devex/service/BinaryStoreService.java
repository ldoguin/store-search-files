package org.couchbase.devex.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.couchbase.devex.BinaryStoreConfiguration;
import org.couchbase.devex.domain.StoredFile;
import org.couchbase.devex.domain.StoredFileDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

@Service
public class BinaryStoreService {

	public static String MIME_TYPE_PDF = "application/pdf";

	private BinaryStoreConfiguration configuration;

	private Bucket bucket;

	private DataExtractionService dataExtractionService;

	private SHA1Service sha1Service;

	public BinaryStoreService(BinaryStoreConfiguration configuration, Bucket bucket,
			DataExtractionService dataExtractionService, SHA1Service sha1Service) {
		this.configuration = configuration;
		this.bucket = bucket;
		this.dataExtractionService = dataExtractionService;
		this.sha1Service = sha1Service;
	}

	public StoredFile findFile(String digest) {
		File f = new File(configuration.getBinaryStoreRoot() + File.separator + digest);
		if (!f.exists()) {
			return null;
		}
		JsonDocument doc = bucket.get(digest);
		if (doc == null)
			return null;
		StoredFileDocument fileDoc = new StoredFileDocument(doc);
		return new StoredFile(f, fileDoc);
	}

	public void deleteFile(String digest) {
		File f = new File(configuration.getBinaryStoreRoot() + File.separator + digest);
		if (!f.exists()) {
			throw new IllegalArgumentException("Can't delete file that does not exist");
		}
		f.delete();
		bucket.remove(digest);
	}

	public void storeFile(String name, MultipartFile uploadedFile) {
		if (!uploadedFile.isEmpty()) {
			try {
				String digest = sha1Service.getSha1Digest(uploadedFile.getInputStream());
				File file2 = new File(configuration.getBinaryStoreRoot() + File.separator + digest);
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file2));
				FileCopyUtils.copy(uploadedFile.getInputStream(), stream);
				stream.close();
				JsonObject metadata = dataExtractionService.extractMetadata(file2);
				metadata.put(StoredFileDocument.BINARY_STORE_DIGEST_PROPERTY, digest);
				metadata.put("type", StoredFileDocument.COUCHBASE_STORED_FILE_DOCUMENT_TYPE);
				metadata.put(StoredFileDocument.BINARY_STORE_LOCATION_PROPERTY, name);
				metadata.put(StoredFileDocument.BINARY_STORE_FILENAME_PROPERTY, uploadedFile.getOriginalFilename());
				String mimeType = metadata.getString(StoredFileDocument.BINARY_STORE_METADATA_MIMETYPE_PROPERTY);
				if (MIME_TYPE_PDF.equals(mimeType)) {
					String fulltextContent = dataExtractionService.extractText(file2);
					metadata.put(StoredFileDocument.BINARY_STORE_METADATA_FULLTEXT_PROPERTY, fulltextContent);
				}
				JsonDocument doc = JsonDocument.create(digest, metadata);
				bucket.upsert(doc);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new IllegalArgumentException("File empty");
		}
	}

}

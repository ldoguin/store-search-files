package org.couchbase.devex.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class SHA1Service {

	public String getSha1Digest(InputStream is) {
		try {
			return DigestUtils.sha1Hex(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

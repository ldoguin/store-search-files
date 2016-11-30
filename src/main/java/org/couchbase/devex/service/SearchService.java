package org.couchbase.devex.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.queries.TermQuery;
import com.couchbase.client.java.search.result.SearchQueryResult;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.consistency.ScanConsistency;

@Service
public class SearchService {

	private Bucket bucket;

	public SearchService(Bucket bucket) {
		this.bucket = bucket;
	}

	public List<Map<String, Object>> getFiles() {
		N1qlQuery query = N1qlQuery
				.simple("SELECT binaryStoreLocation, binaryStoreDigest FROM `default` WHERE type= 'file'");
		query.params().consistency(ScanConsistency.STATEMENT_PLUS);
		N1qlQueryResult res = bucket.query(query);
		List<Map<String, Object>> filenames = res.allRows().stream().map(row -> row.value().toMap())
				.collect(Collectors.toList());
		return filenames;
	}

	public List<Map<String, Object>> searchN1QLFiles(String whereClause) {
		N1qlQuery query = N1qlQuery.simple(
				"SELECT binaryStoreLocation, binaryStoreDigest FROM `default` WHERE type= 'file' " + whereClause);
		query.params().consistency(ScanConsistency.STATEMENT_PLUS);
		N1qlQueryResult res = bucket.query(query);
		List<Map<String, Object>> filenames = res.allRows().stream().map(row -> row.value().toMap())
				.collect(Collectors.toList());
		return filenames;
	}

	public List<Map<String, Object>> searchFulltextFiles(String term) {
		SearchQuery ftq = new SearchQuery("file_fulltext", SearchQuery
				.term(term));
				ftq.fields("binaryStoreDigest", "binaryStoreLocation");
		SearchQueryResult result = bucket.query(ftq);
		List<Map<String, Object>> filenames = result.hits().stream().map(row -> {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("binaryStoreDigest", row.fields().get("binaryStoreDigest"));
			m.put("binaryStoreLocation", row.fields().get("binaryStoreLocation"));
			m.put("fragment", row.fragments().get("fulltext"));
			return m;
		}).collect(Collectors.toList());
		return filenames;
	}

}

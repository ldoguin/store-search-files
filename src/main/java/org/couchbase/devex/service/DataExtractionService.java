package org.couchbase.devex.service;

import java.io.File;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

@Service
public class DataExtractionService {

	private final Logger log = LoggerFactory.getLogger(DataExtractionService.class);

	public JsonObject extractMetadata(File file) {
		String command = "/usr/local/bin/exiftool";
		String[] arguments = { "-json", "-n", file.getAbsolutePath() };
		Commandline commandline = new Commandline();
		commandline.setExecutable(command);
		commandline.addArguments(arguments);

		CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
		CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();

		try {
			CommandLineUtils.executeCommandLine(commandline, out, err);
		} catch (CommandLineException e) {
			throw new RuntimeException(e);
		}

		String output = out.getOutput();
		if (!output.isEmpty()) {
			JsonArray arr = JsonArray.fromJson(output);
			return arr.getObject(0);
		}

		String error = err.getOutput();
		if (!error.isEmpty()) {
			log.error(error);
		}
		return null;
	}

	public String extractText(File file) {
		String command = "/usr/local/bin/pdftotext";
		String[] arguments = { "-raw", file.getAbsolutePath(), "-" };
		Commandline commandline = new Commandline();
		commandline.setExecutable(command);
		commandline.addArguments(arguments);

		CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
		CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();

		try {
			CommandLineUtils.executeCommandLine(commandline, out, err);
		} catch (CommandLineException e) {
			throw new RuntimeException(e);
		}

		String output = out.getOutput();
		if (!output.isEmpty()) {
			return output;
		}

		String error = err.getOutput();
		if (!error.isEmpty()) {
			log.error(error);
		}
		return null;
	}

}

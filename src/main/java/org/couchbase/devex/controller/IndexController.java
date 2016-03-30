package org.couchbase.devex.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.couchbase.devex.domain.StoredFile;
import org.couchbase.devex.service.BinaryStoreService;
import org.couchbase.devex.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

	@Autowired
	private SearchService searchService;

	@Autowired
	private BinaryStoreService binaryStoreService;

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	public String provideUploadInfo(Model model) {
		List<Map<String, Object>> files = searchService.getFiles();
		model.addAttribute("files", files);
		return "uploadForm";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/download/{digest}")
	public String download(@PathVariable String digest, RedirectAttributes redirectAttributes,
			HttpServletResponse response) throws IOException {
		StoredFile sf = binaryStoreService.findFile(digest);
		if (sf == null) {
			redirectAttributes.addFlashAttribute("message", "This file does not exist.");
			return "redirect:/files";
		}
		response.setContentType(sf.getStoredFileDocument().getMimeType());
		response.setHeader("Content-Disposition",
				String.format("inline; filename=\"" + sf.getStoredFileDocument().getBinaryStoreFilename() + "\""));
		response.setContentLength(sf.getStoredFileDocument().getSize());

		InputStream inputStream = new BufferedInputStream(new FileInputStream(sf.getFile()));
		FileCopyUtils.copy(inputStream, response.getOutputStream());
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/delete/{digest}")
	public String delete(@PathVariable String digest, RedirectAttributes redirectAttributes,
			HttpServletResponse response) {
		binaryStoreService.deleteFile(digest);
		redirectAttributes.addFlashAttribute("message", "File deleted successfuly.");
		return "redirect:/files";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/fulltext")
	public String fulltextQuery(@ModelAttribute(value = "name") String query, Model model) throws IOException {
		List<Map<String, Object>> files = searchService.searchFulltextFiles(query);
		model.addAttribute("files", files);
		return "uploadForm";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/n1ql")
	public String n1qlQuery(@ModelAttribute(value = "name") String query, Model model) throws IOException {
		List<Map<String, Object>> files = searchService.searchN1QLFiles(query);
		model.addAttribute("files", files);
		return "uploadForm";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upload")
	public String handleFileUpload(@RequestParam("name") String name, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		if (name.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Name can't be empty!");
			return "redirect:/files";
		}
		binaryStoreService.storeFile(name, file);
		redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + name + "!");
		return "redirect:/files";
	}

}

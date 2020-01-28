package com.harokad.goona.crawler.bridge;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.springframework.stereotype.Controller;

import com.harokad.goona.domain.EdmCategory;
import com.harokad.goona.domain.EdmDocumentFile;
import com.harokad.goona.domain.EdmSource;
import com.harokad.goona.service.EdmCategoryService;
import com.harokad.goona.service.EdmCrawlingService;
import com.harokad.goona.service.EdmDocumentService;
import com.harokad.goona.service.EdmSourceService;

@Controller
public class EdmConnector {

	@Inject
	private EdmCategoryService edmCategoryService;

	@Inject
	private EdmSourceService edmSourceService;

	@Inject
	private EdmCrawlingService edmCrawlingService;
	
    @Inject
    private EdmDocumentService edmDocumentService;

	public void saveEdmDocument(EdmDocumentFile doc, File file) throws IOException {
		edmCrawlingService.save(doc);
	}

	public void deleteEdmDocument(String fileName) throws IOException {
		List<EdmDocumentFile> docs = edmDocumentService.findByName(fileName);
		for(EdmDocumentFile doc : docs){
			edmDocumentService.delete(doc);
		}
		
	}
	
	public void notifyStartCrawling(String source) throws ClientProtocolException, IOException {
		    edmCrawlingService.snapshotCurrentDocumentsForSource(source);
	}

	public void notifyEndOfCrawling(String source) throws ClientProtocolException, IOException {
		 edmCrawlingService.deleteUnusedDocumentsBeforeSnapshotForSource(source);
	}

	public String getIdFromSourceBySourceName(String sourceName, String categoryId) {
		// get Node
		edmSourceService.findOneByName(sourceName);
		EdmSource result = edmSourceService.findOneByName(sourceName);

		// if exits, nothing to do !
		if (result.getId() != null && !result.getId().isEmpty()) {
			return result.getId();
		}

		// else, we have to create it
		EdmSource directory = new EdmSource();
		directory.setDescription("");
		directory.setName(sourceName);
		EdmSource directoryCreated = edmSourceService.save(directory);
		return directoryCreated.getId();
	}

	public String getIdFromCategoryByCategoryName(String categoryName) {
		// get Node
		EdmCategory result = edmCategoryService.findOneByName(categoryName);

		// if exits, nothing to do !
		if (result.getId() != null && !result.getId().isEmpty()) {
			return result.getId();
		}

		// else, we have to create it
		EdmCategory category = new EdmCategory();
		category.setDescription("");
		category.setName(categoryName);
		EdmCategory categoryC = edmCategoryService.save(category);
		return categoryC.getId();
	}
}

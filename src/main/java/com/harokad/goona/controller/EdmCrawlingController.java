package com.harokad.goona.controller;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.harokad.goona.GoonaTiraApp;
import com.harokad.goona.crawler.filesystem.FilesystemCrawler;
import com.harokad.goona.crawler.url.UrlCrawler;
import com.harokad.goona.domain.EdmDocumentFile;
import com.harokad.goona.service.EdmCrawlingService;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("api/crawl")
@Slf4j
public class EdmCrawlingController {

    @Inject 
    private EdmCrawlingService edmCrawlingService;

    @Inject 
    private FilesystemCrawler filesystemCrawler;
    
    @RequestMapping(value = "/start", method = RequestMethod.GET, params = {"source"})
    @ResponseStatus(value=HttpStatus.OK)
    public void startCrawling(@RequestParam(value = "source") String source) {
        log.info("Begin crawling for source : {}", source);
        edmCrawlingService.snapshotCurrentDocumentsForSource(source);
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET, params = {"source"})
    @ResponseStatus(value=HttpStatus.OK)
    public void stopCrawling(@RequestParam(value = "source") String source) {
        log.info("End of crawling for source : {}", source);
        edmCrawlingService.deleteUnusedDocumentsBeforeSnapshotForSource(source);
    }

    @RequestMapping(value = "/filesystem", method = RequestMethod.GET, params = {"path"})
    @ResponseBody
    public Callable<ResponseEntity<?>> crawlFilesystem(
            @RequestParam(value = "path") String path,
            @RequestParam(value = "edmServerHttpAddress", defaultValue = "http://127.0.0.1:8080") String edmServerHttpAddress,
            @RequestParam(value = "sourceName", defaultValue = "") String sourceName,
            @RequestParam(value = "categoryName", defaultValue = "") String categoryName,
            @RequestParam(value = "exclusionRegex", defaultValue = "") String exclusionRegex
       ) {
  
        log.info("[crawlFilesystem] Starting crawling on path : '{}'  (exclusion = '{}')", path, exclusionRegex);
        String rootDir = System.getenv(GoonaTiraApp.GOONA_TIRA);
        if(rootDir != null && path.compareTo(GoonaTiraApp.GOONA_TIRA) == 0) {
            File file = new File(rootDir);
            // recursive crawling
            if (file != null && file.isDirectory()) {
                log.debug("... is a directory !");
                for (File subFile : file.listFiles()) {
                	if(subFile.isDirectory()) {
                        try {
                            filesystemCrawler.importFilesInDir(subFile.getAbsolutePath(), edmServerHttpAddress, sourceName, subFile.getName(), exclusionRegex);
                        } catch (IOException e) {
                            log.error("[crawlFilesystem] Failed to crawl '{}' with embedded crawler", subFile.getPath(), e);
                        }
                	}
                 }
                // release memory
                file = null;
            }
        } else
	        try {
	            filesystemCrawler.importFilesInDir(path, edmServerHttpAddress, sourceName, categoryName, exclusionRegex);
	        } catch (IOException e) {
	            log.error("[crawlFilesystem] Failed to crawl '{}' with embedded crawler", path, e);
	        }

        return () -> ResponseEntity.ok("{message:OK}");
    }
    
    @RequestMapping(value = "/stopfs", method = RequestMethod.GET, params = {"path"})
    @ResponseBody
    public String stopCrawlFilesystem(
            @RequestParam(value = "path") String path) {
        log.info("[crawlFilesystem] Stoping crawling on path : '{}'", path);
        filesystemCrawler.stopCrawler(path);
        return "{message:OK}";
    }

    @RequestMapping(value = "/startfs", method = RequestMethod.GET, params = {"path"})
    @ResponseBody
    public String startCrawlFilesystem(
            @RequestParam(value = "path") String path) {
        log.info("[crawlFilesystem] Starting crawling on path : '{}'", path);
        filesystemCrawler.startCrawler(path);
        return "{message:OK}";
    }
    
    @RequestMapping(value = "/url", method = RequestMethod.GET, params = {"url"})
    @ResponseBody
    public String crawlUrl(
            @RequestParam(value = "url") String url,
            @RequestParam(value = "edmServerHttpAddress", defaultValue = "http://127.0.0.1:8080") String edmServerHttpAddress,
            @RequestParam(value = "sourceName", defaultValue = "unmanned source") String sourceName,
            @RequestParam(value = "categoryName", defaultValue = "unmanned category") String categoryName,
            @RequestParam(value = "exclusionRegex", defaultValue = "") String exclusionRegex
       ) {
        log.info("[crawlUrl] Starting crawling on path : '{}'  (exclusion = '{}')", url, exclusionRegex);
        try {
            UrlCrawler.importFilesAtUrl(url, sourceName, categoryName, exclusionRegex);
        } catch (IOException e) {
            log.error("[crawlUrl] Failed to crawl '{}' with embedded crawler", url, e);
        }

        return "{message:OK}";
    }
    
    @RequestMapping(method=RequestMethod.POST, value="/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EdmDocumentFile create(@RequestBody EdmDocumentFile edmDocument) {
        return edmCrawlingService.save(edmDocument);
    }

}

package com.harokad.goona.crawler.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;

import com.harokad.fileSystemObserver.FileSystemManager;
import com.harokad.fileSystemObserver.FileSystemObservable;
import com.harokad.fileSystemObserver.FileSystemObserver;
import com.harokad.goona.crawler.bridge.EdmConnector;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class FilesystemCrawler {


	
	@Inject
    private EdmConnector edmConnector;
	
    /**
     * 
     * @param filePath
     *            The path of the directory to crawl For example :
     *            /media/raid/documents
     * @param edmServerHttpAddress
     *            The address of the EDM webapp HTTP address For example :
     *            127.0.0.1:8053
     * @param sourceName
     *            A unique name for this source of documents For example :
     * @param exclusionRegex
     *            Documents names which match with this regex will be ignored
     * 
     * @throws IOException
     */
    public void importFilesInDir(String filePath,
            final String edmServerHttpAddress, final String pSourceName,
            final String pCategoryName, final String exclusionRegex)
            throws IOException {
    	String sourceName = pSourceName;
    	String categoryName = pCategoryName;
        // create parents
     	File importDirfile = new File(filePath);
    	if(StringUtils.isEmpty(sourceName)){
    		sourceName = importDirfile.getName();
    	}
       if(StringUtils.isEmpty(categoryName)){
    	   categoryName = importDirfile.getName();
    	}
        String categoryId = edmConnector.getIdFromCategoryByCategoryName( categoryName);
        String sourceId = edmConnector.getIdFromSourceBySourceName(sourceName, categoryId);

        // index
        log.debug("The source ID is {}", sourceId);
        edmConnector.notifyStartCrawling(sourceName);

        FileSystemObserver indexerObs = new FSIndexingObserver(edmConnector, sourceId, categoryId);
        _indexFilesInDir(filePath, exclusionRegex, indexerObs);
        FileSystemManager.watch(filePath, indexerObs);
        edmConnector.notifyEndOfCrawling( sourceName);
    }

    public static boolean isExcluded(String filePath, String exclusionPattern) {
        boolean toExclude = !exclusionPattern.isEmpty()
                && Pattern.compile(exclusionPattern).matcher(filePath).find();
        log.debug("Check if '{}' match with '{}' : {}", filePath,
                exclusionPattern, toExclude);
        return toExclude;
    }

    private  void _indexFilesInDir(String filePath, final String exclusionRegex, FileSystemObserver indexerObs) {

        log.info("Embedded crawler looks for : " + filePath);

        // exclusion pattern
        if (isExcluded(filePath, exclusionRegex)) {
            log.info("File excluded because it matches with exclusion regex");
            return;
        }

        File file = new File(filePath);

        // recursive crawling
        if (file != null && file.isDirectory()) {
            log.debug("... is a directory !");
            for (File subFile : file.listFiles()) {
                _indexFilesInDir(filePath + "/" + subFile.getName(), exclusionRegex, indexerObs);
            }

            // release memory
            file = null;
        }

        // add files
        if (file != null && file.isFile()) {
        	indexerObs.update(null, file);
            // release memory
            file = null;
        }

        // other type
        if (file != null) {
            log.debug("... is nothing !");

            // release memory
            file = null;
        }
    }


	public void startCrawler(String path) {
		FileSystemObservable fsObservable = FileSystemManager.getObservables().get(path);
		if (fsObservable != null){
			fsObservable.start();
		}
	}
	
	public void stopCrawler(String path) {
		FileSystemObservable fsObservable = FileSystemManager.getObservables().get(path);
		if (fsObservable != null){
			fsObservable.stop();
		}
	}
	
}

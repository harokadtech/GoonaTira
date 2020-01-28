package com.harokad.goona.crawler.filesystem;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.RecursiveParserWrapper;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.harokad.fileSystemObserver.FileSystemEvent;
import com.harokad.fileSystemObserver.FileSystemObserver;
import com.harokad.goona.crawler.bridge.EdmConnector;
import com.harokad.goona.domain.EdmDocumentFile;
import com.harokad.goona.service.ocr.TikaInstance;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FSIndexingObserver extends FileSystemObserver {

    private Logger logger = LoggerFactory.getLogger(FSIndexingObserver.class);
    
    final Map<String, Kind<Path>> eventsMaps = new ConcurrentHashMap<>();
 
    private final EdmConnector edmConnector;
    private final String sourceId;
    private final String categoryId;

	public FSIndexingObserver(EdmConnector edmConnector, String sourceId,
			String categoryId) {
		super();
		this.edmConnector = edmConnector;
		this.sourceId = sourceId;
		this.categoryId = categoryId;
	}

	@Override
    public void update(Observable o, Object arg) {
		if (arg instanceof FileSystemEvent){
	    	FileSystemEvent event = (FileSystemEvent) arg;
	        String absolutePath = event.getPath().toFile().getAbsolutePath();
	        addEvent(event.getType(), absolutePath);
		} else if (arg instanceof File){
	        String absolutePath = ((File)arg).getAbsolutePath();
	        addEvent(StandardWatchEventKinds.ENTRY_CREATE, absolutePath);
		} else if (arg instanceof Long){
			try {
				flush();
			} catch (IOException e) {
				 logger.error("Error while flushing ", e);
			}
		}

    }

    private void addEvent(Kind<Path> type, String absolutePath) {
    	
		if(type == StandardWatchEventKinds.ENTRY_MODIFY) {
        	Kind<Path> oldtype = eventsMaps.get(absolutePath);
        	if(oldtype == StandardWatchEventKinds.ENTRY_CREATE){
        		// ignore CREATE + MODIFY = 
        	} else {
                logger.info("ENTRY_MODIFY " + absolutePath);
                eventsMaps.put(absolutePath, type);
        	}
        } else {
            logger.info(type + "  " + absolutePath);
            eventsMaps.put(absolutePath, type);
        }
		
	}
    
    
	public void flush() throws IOException {
		
		for (Map.Entry<String, Kind<Path>> entry : eventsMaps.entrySet() ){
			Kind<Path> type = entry.getValue();
			if(type == StandardWatchEventKinds.ENTRY_CREATE) {
				indexFileDocument(new File(entry.getKey()));
	        } else if(type == StandardWatchEventKinds.ENTRY_MODIFY) {
	        	indexFileDocument(new File(entry.getKey()));
	        } else if(type == StandardWatchEventKinds.ENTRY_DELETE) {
	        	 edmConnector.deleteEdmDocument(entry.getKey());    
	        }	
		}
		eventsMaps.clear();
    }
	
    public static final int MAX_FILE_SIZE_MBYTES = 50;
    
	private void indexFileDocument(File file) {
		log.debug("Indexing file -- " + file.getName());

		double bytes = file.length();
		double kilobytes = bytes / 1024;
		double megabytes = kilobytes / 1024;
		String filePath =  file.getAbsolutePath();
		if (megabytes > MAX_FILE_SIZE_MBYTES) {
		    log.warn("Skipping too big file ({})", filePath);
		} else {
		    // construct DTO
		    EdmDocumentFile document = new EdmDocumentFile();
		    document.setDate(new Date(file.lastModified()));
		    document.setNodePath(filePath.replaceAll("\\\\", "/"));
		    document.setSourceId(sourceId);
		    document.setCategoryId(categoryId);
		    document.setName(FilenameUtils.removeExtension(file.getName()));
		    document.setDescription(file.getName() + " indexed from folder "  + file.getParent());
		    document.setFileExtension(FilenameUtils.getExtension(filePath).toLowerCase());
 
		 // Extracting content with Tika
		 Metadata metadata = new Metadata();
		 String parsedContent;
		 try {
		 	InputStream fileInputStream = new FileInputStream(file);
		 	String mimeType = document.getFileExtension();
			if (mimeType!=null && (mimeType.contains("pdf") || mimeType.contains("png") || mimeType.contains("jpg")|| mimeType.contains("jpeg"))){
				parsedContent = tessaractOcrFile(fileInputStream, metadata);
				log.debug("-------- Tessaract OCR : for file {} ----------------\n", document.getNodePath());
			} else {
		     	// Set the maximum length of strings returned by the parseToString method, -1 sets no limit
		        parsedContent = TikaInstance.tika().parseToString(fileInputStream, metadata, MAX_EXTRACTED_OCR_TEXT);
		        log.debug("-------- Normal Tika extract : for file {} -------- \n", document.getNodePath());
			}
			document.setSearchText(parsedContent);
			
		 } catch (Throwable e) {
		 	log.debug("Failed to extract characters of text for [" + file.getName() + "]", e);
		     parsedContent = "";
		 }
		// throw new ClientException("InvalidFileType", "File type not recognized");
		    // save DTO
		    try {
		        //document.setFileContentType(Files.probeContentType(file.toPath()));
		        document.setFileContentType(metadata.get(Metadata.CONTENT_TYPE));
		       /* byte[] readAllBytes = Files.readAllBytes(file.toPath());
				document.setFileContent(readAllBytes);*/
		        edmConnector.saveEdmDocument(document, file);
		    }
		    catch (IOException e) {
		        log.error("failed to save edm docuement '{}'", filePath, e);
		    }
		}
	}
	
	public static final int MAX_EXTRACTED_OCR_TEXT = 10000;
	public static final int MIN_EXTRACTED_OCR_TEXT = 100;
	private String tessaractOcrFile(InputStream  inputStream, Metadata metadata) throws IOException, SAXException, TikaException {
	   	String suffix = "full";
    	//suffix = "default";
    	InputStream tessCfg = this.getClass().getResourceAsStream("/tessaract-settings/TesseractOCRConfig-"+suffix+".properties");
    	TesseractOCRConfig config = new TesseractOCRConfig(tessCfg);
        Parser parser = new RecursiveParserWrapper(new AutoDetectParser(),
                new BasicContentHandlerFactory(
                        BasicContentHandlerFactory.HANDLER_TYPE.TEXT, MAX_EXTRACTED_OCR_TEXT));

        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(true);

        ParseContext parseContext = new ParseContext();
        parseContext.set(TesseractOCRConfig.class, config);
        parseContext.set(Parser.class, parser);
        parseContext.set(PDFParserConfig.class, pdfConfig);

        DefaultHandler handler = new DefaultHandler();
        parser.parse(inputStream, handler, metadata, parseContext);

        List<Metadata> metadataList = ((RecursiveParserWrapper) parser).getMetadata();
    
        StringBuilder contents = new StringBuilder();
        for (Metadata m : metadataList) {
        	 String str = m.get(RecursiveParserWrapper.TIKA_CONTENT);
        	 if(str ==null) {
        		 continue;
        	 }
            str = str.replaceAll("\n+", "\n");
			if(str.length()>MIN_EXTRACTED_OCR_TEXT){
	            contents.append(str);	
			}
        }
        String extractedText = contents.toString();
       return extractedText;
	}

	
}

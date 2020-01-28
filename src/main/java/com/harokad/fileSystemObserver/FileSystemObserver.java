package com.harokad.fileSystemObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Snakepit on 19/10/2015.
 */
public class FileSystemObserver implements Observer {

    private Logger logger = LoggerFactory.getLogger(FileSystemObserver.class);
    final Map<String, StandardWatchEventKinds> eventsMaps = new ConcurrentHashMap<>();
    
    @Override
    public void update(Observable o, Object arg) {
    	FileSystemEvent event = (FileSystemEvent) arg;
        String absolutePath = event.getPath().toFile().getAbsolutePath();
        addEvent(event.getType(), absolutePath);
    }
    
    private void addEvent(Kind<Path> type, String absolutePath) {
		if(type == StandardWatchEventKinds.ENTRY_CREATE) {
            logger.info("ENTRY_CREATE " + absolutePath);
        } else if(type == StandardWatchEventKinds.ENTRY_MODIFY) {
            logger.info("ENTRY_MODIFY " + absolutePath);
        } else if(type == StandardWatchEventKinds.ENTRY_DELETE) {
            logger.info("ENTRY_DELETE " + absolutePath);
        }
		
	}

	public void flush() throws IOException {
    
    }
}

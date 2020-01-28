package com.harokad.fileSystemObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

/**
 * Created by Snakepit on 19/10/2015.
 */
public class FileSystemManager {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemManager.class);

    private static Map<String, FileSystemObservable> observables = new HashMap<String, FileSystemObservable>();

    public static Map<String, FileSystemObservable> getObservables() {
		return observables;
	}

	public static FileSystemObservable watch(String pathString, Observer observer) throws IOException {
        File file = new File(pathString);
        if(!file.exists()) { throw new IOException("file " + pathString + " does not exists"); }
        Path path = Paths.get(file.toURI());
        FileSystemObservable fso = observables.get(pathString);
        if(fso == null) {
            logger.info("Creating new observable");
            fso = new FileSystemObservable(path);
            fso.addObserver(observer);
            fso.start();
            observables.put(pathString, fso);
        } else {
            logger.info("add observer to existing observable");
            fso.addObserver(observer);
        }
		return fso;
    }
}

package com.harokad.fileSystemObserver;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;

public class FileSystemEvent {

	final WatchEvent.Kind<Path> type;
	final Path path;
	final boolean directory;


	public FileSystemEvent(WatchEvent<Path> event) {
		super();
		this.type = event.kind();
		this.path = event.context();
		this.directory = false;
	}

	public FileSystemEvent(WatchEvent<Path> event, boolean directory) {
		super();
		this.type = event.kind();
		this.path = event.context();
		this.directory = directory;
	}
	
	
	public FileSystemEvent(Kind<Path> type, Path path) {
		super();
		this.type = type;
		this.path = path;
		this.directory = false;
	}
	
	public WatchEvent.Kind<Path> getType() {
		return type;
	}
	public Path getPath() {
		return path;
	}
	

	public boolean isDirectory() {
		return directory;
	}
}

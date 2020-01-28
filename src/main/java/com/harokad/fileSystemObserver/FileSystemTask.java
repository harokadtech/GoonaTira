package com.harokad.fileSystemObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by Snakepit on 19/10/2015.
 */
public class FileSystemTask implements Runnable {

	private Logger logger = LoggerFactory.getLogger(FileSystemTask.class);
	private WatchService watcher;
	private Path path;
	private boolean stopWatch;
	private long lastFlushTime = 0L;
	private int scanRate = 5000;
	private Observable observables;
	final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();

	/**
	 * @param path
	 * @param observables
	 * @throws IOException
	 */

	public FileSystemTask(Path path, FileSystemObservable observables) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.path = path;
		this.path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		this.observables = observables;
		this.stopWatch = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		logger.info("**** START FileSystemTask *****");

		while (!stopWatch) {
			long currentTimeMillis = System.currentTimeMillis();
			if (currentTimeMillis - lastFlushTime > scanRate) {
				// flush
				observables.notifyObservers(currentTimeMillis);
			}

			WatchKey key = null;
			try {
				key = watcher.take(); // poll(10, TimeUnit.MILLISECONDS);
				Thread.sleep(scanRate);
			} catch (InterruptedException | ClosedWatchServiceException e) {
				stopWatch();
			}
			Path path = keys.get(key);
			boolean pathExists = Files.isReadable(path);
			for (WatchEvent<?> i : key.pollEvents()) {
				System.out.printf("------- LOOP ---------%n");
				WatchEvent<Path> event = (WatchEvent<Path>) i;
				WatchEvent.Kind<Path> kind = null;
				if (pathExists) {
					kind = event.kind();
				} else {
					kind = ENTRY_DELETE;
				}
				Path name = event.context();
				Path child = path.resolve(name);
				logger.info(kind.name() + " " + child);
				boolean isDirectory = Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS);
				if (isDirectory) {
					if (kind == ENTRY_CREATE) {
						try {
							walk(child, keys, watcher);
						} catch (IOException e) {
							stopWatch();
							logger.error("Error during walk", e);
						}
					} else {
						// ignore directory MODIFY
						observables.notifyObservers(new FileSystemEvent(event, isDirectory));
					}
				} else {
					observables.notifyObservers(new FileSystemEvent(event));
				}
			}
			if (key.reset() == false) {
				logger.info(key + " is invalid");
				keys.remove(key);
				if (keys.isEmpty()) {
					stopWatch();
				}
			}

		}
		// Force flush last time if needed
		observables.notifyObservers(System.currentTimeMillis());
		logger.info("**** END FileSystemTask *****");
	}

	public void stopWatch() {
		this.stopWatch = true;
	}

	public boolean isRunning() {
		return stopWatch == false;
	}

	void walk(Path root, final Map<WatchKey, Path> keys, final WatchService ws) throws IOException {
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerWatcher(dir, keys, ws);
				return super.preVisitDirectory(dir, attrs);
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				observables.notifyObservers(new FileSystemEvent(ENTRY_CREATE, file));
				logger.info(ENTRY_CREATE.name() + " " + file);
				return super.visitFile(file, attrs);
			}
		});
	}

	static void registerWatcher(Path dir, Map<WatchKey, Path> keys, WatchService ws) throws IOException {
		WatchKey key = dir.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);
	}
}

package com.harokad.fileSystemObserver;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FSWatcher {

	public static void main(String[] args) throws Exception {
		ExecutorService service = Executors.newCachedThreadPool();
		final FileSystem fs = FileSystems.getDefault();
		final WatchService ws = fs.newWatchService();
		final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();

		registerWatcher(fs.getPath("D:/devWin/searchdocuments/VisaDocs"), keys, ws);
		registerWatcher(fs.getPath("D:/devWin/searchdocuments/Dev4Africa"), keys, ws);

		service.submit(new Runnable() {
			@Override
			public void run() {
				System.out.println("START");
				while (Thread.interrupted() == false) {
					WatchKey key;
					try {
						key = ws.take(); // poll(10, TimeUnit.MILLISECONDS);
						Thread.sleep(2000);
					} catch (InterruptedException | ClosedWatchServiceException e) {
						break;
					}
					Path path = keys.get(key);
					boolean pathExists = Files.isReadable(path);
					for (WatchEvent<?> i : key.pollEvents()) {
						System.out.printf("------- LOOP ---------%n");
						@SuppressWarnings("unchecked")
						WatchEvent<Path> event =(WatchEvent<Path>) i;
						WatchEvent.Kind<Path> kind = null;
						if (pathExists) {
							kind = event.kind();
						} else {
							kind = ENTRY_DELETE;
						}
						Path name = event.context();
						Path child = path.resolve(name);
						System.out.printf("%s: %s%n", kind.name(), child);
						if (kind == ENTRY_CREATE) {
							if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
								try {
									walk(child, keys, ws);
								} catch (IOException e) {
									System.out.printf("%s is invalid %n", key);
									e.printStackTrace();
								}
							}
						}
					}
					if (key.reset() == false) {
						System.out.printf("%s is invalid %n", key);
						keys.remove(key);
						if (keys.isEmpty()) {
							break;
						}
					}
				}
				System.out.println("END");
			}
		});

		while (true) {
			Path p = fs.getPath("D:/devWin/searchdocuments/end");
			if (Files.isReadable(p)) {
				ws.close();
				service.shutdownNow();
				System.out.println("ENDING");
				break;
			}
		}
	}

	static void walk(Path root, final Map<WatchKey, Path> keys, final WatchService ws) throws IOException {
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerWatcher(dir, keys, ws);
				return super.preVisitDirectory(dir, attrs);
			}
			 @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			        throws IOException
			    {
				    System.out.printf("%s: walk : %s %n", ENTRY_CREATE.name(), file);
			        return super.visitFile(file, attrs);
			    }
		});
	}

	static void registerWatcher(Path dir, Map<WatchKey, Path> keys, WatchService ws) throws IOException {
		WatchKey key = dir.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);
	}

}
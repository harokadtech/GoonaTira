package com.harokad.fileSystemObserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FileSystemObservable extends Observable {

    private FileSystemTask task;
    private ExecutorService executor;

    public FileSystemObservable(Path path) throws IOException {
        task = new FileSystemTask(path, this);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void notifyObservers(Object arg) {
        super.setChanged();
        super.notifyObservers(arg);
    }

    public void start() {
        executor.execute(task);
    }

    public void stop() {
        task.stopWatch();
        executor.shutdown();
    }
}

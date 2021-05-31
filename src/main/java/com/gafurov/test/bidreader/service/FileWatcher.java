package com.gafurov.test.bidreader.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class FileWatcher implements Runnable {
    private final FileListener listener;
    private final Path path;

    private final Logger logger = LoggerFactory.getLogger(FileChangesListener.class);

    public FileWatcher(Path path) {
        this.path = path;
        this.listener = new FileChangesListener();
    }

    public void watch() {
        if (path.toFile().exists()) {
            Thread thread = new Thread(this);
            thread.start();
        } else {
            logger.error("Invalid file path, please check it");
        }
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            boolean poll = true;
            while (poll) {
                poll = pollEvents(watchService);
            }
        } catch (IOException | InterruptedException | ClosedWatchServiceException e) {
            logger.error("File watcher initialisation failed");
            Thread.currentThread().interrupt();
        }
    }

    private boolean pollEvents(WatchService watchService) throws InterruptedException {
        WatchKey key = watchService.take();
        Path path = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents()) {
            notifyListeners(event.kind(), path.resolve((Path) event.context()));
        }
        return key.reset();
    }

    private void notifyListeners(WatchEvent.Kind<?> kind, Path path) {
        if (kind == ENTRY_MODIFY || kind == ENTRY_CREATE) {
            listener.onModified(path);
        }
    }
}

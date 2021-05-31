package com.gafurov.test.bidreader.service;

import java.nio.file.Path;

public class BidReaderService {
    public void consumeBids(Path path) {
        FileWatcher fileWatcher = new FileWatcher(path);
        fileWatcher.watch();
    }
}

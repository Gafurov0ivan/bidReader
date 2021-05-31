package com.gafurov.test.bidreader.service;

import java.nio.file.Path;
import java.util.EventListener;

public interface FileListener extends EventListener {
    void onModified(Path event);
}

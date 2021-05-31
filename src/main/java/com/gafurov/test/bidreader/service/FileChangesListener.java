package com.gafurov.test.bidreader.service;

import com.gafurov.test.bidreader.model.Bid;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class FileChangesListener implements FileListener {
    private AtomicLong processedCount;
    private final Map<String, Collection<Future<?>>> queues;
    private final ExecutorService executorService;
    private Base64.Decoder decoder = Base64.getDecoder();

    private final Logger logger = LoggerFactory.getLogger(FileChangesListener.class);

    public FileChangesListener() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.processedCount = new AtomicLong();
        this.queues = new HashMap<>();
    }

    @Override
    public void onModified(Path path) {
        StringBuilder builder = new StringBuilder();
        try (Stream<String> lines = Files.lines(path).skip(processedCount.get() > 0 ? processedCount.get() - 1 : 0)) {
            lines.forEach(line -> {
                builder.append(line);
                processedCount.getAndIncrement();
            });
        } catch (IOException e) {
            logger.error("Can't parse bid file, please check the file");
        }
        List<Bid> bids = readBidJson(builder.toString());
        for (Bid bid : bids) {
            final String message = String.format(
                    "Bid queued, id: %s, ts: %s, ty: %s, payload: %s",
                    bid.getId(),
                    bid.getTimestamp(),
                    bid.getType(),
                    decoder.decode(bid.getPayload())
            );
            if (queues.containsKey(bid.getType())) {
                queues.get(bid.getType()).add(executorService.submit(() -> logger.info(message)));
            } else {
                Collection<Future<?>> queue = new LinkedList<>();
                queue.add(executorService.submit(() -> logger.info(message)));
                queues.put(bid.getType(), queue);
            }
        }
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Some of the threads were interrupted");
        }
    }

    private List<Bid> readBidJson(String json) {
        String resolvedBracketsJson = "[" + json.replaceFirst("\\[", "");
        List<Bid> bids = new LinkedList<>();
        if (json.isEmpty()) {
            return bids;
        }

        JSONArray arr = new JSONArray(resolvedBracketsJson);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject bid = (JSONObject) ((JSONObject) arr.get(i)).get("bid");
            bids.add(new Bid(
                    new BigDecimal(bid.getString("id")),
                    bid.getLong("ts"),
                    bid.getString("ty"),
                    bid.getString("pl")
            ));
        }
        return bids;
    }
}

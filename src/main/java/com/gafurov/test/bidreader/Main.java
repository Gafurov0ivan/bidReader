package com.gafurov.test.bidreader;

import com.gafurov.test.bidreader.service.BidReaderService;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final BidReaderService bidReaderService = new BidReaderService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please insert path for input file");
        Path filePath = Path.of(scanner.nextLine());
        bidReaderService.consumeBids(filePath);
    }
}

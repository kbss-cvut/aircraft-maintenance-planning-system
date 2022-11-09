package cz.cvut.kbss.textanalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws IOException {
        Extraction extraction = new Extraction();

        System.out.println("Starting extraction");
        if (args.length != 2) {
            System.err.println("Wrong number of arguments. Please provide input directory and output path.");
            exit(1);
        }

        String inputDirectory = args[0];
        String outputDirectory = args[1];
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) Files.createFile(outputPath);

        extraction.processDataset(inputDirectory, outputDirectory);
        System.out.println("Finished extraction");
    }
}

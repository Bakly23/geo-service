package ru.kolpakov.test.geoservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

public class GeoServiceDataGenerator {
    private static final Random rand = new Random();

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("There must be exactly two arguments: output path, average distance error and number of users.");
        }
        String outputPath = args[0];
        double avgDistanceError = Double.parseDouble(args[1]);
        int numberOfUsers = Integer.parseInt(args[2]);
        Files.write(Paths.get(outputPath, "geo_cells.csv"), IntStream.range(-180, 180)
                .boxed()
                .flatMap(i -> IntStream.range(-90, 90).mapToObj(j -> i + ";" + j + ";" + abs(rand.nextGaussian() + avgDistanceError)))
                .collect(Collectors.joining("\n"))
                .getBytes());

        Files.write(Paths.get(outputPath, "users.csv"), (Iterable<String>) IntStream.rangeClosed(1, numberOfUsers)
                .mapToObj(i -> i + ";" + (rand.nextInt(360) - 180) + ";" + (rand.nextInt(180) - 90))
                ::iterator);
    }
}

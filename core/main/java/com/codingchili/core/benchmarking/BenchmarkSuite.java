package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

import java.util.function.Consumer;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.storage.*;

/**
 * @author Robin Duda
 *         <p>
 *         Contains system benchmarks..
 */
public class BenchmarkSuite {

    public static void main(String[] args) {
        maps(new BenchmarkConsoleListerner()).setHandler(done -> {
            new BenchmarkHTMLReport(done.result()).saveTo("report-zz.html");
        });
    }

    /**
     * Runs all core map benchmarks.
     *
     * @return a future that is completed with the results of the benchmark.
     */
    public static Future<BenchmarkGroup> maps(BenchmarkListener listener) {
        BenchmarkGroup group = new AbstractBenchmarkGroup("Map benchmarks", 1000, 5);
        Future<BenchmarkGroup> future = Future.future();

        Consumer<Class> add = (clazz) -> group.add(
                new MapBenchmarkImplementation(group, clazz, clazz.getSimpleName()));

        add.accept(JsonMap.class);
        add.accept(PrivateMap.class);
        add.accept(SharedMap.class);
        add.accept(IndexedMap.class);

        new BenchmarkExecutor(future, group).setListener(listener);
        return future;
    }
}

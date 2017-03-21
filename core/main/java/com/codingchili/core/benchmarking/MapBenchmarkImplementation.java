package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.ID_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.Storable;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.core.testing.StorageObject;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         <p>
 *         Implementation of a map for use with benchmarking.
 */
public class MapBenchmarkImplementation implements BenchmarkImplementation {
    private static final String COLLECTION = "collection";
    private static final String DB = "db";
    private AtomicInteger counter = new AtomicInteger(0);
    private List<Benchmark> benchmarks = new ArrayList<>();
    private AsyncStorage<Storable> storage;
    private Vertx vertx;
    private Class plugin;
    private String group;
    private String implementation;

    public MapBenchmarkImplementation(Class plugin, String group, String implementation) {
        this.group = group;
        this.implementation = implementation;
        this.plugin = plugin;

        add(this::putOne, "put all")
                .add(this::getOne, "get all")
                .add(this::betweenQuery, "between query")
                .add(this::equalToQuery, "equal to query")
                .add(this::equalToPrimaryKey, "equal to primary key")
                .add(this::regexpQuery, "regular expression")
                .add(this::startsWithQuery, "starts with");
    }

    private MapBenchmarkImplementation add(BenchmarkOperation operation, String name) {
        this.add(new MapBenchmark(operation, group, implementation, name));
        return this;
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> handler) {
        System.out.println("initialize " + implementation);
        this.vertx = Vertx.vertx();
        new StorageLoader<>(new StorageContext<>(vertx))
                .withPlugin(plugin)
                .withClass(StorageObject.class)
                .withDB(DB, COLLECTION).build(store -> {
            this.storage = store.result();
            handler.handle(Future.succeededFuture());
        });
    }

    @Override
    public void next(Future<Void> future) {
        System.out.println("");

        counter = new AtomicInteger(0);
        future.complete();
    }

    @Override
    public void reset(Handler<AsyncResult<Void>> future) {
        System.out.println("reset " + implementation);

        storage.clear(future);
    }

    @Override
    public void shutdown(Handler<AsyncResult<Void>> future) {
        System.out.println("shutdown " + implementation);

        vertx.close(future);
    }

    @Override
    public String name() {
        return implementation;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public BenchmarkImplementation add(Benchmark benchmark) {
        benchmarks.add(benchmark);
        return this;
    }

    @Override
    public List<Benchmark> benchmarks() {
        return this.benchmarks;
    }

    /**
     * Measures time taken to put all entries into the map one by one.
     */
    private void putOne(Future<Void> future) {
        int id = counter.getAndIncrement();
        storage.put(new StorageObject(getName(id), id), done -> future.complete());
    }

    private String getName(int id) {
        return id + ".name";
    }

    /**
     * Measures the time taken to get all entries one by one by their primary key.
     */
    private void getOne(Future<Void> future) {
        storage.get(getName(counter.getAndIncrement()), done -> future.complete());
    }

    /**
     * Measures the time taken to get all entries starting with the given string.
     * The query does not target the primary key.
     */
    private void startsWithQuery(Future<Void> future) {
        storage.query(ID_NAME)
                .startsWith(counter.getAndIncrement() + "")
                .execute(done -> future.complete());
    }

    /**
     * Measures the time taken to get all entries that equally matches the given string.
     * The query does not target the primary key.
     */
    private void equalToQuery(Future<Void> future) {
        storage.query(ID_NAME)
                .equalTo(getName(counter.getAndIncrement()))
                .execute(done -> future.complete());
    }

    /**
     * Measures the time taken to get all entries that contains a specified value within
     * a given range. The query does not target the primary key.
     */
    private void betweenQuery(Future<Void> future) {
        int low = counter.getAndIncrement();
        storage.query(StorageObject.levelField)
                .between((long) (low - 1), (long) (low + 1))
                .execute(done -> future.complete());
    }

    /**
     * Measures the time taken to get all entries that matches the given regular expression.
     * The query does not target the primary key.
     */
    private void regexpQuery(Future<Void> future) {
        storage.query(ID_NAME).matches(".*")
                .execute(done -> future.complete());
    }

    /**
     * Measures the time taken to get all entries that are equal to the given primary key.
     */
    private void equalToPrimaryKey(Future<Void> future) {
        storage.query(Storable.idField)
                .equalTo(counter.getAndIncrement() + "")
                .execute(done -> future.complete());
    }
}
package com.codingchili.core.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.testing.MapTestCases;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the storage providers in core. Reuse these tests when new
 *         storage subsystems are implemented using the StorageLoader.
 */
@Ignore("Requires running elasticsearch 5.2.2, travis runs an older version.")
@RunWith(VertxUnitRunner.class)
public class ElasticMapIT extends MapTestCases {
    private static final int ELASTIC_REFRESH = 1200;

    public ElasticMapIT() {
        /**
         * sets a STARTUP_DELAY between initializing the database and starting the tests.
         * This is required as elasticsearch is near-real-time only.
         */
        STARTUP_DELAY = ELASTIC_REFRESH;
    }

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, ElasticMap.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }

    /**
     * elasticsearch is near-realtime which means that clear does not return realtime results.
     */
    @Override
    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.clear(clear -> {
            test.assertTrue(clear.succeeded());

            context.timer(ELASTIC_REFRESH, event -> store.size(size -> {
                test.assertFalse(size.succeeded(), "should fail to count on missing index.");
                async.complete();
            }));
        });
    }

    @Ignore("Searching with case sensitivity is not supported for ElasticSearch.")
    @Override
    public void testCaseSensitivityEqualsNotIgnored(TestContext test) {
    }

    @Ignore("Querying nested fields cannot be combined with sorting, without loading field-data into memory.")
    @Override
    public void testSortByNestedField(TestContext test) {
    }
}

package com.codingchili.core.Context;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Exception.*;

import static com.codingchili.core.Configuration.Strings.getNoServicesConfiguredForBlock;
import static com.codingchili.core.Context.LaunchContextMock.*;

/**
 * @author Robin Duda
 *
 * Tests for the launcher context.
 */
@RunWith(VertxUnitRunner.class)
public class LaunchContextTest {
    private LaunchContext context;

    @Before
    public void setUp() {
        this.context = new LaunchContextMock(new String[]{});
    }

    @Test
    public void testGetBlock() throws CoreException {
        Assert.assertTrue(context.block(BLOCK_1).contains(SERVICE_1));
        Assert.assertTrue(context.block(BLOCK_2).contains(SERVICE_2));
    }

    @Test
    public void testGetHostBlock() throws CoreException {
        Assert.assertTrue(context.block(HOST_1).contains(SERVICE_1));
        Assert.assertTrue(context.block(HOST_2).contains(SERVICE_1));
    }

    @Test
    public void testGetMissingBlock() throws CoreException {
        try {
            Assert.assertTrue(context.block(BLOCK_NULL).isEmpty());
        } catch (BlockNotConfiguredException e) {
            Assert.assertTrue(e.getMessage().contains(BLOCK_NULL));
        }
    }

    @Test
    public void testGetMissingHost() throws CoreException {
        try {
            context.block(HOST_3).contains(SERVICE_1);
        } catch (RemoteBlockNotConfiguredException e) {
            Assert.assertTrue(e.getMessage().contains(HOST_3));
            Assert.assertTrue(e.getMessage().contains(BLOCK_NULL));
        }
    }

    @Test
    public void testGetEmptyBlockThrows(TestContext test) throws CoreException {
        try {
            context.block(BLOCK_EMPTY);
            test.fail("Should throw exception when block is empty.");
        } catch (NoServicesConfiguredForBlock e) {
            test.assertEquals(getNoServicesConfiguredForBlock(BLOCK_EMPTY), e.getMessage());
        }
    }

    @Test
    public void testGetDefaultBlockWhenNoArgs(TestContext test) throws CoreException {
        try {
            context.block(new String[]{});
        } catch (NoServicesConfiguredForBlock e) {
            test.assertTrue(e.getMessage().contains(BLOCK_DEFAULT));
        }
    }
}

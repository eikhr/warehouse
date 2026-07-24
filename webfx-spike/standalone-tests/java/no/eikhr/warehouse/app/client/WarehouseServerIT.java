package no.eikhr.warehouse.app.client;

import no.eikhr.warehouse.app.model.Item;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Integration test: hits the live server via the JRE Fetch provider. */
class WarehouseServerIT {
    @Test
    void getItemsReturnsLiveInventory() throws Exception {
        WarehouseServer server = new WarehouseServer("https://warehouse.eikhr.no");
        AtomicReference<List<Item>> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        server.getItems()
            .onFailure(e -> { error.set(e); done.countDown(); })
            .onSuccess(items -> { result.set(items); done.countDown(); });
        assertTrue(done.await(15, TimeUnit.SECONDS), "request timed out");
        assertNull(error.get(), () -> "request failed: " + error.get());
        assertNotNull(result.get());
        assertFalse(result.get().isEmpty(), "expected at least one item");
        assertNotNull(result.get().get(0).getName());
    }
}

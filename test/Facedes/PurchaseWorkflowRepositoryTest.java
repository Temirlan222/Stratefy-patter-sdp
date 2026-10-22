package Facedes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseWorkflowRepositoryTest {
    @Test
    void savesPurchaseDataAcrossReopening(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("workflow.db");
        PurchaseWorkflowRepository repository = new PurchaseWorkflowRepository(url);
        repository.save(new PurchaseWorkflow(1, 10, 5, 20, 5000));

        PurchaseWorkflow saved = new PurchaseWorkflowRepository(url).findById(1);

        assertNotNull(saved);
        assertEquals(1, saved.getPurchaseId());
        assertEquals(10, saved.getEventId());
        assertEquals(5, saved.getSeatId());
        assertEquals(20, saved.getCustomerId());
        assertEquals(5000, saved.getBasePrice());
        assertEquals("PENDING_A", saved.getStatus());
    }

    @Test
    void updatesProgressAndContinuesAfterReopening(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("workflow.db");
        PurchaseWorkflowRepository repository = new PurchaseWorkflowRepository(url);
        PurchaseWorkflow purchase = new PurchaseWorkflow(1, 10, 5, 20, 5000);
        repository.save(purchase);
        purchase.markACompleted();
        repository.save(purchase);

        PurchaseWorkflowRepository reopened = new PurchaseWorkflowRepository(url);
        PurchaseWorkflow restored = reopened.findById(1);
        assertNotNull(restored);
        assertEquals("PENDING_B", restored.getStatus());
        restored.markBCompleted();
        reopened.save(restored);

        PurchaseWorkflowRepository third = new PurchaseWorkflowRepository(url);
        PurchaseWorkflow pendingC = third.findById(1);
        assertNotNull(pendingC);
        assertEquals("PENDING_C", pendingC.getStatus());
        pendingC.markCCompleted();
        third.save(pendingC);

        PurchaseWorkflow completed = new PurchaseWorkflowRepository(url).findById(1);
        assertNotNull(completed);
        assertEquals("COMPLETED", completed.getStatus());
    }

    @Test
    void returnsNullForMissingPurchase(@TempDir Path tempDir) throws SQLException {
        PurchaseWorkflowRepository repository = new PurchaseWorkflowRepository(
                "jdbc:sqlite:" + tempDir.resolve("workflow.db"));

        assertNull(repository.findById(99));
    }
}
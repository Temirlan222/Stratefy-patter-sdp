package Facedes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseWorkflowTest {
    @Test
    void completesStepsInOrder() {
        PurchaseWorkflow purchase = new PurchaseWorkflow(1, 10, 5, 20, 5000);
        assertEquals("PENDING_A", purchase.getStatus());

        purchase.markACompleted();
        assertEquals("PENDING_B", purchase.getStatus());
        purchase.markBCompleted();
        assertEquals("PENDING_C", purchase.getStatus());
        purchase.markCCompleted();
        assertEquals("COMPLETED", purchase.getStatus());
    }

    @Test
    void cannotSkipSteps() {
        PurchaseWorkflow purchase = new PurchaseWorkflow(1, 10, 5, 20, 5000);

        assertThrows(IllegalArgumentException.class, () -> purchase.markBCompleted());
        assertThrows(IllegalArgumentException.class, () -> purchase.markCCompleted());
        assertEquals("PENDING_A", purchase.getStatus());

        purchase.markACompleted();
        assertThrows(IllegalArgumentException.class, () -> purchase.markCCompleted());
        assertEquals("PENDING_B", purchase.getStatus());
    }

    @Test
    void cannotRepeatCompletedSteps() {
        PurchaseWorkflow purchase = new PurchaseWorkflow(1, 10, 5, 20, 5000);
        purchase.markACompleted();
        assertThrows(IllegalArgumentException.class, () -> purchase.markACompleted());
        assertEquals("PENDING_B", purchase.getStatus());

        purchase.markBCompleted();
        assertThrows(IllegalArgumentException.class, () -> purchase.markBCompleted());
        assertEquals("PENDING_C", purchase.getStatus());

        purchase.markCCompleted();
        assertThrows(IllegalArgumentException.class, () -> purchase.markACompleted());
        assertThrows(IllegalArgumentException.class, () -> purchase.markBCompleted());
        assertThrows(IllegalArgumentException.class, () -> purchase.markCCompleted());
        assertEquals("COMPLETED", purchase.getStatus());
    }

    @Test
    void rejectsUnknownRestoredStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> new PurchaseWorkflow(1, 10, 5, 20, 5000, "UNKNOWN"));
    }

    @Test
    void compensationHasControlledTransitions() {
        PurchaseWorkflow purchase = new PurchaseWorkflow(1, 10, 5, 20, 5000);
        assertThrows(IllegalArgumentException.class, () -> purchase.markCompensated());
        purchase.beginCompensation();
        assertEquals("COMPENSATING", purchase.getStatus());
        purchase.beginCompensation();
        assertThrows(IllegalArgumentException.class, () -> purchase.markACompleted());
        purchase.markCompensated();
        assertEquals("COMPENSATED", purchase.getStatus());
        assertThrows(IllegalArgumentException.class, () -> purchase.beginCompensation());

        PurchaseWorkflow completed = new PurchaseWorkflow(2, 10, 6, 20, 5000, "COMPLETED");
        assertThrows(IllegalArgumentException.class, () -> completed.beginCompensation());
    }
}
package app.service.impl;

import app.config.JPAConfig;
import app.services.impl.WorkoutService;
import app.services.dto.WorkoutDTO;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkoutServiceTest {

    private static WorkoutService svc;
    private static final int USER_ID = 42;

    @BeforeAll
    static void setup() {
        JPAConfig.emfTest();
        svc = new WorkoutService();
    }

    @Test @Order(1)
    void create_and_listMine() {
        WorkoutDTO w = svc.createMine(USER_ID, LocalDate.parse("2025-10-21"), "Leg day");
        assertNotNull(w.id());
        assertEquals(USER_ID, w.userId());

        List<WorkoutDTO> mine = svc.listMine(USER_ID);
        assertEquals(1, mine.size());
        assertEquals("Leg day", mine.get(0).notes());
    }

    @Test @Order(2)
    void update_and_getMine() {
        Integer id = svc.listMine(USER_ID).get(0).id();
        WorkoutDTO up = svc.updateMine(USER_ID, id, LocalDate.parse("2025-10-22"), "Leg + core");
        assertNotNull(up);
        assertEquals("Leg + core", up.notes());

        WorkoutDTO fetched = svc.getMine(USER_ID, id);
        assertEquals(LocalDate.parse("2025-10-22"), fetched.date());
    }

    @Test @Order(3)
    void deleteMine_and_scoping() {
        Integer id = svc.listMine(USER_ID).get(0).id();
        assertTrue(svc.deleteMine(USER_ID, id));
        assertNull(svc.getMine(USER_ID, id));

        // Anden bruger må ikke se/slette
        assertNull(svc.getMine(999, id));
        assertFalse(svc.deleteMine(999, id));
    }
}

package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

@QuarkusTest
public class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setup() {
        warehouseRepository.deleteAll();

        Warehouse w1 = new Warehouse();
        w1.businessUnitCode = "WH-100";
        w1.location = "LOC-1";
        w1.capacity = 100;
        w1.stock = 50;
        warehouseRepository.create(w1);

        Warehouse w2 = new Warehouse();
        w2.businessUnitCode = "WH-200";
        w2.location = "LOC-2";
        w2.capacity = 200;
        w2.stock = 150;
        warehouseRepository.create(w2);
    }

    @Test
    void testGetAll() {
        List<Warehouse> result = warehouseRepository.getAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindByBusinessUnitCode_Existing() {
        Warehouse result = warehouseRepository.findByBusinessUnitCode("WH-100");
        assertNotNull(result);
        assertEquals("WH-100", result.businessUnitCode);
        assertEquals("LOC-1", result.location);
    }

    @Test
    void testFindByBusinessUnitCode_NotFound() {
        Warehouse result = warehouseRepository.findByBusinessUnitCode("WH-UNKNOWN");
        assertNull(result);
    }

    @Test
    @Transactional
    void testUpdate() {
        Warehouse existing = warehouseRepository.findByBusinessUnitCode("WH-100");
        existing.location = "NEW-LOC";
        existing.capacity = 999;

        warehouseRepository.update(existing);

        Warehouse updated = warehouseRepository.findByBusinessUnitCode("WH-100");
        assertEquals("NEW-LOC", updated.location);
        assertEquals(999, updated.capacity);
    }

    @Test
    @Transactional
    void testRemove() {
        Warehouse existing = warehouseRepository.findByBusinessUnitCode("WH-200");
        warehouseRepository.remove(existing);

        Warehouse deleted = warehouseRepository.findByBusinessUnitCode("WH-200");
        assertNull(deleted);
    }

    @Test
    @Transactional
    void testRemove_NonExistent_ThrowsException() {
        Warehouse notFound = new Warehouse();
        notFound.businessUnitCode = "DOES-NOT-EXIST";

        assertThrows(IllegalArgumentException.class, () -> {
            warehouseRepository.remove(notFound);
        });
    }

    @Test
    void testSearch_ByLocation() {
        List<Warehouse> result = warehouseRepository.search("LOC-2", null, null, null, null, 0, 10);
        assertEquals(1, result.size());
        assertEquals("WH-200", result.get(0).businessUnitCode);
    }

    @Test
    void testSearch_ByCapacityRange() {
        List<Warehouse> result = warehouseRepository.search(null, 50, 150, null, null, 0, 10);
        assertEquals(1, result.size());
        assertEquals("WH-100", result.get(0).businessUnitCode);
    }
}

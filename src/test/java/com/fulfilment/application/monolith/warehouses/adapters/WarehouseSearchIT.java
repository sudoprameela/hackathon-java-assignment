package com.fulfilment.application.monolith.warehouses.adapters;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@QuarkusTest
public class WarehouseSearchIT {

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    public void setup() {
        em.createQuery("DELETE FROM DbWarehouse").executeUpdate();

        // Create test data
        createWarehouse("WH-SEARCH-1", "AMSTERDAM-001", 100, 50, null, LocalDateTime.now().minusDays(1));
        createWarehouse("WH-SEARCH-2", "AMSTERDAM-001", 50, 10, null, LocalDateTime.now().minusDays(2));
        createWarehouse("WH-SEARCH-3", "ZWOLLE-001", 200, 100, null, LocalDateTime.now().minusDays(3));
        createWarehouse("WH-SEARCH-ARCHIVED", "AMSTERDAM-002", 150, 0, LocalDateTime.now(),
                LocalDateTime.now().minusDays(4));

        em.flush();
        em.clear();
    }

    private void createWarehouse(String code, String location, int capacity, int stock, LocalDateTime archivedAt,
            LocalDateTime createdAt) {
        DbWarehouse w = new DbWarehouse();
        w.businessUnitCode = code;
        w.location = location;
        w.capacity = capacity;
        w.stock = stock;
        w.archivedAt = archivedAt;
        w.createdAt = createdAt;
        em.persist(w);
    }

    @Test
    public void testSearchByLocation() {
        given()
                .queryParam("location", "AMSTERDAM-001")
                .when().get("/warehouse/search")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("businessUnitCode", hasItems("WH-SEARCH-1", "WH-SEARCH-2"));
    }

    @Test
    public void testSearchByCapacityFilteredAndExcludesArchived() {
        given()
                .queryParam("minCapacity", 60)
                .queryParam("maxCapacity", 250)
                .when().get("/warehouse/search")
                .then()
                .statusCode(200)
                // Should find WH-SEARCH-1 (100) and WH-SEARCH-3 (200), exclude WH-SEARCH-2 (50)
                // and WH-SEARCH-ARCHIVED
                .body("size()", is(2))
                .body("businessUnitCode", hasItems("WH-SEARCH-1", "WH-SEARCH-3"));
    }

    @Test
    public void testSearchSortingAndPagination() {
        given()
                .queryParam("sortBy", "capacity")
                .queryParam("sortOrder", "desc")
                .queryParam("page", 0)
                .queryParam("pageSize", 2)
                .when().get("/warehouse/search")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                // capacity desc: WH-SEARCH-3 (200), WH-SEARCH-1 (100), WH-SEARCH-2 (50)
                .body("[0].businessUnitCode", equalTo("WH-SEARCH-3"))
                .body("[1].businessUnitCode", equalTo("WH-SEARCH-1"));

        given()
                .queryParam("sortBy", "capacity")
                .queryParam("sortOrder", "desc")
                .queryParam("page", 1)
                .queryParam("pageSize", 2)
                .when().get("/warehouse/search")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].businessUnitCode", equalTo("WH-SEARCH-2"));
    }
}

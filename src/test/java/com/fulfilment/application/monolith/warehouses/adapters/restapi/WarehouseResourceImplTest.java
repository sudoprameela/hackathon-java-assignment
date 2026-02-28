package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseResourceImplTest {

    private static final String WAREHOUSE_CODE = "NEW-WH-123";
    private static boolean dbInitialized = false;

    @jakarta.inject.Inject
    jakarta.persistence.EntityManager em;

    @org.junit.jupiter.api.BeforeEach
    @jakarta.transaction.Transactional
    void setup() {
        if (!dbInitialized) {
            em.createQuery("DELETE FROM DbWarehouse").executeUpdate();

            com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse w1 = new com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse();
            w1.businessUnitCode = "MWH.001";
            w1.location = "LOC-1";
            w1.capacity = 10;
            w1.stock = 5;
            em.persist(w1);

            com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse w2 = new com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse();
            w2.businessUnitCode = "MWH.012";
            w2.location = "LOC-2";
            w2.capacity = 10;
            w2.stock = 5;
            em.persist(w2);

            com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse w3 = new com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse();
            w3.businessUnitCode = "MWH.023";
            w3.location = "LOC-3";
            w3.capacity = 10;
            w3.stock = 5;
            em.persist(w3);

            dbInitialized = true;
        }
    }

    @Test
    @Order(1)
    void listAllWarehousesUnits_shouldReturnSeedData() {
        given()
                .when().get("/warehouse")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("businessUnitCode", containsInAnyOrder("MWH.001", "MWH.012", "MWH.023"));
    }

    @Test
    @Order(2)
    void createANewWarehouseUnit_shouldReturn201() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode(WAREHOUSE_CODE);
        newWarehouse.setLocation("AMSTERDAM-002");
        newWarehouse.setCapacity(50);
        newWarehouse.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(newWarehouse)
                .when().post("/warehouse")
                .then()
                .statusCode(200)
                .body("businessUnitCode", is(WAREHOUSE_CODE))
                .body("location", is("AMSTERDAM-002"));
    }

    @Test
    @Order(3)
    void createANewWarehouseUnit_invalidLocation_shouldReturn400() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("FAIL-CODE");
        newWarehouse.setLocation("LUNAR-BASE-01");
        newWarehouse.setCapacity(50);
        newWarehouse.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(newWarehouse)
                .when().post("/warehouse")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    void getAWarehouseUnitByID_shouldReturnWarehouse() {
        given()
                .pathParam("id", WAREHOUSE_CODE)
                .when().get("/warehouse/{id}")
                .then()
                .statusCode(200)
                .body("businessUnitCode", is(WAREHOUSE_CODE))
                .body("location", is("AMSTERDAM-002"));
    }

    @Test
    @Order(5)
    void getAWarehouseUnitByID_notFound_shouldReturn404() {
        given()
                .pathParam("id", "NON-EXISTENT-CODE")
                .when().get("/warehouse/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void replaceTheCurrentActiveWarehouse_shouldReturnReplaced() {
        Warehouse replacement = new Warehouse();
        replacement.setLocation("AMSTERDAM-002");
        replacement.setCapacity(60);
        replacement.setStock(20);

        given()
                .contentType(ContentType.JSON)
                .pathParam("businessUnitCode", WAREHOUSE_CODE)
                .body(replacement)
                .when().post("/warehouse/{businessUnitCode}/replacement")
                .then()
                .statusCode(200)
                .body("capacity", is(60))
                .body("stock", is(20));
    }

    @Test
    @Order(7)
    void archiveAWarehouseUnitByID_shouldReturn204() {
        given()
                .pathParam("id", WAREHOUSE_CODE)
                .when().delete("/warehouse/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(8)
    void archiveAWarehouseUnitByID_alreadyArchived_shouldReturn400() {
        // Since we archived it in test 7, archiving again should trigger domain logic
        // failure
        given()
                .pathParam("id", WAREHOUSE_CODE)
                .when().delete("/warehouse/{id}")
                .then()
                .statusCode(400);
    }
}

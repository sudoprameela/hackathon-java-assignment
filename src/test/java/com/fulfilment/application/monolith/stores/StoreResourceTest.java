package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreResourceTest {

    @Inject
    EntityManager em;

    private Long firstStoreId;

    @BeforeEach
    @Transactional
    void setup() {
        em.createQuery("DELETE FROM Store").executeUpdate();

        com.fulfilment.application.monolith.stores.Store store = new com.fulfilment.application.monolith.stores.Store();
        store.name = "TONSTAD";
        store.quantityProductsInStock = 10;
        em.persist(store);
        firstStoreId = store.id;

        com.fulfilment.application.monolith.stores.Store store2 = new com.fulfilment.application.monolith.stores.Store();
        store2.name = "KALLAX";
        store2.quantityProductsInStock = 5;
        em.persist(store2);
    }

    @Test
    @Order(1)
    void listAllStores_shouldReturnSeedData() {
        given()
                .when().get("/store")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @Order(2)
    void getStoreById_existing_shouldReturnStore() {
        given()
                .pathParam("id", firstStoreId)
                .when().get("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD"))
                .body("quantityProductsInStock", is(10));
    }

    @Test
    @Order(3)
    void getStoreById_nonExisting_shouldReturn404() {
        given()
                .pathParam("id", 999)
                .when().get("/store/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    void createStore_validPayload_shouldReturn201() {
        com.fulfilment.application.monolith.stores.Store newStore = new com.fulfilment.application.monolith.stores.Store();
        newStore.name = "NEW_STORE";
        newStore.quantityProductsInStock = 50;

        given()
                .contentType(ContentType.JSON)
                .body(newStore)
                .when().post("/store")
                .then()
                .statusCode(201)
                .body("name", is("NEW_STORE"));
    }

    @Test
    @Order(5)
    void updateStore_existingId_shouldReturnUpdated() {
        com.fulfilment.application.monolith.stores.Store updated = new com.fulfilment.application.monolith.stores.Store();
        updated.name = "TONSTAD_UPDATED";
        updated.quantityProductsInStock = 25;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", firstStoreId)
                .body(updated)
                .when().put("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD_UPDATED"))
                .body("quantityProductsInStock", is(25));
    }

    @Test
    @Order(6)
    void patchStore_existingId_shouldReturnPatched() {
        com.fulfilment.application.monolith.stores.Store patched = new com.fulfilment.application.monolith.stores.Store();
        patched.name = "TONSTAD_PATCHED";
        patched.quantityProductsInStock = 12;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", firstStoreId)
                .body(patched)
                .when().patch("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD_PATCHED"))
                .body("quantityProductsInStock", is(12));
    }

    @Test
    @Order(7)
    void deleteStore_existingId_shouldReturn204() {
        given()
                .pathParam("id", firstStoreId)
                .when().delete("/store/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(8)
    void deleteStore_nonExisting_shouldReturn404() {
        given()
                .pathParam("id", 999)
                .when().delete("/store/{id}")
                .then()
                .statusCode(404);
    }
}

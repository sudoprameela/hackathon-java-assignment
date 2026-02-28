package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTest {

    @Inject
    EntityManager em;

    private Long firstProductId;

    @BeforeEach
    @Transactional
    void setup() {
        em.createQuery("DELETE FROM Product").executeUpdate();

        com.fulfilment.application.monolith.products.Product product = new com.fulfilment.application.monolith.products.Product();
        product.name = "TONSTAD";
        product.stock = 10;
        em.persist(product);
        firstProductId = product.id;

        com.fulfilment.application.monolith.products.Product product2 = new com.fulfilment.application.monolith.products.Product();
        product2.name = "KALLAX";
        product2.stock = 5;
        em.persist(product2);
    }

    @Test
    @Order(1)
    void listAllProducts_shouldReturnSeedData() {
        given()
                .when().get("/product")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @Order(2)
    void getProductById_existing_shouldReturnProduct() {
        given()
                .pathParam("id", firstProductId)
                .when().get("/product/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD"))
                .body("stock", is(10));
    }

    @Test
    @Order(3)
    void getProductById_nonExisting_shouldReturn404() {
        given()
                .pathParam("id", 999)
                .when().get("/product/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    void createProduct_validPayload_shouldReturn201() {
        com.fulfilment.application.monolith.products.Product newProduct = new com.fulfilment.application.monolith.products.Product();
        newProduct.name = "NEW_PRODUCT";
        newProduct.description = "A great product";
        newProduct.price = java.math.BigDecimal.valueOf(9.99);
        newProduct.stock = 100;

        given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .when().post("/product")
                .then()
                .statusCode(201)
                .body("name", is("NEW_PRODUCT"));
    }

    @Test
    @Order(5)
    void updateProduct_existingId_shouldReturnUpdated() {
        com.fulfilment.application.monolith.products.Product updated = new com.fulfilment.application.monolith.products.Product();
        updated.name = "TONSTAD_UPDATED";
        updated.stock = 25;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", firstProductId)
                .body(updated)
                .when().put("/product/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD_UPDATED"))
                .body("stock", is(25));
    }

    @Test
    @Order(6)
    void updateProduct_nonExisting_shouldReturn404() {
        com.fulfilment.application.monolith.products.Product updated = new com.fulfilment.application.monolith.products.Product();
        updated.name = "NON_EXISTING";

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 999)
                .body(updated)
                .when().put("/product/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    void deleteProduct_existingId_shouldReturn204() {
        given()
                .pathParam("id", firstProductId)
                .when().delete("/product/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(8)
    void deleteProduct_nonExisting_shouldReturn404() {
        given()
                .pathParam("id", 999)
                .when().delete("/product/{id}")
                .then()
                .statusCode(404);
    }
}

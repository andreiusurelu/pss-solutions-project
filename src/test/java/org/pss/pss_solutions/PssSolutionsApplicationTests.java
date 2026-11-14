package org.pss.pss_solutions;

import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pss.pss_solutions.dao.Order;
import org.pss.pss_solutions.dao.OrderRecord;
import org.pss.pss_solutions.dao.OrderStatus;
import org.pss.pss_solutions.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class PssSolutionsApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private OrderService orderService;

    @Test
    void createOrder() {
        OrderRecord record =  new OrderRecord("gelu", 69.0);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated()
                .expectBody(Long.class)
                .consumeWith(result -> {
                    Long id = result.getResponseBody();
                    assertNotNull(id);
                    assert id > 0L;
                });
    }

    @Test
    void findOrder() {
        OrderRecord record =  new OrderRecord("gelu", 69.0);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount());
                        }
                );
    }

    @Test
    void findOrderNonExistentId() {
        restTestClient.get().uri("/%d".formatted(1L))
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void findOrderNullId() {
        restTestClient.get().uri("/%d".formatted((Long)null)).exchange().expectStatus().isBadRequest();
    }

    @Test
    void findOrderNoIdProvided() {
        restTestClient.get().uri("/").exchange().expectStatus().isBadRequest();
    }

    @Test
    void findOrders() {
        OrderRecord record1 = new OrderRecord("gelu", 69.0);
        OrderRecord record2 = new OrderRecord("relu", 42.0);
        restTestClient.post()
                .body(record1).exchange().expectStatus().isCreated().expectBody(Long.class);
        restTestClient.post()
                .body(record2).exchange().expectStatus().isCreated().expectBody(Long.class);

        restTestClient.get().exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void createInvalidOrderNullCustomerName() {
        OrderRecord record = new OrderRecord(null, 42.00);
        restTestClient.post().body(record).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createInvalidOrderBlankCustomerName() {
        OrderRecord record = new OrderRecord("   ", 42.00);
        restTestClient.post().body(record).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createInvalidOrderNegativeAmount() {
        OrderRecord record = new OrderRecord("gelu", -42.00);
        restTestClient.post().body(record).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createInvalidOrderZeroAmount() {
        OrderRecord record = new OrderRecord("relu", 0.0);
        restTestClient.post().body(record).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createInvalidOrderNullAmount() {
        OrderRecord record = new OrderRecord("relu", null);
        restTestClient.post().body(record).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateOrderStatus() {
        OrderRecord record = new OrderRecord("gelu", 42.00);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            Order createdOrder = restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody(Order.class)
                                    .returnResult().getResponseBody();
                            assertNotNull(createdOrder);
                            assertEquals(id, (Long)createdOrder.getId());
                            assertEquals(record.customerName(), createdOrder.getCustomerName());
                            assertEquals(record.amount(), createdOrder.getAmount());
                            assertEquals(OrderStatus.PENDING, createdOrder.getOrderStatus());

                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", OrderStatus.COMPLETED)
                                            .build(id)).exchange()
                                    .expectStatus().isOk().expectBody().isEmpty();

                            Order updatedOrder = restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody(Order.class)
                                    .returnResult().getResponseBody();
                            assertNotNull(updatedOrder);
                            assertNotEquals(createdOrder.getUpdatedAt(), updatedOrder.getUpdatedAt());
                            assertEquals(OrderStatus.COMPLETED, updatedOrder.getOrderStatus());
                            assertNotEquals(createdOrder.getOrderStatus(), updatedOrder.getOrderStatus());
                        }
                );
    }

    @Test
    void updateOrderStatusSameStatus() {
        OrderRecord record = new OrderRecord("gelu", 42.00);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            Order createdOrder = restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody(Order.class)
                                    .returnResult().getResponseBody();
                            assertNotNull(createdOrder);
                            assertEquals(id, (Long)createdOrder.getId());
                            assertEquals(record.customerName(), createdOrder.getCustomerName());
                            assertEquals(record.amount(), createdOrder.getAmount());
                            assertEquals(OrderStatus.PENDING, createdOrder.getOrderStatus());

                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", OrderStatus.PENDING)
                                            .build(id)).exchange()
                                    .expectStatus().isOk().expectBody().isEmpty();

                            Order updatedOrder = restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody(Order.class)
                                    .returnResult().getResponseBody();
                            assertNotNull(updatedOrder);
                            assertEquals(createdOrder.getUpdatedAt(), updatedOrder.getUpdatedAt());
                            assertEquals(createdOrder.getOrderStatus(), updatedOrder.getOrderStatus());
                        }
                );
    }

    @Test
    void updateOrderStatusAlreadyCompleted() {
        OrderRecord record = new OrderRecord("gelu", 42.00);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount());
                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", OrderStatus.COMPLETED)
                                            .build(id)).exchange()
                                    .expectStatus().isOk().expectBody().isEmpty();
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount())
                                    .jsonPath("$.orderStatus").isEqualTo(OrderStatus.COMPLETED.toString());
                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", OrderStatus.PENDING)
                                            .build(id)).exchange()
                                    .expectStatus().isBadRequest();
                        }
                );
    }

    @Test
    void updateOrderStatusAlreadyCanceled() {
        OrderRecord record = new OrderRecord("gelu", 42.00);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount());
                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", OrderStatus.CANCELLED)
                                            .build(id)).exchange()
                                    .expectStatus().isOk().expectBody().isEmpty();
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount())
                                    .jsonPath("$.orderStatus").isEqualTo(OrderStatus.CANCELLED.toString());
                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", OrderStatus.PENDING)
                                            .build(id)).exchange()
                                    .expectStatus().isBadRequest();
                        }
                );
    }

    @Test
    void updateOrderStatusEmptyStatus() {
        OrderRecord record = new OrderRecord("gelu", 42.00);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount());
                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", "")
                                            .build(id)).exchange()
                                    .expectStatus().isBadRequest();
                        }
                );
    }

    @Test
    void updateOrderStatusNullStatus() {
        OrderRecord record = new OrderRecord("gelu", 42.00);
        restTestClient.post()
                .body(record).exchange().expectStatus().isCreated().expectBody(Long.class).consumeWith(
                        result -> {
                            Long id = result.getResponseBody();
                            assertNotNull(id);
                            assert id > 0L;
                            restTestClient.get().uri("/%d".formatted(id))
                                    .exchange().expectStatus().isFound().expectBody()
                                    .jsonPath("$.customerName").isEqualTo(record.customerName())
                                    .jsonPath("$.amount").isEqualTo(record.amount());
                            restTestClient.put().uri(uriBuilder -> uriBuilder
                                            .path("/{id}")
                                            .queryParam("orderStatus", (OrderStatus)null)
                                            .build(id)).exchange()
                                    .expectStatus().isBadRequest();
                        }
                );
    }

    @Test
    void updateOrderStatusInvalidId() {
        restTestClient.put().uri(uriBuilder -> uriBuilder
                        .path("/{id}")
                        .queryParam("orderStatus", OrderStatus.COMPLETED)
                        .build(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrderStatusNullId() {
        restTestClient.put().uri(uriBuilder -> uriBuilder
                        .path("/{id}")
                        .queryParam("orderStatus", OrderStatus.COMPLETED)
                        .build((Long)null)).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getInvalidCommand() {
        restTestClient.get().uri("/invalid").exchange().expectStatus().isBadRequest();
    }

    @Test
    void postInvalidCommand() {
        OrderRecord record = new OrderRecord("relu", 42.0);
        restTestClient.post().uri("/invalid").body(record).exchange().expectStatus().isBadRequest();
    }

    @Test
    void putInvalidCommand() {
        restTestClient.put().uri(uriBuilder -> uriBuilder
                        .path("/invalid")
                        .queryParam("orderStatus", OrderStatus.COMPLETED)
                        .build()).exchange()
                .expectStatus().isBadRequest();
    }

    @BeforeEach
    void initRestTestClient() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:%d/orders".formatted(port))
                .defaultHeader("ContentType", "application/json")
                .build();
    }
    @AfterEach
    void cleanDatabase() {
        orderService.cleanDatabase();
    }

}

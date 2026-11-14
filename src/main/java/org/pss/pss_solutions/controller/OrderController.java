package org.pss.pss_solutions.controller;

import org.pss.pss_solutions.dao.Order;
import org.pss.pss_solutions.dao.OrderRecord;
import org.pss.pss_solutions.dao.OrderStatus;
import org.pss.pss_solutions.exception.InvalidRequestParameterException;
import org.pss.pss_solutions.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseStatus(HttpStatus.FOUND)
    public Order getOrder(@PathVariable Long id){
        return orderService.getOrder(id);
    }

    @GetMapping("/")
    public void getOrderWithoutId() {
        throw new InvalidRequestParameterException("Order request needs an ID.");
    }

    @GetMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrders(@RequestParam(required = false) OrderStatus orderStatus, Pageable pageable) {
        return orderService.findOrders(orderStatus, pageable);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createOrder(@RequestBody OrderRecord orderRecord) {
        return orderService.createOrder(orderRecord);
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus orderStatus) {
        orderService.updateOrderStatus(id, orderStatus);
    }

    @GetMapping(value = "/total", produces = "application/json")
    public Double getTotal(@RequestParam OrderStatus orderStatus) {
        return orderService.getTotalByOrderStatus(orderStatus);
    }
}

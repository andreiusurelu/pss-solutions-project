package org.pss.pss_solutions.service;

import org.pss.pss_solutions.dao.Order;
import org.pss.pss_solutions.dao.OrderRecord;
import org.pss.pss_solutions.dao.OrderStatus;
import org.pss.pss_solutions.exception.InvalidOrderPropertyException;
import org.pss.pss_solutions.exception.InvalidRequestParameterException;
import org.pss.pss_solutions.exception.OrderNotFoundException;
import org.pss.pss_solutions.exception.OrderStatusException;
import org.pss.pss_solutions.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getOrder(Long id) {
        if (id == null) {
            throw new InvalidRequestParameterException("Order id is null");
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + id));
    }

    public Long createOrder(OrderRecord orderRecord) {
        if (orderRecord.customerName() == null) {
            throw new InvalidOrderPropertyException("Customer name is null.");
        }
        else if (orderRecord.customerName().isBlank()) {
            throw new InvalidOrderPropertyException("Client name is blank.");
        } else if (orderRecord.amount() == null || orderRecord.amount() <= 0.0) {
            throw new InvalidOrderPropertyException("Amount is not positive.");
        }
        else {
            Order order = new Order();
            order.setCustomerName(orderRecord.customerName());
            order.setAmount(orderRecord.amount());
            order.setCreatedAt(new Date());
            order.setUpdatedAt(new Date());
            order.setOrderStatus(OrderStatus.PENDING);
            Order result = orderRepository.save(order);
            return result.getId();
        }
    }

    public List<Order> findOrders(OrderStatus orderStatus, Pageable pageable) {
        Set<String> allowedColumns = Set.of("amount", "createdAt");
        boolean valid = pageable.getSort().stream()
                .allMatch(order -> allowedColumns.contains(order.getProperty()));
        if (valid) {
            if (orderStatus == null)
                return orderRepository.findAllBy(pageable);
            else
                return orderRepository.findAllByOrderStatus(orderStatus, pageable);
        }
        else
            throw new InvalidOrderPropertyException("Invalid sort column");
    }

    public void updateOrderStatus(Long id, OrderStatus orderStatus) {
        if (id == null) {
            throw new InvalidRequestParameterException("Id is null");
        }
        if (orderStatus == null) {
            throw new InvalidOrderPropertyException("Id is null or invalid");
        }
        Order o = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Could not find order of id " + id));
        if (o.getOrderStatus().equals(orderStatus)) {
        }
        else if (o.getOrderStatus().equals(OrderStatus.COMPLETED)) {
            throw new OrderStatusException("Order with id " + id + " is already finished.");
        }
        else if (o.getOrderStatus().equals(OrderStatus.CANCELLED)) {
            throw new OrderStatusException("Order with id " + id + " is cancelled.");
        }
        else {
            o.setOrderStatus(orderStatus);
            o.setUpdatedAt(new Date());
            orderRepository.save(o);
        }
    }

    public Double getTotalByOrderStatus(OrderStatus orderStatus) {
        List<Order> orders = orderRepository.findAllByOrderStatus(orderStatus);
        if (orders.isEmpty()) {
            return 0.0;
        }
        else {
            return orders.stream().mapToDouble(Order::getAmount).sum();
        }
    }

    public void cleanDatabase() {
        orderRepository.deleteAll();
    }
}

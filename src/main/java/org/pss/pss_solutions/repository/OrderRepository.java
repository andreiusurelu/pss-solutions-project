package org.pss.pss_solutions.repository;

import org.pss.pss_solutions.dao.Order;
import org.pss.pss_solutions.dao.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllBy(Pageable pageable);
    List<Order> findAllByOrderStatus(OrderStatus orderStatus);
    List<Order> findAllByOrderStatus(OrderStatus orderStatus, Pageable pageable);
}

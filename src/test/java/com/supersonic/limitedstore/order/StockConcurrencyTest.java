package com.supersonic.limitedstore.order;

import com.supersonic.limitedstore.domain.order.infrastructure.client.ProductClient;
import com.supersonic.limitedstore.domain.order.presentation.dto.req.OrderRequestDto;
import com.supersonic.limitedstore.domain.order.repository.OrderRepository;
import com.supersonic.limitedstore.domain.order.service.OrderService;
import com.supersonic.limitedstore.domain.product.entity.Product;
import com.supersonic.limitedstore.domain.product.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
public class StockConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void 동시에_100명_주문시_재고_음수() throws InterruptedException {
        Product product = Product.builder()
            .name("테스트상품")
            .description("테스트")
            .price(10000)
            .stock(10)
            .releaseAt(LocalDateTime.now())
            .build();
        productRepository.save(product);

        given(productClient.exists(product.getId())).willReturn(true);

        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        for (int i = 0; i < threadCount; i++) {
            UUID memberId = UUID.randomUUID();
            OrderRequestDto dto = OrderRequestDto.builder()
                .productId(product.getId())
                .build();

            executorService.submit(() -> {
                try {
                    orderService.createOrder(memberId, dto);
                } catch (Exception e) {

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product result = productRepository.findById(product.getId()).orElseThrow();
        System.out.println("최종 재고: " + result.getStock());
        assertThat(result.getStock()).isGreaterThanOrEqualTo(0);
    }
}

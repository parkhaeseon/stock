package com.example.stock.facade;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OptimisticLockStockFacadeTest {

    @Autowired
    //private StockService stockService;
    private OptimisticLockStockFacade optimisticLockStockFacade; // 낙관적 락

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void requestStockDecreaseOneHundred() throws InterruptedException {
        int threadCount = 100;

        // 멀티쓰레드 이용해야하기 때문에 ExecutorService 이용.
        // ExecutorService는 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 Java의 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch는 다른 Thread에서 수행 중인 작업이 완료될 때까지, 대기할 수 있도록 도와주는 Class이다.
        CountDownLatch latch = new CountDownLatch(threadCount);

        // for문을 활용해서 100개의 요청을 보내준다.
        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 낙관적 락 장점 : 별도의 Lock을 잡지는 않음.
                    // 그러나, update가 실패 했을 때, 재시도 로직을 개발자가 구현해야함.
                    // 그리고 충돌이 빈번하게 일어난다면 비관적 락
                    // 빈번하게 일어나지 않을 것이라고 예상한다면 낙관적 락
                   optimisticLockStockFacade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(0, stock.getQuantity());
    }

}
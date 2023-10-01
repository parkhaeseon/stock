package com.example.stock.service;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    //private StockService stockService;
    private PessimisticLockStockService stockService; // 비관적 락 : Test Log를 보면 "~for update" 라는 Log가 있는데 이 부분이 Lock 걸고 데이터를 가져오는 부분이다.

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

    // 요청이 1개씩 들어올 때
    // 결과 : 성공
    @Test
    public void requestStockDecrease() {
        stockService.decrease(1L, 1L);

        // 100 - = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(99, stock.getQuantity());
    }

    // 동시에 요청이 100개 들어올 때(멀티쓰레드 사용)
    // synchronized 붙이기 전 결과 : 실패. 우리가 예상한건 0개인데 실제로 Test 수행 결과 90으로 나옴.
    //
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
                    stockService.decrease(1L, 1L);
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100 - (1*100) = 0
        // 0개 되는걸로 예상
        assertEquals(0, stock.getQuantity());

        // 아래는 Test 결과 코드
        /**
         * expected: <0> but was: <90>
         * Expected :0
         * Actual   :90
         */

        /**
         * 우리의 예상과 다른 결과가 나온 이유는?
         * → 레이스 컨디션(Race Condition)이 발생했기 때문이다.
         * → 레이스 컨디션(Race Condition) : 둘 이상의 스레드가 공유 데이터에 액세스할 수 있고 동시에 변경하려고 할 때 발생하는 문제
         *
         * 쉽게 설명하자면, Thread1이 Data를 Select해서 Update하기 전에
         * Thread2도 Data를 가져가기 때문이다.
         * 우리가 바라는 것 : Thread1의 Data Select → Thread1의 Data update → Thread2의 Data Select → Thread2의 Data update
         * 현실 : Thread1의 Date Select → Thread2의 Data Select → Thread1의 Data update → Thread2의 Data update
         * 위의 '현실'에서 Thread2가 Select한 Data는 Thread1이 update하기 전의 Data이다.
         */

        // 위까지 synchronized를 붙이기 전이다. StockService.java의 decrease() 메소드

        /**
         * 하지만, synchronized를 붙여도 Test는 실패한다.
         * 이유는 @Transactional의 동작 방식 때문이다.(StockService.java의 decrease())
         * @Transactional : Spring에서는 @Transactional가 붙은 Class를 새로운 Class로 생성해서 실행한다.
         * 쉽게 말하면, DB가 Update 하기 전에 다른 Thread가 decrease()를 호출할 수 있다.
         * → @Transactional을 주석 처리하면 성공한다.
         * 하지만 synchronized의 문제점이 있는데,
         * 서버가 1대일때는 되는듯싶으나 여러대의 서버를 사용하게되면 사용하지 않았을때와 동일한 문제가 발생된다.
         * 인스턴스단위로 thread-safe 이 보장이 되고, 여러서버가 된다면 여러개의 인스턴스가 있는것과 동일하기 때문입니다.
         * java의 synchronized는 하나의 프로세스 안에서만 보장이 된다.
         * 쉽게 말하면 하나의 서버에서만 보장되고 여러 대의 서버에서는 레이스 컨디션이 발생한다.
         * → @Transactional을 주석 처리하고 synchronized만 있으면 성공한다.(StockService.java의 decrease())
         */


    }
}
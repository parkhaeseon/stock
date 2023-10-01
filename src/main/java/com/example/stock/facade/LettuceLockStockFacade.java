package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    /**
     * 레튜스
     * 장점 : 구현 간단.
     * 단점 : spin lock 방식이라서, redis에 부하를 줌. 그래서, Thread sleep을 통해 lock 획득 재시간에 텀을 두어야함.
     */

    // redis를 활용해서 lock을 수행하기 위해
    private RedisLockRepository redisLockRepository;

    // 재고 감소를 위해
    private StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {

        // lock 획득 실패할 경우 100 millis 후에 재시도
        while(!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }

        try {
            stockService.decrease(id, quantity);
        }
        finally {
            redisLockRepository.unlock(id);
        }
    }
}

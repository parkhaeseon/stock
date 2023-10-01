package com.example.stock.facade;

import com.example.stock.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockFacade {

    /**
     * Redisson 같은 경우 lock 관련된 class를 라이브러리를 제공해주므로 별도의 repository를 작성하지 않아도 된다.
     *
     * pub-sub 기반이라서 redis 부하 줄여줌.
     * 그러나 구현이 복잡. 별도의 라이브러리를 사용해야하는 부담감
     */

    private RedissonClient redissonClient;
    private StockService stockService;

    public RedissonLockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        // redissonClient를 활용해서 lock 객체를 가져온다.
        RLock lock = redissonClient.getLock(id.toString());

        try {
            // 나는 밑에 오류 난다..
            // import java.util.concurrent.TimeUnit;가 안된다.
            // 테스트가 실패할 경우 lock 기다리는 시간을 좀 더 늘리자.
            boolean available = lock.tryLock(20, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }

            stockService.decrease(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}

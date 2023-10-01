package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.StockService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

@Component
public class NamedLockStockFacade {

    /**
     *
     * 3. Named Lock
     * a. 이름을 가진 metadata locking 입니다.
     * 이름을 가진 lock 을 획득한 후 해제할때까지 다른 세션은 이 lock 을 획득할 수 없도록 합니다.
     * 주의할점으로는 transaction 이 종료될 때 lock 이 자동으로 해제되지 않습니다.
     * 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제됩니다.
     * metadata 단위로 Lock을 건다. 즉, Stock에 직접적으로 Lock을 걸지 않고, 별도의 공간에 Lock을 건다.
     * 트랙잭션 종료시에 락해제, 세션 관리를 잘해야하기 떄문에 주의해야하고 실무에서는 다소 복잡하게 구현될 수 있다.
     */

    // 락을 획득하기 위해
    private final LockRepository lockRepository;

    // 재고 감소를 위해
    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {

        /**
         * 같은 데이터소스를 사용할 것이기 때문에
         *     hikari:
         *       maximum-pool-size: 40
         * application.yml에 추가했다.
         */

        try {
            lockRepository.getLock(id.toString());

            // https://woodcock.tistory.com/40
            try {
                stockService.decrease(id, quantity);
            }
            catch (Exception e) {

            }
        }
        finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}

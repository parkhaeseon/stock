package com.example.stock.facade;

import com.example.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockStockFacade {


    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {

        while(true) {
            try {
                optimisticLockStockService.decrease(id, quantity);
                break; // 정상적으로 update가 된다면, while 탈출
            }
            catch (Exception e) {
                // 수량 감소에 실패한다면 50 millis 후에 재시도
                Thread.sleep(50);
            }
        }

    }
}

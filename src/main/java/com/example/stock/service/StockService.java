package com.example.stock.service;

// 재고를 감소시키는 기능

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    // 생성자 주입 방식만 final 이용할 수 있다.
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // NamedLock 테스트를 위해 전체 주석 처리
    // synchronized를 붙여 주면, 1개의 Thread만 접근이 가능하다.
    // → 하지만, 실패한다. 이유는 @Transactional의 동작 방식 때문이다.
    // @Transactional : Spring에서는 @Transactional가 붙은 Class를 새로운 Class로 생성해서 실행한다.
    // 쉽게 말하면, DB가 Update 하기 전에 다른 Thread가 decrease()를 호출할 수 있다.
    // → @Transactional을 주석 처리하고 synchronized만 있으면 성공한다.
    //@Transactional
//    public synchronized void decrease(Long id, Long quantity) {
//        // ctrl + alt + v : return 값에 맞는 local 변수 자동 생성
//        // Stock 조회
//        Stock stock = stockRepository.findById(id).orElseThrow();
//
//        // 재고 감소
//        stock.decrease(quantity);
//
//        // 갱신된 값을 저장
//        stockRepository.saveAndFlush(stock);
//
//        /**
//         * java synchronized 문제점
//         * 서버가 1대일때는 되는듯싶으나 여러대의 서버를 사용하게되면 사용하지 않았을때와 동일한 문제가 발생된다.
//         * 인스턴스단위로 thread-safe 이 보장이 되고, 여러서버가 된다면 여러개의 인스턴스가 있는것과 동일하기 때문입니다.
//         * java의 synchronized는 하나의 프로세스 안에서만 보장이 된다.
//         * 쉽게 말하면 하나의 서버에서만 보장되고 여러 대의 서버에서는 레이스 컨디션이 발생한다.
//         */
//    }

    // NamedLockTest
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long id, Long quantity) {

        // stockService에서는 부모의 트랙잭션(아마도 getLock 등) 별도로 실행이 되어야 하기 때문에
        // propagation 변경했음. Propagation.REQUIRES_NEW -> 별도의 트랙잭션 생성(독립적이다 라는 뜻은 아니다)

        // ctrl + alt + v : return 값에 맞는 local 변수 자동 생성
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값을 저장
        stockRepository.saveAndFlush(stock);
    }
}

package com.example.stock.repository;

import com.example.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// 여기서는 편의성을 위해 Stock을 하드코딩으로 Setting하지만
// 실무에서는 별도의 JDBC 등등을 사용해야한다.
public interface LockRepository extends JpaRepository<Stock, Long> {

    /**
     * mysql에서는 get_lock으로 lock을 걸고, release_lock으로 lock을 해제한다.
     */

    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);

}

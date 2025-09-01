package be.com.catalogservice.domain;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Modifying
    @Transactional  // 사용자 지정 쿼리는 트랜잭션 어노테이션을 명시적으로 선언해주어야 함 그래야 트랜잭션 컨텍스트 안에서 실행이 됨
    @Query("DELETE FROM BOOK WHERE isbn = :isbn")
    void deleteByIsbn(String isbn);
}


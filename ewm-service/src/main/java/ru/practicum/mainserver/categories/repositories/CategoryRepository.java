package ru.practicum.mainserver.categories.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainserver.categories.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c " +
            "from Category c " +
            "order by c.id " +
            "limit ?2 " +
            "offset ?1 ")
    List<Category> findByFromAndSize(int from, int size);

}

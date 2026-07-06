package ru.practicum.mainserver.compilations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainserver.compilations.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("select c " +
            "from Compilation c " +
            "order by c.id " +
            "limit ?2 " +
            "offset ?1 ")
    List<Compilation> findByFromAndSize(int from, int size);

    @Query("select c " +
            "from Compilation c " +
            "where c.pinned = ?1 " +
            "order by c.id " +
            "limit ?3 " +
            "offset ?2")
    List<Compilation> findByFromAndSizeAndPinned(boolean pinned, int from, int size);

}

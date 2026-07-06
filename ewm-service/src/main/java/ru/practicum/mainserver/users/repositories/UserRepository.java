package ru.practicum.mainserver.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainserver.users.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u " +
            "from User u " +
            "where u.id in ?1 or ?1 is null " +
            "order by u.id " +
            "limit ?3 " +
            "offset ?2")
    List<User> findAllByIdsAndFromAndSize(String[] ids, Integer from, Integer size);

}

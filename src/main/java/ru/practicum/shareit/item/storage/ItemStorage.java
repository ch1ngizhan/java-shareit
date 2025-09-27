package ru.practicum.shareit.item.storage;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemStorage extends JpaRepository<Item, Long> {


    @Query("SELECT i FROM Item i " +
            "WHERE i.available = TRUE AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?1, '%')))")
    List<Item> search(String text);

    List<Item> findByOwnerId(Long userId);

}

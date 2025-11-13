package ru.practicum.shareit.item.storage;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {


    @Query("SELECT i FROM Item i " +
            "WHERE i.available = TRUE AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?1, '%')))")
    List<Item> search(String text);

    List<Item> findByOwnerId(Long userId);

    List<Item> findByOwnerIdOrderByIdDesc(Long ownerId);
}

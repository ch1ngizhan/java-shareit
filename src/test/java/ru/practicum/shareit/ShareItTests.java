package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;

@SpringBootTest
class ShareItTests {


    @Autowired
    private ItemStorage itemStorage;

    @Test
    void testItemCreation() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        // Не устанавливаем available явно

        Item saved = itemStorage.save(item);
        System.out.println("Saved item available: " + saved.getAvailable());

        // Проверяем, что available не null
        assert saved.getAvailable() != null : "Available should not be null!";
    }

}

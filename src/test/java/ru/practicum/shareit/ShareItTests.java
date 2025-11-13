package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.storage.ItemStorage;

@SpringBootTest
class ShareItTests {


    @Autowired
    private ItemStorage itemStorage;

    @Test
    void testItemCreation() {
    }

}

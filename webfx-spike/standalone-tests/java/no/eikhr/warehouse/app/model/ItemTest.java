package no.eikhr.warehouse.app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    @Test
    void gettersAndSettersRoundTrip() {
        Item i = new Item();
        i.setId("abc");
        i.setName("Jägermeister");
        i.setAmount(3);
        i.setBrand("Mast-Jägermeister");
        i.setRegularPrice(1.5);
        i.setCreationDate("2022-11-26T20:43:23.206");
        assertEquals("abc", i.getId());
        assertEquals("Jägermeister", i.getName());
        assertEquals(3, i.getAmount());
        assertEquals("Mast-Jägermeister", i.getBrand());
        assertEquals(1.5, i.getRegularPrice());
        assertEquals("2022-11-26T20:43:23.206", i.getCreationDate());
    }
}

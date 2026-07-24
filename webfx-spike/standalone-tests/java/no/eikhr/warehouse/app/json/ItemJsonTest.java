package no.eikhr.warehouse.app.json;

import no.eikhr.warehouse.app.model.Item;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemJsonTest {
    @Test
    void parsesServerItemJson() {
        String body = "{\"id\":\"abc\",\"name\":\"Vodka 40% vann\",\"amount\":3,"
                    + "\"brand\":\"Koskenkorva\",\"regularPrice\":1.5,\"barcode\":null,"
                    + "\"creationDate\":\"2022-11-26T20:43:23.206\"}";
        ReadOnlyAstObject obj = Json.parseObject(body);
        Item i = ItemJson.fromAst(obj);
        assertEquals("abc", i.getId());
        assertEquals("Vodka 40% vann", i.getName());
        assertEquals(3, i.getAmount());
        assertEquals("Koskenkorva", i.getBrand());
        assertEquals(1.5, i.getRegularPrice());
        assertNull(i.getBarcode());
    }

    @Test
    void serializesItemToJsonRoundTrip() {
        Item i = new Item();
        i.setId("x1"); i.setName("Test"); i.setAmount(7); i.setRegularPrice(2.0);
        String json = ItemJson.toJsonString(i);
        Item back = ItemJson.fromAst(Json.parseObject(json));
        assertEquals("x1", back.getId());
        assertEquals("Test", back.getName());
        assertEquals(7, back.getAmount());
        assertEquals(2.0, back.getRegularPrice());
    }
}

package no.eikhr.warehouse.app.json;

import no.eikhr.warehouse.app.model.Item;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Item} &lt;-&gt; AST-JSON, replacing Jackson.
 *
 * <p>Note the asymmetric dimension keys: the server (Jackson) <em>emits</em>
 * {@code height}/{@code width}/{@code length} from the getters but <em>reads</em>
 * them back through constructor params annotated
 * {@code @JsonProperty("itemHeight"/"itemWidth"/"itemLength")}. So we parse the
 * emitted names and serialize the constructor names, giving a correct round-trip
 * against the live server.
 */
public final class ItemJson {
    private ItemJson() {}

    public static Item fromAst(ReadOnlyAstObject o) {
        Item i = new Item();
        i.setId(o.getString("id"));
        i.setName(o.getString("name"));
        i.setAmount(o.getInteger("amount", 0));
        i.setBarcode(o.getString("barcode"));
        i.setBrand(o.getString("brand"));
        i.setRegularPrice(o.getDouble("regularPrice"));
        i.setSalePrice(o.getDouble("salePrice"));
        i.setPurchasePrice(o.getDouble("purchasePrice"));
        i.setSection(o.getString("section"));
        i.setRow(o.getString("row"));
        i.setShelf(o.getString("shelf"));
        i.setHeight(o.getDouble("height"));
        i.setWidth(o.getDouble("width"));
        i.setLength(o.getDouble("length"));
        i.setWeight(o.getDouble("weight"));
        i.setCreationDate(o.getString("creationDate"));
        return i;
    }

    public static List<Item> listFromAst(ReadOnlyAstArray arr) {
        List<Item> list = new ArrayList<>();
        for (int k = 0; k < arr.size(); k++)
            list.add(fromAst(arr.getObject(k)));
        return list;
    }

    public static AstObject toAst(Item i) {
        AstObject o = AST.createObject();
        o.set("id", i.getId());
        o.set("name", i.getName());
        o.set("amount", i.getAmount());
        o.set("barcode", i.getBarcode());
        o.set("brand", i.getBrand());
        o.set("regularPrice", i.getRegularPrice());
        o.set("salePrice", i.getSalePrice());
        o.set("purchasePrice", i.getPurchasePrice());
        o.set("section", i.getSection());
        o.set("row", i.getRow());
        o.set("shelf", i.getShelf());
        // Server deserializes dimensions from these constructor-param names:
        o.set("itemHeight", i.getHeight());
        o.set("itemWidth", i.getWidth());
        o.set("itemLength", i.getLength());
        o.set("weight", i.getWeight());
        o.set("creationDate", i.getCreationDate());
        return o;
    }

    public static String toJsonString(Item i) {
        return Json.formatNode(toAst(i));
    }
}

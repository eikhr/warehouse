package no.eikhr.warehouse.app.model;

/**
 * WebFX-compatible port of {@code core.main.Item}.
 *
 * <p>Plain data class: no Jackson annotations, no {@code java.time}. The
 * {@code creationDate} is kept as the ISO-8601 String the server already sends
 * (java.time does not transpile).
 */
public class Item {
    private String id;
    private String name;
    private int amount;
    private String barcode;
    private String brand;
    private Double regularPrice;
    private Double salePrice;
    private Double purchasePrice;
    private String section;
    private String row;
    private String shelf;
    private Double height;
    private Double width;
    private Double length;
    private Double weight;
    private String creationDate; // ISO-8601 string; java.time does not transpile

    public Item() {}

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public int getAmount() { return amount; }
    public void setAmount(int v) { amount = v; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String v) { barcode = v; }
    public String getBrand() { return brand; }
    public void setBrand(String v) { brand = v; }
    public Double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(Double v) { regularPrice = v; }
    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double v) { salePrice = v; }
    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double v) { purchasePrice = v; }
    public String getSection() { return section; }
    public void setSection(String v) { section = v; }
    public String getRow() { return row; }
    public void setRow(String v) { row = v; }
    public String getShelf() { return shelf; }
    public void setShelf(String v) { shelf = v; }
    public Double getHeight() { return height; }
    public void setHeight(Double v) { height = v; }
    public Double getWidth() { return width; }
    public void setWidth(Double v) { width = v; }
    public Double getLength() { return length; }
    public void setLength(Double v) { length = v; }
    public Double getWeight() { return weight; }
    public void setWeight(Double v) { weight = v; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String v) { creationDate = v; }
}

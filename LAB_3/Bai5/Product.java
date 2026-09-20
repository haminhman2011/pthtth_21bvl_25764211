public class Product {
    private final String code;      // mã sản phẩm (không rỗng)
    private final String name;      // tên sản phẩm (không rỗng)
    private final double unitPrice; // đơn giá (> 0)
    private final int quantity;     // số lượng (>= 0)

    public Product(String code, String name, double unitPrice, int quantity) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã không được rỗng");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên không được rỗng");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        this.code = code;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // ---- Getters (đóng gói) ----
    public String getCode()      { return code; }
    public String getName()      { return name; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity()     { return quantity; }

    /** Giá trị tồn kho = đơn giá * số lượng. */
    public double inventoryValue() {
        return unitPrice * quantity;
    }

    /** Một dòng CSV: ma,ten,donGia,soLuong */
    public String toCsvRow() {
        return "%s,%s,%.0f,%d".formatted(code, name, unitPrice, quantity);
    }

    @Override
    public String toString() {
        return "%-6s | %-15s | đơn giá %,10.0f | SL %3d | tồn kho %,12.0f VND".formatted(
                code, name, unitPrice, quantity, inventoryValue());
    }
}

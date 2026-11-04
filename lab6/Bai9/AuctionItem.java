package Bai9;

import java.io.Serializable;

/**
 * BÀI 9 - Sản phẩm đấu giá
 *
 * Phải implements Serializable vì đối tượng này được gửi qua mạng
 * (server trả danh sách sản phẩm về cho client).
 */
public class AuctionItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;               // mã sản phẩm (server tự tăng)
    private String name;          // tên sản phẩm
    private String seller;        // người bán
    private double startPrice;    // giá khởi điểm
    private double currentPrice;  // giá cao nhất hiện tại
    private String highestBidder; // người đang trả giá cao nhất (null = chưa ai trả)

    public AuctionItem(int id, String name, String seller, double startPrice) {
        this.id = id;
        this.name = name;
        this.seller = seller;
        this.startPrice = startPrice;
        this.currentPrice = startPrice; // ban đầu giá hiện tại = giá khởi điểm
        this.highestBidder = null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSeller() {
        return seller;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    // Cập nhật giá cao nhất và người trả giá
    public void setHighestBid(double price, String bidder) {
        this.currentPrice = price;
        this.highestBidder = bidder;
    }

    @Override
    public String toString() {
        return String.format("#%-3d %-15s | Nguoi ban: %-8s | Khoi diem: %,12.0f | Hien tai: %,12.0f | Cao nhat: %s",
                id, name, seller, startPrice, currentPrice,
                (highestBidder == null ? "(chua co)" : highestBidder));
    }
}

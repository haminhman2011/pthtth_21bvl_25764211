# Bài 4 — Máy tính từ xa TCP

Biên dịch từ thư mục gốc dự án:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_4/*.java
```

Chạy server trong một terminal:

```powershell
java Bai_5.Bai_4.CalculatorServer
```

Chạy client trong terminal khác:

```powershell
java Bai_5.Bai_4.CalculatorClient
```

Mặc định `localhost:5004`. Đổi cổng bằng tham số server `[port]` và client `[host] [port]`. Nhập `QUIT` để client đóng socket (lệnh cục bộ, không gửi đến server). Nhấn Ctrl+C để dừng server.

## Giao thức

Mã hóa UTF-8, mỗi yêu cầu và phản hồi là một dòng. Client có thể gửi nhiều yêu cầu trên cùng kết nối. Server phục vụ lần lượt từng client và tiếp tục chờ client mới khi phiên hiện tại kết thúc.

Yêu cầu: `CALC toán_tử toán_hạng_1 toán_hạng_2`. Từ khóa `CALC` phân biệt hoa/thường; cho phép nhiều khoảng trắng hoặc tab giữa các thành phần và khoảng trắng ở hai đầu. Toán tử: `+`, `-`, `*`, `/`. Toán hạng dùng dấu chấm thập phân, có thể có dấu âm/dương hoặc dạng số mũ như `1e3`. Không chấp nhận `NaN` hoặc `Infinity`.

| Yêu cầu | Phản hồi |
| --- | --- |
| `CALC + 100 200` | `OK 300` |
| `CALC - 100 200` | `OK -100` |
| `CALC * 1.5 2` | `OK 3` |
| `CALC / 10 4` | `OK 2.5` |
| `CALC / 10 0` | `ERR DIVIDE_BY_ZERO` |
| `CALC % 10 3` | `ERR UNSUPPORTED_OPERATOR` |
| `CALC + a 2` | `ERR INVALID_NUMBER` |
| `CALC + 1` | `ERR INVALID_FORMAT` |

Thứ tự kiểm tra lỗi: số thành phần/từ khóa → toán tử → toán hạng → phép tính. Chia cho `0`, `-0` hay `0.00` đều báo lỗi. Dùng `BigDecimal` để tính số thập phân; cộng, trừ, nhân không làm tròn, phép chia dùng `MathContext.DECIMAL128` (34 chữ số có nghĩa, làm tròn HALF_EVEN). Kết quả bỏ số 0 thập phân dư; số có scale ngoài khoảng -100 đến 100 có thể hiển thị dạng số mũ. Nếu vượt giới hạn biểu diễn của BigDecimal, trả `ERR ARITHMETIC_ERROR`.

Các ca kiểm tra qua kết nối TCP thực tế và phiên chạy client được lưu trong `minh_chung.txt`.

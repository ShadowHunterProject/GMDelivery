# GMDelivery Plugin

GMDelivery là một plugin giao hàng (delivery missions) dành cho server Minecraft (tương thích Spigot API 1.21). Plugin cho phép người chơi nhận các nhiệm vụ nộp vật phẩm ngẫu nhiên để đổi lấy XP, câu lệnh hoặc Voucher. Với hệ thống phân loại danh mục (Categories) linh hoạt, plugin mang đến trải nghiệm cày cuốc nhiệm vụ hấp dẫn.

## ✨ Tính Năng Nổi Bật

- **Hệ Thống Phân Loại (Categories):** Nhiệm vụ được chia thành nhiều thư mục riêng biệt (Ví dụ: Nông Sản, Khoáng Sản, Huyền Thoại) hiển thị gọn gàng trong GUI.
- **Tiến Trình Cấp Độ (Leveling System):** Tích hợp hệ thống thăng cấp độ cho người chơi, từ đó mở khóa các nhiệm vụ khó hơn với phần thưởng cao hơn. File `levels.yml` được cấu hình toán học hỗ trợ lên tới 1000 cấp độ.
- **Tích Hợp MMOItems & Vouchers:** Hỗ trợ thu thập và trả thưởng bằng các vật phẩm xịn từ hệ thống `MMOItems` hoặc phát phiếu thưởng của plugin `Vouchers`.
- **Cơ Chế Chờ (Wait Time):** Sau khi nộp vật phẩm, người chơi phải chờ một thời gian nhất định để nhận phần thưởng.
- **Quyền Lợi VIP:** Hỗ trợ giảm thời gian chờ dựa trên permission VIP và cho phép mở rộng số lượng nhiệm vụ được nhận cùng lúc.
- **GUI Tối Ưu:** Giao diện trực quan tích hợp với `GMCore`, bọc kính đen xịn xò.
- **Lưu Trữ SQLite:** Sử dụng Database cục bộ nhẹ và nhanh để lưu trữ dữ liệu người chơi.

## 📥 Tương Thích
- **Minecraft Version:** 1.21
- **Plugin phụ thuộc (Bắt buộc):** `GMCore`
- **Plugin hỗ trợ (Khuyên dùng):** `MMOItems`, `Vouchers`

## ⌨ Câu Lệnh (Commands)

| Lệnh | Alias | Quyền (Permission) | Chức năng |
|---|---|---|---|
| `/giaohang` | `/gh` | Không | Mở giao diện Danh mục nhiệm vụ giao hàng. |
| `/giaohang reload` | `/gh reload` | `gmdelivery.admin` | Tải lại tất cả các file cấu hình. |

## 🛡 Quyền (Permissions)

| Permission | Mô tả |
|---|---|
| `gmdelivery.admin` | Dành cho Admin để dùng lệnh `/giaohang reload`. |
| `gmdelivery.time.<% giảm giá>` | Giảm phần trăm thời gian chờ nhận thưởng (Ví dụ: `gmdelivery.time.10` giảm 10% thời gian chờ). |
| `gmdelivery.tasks.<số lượng>` | Cho phép người chơi nhận nhiều hơn 1 nhiệm vụ cùng lúc (Ví dụ: `gmdelivery.tasks.3` cho phép nhận 3 nhiệm vụ song song từ các danh mục khác nhau). |

## 🛠 Hướng Dẫn Tạo Nhiệm Vụ (Tasks)

Các nhiệm vụ được chia thành nhiều file `.yml` nằm trong thư mục `plugins/GMDelivery/tasks/`.

Ví dụ một file `tasks/huyen_thoai.yml`:
```yaml
tasks:
  "task_1":
    category: "huyen_thoai" # Phải trùng với khóa trong categories.yml
    name: "&3Cửa Hàng Trao Đổi Voucher"
    lore:
      - "&7Chợ đen đang cần thu mua Voucher."
    level_required: 12
    wait_time_seconds: 1800
    items:
      # Định dạng: LOẠI:ID:SỐ_LƯỢNG
      - "VANILLA:APPLE:10"
      - "MMOITEMS:SWORD:STEEL_SWORD:1"
      - "VOUCHERS:gold_voucher:3"
    rewards:
      xp: 600
      commands:
        - "give %player% netherite_ingot 5"
      vouchers:
        - "legendary_voucher:2"
```

## ⚙ Hướng Dẫn Phân Loại Danh Mục (Categories)

Bạn có thể chỉnh sửa các biểu tượng danh mục xuất hiện ở Menu chính tại file `categories.yml`:

```yaml
categories:
  nong_san:
    display-name: "&a🌾 Nông Sản"
    icon: WHEAT
    slot: 11
    lore:
      - "&7▶ &fNhóm nhiệm vụ liên quan đến"
      - "&fthu hoạch nông sản."
```

---
**Tác giả:** Gemix

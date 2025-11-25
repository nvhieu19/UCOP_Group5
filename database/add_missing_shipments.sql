-- =========================================================
-- SCRIPT THÊM VẬN CHUYỂN CHO CÁC ĐƠN HÀNG ĐÃ THANH TOÁN
-- =========================================================

USE `ucop_project`;

-- Xóa các vận chuyển cũ (nếu muốn reset)
-- DELETE FROM shipments;
-- ALTER TABLE shipments AUTO_INCREMENT = 1;

-- =========================================================
-- THÊM VẬN CHUYỂN CHO CÁC ĐƠN HÀNG PAID/SHIPPED
-- Format mã vận đơn: SHIP-ORD{OrderID}-{RandomNumber}
-- =========================================================

-- Order 18: PAID
INSERT INTO shipments (tracking_number, shipping_method, status, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD18-00001', 'Standard', 'PREPARING', 'Địa chỉ chưa xác định', NOW(), 18, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 18);

-- Order 19: SHIPPED
INSERT INTO shipments (tracking_number, shipping_method, status, shipped_date, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD19-00002', 'Express', 'SHIPPED', '2025-11-26 10:00:00', 'Địa chỉ chưa xác định', NOW(), 19, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 19);

-- Order 20: PAID
INSERT INTO shipments (tracking_number, shipping_method, status, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD20-00003', 'Standard', 'PREPARING', 'Địa chỉ chưa xác định', NOW(), 20, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 20);

-- Order 23: PAID
INSERT INTO shipments (tracking_number, shipping_method, status, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD23-00004', 'Standard', 'PREPARING', 'Địa chỉ chưa xác định', NOW(), 23, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 23);

-- Order 24: SHIPPED
INSERT INTO shipments (tracking_number, shipping_method, status, shipped_date, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD24-00005', 'Express', 'SHIPPED', '2025-11-26 11:00:00', 'Địa chỉ chưa xác định', NOW(), 24, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 24);

-- Order 25: PAID
INSERT INTO shipments (tracking_number, shipping_method, status, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD25-00006', 'Standard', 'PREPARING', 'Địa chỉ chưa xác định', NOW(), 25, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 25);

-- Order 26: PAID
INSERT INTO shipments (tracking_number, shipping_method, status, address, created_at, order_id, staff_id)
SELECT 'SHIP-ORD26-00007', 'Standard', 'PREPARING', 'Địa chỉ chưa xác định', NOW(), 26, 1
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE order_id = 26);


-- =========================================================
-- KIỂM TRA KẾT QUẢ
-- =========================================================

-- Xem tất cả vận chuyển
SELECT COUNT(*) as total_shipments FROM shipments;
SELECT shipment_id, tracking_number, status, order_id, created_at FROM shipments ORDER BY order_id;

-- Xem các đơn hàng có vận chuyển
SELECT 
  o.order_id, 
  o.status as order_status, 
  COUNT(s.shipment_id) as shipment_count
FROM orders o
LEFT JOIN shipments s ON o.order_id = s.order_id
WHERE o.status IN ('PAID', 'SHIPPED')
GROUP BY o.order_id, o.status
ORDER BY o.order_id;

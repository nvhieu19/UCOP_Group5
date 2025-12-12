-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: ucop_project
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(50) NOT NULL,
  `performed_by` varchar(50) DEFAULT NULL,
  `target_table` varchar(50) DEFAULT NULL,
  `description` text,
  `timestamp` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:02:47'),(2,'CREATE','amdin','users','Created new user: hieucus','2025-11-26 03:04:26'),(3,'UPDATE','amdin','users','Updated user info: khach_hang_giau_co','2025-11-26 03:04:35'),(4,'UPDATE','amdin','users','Updated user info: khach_hang_vip','2025-11-26 03:04:38'),(5,'UPDATE','amdin','users','Updated user info: khach_hang_vip','2025-11-26 03:04:40'),(6,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 03:04:49'),(7,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:05:47'),(8,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:17:33'),(9,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 03:17:39'),(10,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 03:20:38'),(11,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 03:36:13'),(12,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:37:13'),(13,'CREATE','amdin','users','Created new user: hieustaff','2025-11-26 03:37:56'),(14,'LOGIN','hieustaff','users','User logged in successfully','2025-11-26 03:38:04'),(15,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:38:15'),(16,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:47:47'),(17,'LOGIN','amdin','users','User logged in successfully','2025-11-26 03:52:06'),(18,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 03:52:50'),(19,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 03:55:30'),(20,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:07:55'),(21,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:13:50'),(22,'UPDATE','amdin','users','Updated user info: khach_hang_giau_co','2025-11-26 04:13:54'),(23,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:17:54'),(24,'DELETE','amdin','users','Deleted user: khach_hang_giau_co','2025-11-26 04:18:01'),(25,'LOGIN','hieustaff','users','User logged in successfully','2025-11-26 04:18:18'),(26,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:30:01'),(27,'LOGIN','hieucus','users','User logged in successfully','2025-11-26 04:30:17'),(28,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:30:29'),(29,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:33:41'),(30,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:36:14'),(31,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:39:17'),(32,'LOGIN','amdin','users','User logged in successfully','2025-11-26 04:53:37'),(33,'LOGIN','amdin','users','User logged in successfully','2025-11-26 05:02:32'),(34,'LOGIN','amdin','users','User logged in successfully','2025-11-26 05:09:07');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  PRIMARY KEY (`category_id`),
  KEY `FKsaok720gsu4u2wrgbk10b5n8d` (`parent_id`),
  CONSTRAINT `FKsaok720gsu4u2wrgbk10b5n8d` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Do dien tu cac loai','Dien Tu',NULL),(2,'May tinh xach tay','Laptop',1),(3,'Smartphone','Dien Thoai',NULL),(4,'Phuong tien di lai','Xe Co',1),(5,'Gaming Laptop','Laptop',NULL),(6,'1','1',3);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `sku` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stock_quantity` int DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `category_id` bigint NOT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `UK_6ekhs0v78950udvne2fj7y2ee` (`sku`),
  KEY `FKjcdcde7htb3tyjgouo4g9xbmr` (`category_id`),
  CONSTRAINT `FKjcdcde7htb3tyjgouo4g9xbmr` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES (8,'Iphone 15',20500000.00,'IP15','ACTIVE',54,'Cái',1,''),(9,'Samsung s25',24940000.00,'SS25','ACTIVE',36,'Cái',1,''),(11,'Iphoen 17 Pro Max',45310000.00,'IP17PRM','ACTIVE',22,'Cái',1,''),(12,'Iphone 177',24670000.00,'IP177','ACTIVE',14,'Cái',1,'');
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(19,2) DEFAULT NULL,
  `quantity` int NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK88tn2oqcxl1034banqif9r70x` (`item_id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  CONSTRAINT `FK88tn2oqcxl1034banqif9r70x` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (16,24940000.00,1,9,16),(17,20500000.00,1,8,17),(18,20500000.00,1,8,18),(19,20500000.00,1,8,19),(20,20500000.00,1,8,19),(21,24940000.00,1,9,20),(22,24940000.00,1,9,20),(23,20500000.00,1,8,21),(24,24940000.00,1,9,22),(25,24940000.00,1,9,23),(26,24940000.00,1,9,24),(27,24940000.00,1,9,25),(28,20500000.00,1,8,26),(29,45310000.00,1,11,27);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` decimal(19,2) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `discountAmount` decimal(19,2) DEFAULT NULL,
  `promotionCode` varchar(255) DEFAULT NULL,
  `shippingFee` decimal(19,2) DEFAULT NULL,
  `subTotal` decimal(19,2) DEFAULT NULL,
  `taxAmount` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (2,'2025-11-26 05:01:16.000000','PAID',100000000.00,2,NULL,NULL,NULL,NULL,NULL),(3,'2025-11-26 05:02:12.000000','PAID',150000000.00,3,NULL,NULL,NULL,NULL,NULL),(4,'2025-11-26 05:02:12.000000','PLACED',200000000.00,4,NULL,NULL,NULL,NULL,NULL),(5,'2025-11-26 05:02:12.000000','PLACED',175000000.00,4,NULL,NULL,NULL,NULL,NULL),(16,'2025-11-25 14:35:55.320985','PLACED',27434000.00,7,0.00,NULL,0.00,24940000.00,2494000.00),(17,'2025-11-25 14:36:39.005507','PLACED',22550000.00,7,0.00,NULL,0.00,20500000.00,2050000.00),(18,'2025-11-25 14:46:14.050305','PAID',22550000.00,5,0.00,NULL,0.00,20500000.00,2050000.00),(19,'2025-11-25 14:47:45.203074','SHIPPED',45100000.00,5,0.00,NULL,0.00,41000000.00,4100000.00),(20,'2025-11-25 14:48:02.944253','PAID',54868000.00,5,0.00,NULL,0.00,49880000.00,4988000.00),(21,'2025-11-26 03:03:28.985729','SHIPPED',22550000.00,5,0.00,NULL,0.00,20500000.00,2050000.00),(22,'2025-11-26 03:36:24.511753','PLACED',27434000.00,8,0.00,NULL,0.00,24940000.00,2494000.00),(23,'2025-11-26 03:49:37.474656','PAID',27434000.00,5,0.00,NULL,0.00,24940000.00,2494000.00),(24,'2025-11-26 03:55:37.284483','SHIPPED',27464000.00,8,0.00,NULL,30000.00,24940000.00,2494000.00),(25,'2025-11-26 04:07:59.618187','PAID',17464000.00,5,10000000.00,'Long123',30000.00,24940000.00,2494000.00),(26,'2025-11-26 04:53:48.255033','PAID',22580000.00,5,0.00,NULL,30000.00,20500000.00,2050000.00),(27,'2025-11-26 05:09:26.174830','COD_PENDING',49871000.00,5,0.00,NULL,30000.00,45310000.00,4531000.00);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,2) DEFAULT NULL,
  `payment_date` datetime(6) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK81gagumt0r8y3rmudcgpbk42l` (`order_id`),
  CONSTRAINT `FK81gagumt0r8y3rmudcgpbk42l` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (11,54868000.00,'2025-11-25 14:52:22.411709','WALLET','PENDING',20),(12,24835000.00,'2025-11-26 03:04:00.307423','BANK_TRANSFER','PENDING',21),(13,49640000.00,'2025-11-26 03:49:12.309456','BANK_TRANSFER','PENDING',19),(14,30240400.00,'2025-11-26 03:55:55.405180','WALLET_QR','PENDING',24),(15,22550000.00,'2025-11-26 04:08:17.553501','BANK_TRANSFER','PENDING',18),(16,27434000.00,'2025-11-26 04:54:03.074539','WALLET_QR','PENDING',23),(17,22580000.00,'2025-11-26 04:54:06.524618','WALLET_QR','PENDING',26),(18,17464000.00,'2025-11-26 05:02:49.281927','WALLET_QR','PENDING',25);
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `discountType` varchar(255) DEFAULT NULL,
  `discountValue` double NOT NULL,
  `endDate` date DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_jdho73ymbyu46p2hh562dk4kk` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
INSERT INTO `promotions` VALUES (1,'SUMMER2025','FIXED',500000,'2025-12-24','2025-11-24'),(2,'Long123','FIXED',10000000,'2025-12-25','2025-11-25');
/*!40000 ALTER TABLE `promotions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN'),(3,'CUSTOMER'),(2,'STAFF');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipments`
--

DROP TABLE IF EXISTS `shipments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipments` (
  `shipment_id` bigint NOT NULL AUTO_INCREMENT,
  `tracking_number` varchar(255) NOT NULL,
  `shipping_method` varchar(100) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'PREPARING',
  `shipped_date` datetime(6) DEFAULT NULL,
  `delivery_date` datetime(6) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `order_id` bigint NOT NULL,
  `staff_id` bigint DEFAULT NULL,
  PRIMARY KEY (`shipment_id`),
  UNIQUE KEY `UK_tracking_number` (`tracking_number`),
  KEY `FK_order_id` (`order_id`),
  KEY `FK_staff_id` (`staff_id`),
  CONSTRAINT `FK_shipment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE RESTRICT,
  CONSTRAINT `FK_shipment_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipments`
--

LOCK TABLES `shipments` WRITE;
/*!40000 ALTER TABLE `shipments` DISABLE KEYS */;
INSERT INTO `shipments` VALUES (1,'SHIP-A1B2C3D4','Standard','IN_TRANSIT','2025-11-25 08:30:00.000000',NULL,'123 Đường Nguyễn Huệ, TP.HCM','2025-11-26 05:02:12.000000',2,1),(2,'SHIP-E5F6G7H8','Express','DELIVERED','2025-11-24 10:00:00.000000','2025-11-25 15:30:00.000000','456 Đường Lê Lợi, Hà Nội','2025-11-26 05:02:12.000000',3,1),(3,'SHIP-I9J0K1L2','Standard','PREPARING',NULL,NULL,'789 Đường Trần Hưng Đạo, Đà Nẵng','2025-11-26 05:02:12.000000',4,1),(4,'SHIP-M3N4O5P6','Express','PREPARING','2025-11-25 12:00:00.000000',NULL,'321 Đường Pasteur, TP.HCM','2025-11-26 05:02:12.000000',5,1);
/*!40000 ALTER TABLE `shipments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_items`
--

DROP TABLE IF EXISTS `stock_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `on_hand` int DEFAULT NULL,
  `reserved` int NOT NULL,
  `item_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kgm4wvwoiqj9r8hrqc0cfeyvi` (`item_id`),
  KEY `FKd27qtvqm0n1g5uuh8sfwqh1lk` (`warehouse_id`),
  CONSTRAINT `FKd27qtvqm0n1g5uuh8sfwqh1lk` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouses` (`warehouse_id`),
  CONSTRAINT `FKq2sfm6dowjmqlt38goxaxrxwb` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_items`
--

LOCK TABLES `stock_items` WRITE;
/*!40000 ALTER TABLE `stock_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `stock_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,1),(2,1),(5,1),(9,2),(6,3),(8,3);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2025-11-24 13:28:02.012000','pass123','ACTIVE','dinh_admin_v2'),(2,'2025-11-24 13:34:17.820000','123','ACTIVE','khach_hang_vip'),(3,'2025-11-26 05:02:12.000000','123','ACTIVE','khach_hang_giau_co'),(4,'2025-11-24 13:39:37.313000','pass123','ACTIVE','vip_member'),(5,'2025-11-24 14:48:15.339738','123','ACTIVE','amdin'),(6,'2025-11-24 18:54:33.493486','123','ACTIVE','long123'),(7,'2025-11-25 13:22:21.838126','123','ACTIVE','guest'),(8,'2025-11-26 03:04:25.897026','123','ACTIVE','hieucus'),(9,'2025-11-26 03:37:55.503734','123','ACTIVE','hieustaff');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallets`
--

DROP TABLE IF EXISTS `wallets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `balance` decimal(19,2) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sswfdl9fq40xlkove1y5kc7kv` (`user_id`),
  CONSTRAINT `FKc1foyisidw7wqqrkamafuwn4e` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallets`
--

LOCK TABLES `wallets` WRITE;
/*!40000 ALTER TABLE `wallets` DISABLE KEYS */;
INSERT INTO `wallets` VALUES (7,3477654011.00,5),(8,12282071923.00,8);
/*!40000 ALTER TABLE `wallets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warehouses`
--

DROP TABLE IF EXISTS `warehouses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouses` (
  `warehouse_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warehouses`
--

LOCK TABLES `warehouses` WRITE;
/*!40000 ALTER TABLE `warehouses` DISABLE KEYS */;
/*!40000 ALTER TABLE `warehouses` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-26  5:11:18

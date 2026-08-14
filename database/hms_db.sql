-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: hms_db
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `role_id` bigint unsigned NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','BLOCKED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_accounts_role` (`role_id`),
  KEY `idx_accounts_phone` (`phone`),
  CONSTRAINT `fk_accounts_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (1,1,'System Admin','admin@hms.com','0900000001','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42'),(2,2,'Nguyen Van An','customer1@gmail.com','0900000002','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42'),(3,2,'Tran Minh Anh','customer2@gmail.com','0900000003','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42'),(4,3,'Le Thu Ha','receptionist@hms.com','0900000004','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42'),(5,4,'Pham Thi Lan','housekeeping1@hms.com','0900000005','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42'),(6,4,'Nguyen Duc Minh','housekeeping2@hms.com','0900000006','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42'),(7,5,'Hoang Van Nam','manager@hms.com','0900000007','pbkdf2_sha256$120000$ZwhKm/wJSRSgoGVtMMX8Ww==$dI3RLmmwnvBiZp8oYncNaULGnnIxf8EyfQ57sG9KMy4=','ACTIVE','2026-08-14 08:59:23','2026-08-14 19:58:42');
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_guests`
--

DROP TABLE IF EXISTS `booking_guests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_guests` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_id` bigint unsigned NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `identity_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `is_primary_guest` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_booking_guests_booking` (`booking_id`),
  CONSTRAINT `fk_booking_guests_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_guests`
--

LOCK TABLES `booking_guests` WRITE;
/*!40000 ALTER TABLE `booking_guests` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_guests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_rooms`
--

DROP TABLE IF EXISTS `booking_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_rooms` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_id` bigint unsigned NOT NULL,
  `room_id` bigint unsigned NOT NULL,
  `price_per_night` decimal(15,2) NOT NULL,
  `number_of_nights` int unsigned NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_booking_room` (`booking_id`,`room_id`),
  KEY `idx_booking_rooms_booking` (`booking_id`),
  KEY `idx_booking_rooms_room` (`room_id`),
  CONSTRAINT `fk_booking_rooms_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_booking_rooms_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_booking_room_nights` CHECK ((`number_of_nights` > 0)),
  CONSTRAINT `chk_booking_room_price` CHECK ((`price_per_night` >= 0)),
  CONSTRAINT `chk_booking_room_subtotal` CHECK ((`subtotal` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_rooms`
--

LOCK TABLES `booking_rooms` WRITE;
/*!40000 ALTER TABLE `booking_rooms` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_id` bigint unsigned DEFAULT NULL,
  `promotion_id` bigint unsigned DEFAULT NULL,
  `booking_source` enum('ONLINE','RECEPTION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `check_in_datetime` datetime NOT NULL,
  `check_out_datetime` datetime NOT NULL,
  `total_room_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_service_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_damage_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status` enum('PENDING_PAYMENT','CONFIRMED','CHECKED_IN','CHECKOUT_PENDING','CHECKED_OUT','CANCELLED','NO_SHOW') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_PAYMENT',
  `cancellation_reason` text COLLATE utf8mb4_unicode_ci,
  `cancelled_at` datetime DEFAULT NULL,
  `created_by` bigint unsigned DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `booking_code` (`booking_code`),
  KEY `fk_bookings_created_by` (`created_by`),
  KEY `idx_bookings_customer` (`customer_id`),
  KEY `idx_bookings_status` (`status`),
  KEY `idx_bookings_datetime` (`check_in_datetime`,`check_out_datetime`),
  KEY `fk_booking_promotion` (`promotion_id`),
  CONSTRAINT `fk_booking_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_bookings_created_by` FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_bookings_customer` FOREIGN KEY (`customer_id`) REFERENCES `accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_booking_dates` CHECK ((`check_out_date` > `check_in_date`)),
  CONSTRAINT `chk_booking_datetimes` CHECK ((`check_out_datetime` > `check_in_datetime`)),
  CONSTRAINT `chk_booking_money` CHECK (((`total_room_amount` >= 0) and (`total_service_amount` >= 0) and (`total_damage_amount` >= 0) and (`discount_amount` >= 0) and (`total_amount` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `check_ins`
--

DROP TABLE IF EXISTS `check_ins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `check_ins` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_id` bigint unsigned NOT NULL,
  `checked_in_by` bigint unsigned NOT NULL,
  `actual_check_in_time` datetime NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `booking_id` (`booking_id`),
  KEY `fk_checkin_checked_by` (`checked_in_by`),
  CONSTRAINT `fk_checkin_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_checkin_checked_by` FOREIGN KEY (`checked_in_by`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `check_ins`
--

LOCK TABLES `check_ins` WRITE;
/*!40000 ALTER TABLE `check_ins` DISABLE KEYS */;
/*!40000 ALTER TABLE `check_ins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `check_outs`
--

DROP TABLE IF EXISTS `check_outs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `check_outs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_id` bigint unsigned NOT NULL,
  `checked_out_by` bigint unsigned NOT NULL,
  `actual_check_out_time` datetime NOT NULL,
  `final_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `note` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `booking_id` (`booking_id`),
  KEY `fk_checkout_checked_by` (`checked_out_by`),
  CONSTRAINT `fk_checkout_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_checkout_checked_by` FOREIGN KEY (`checked_out_by`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_checkout_final_amount` CHECK ((`final_amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `check_outs`
--

LOCK TABLES `check_outs` WRITE;
/*!40000 ALTER TABLE `check_outs` DISABLE KEYS */;
/*!40000 ALTER TABLE `check_outs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `checkin_equipment_snapshots`
--

DROP TABLE IF EXISTS `checkin_equipment_snapshots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `checkin_equipment_snapshots` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `check_in_id` bigint unsigned NOT NULL,
  `booking_room_id` bigint unsigned NOT NULL,
  `room_equipment_id` bigint unsigned NOT NULL,
  `initial_status` enum('NORMAL','DAMAGED','MISSING','WAITING_REPAIR','WAITING_REPLACEMENT','MAINTENANCE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `initial_quantity` int unsigned NOT NULL DEFAULT '1',
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_checkin_snapshot` (`check_in_id`,`booking_room_id`,`room_equipment_id`),
  KEY `fk_snapshot_room_equipment` (`room_equipment_id`),
  KEY `idx_snapshot_booking_room` (`booking_room_id`),
  CONSTRAINT `fk_snapshot_booking_room` FOREIGN KEY (`booking_room_id`) REFERENCES `booking_rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_snapshot_checkin` FOREIGN KEY (`check_in_id`) REFERENCES `check_ins` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_snapshot_room_equipment` FOREIGN KEY (`room_equipment_id`) REFERENCES `room_equipment` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_snapshot_quantity` CHECK ((`initial_quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `checkin_equipment_snapshots`
--

LOCK TABLES `checkin_equipment_snapshots` WRITE;
/*!40000 ALTER TABLE `checkin_equipment_snapshots` DISABLE KEYS */;
/*!40000 ALTER TABLE `checkin_equipment_snapshots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `damage_reports`
--

DROP TABLE IF EXISTS `damage_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `damage_reports` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `inspection_item_id` bigint unsigned NOT NULL,
  `booking_id` bigint unsigned NOT NULL,
  `room_equipment_id` bigint unsigned NOT NULL,
  `damage_type` enum('DAMAGED','MISSING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `compensation_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `charge_status` enum('PENDING','CHARGED','PAID','WAIVED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `note` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `inspection_item_id` (`inspection_item_id`),
  KEY `idx_damage_booking` (`booking_id`),
  KEY `idx_damage_equipment` (`room_equipment_id`),
  KEY `idx_damage_charge_status` (`charge_status`),
  CONSTRAINT `fk_damage_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_damage_inspection_item` FOREIGN KEY (`inspection_item_id`) REFERENCES `inspection_items` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_damage_room_equipment` FOREIGN KEY (`room_equipment_id`) REFERENCES `room_equipment` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_damage_compensation_amount` CHECK ((`compensation_amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `damage_reports`
--

LOCK TABLES `damage_reports` WRITE;
/*!40000 ALTER TABLE `damage_reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `damage_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipment`
--

DROP TABLE IF EXISTS `equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `default_compensation_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  CONSTRAINT `chk_equipment_compensation_price` CHECK ((`default_compensation_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment`
--

LOCK TABLES `equipment` WRITE;
/*!40000 ALTER TABLE `equipment` DISABLE KEYS */;
INSERT INTO `equipment` VALUES (1,'TV','Smart TV',5000000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(2,'Hair Dryer','Hair dryer for guest use',500000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(3,'Kettle','Electric kettle',400000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(4,'Remote Control','TV remote control',200000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(5,'Air Conditioner','Air conditioning unit',7000000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(6,'Mini Fridge','Mini refrigerator',3000000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(7,'Bedside Lamp','Bedside lamp',500000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(8,'Bath Towel','Hotel bath towel',150000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23');
/*!40000 ALTER TABLE `equipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipment_maintenance_logs`
--

DROP TABLE IF EXISTS `equipment_maintenance_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipment_maintenance_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `housekeeping_task_id` bigint unsigned DEFAULT NULL,
  `room_equipment_id` bigint unsigned NOT NULL,
  `damage_report_id` bigint unsigned DEFAULT NULL,
  `action_type` enum('REPAIR','REPLACE','RESTORE','REMOVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `previous_status` enum('NORMAL','DAMAGED','MISSING','WAITING_REPAIR','WAITING_REPLACEMENT','MAINTENANCE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `new_status` enum('NORMAL','DAMAGED','MISSING','WAITING_REPAIR','WAITING_REPLACEMENT','MAINTENANCE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `proof_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confirmed_by` bigint unsigned NOT NULL,
  `confirmed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_maintenance_task` (`housekeeping_task_id`),
  KEY `idx_maintenance_equipment` (`room_equipment_id`),
  KEY `idx_maintenance_damage` (`damage_report_id`),
  KEY `idx_maintenance_confirmed_by` (`confirmed_by`),
  CONSTRAINT `fk_maintenance_confirmed_by` FOREIGN KEY (`confirmed_by`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_maintenance_damage` FOREIGN KEY (`damage_report_id`) REFERENCES `damage_reports` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_maintenance_equipment` FOREIGN KEY (`room_equipment_id`) REFERENCES `room_equipment` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_maintenance_task` FOREIGN KEY (`housekeeping_task_id`) REFERENCES `housekeeping_tasks` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment_maintenance_logs`
--

LOCK TABLES `equipment_maintenance_logs` WRITE;
/*!40000 ALTER TABLE `equipment_maintenance_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `equipment_maintenance_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedbacks`
--

DROP TABLE IF EXISTS `feedbacks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedbacks` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_id` bigint unsigned NOT NULL,
  `customer_id` bigint unsigned NOT NULL,
  `rating` tinyint unsigned NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `status` enum('VISIBLE','HIDDEN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'VISIBLE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_feedback_booking_customer` (`booking_id`,`customer_id`),
  KEY `fk_feedback_customer` (`customer_id`),
  CONSTRAINT `fk_feedback_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_feedback_customer` FOREIGN KEY (`customer_id`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_feedback_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedbacks`
--

LOCK TABLES `feedbacks` WRITE;
/*!40000 ALTER TABLE `feedbacks` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedbacks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotel_config`
--

DROP TABLE IF EXISTS `hotel_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotel_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `hotel_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `check_in_time` time NOT NULL DEFAULT '14:00:00',
  `check_out_time` time NOT NULL DEFAULT '12:00:00',
  `same_day_refund_rate` decimal(5,2) NOT NULL DEFAULT '0.00',
  `before_day_refund_rate` decimal(5,2) NOT NULL DEFAULT '100.00',
  `tax_rate` decimal(5,2) NOT NULL DEFAULT '0.00',
  `service_fee_rate` decimal(5,2) NOT NULL DEFAULT '0.00',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_before_day_refund_rate` CHECK ((`before_day_refund_rate` between 0 and 100)),
  CONSTRAINT `chk_same_day_refund_rate` CHECK ((`same_day_refund_rate` between 0 and 100)),
  CONSTRAINT `chk_service_fee_rate` CHECK ((`service_fee_rate` between 0 and 100)),
  CONSTRAINT `chk_tax_rate` CHECK ((`tax_rate` between 0 and 100))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotel_config`
--

LOCK TABLES `hotel_config` WRITE;
/*!40000 ALTER TABLE `hotel_config` DISABLE KEYS */;
INSERT INTO `hotel_config` VALUES (1,'HMS Hotel','Ha Noi, Viet Nam','0123456789','hotel@example.com','14:00:00','12:00:00',0.00,100.00,0.00,0.00,'2026-08-14 08:58:35','2026-08-14 08:58:35');
/*!40000 ALTER TABLE `hotel_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `housekeeping_tasks`
--

DROP TABLE IF EXISTS `housekeeping_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `housekeeping_tasks` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_id` bigint unsigned NOT NULL,
  `booking_room_id` bigint unsigned DEFAULT NULL,
  `room_equipment_id` bigint unsigned DEFAULT NULL,
  `assigned_to` bigint unsigned DEFAULT NULL,
  `task_type` enum('CLEANING','CHECKOUT_INSPECTION','EQUIPMENT_REPAIR','EQUIPMENT_REPLACEMENT','MAINTENANCE_CHECK') COLLATE utf8mb4_unicode_ci NOT NULL,
  `priority` enum('LOW','NORMAL','HIGH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL',
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `note` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `started_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_housekeeping_room_equipment` (`room_equipment_id`),
  KEY `idx_housekeeping_room` (`room_id`),
  KEY `idx_housekeeping_booking_room` (`booking_room_id`),
  KEY `idx_housekeeping_assigned` (`assigned_to`),
  KEY `idx_housekeeping_status` (`status`),
  CONSTRAINT `fk_housekeeping_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_housekeeping_booking_room` FOREIGN KEY (`booking_room_id`) REFERENCES `booking_rooms` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_housekeeping_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_housekeeping_room_equipment` FOREIGN KEY (`room_equipment_id`) REFERENCES `room_equipment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `housekeeping_tasks`
--

LOCK TABLES `housekeeping_tasks` WRITE;
/*!40000 ALTER TABLE `housekeeping_tasks` DISABLE KEYS */;
INSERT INTO `housekeeping_tasks` VALUES (1,7,NULL,31,5,'EQUIPMENT_REPLACEMENT','HIGH','PENDING','Replace damaged hair dryer in Room 203','2026-08-14 08:59:23',NULL,NULL);
/*!40000 ALTER TABLE `housekeeping_tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inspection_items`
--

DROP TABLE IF EXISTS `inspection_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inspection_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `inspection_id` bigint unsigned NOT NULL,
  `room_equipment_id` bigint unsigned NOT NULL,
  `condition_status` enum('NORMAL','DAMAGED','MISSING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int unsigned NOT NULL DEFAULT '1',
  `damage_fee` decimal(15,2) NOT NULL DEFAULT '0.00',
  `note` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inspection_equipment` (`inspection_id`,`room_equipment_id`),
  KEY `fk_inspection_item_equipment` (`room_equipment_id`),
  CONSTRAINT `fk_inspection_item_equipment` FOREIGN KEY (`room_equipment_id`) REFERENCES `room_equipment` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_inspection_item_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `room_inspections` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_inspection_item_damage_fee` CHECK ((`damage_fee` >= 0)),
  CONSTRAINT `chk_inspection_item_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inspection_items`
--

LOCK TABLES `inspection_items` WRITE;
/*!40000 ALTER TABLE `inspection_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `inspection_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice_items`
--

DROP TABLE IF EXISTS `invoice_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint unsigned NOT NULL,
  `damage_report_id` bigint unsigned DEFAULT NULL,
  `item_type` enum('ROOM','SERVICE','DAMAGE','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int unsigned NOT NULL DEFAULT '1',
  `unit_price` decimal(15,2) NOT NULL,
  `total_price` decimal(15,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_invoice_items_invoice` (`invoice_id`),
  KEY `idx_invoice_items_damage` (`damage_report_id`),
  CONSTRAINT `fk_invoice_item_damage` FOREIGN KEY (`damage_report_id`) REFERENCES `damage_reports` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_invoice_item_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_invoice_item_quantity` CHECK ((`quantity` > 0)),
  CONSTRAINT `chk_invoice_item_total_price` CHECK ((`total_price` >= 0)),
  CONSTRAINT `chk_invoice_item_unit_price` CHECK ((`unit_price` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_items`
--

LOCK TABLES `invoice_items` WRITE;
/*!40000 ALTER TABLE `invoice_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoices`
--

DROP TABLE IF EXISTS `invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoices` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `invoice_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `booking_id` bigint unsigned NOT NULL,
  `room_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `service_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `damage_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `tax_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status` enum('UNPAID','PARTIALLY_PAID','PAID','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNPAID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `invoice_code` (`invoice_code`),
  UNIQUE KEY `booking_id` (`booking_id`),
  CONSTRAINT `fk_invoice_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_invoice_amounts` CHECK (((`room_amount` >= 0) and (`service_amount` >= 0) and (`damage_amount` >= 0) and (`discount_amount` >= 0) and (`tax_amount` >= 0) and (`total_amount` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoices`
--

LOCK TABLES `invoices` WRITE;
/*!40000 ALTER TABLE `invoices` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `news`
--

DROP TABLE IF EXISTS `news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumbnail_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('DRAFT','PUBLISHED','HIDDEN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `created_by` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `published_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_news_created_by` (`created_by`),
  KEY `idx_news_status` (`status`),
  CONSTRAINT `fk_news_created_by` FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news`
--

LOCK TABLES `news` WRITE;
/*!40000 ALTER TABLE `news` DISABLE KEYS */;
INSERT INTO `news` VALUES (1,'Welcome to HMS Hotel','Welcome to our hotel. We hope you have a comfortable and enjoyable stay.','/uploads/news/welcome.jpg','PUBLISHED',7,'2026-08-14 08:59:23','2026-08-14 08:59:23','2026-08-14 08:59:23'),(2,'Weekend Promotion','Special weekend room promotion available for selected room types.','/uploads/news/weekend-promotion.jpg','PUBLISHED',7,'2026-08-14 08:59:23','2026-08-14 08:59:23','2026-08-14 08:59:23');
/*!40000 ALTER TABLE `news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `booking_id` bigint unsigned NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `payment_method` enum('CASH','BANK_TRANSFER','CREDIT_CARD','ONLINE_PAYMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_type` enum('DEPOSIT','BOOKING_PAYMENT','FINAL_PAYMENT','DAMAGE_PAYMENT','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PENDING','SUCCESS','FAILED','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `processed_by` bigint unsigned DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_payments_processed_by` (`processed_by`),
  KEY `idx_payments_booking` (`booking_id`),
  KEY `idx_payments_status` (`status`),
  CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_payments_processed_by` FOREIGN KEY (`processed_by`) REFERENCES `accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_payment_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `discount_type` enum('PERCENT','FIXED_AMOUNT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_value` decimal(15,2) NOT NULL,
  `max_discount_amount` decimal(15,2) DEFAULT NULL,
  `min_booking_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `usage_limit` int unsigned DEFAULT NULL,
  `used_count` int unsigned NOT NULL DEFAULT '0',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_by` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `promotions_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`),
  CONSTRAINT `promotions_chk_1` CHECK ((`discount_value` > 0)),
  CONSTRAINT `promotions_chk_2` CHECK ((`end_date` > `start_date`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
/*!40000 ALTER TABLE `promotions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refund_requests`
--

DROP TABLE IF EXISTS `refund_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund_requests` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `refund_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `booking_id` bigint unsigned NOT NULL,
  `customer_id` bigint unsigned NOT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `refund_method` enum('BANK_TRANSFER','ORIGINAL_PAYMENT_METHOD') COLLATE utf8mb4_unicode_ci NOT NULL,
  `bank_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_account_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_account_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paid_amount` decimal(15,2) NOT NULL,
  `refund_rate` decimal(5,2) NOT NULL,
  `refund_amount` decimal(15,2) NOT NULL,
  `status` enum('PENDING','PROCESSING','REFUNDED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `rejection_reason` text COLLATE utf8mb4_unicode_ci,
  `requested_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_by` bigint unsigned DEFAULT NULL,
  `processed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `refund_code` (`refund_code`),
  UNIQUE KEY `booking_id` (`booking_id`),
  KEY `fk_refund_customer` (`customer_id`),
  KEY `fk_refund_processed_by` (`processed_by`),
  KEY `idx_refund_status` (`status`),
  CONSTRAINT `fk_refund_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_refund_customer` FOREIGN KEY (`customer_id`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_refund_processed_by` FOREIGN KEY (`processed_by`) REFERENCES `accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_refund_money` CHECK (((`paid_amount` >= 0) and (`refund_amount` >= 0) and (`refund_amount` <= `paid_amount`))),
  CONSTRAINT `chk_refund_rate` CHECK ((`refund_rate` between 0 and 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refund_requests`
--

LOCK TABLES `refund_requests` WRITE;
/*!40000 ALTER TABLE `refund_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `refund_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refund_transactions`
--

DROP TABLE IF EXISTS `refund_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund_transactions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `refund_request_id` bigint unsigned NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `transaction_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `proof_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('SUCCESS','FAILED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUCCESS',
  `note` text COLLATE utf8mb4_unicode_ci,
  `processed_by` bigint unsigned NOT NULL,
  `processed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_refund_transaction_processed_by` (`processed_by`),
  KEY `idx_refund_transactions_request` (`refund_request_id`),
  CONSTRAINT `fk_refund_transaction_processed_by` FOREIGN KEY (`processed_by`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_refund_transaction_request` FOREIGN KEY (`refund_request_id`) REFERENCES `refund_requests` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_refund_transaction_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refund_transactions`
--

LOCK TABLES `refund_transactions` WRITE;
/*!40000 ALTER TABLE `refund_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `refund_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN','System administrator','2026-08-14 08:58:35'),(2,'CUSTOMER','Hotel customer','2026-08-14 08:58:35'),(3,'RECEPTIONIST','Hotel receptionist','2026-08-14 08:58:35'),(4,'HOUSEKEEPING','Housekeeping employee','2026-08-14 08:58:35'),(5,'HOTEL_MANAGER','Hotel manager','2026-08-14 08:58:35');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_equipment`
--

DROP TABLE IF EXISTS `room_equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_equipment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_id` bigint unsigned NOT NULL,
  `equipment_id` bigint unsigned NOT NULL,
  `quantity` int unsigned NOT NULL DEFAULT '1',
  `status` enum('NORMAL','DAMAGED','MISSING','WAITING_REPAIR','WAITING_REPLACEMENT','MAINTENANCE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL',
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_by` bigint unsigned DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_equipment` (`room_id`,`equipment_id`),
  KEY `fk_room_equipment_equipment` (`equipment_id`),
  KEY `fk_room_equipment_updated_by` (`updated_by`),
  KEY `idx_room_equipment_room` (`room_id`),
  KEY `idx_room_equipment_status` (`status`),
  CONSTRAINT `fk_room_equipment_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_room_equipment_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_room_equipment_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_room_equipment_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_equipment`
--

LOCK TABLES `room_equipment` WRITE;
/*!40000 ALTER TABLE `room_equipment` DISABLE KEYS */;
INSERT INTO `room_equipment` VALUES (1,1,1,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(2,1,2,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(3,1,3,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(4,1,4,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(5,1,5,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(6,1,7,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(7,1,8,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(8,2,1,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(9,2,2,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(10,2,3,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(11,2,4,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(12,2,5,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(13,2,7,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(14,2,8,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(15,3,1,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(16,3,2,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(17,3,3,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(18,3,4,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(19,3,5,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(20,3,6,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(21,3,7,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(22,3,8,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(23,5,1,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(24,5,2,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(25,5,3,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(26,5,4,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(27,5,5,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(28,5,7,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(29,5,8,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(30,7,1,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(31,7,2,1,'WAITING_REPLACEMENT','Hair dryer damaged by previous guest, waiting replacement',NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(32,7,3,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(33,7,4,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(34,7,5,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(35,7,6,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(36,7,7,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(37,7,8,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(38,9,1,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(39,9,2,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(40,9,3,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(41,9,4,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(42,9,5,2,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(43,9,6,1,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(44,9,7,3,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23'),(45,9,8,4,'NORMAL',NULL,NULL,'2026-08-14 08:59:23','2026-08-14 08:59:23');
/*!40000 ALTER TABLE `room_equipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_inspections`
--

DROP TABLE IF EXISTS `room_inspections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_inspections` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `housekeeping_task_id` bigint unsigned NOT NULL,
  `booking_room_id` bigint unsigned NOT NULL,
  `inspected_by` bigint unsigned NOT NULL,
  `status` enum('PENDING','PASSED','DAMAGE_FOUND') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `note` text COLLATE utf8mb4_unicode_ci,
  `inspected_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `housekeeping_task_id` (`housekeeping_task_id`),
  UNIQUE KEY `booking_room_id` (`booking_room_id`),
  KEY `fk_inspection_inspected_by` (`inspected_by`),
  KEY `idx_inspection_status` (`status`),
  CONSTRAINT `fk_inspection_booking_room` FOREIGN KEY (`booking_room_id`) REFERENCES `booking_rooms` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_inspection_inspected_by` FOREIGN KEY (`inspected_by`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_inspection_task` FOREIGN KEY (`housekeeping_task_id`) REFERENCES `housekeeping_tasks` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_inspections`
--

LOCK TABLES `room_inspections` WRITE;
/*!40000 ALTER TABLE `room_inspections` DISABLE KEYS */;
/*!40000 ALTER TABLE `room_inspections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_types`
--

DROP TABLE IF EXISTS `room_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_types` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `capacity` int unsigned NOT NULL,
  `base_price` decimal(15,2) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  CONSTRAINT `chk_room_type_capacity` CHECK ((`capacity` > 0)),
  CONSTRAINT `chk_room_type_price` CHECK ((`base_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_types`
--

LOCK TABLES `room_types` WRITE;
/*!40000 ALTER TABLE `room_types` DISABLE KEYS */;
INSERT INTO `room_types` VALUES (1,'Standard','Standard room with one queen bed, suitable for 2 guests.',2,800000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(2,'Deluxe','Deluxe room with larger space and premium amenities.',2,1200000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(3,'Twin','Room with two single beds, suitable for 2 guests.',2,1000000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(4,'Suite','Luxury suite with living area and premium amenities.',4,2500000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23'),(5,'Family','Family room suitable for groups or families.',4,1800000.00,'ACTIVE','2026-08-14 08:59:23','2026-08-14 08:59:23');
/*!40000 ALTER TABLE `room_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_type_id` bigint unsigned NOT NULL,
  `room_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `floor_number` int DEFAULT NULL,
  `status` enum('AVAILABLE','OCCUPIED','INSPECTION','CLEANING','NOT_READY','MAINTENANCE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE',
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `room_number` (`room_number`),
  KEY `idx_rooms_room_type` (`room_type_id`),
  KEY `idx_rooms_status` (`status`),
  CONSTRAINT `fk_rooms_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,1,'101',1,'AVAILABLE','Standard room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(2,1,'102',1,'AVAILABLE','Standard room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(3,2,'103',1,'AVAILABLE','Deluxe room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(4,2,'104',1,'MAINTENANCE','Deluxe room currently under maintenance','2026-08-14 08:59:23','2026-08-14 08:59:23'),(5,3,'201',2,'AVAILABLE','Twin room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(6,3,'202',2,'AVAILABLE','Twin room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(7,2,'203',2,'AVAILABLE','Deluxe room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(8,2,'204',2,'CLEANING','Deluxe room currently being cleaned','2026-08-14 08:59:23','2026-08-14 08:59:23'),(9,4,'301',3,'AVAILABLE','Suite room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(10,4,'302',3,'AVAILABLE','Suite room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(11,5,'303',3,'AVAILABLE','Family room','2026-08-14 08:59:23','2026-08-14 08:59:23'),(12,5,'304',3,'NOT_READY','Family room waiting for equipment replacement','2026-08-14 08:59:23','2026-08-14 08:59:23');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-14 23:23:01

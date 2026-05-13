-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: im_finals
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

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
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `adminID` int(11) NOT NULL,
  `approvalCode` varchar(50) NOT NULL,
  PRIMARY KEY (`adminID`),
  CONSTRAINT `fk_admin_systemuser` FOREIGN KEY (`adminID`) REFERENCES `systemuser` (`userID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'ADM-1001'),(2,'ADM-1002');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `approval`
--

DROP TABLE IF EXISTS `approval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval` (
  `leaveRequestID` int(11) NOT NULL,
  `userID` int(11) NOT NULL,
  `approvalDate` date NOT NULL,
  `sequenceNumber` int(11) NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`leaveRequestID`,`userID`),
  KEY `fk_approval_systemuser` (`userID`),
  CONSTRAINT `fk_approval_leaverequest` FOREIGN KEY (`leaveRequestID`) REFERENCES `leaverequest` (`leaveRequestID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_approval_systemuser` FOREIGN KEY (`userID`) REFERENCES `systemuser` (`userID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approval`
--

LOCK TABLES `approval` WRITE;
/*!40000 ALTER TABLE `approval` DISABLE KEYS */;
INSERT INTO `approval` VALUES (1,1,'2026-05-09',1,'Approved'),(1,2,'2026-05-09',2,'Approved'),(2,1,'2026-05-20',1,'Pending'),(3,2,'2026-05-14',1,'Approved');
/*!40000 ALTER TABLE `approval` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `attendanceID` int(11) NOT NULL AUTO_INCREMENT,
  `startDate` date NOT NULL,
  `endDate` date NOT NULL,
  `instructorStatus` varchar(20) NOT NULL,
  `remarks` text DEFAULT NULL,
  `classCode` varchar(20) NOT NULL,
  `actualInstructID` int(11) DEFAULT NULL,
  `leaveRequestID` int(11) DEFAULT NULL,
  `checkedBy` int(11) DEFAULT NULL,
  PRIMARY KEY (`attendanceID`),
  KEY `fk_attendance_classschedule` (`classCode`),
  KEY `fk_attendance_actualinstructor` (`actualInstructID`),
  KEY `fk_attendance_leaverequest` (`leaveRequestID`),
  KEY `fk_attendance_checkedby` (`checkedBy`),
  CONSTRAINT `fk_attendance_actualinstructor` FOREIGN KEY (`actualInstructID`) REFERENCES `instructor` (`instructID`) ON UPDATE CASCADE,
  CONSTRAINT `fk_attendance_checkedby` FOREIGN KEY (`checkedBy`) REFERENCES `systemuser` (`userID`) ON UPDATE CASCADE,
  CONSTRAINT `fk_attendance_classschedule` FOREIGN KEY (`classCode`) REFERENCES `classschedule` (`classCode`) ON UPDATE CASCADE,
  CONSTRAINT `fk_attendance_leaverequest` FOREIGN KEY (`leaveRequestID`) REFERENCES `leaverequest` (`leaveRequestID`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
INSERT INTO `attendance` VALUES (1,'2026-05-10','2026-05-10','Absent','Instructor on approved sick leave','CS101-A',NULL,1,3),(2,'2026-05-11','2026-05-11','Present','Class conducted normally','CS201-A',2,NULL,4),(3,'2026-05-15','2026-05-15','Substituted','Temporary substitute instructor assigned','MATH101-A',1,3,3);
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `checker`
--

DROP TABLE IF EXISTS `checker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `checker` (
  `checkerID` int(11) NOT NULL,
  PRIMARY KEY (`checkerID`),
  CONSTRAINT `fk_checker_systemuser` FOREIGN KEY (`checkerID`) REFERENCES `systemuser` (`userID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `checker`
--

LOCK TABLES `checker` WRITE;
/*!40000 ALTER TABLE `checker` DISABLE KEYS */;
INSERT INTO `checker` VALUES (3),(4);
/*!40000 ALTER TABLE `checker` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `checkerdetails`
--

DROP TABLE IF EXISTS `checkerdetails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `checkerdetails` (
  `checkerID` int(11) NOT NULL,
  `scheduleID` int(11) NOT NULL,
  `shiftStart` time NOT NULL,
  `shiftEnd` time NOT NULL,
  `building` varchar(100) NOT NULL,
  `floor` varchar(20) NOT NULL,
  `day` varchar(20) NOT NULL,
  PRIMARY KEY (`checkerID`,`scheduleID`),
  CONSTRAINT `fk_checkerdetails_checker` FOREIGN KEY (`checkerID`) REFERENCES `checker` (`checkerID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `checkerdetails`
--

LOCK TABLES `checkerdetails` WRITE;
/*!40000 ALTER TABLE `checkerdetails` DISABLE KEYS */;
INSERT INTO `checkerdetails` VALUES (3,1,'07:00:00','12:00:00','Main Building','1st Floor','Monday'),(3,2,'07:00:00','12:00:00','Main Building','1st Floor','Wednesday'),(4,1,'13:00:00','18:00:00','Science Building','2nd Floor','Tuesday'),(4,2,'13:00:00','18:00:00','Science Building','2nd Floor','Thursday');
/*!40000 ALTER TABLE `checkerdetails` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classschedule`
--

DROP TABLE IF EXISTS `classschedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classschedule` (
  `classCode` varchar(20) NOT NULL,
  `courseNo` varchar(20) NOT NULL,
  `startTime` time NOT NULL,
  `endTime` time NOT NULL,
  `days` varchar(50) NOT NULL,
  `instructID` int(11) DEFAULT NULL,
  `roomID` int(11) DEFAULT NULL,
  `assignedChecker` int(11) DEFAULT NULL,
  PRIMARY KEY (`classCode`),
  UNIQUE KEY `instructID_UNIQUE` (`instructID`),
  KEY `fk_classschedule_instructor` (`instructID`),
  KEY `fk_classschedule_room` (`roomID`),
  KEY `fk_classschedule_checker` (`assignedChecker`),
  CONSTRAINT `fk_classschedule_checker` FOREIGN KEY (`assignedChecker`) REFERENCES `checker` (`checkerID`) ON UPDATE CASCADE,
  CONSTRAINT `fk_classschedule_instructor` FOREIGN KEY (`instructID`) REFERENCES `instructor` (`instructID`) ON UPDATE CASCADE,
  CONSTRAINT `fk_classschedule_room` FOREIGN KEY (`roomID`) REFERENCES `room` (`roomID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classschedule`
--

LOCK TABLES `classschedule` WRITE;
/*!40000 ALTER TABLE `classschedule` DISABLE KEYS */;
INSERT INTO `classschedule` VALUES ('CS101-A','CS101','08:00:00','09:30:00','MWF',1,1,3),('CS201-A','CS201','10:00:00','11:30:00','TTH',2,2,4),('ENG101-A','ENG101','15:00:00','16:30:00','TTH',4,3,4),('MATH101-A','MATH101','13:00:00','14:30:00','MWF',3,1,3);
/*!40000 ALTER TABLE `classschedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `departmentID` int(11) NOT NULL AUTO_INCREMENT,
  `departmentName` varchar(100) NOT NULL,
  `school` varchar(100) NOT NULL,
  PRIMARY KEY (`departmentID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (1,'Computer Science','School of Computing'),(2,'Mathematics','School of Science'),(3,'English','School of Arts');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instructor`
--

DROP TABLE IF EXISTS `instructor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instructor` (
  `instructID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `departmentID` int(11) NOT NULL,
  PRIMARY KEY (`instructID`),
  KEY `fk_instructor_department` (`departmentID`),
  CONSTRAINT `fk_instructor_department` FOREIGN KEY (`departmentID`) REFERENCES `department` (`departmentID`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instructor`
--

LOCK TABLES `instructor` WRITE;
/*!40000 ALTER TABLE `instructor` DISABLE KEYS */;
INSERT INTO `instructor` VALUES (1,'Dr. Albert Reyes',1),(2,'Prof. Monica Cruz',1),(3,'Dr. Samuel Lim',2),(4,'Prof. Diana Flores',3);
/*!40000 ALTER TABLE `instructor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leaverequest`
--

DROP TABLE IF EXISTS `leaverequest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leaverequest` (
  `leaveRequestID` int(11) NOT NULL AUTO_INCREMENT,
  `leaveType` varchar(50) NOT NULL,
  `startDate` date NOT NULL,
  `endDate` date NOT NULL,
  `status` varchar(20) NOT NULL,
  `leaveReason` text NOT NULL,
  `instructID` int(11) NOT NULL,
  `approvedBy` int(11) DEFAULT NULL,
  PRIMARY KEY (`leaveRequestID`),
  KEY `fk_leaverequest_instructor` (`instructID`),
  KEY `fk_leave_whoApproved_idx` (`approvedBy`),
  CONSTRAINT `fk_leave_whoApproved` FOREIGN KEY (`approvedBy`) REFERENCES `systemuser` (`userID`) ON UPDATE CASCADE,
  CONSTRAINT `fk_leaverequest_instructor` FOREIGN KEY (`instructID`) REFERENCES `instructor` (`instructID`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leaverequest`
--

LOCK TABLES `leaverequest` WRITE;
/*!40000 ALTER TABLE `leaverequest` DISABLE KEYS */;
INSERT INTO `leaverequest` VALUES (1,'Sick Leave','2026-05-10','2026-05-12','Approved','Flu and fever recovery',1,NULL),(2,'Vacation Leave','2026-06-01','2026-06-05','Pending','Family vacation trip',2,NULL),(3,'Emergency Leave','2026-05-15','2026-05-16','Approved','Family emergency',3,NULL);
/*!40000 ALTER TABLE `leaverequest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `roomID` int(11) NOT NULL AUTO_INCREMENT,
  `floor` varchar(20) NOT NULL,
  `building` varchar(100) NOT NULL,
  `capacity` int(11) NOT NULL,
  `roomType` varchar(50) NOT NULL,
  PRIMARY KEY (`roomID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
INSERT INTO `room` VALUES (1,'1st Floor','Main Building',40,'Lecture Room'),(2,'2nd Floor','Science Building',30,'Laboratory'),(3,'3rd Floor','Arts Building',35,'Lecture Room');
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `systemuser`
--

DROP TABLE IF EXISTS `systemuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `systemuser` (
  `userID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `createdBy` int(11) NOT NULL,
  `departmentID` int(11) DEFAULT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `ak_systemuser_username` (`username`),
  UNIQUE KEY `ak_systemuser_email` (`email`),
  KEY `fk_systemuser_createdby` (`createdBy`),
  KEY `fk_department_idx` (`departmentID`),
  CONSTRAINT `fk_department` FOREIGN KEY (`departmentID`) REFERENCES `department` (`departmentID`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `systemuser`
--

LOCK TABLES `systemuser` WRITE;
/*!40000 ALTER TABLE `systemuser` DISABLE KEYS */;
INSERT INTO `systemuser` VALUES (1,'John Admin','johnadmin','johnadmin@email.com','pass123','ADMIN',0,NULL),(2,'Maria Admin','mariaadmin','mariaadmin@email.com','pass123','ADMIN',1,NULL),(3,'Kevin Checker','kevinchecker','kevinchecker@email.com','pass123','CHECKER',1,NULL),(4,'Anna Checker','annachecker','annachecker@email.com','pass123','CHECKER',1,NULL);
/*!40000 ALTER TABLE `systemuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'im_finals'
--
/*!50003 DROP FUNCTION IF EXISTS `fn_CheckScheduleConflict` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_CheckScheduleConflict`(p_classCode VARCHAR(50),
    p_roomID INT,
    p_instructID INT,
    p_days VARCHAR(10),
    p_start TIME,
    p_end TIME
) RETURNS tinyint(1)
    READS SQL DATA
BEGIN
    DECLARE conflict_count INT;
    SELECT COUNT(*) INTO conflict_count
    FROM classschedule
    WHERE classCode <> p_classCode
      AND ((roomID IS NOT NULL AND roomID = p_roomID) 
           OR (instructID IS NOT NULL AND instructID = p_instructID))
      AND startTime < p_end 
      AND endTime > p_start
      AND fn_DaysOverlap(days, p_days);
    
    RETURN conflict_count > 0;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_DaysOverlap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DaysOverlap`(d1 VARCHAR(10), d2 VARCHAR(10)) RETURNS tinyint(1)
    DETERMINISTIC
BEGIN
    -- Check for Th first to avoid partial matches with T
    IF (d1 LIKE '%Th%' AND d2 LIKE '%Th%') THEN RETURN TRUE; END IF;
    IF (d1 LIKE '%M%' AND d2 LIKE '%M%') THEN RETURN TRUE; END IF;
    IF (d1 LIKE '%W%' AND d2 LIKE '%W%') THEN RETURN TRUE; END IF;
    IF (d1 LIKE '%F%' AND d2 LIKE '%F%') THEN RETURN TRUE; END IF;
    IF (d1 LIKE '%S%' AND d2 LIKE '%S%') THEN RETURN TRUE; END IF;
    -- Check for T by removing Th to ensure it's a standalone Tuesday
    IF (REPLACE(d1, 'Th', '') LIKE '%T%' AND REPLACE(d2, 'Th', '') LIKE '%T%') THEN RETURN TRUE; END IF;
    RETURN FALSE;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_GetInstructorAttendanceRate` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_GetInstructorAttendanceRate`(p_instructID INT) RETURNS decimal(5,2)
    READS SQL DATA
BEGIN
    DECLARE total_classes INT;
    DECLARE present_classes INT;
    
    SELECT COUNT(*) INTO total_classes 
    FROM attendance a JOIN classschedule cs ON a.classCode = cs.classCode 
    WHERE cs.instructID = p_instructID;
    
    IF total_classes = 0 THEN RETURN 0.00; END IF;
    
    SELECT COUNT(*) INTO present_classes 
    FROM attendance a JOIN classschedule cs ON a.classCode = cs.classCode 
    WHERE cs.instructID = p_instructID AND a.instructorStatus = 'Present';
    
    RETURN (present_classes / total_classes) * 100;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_GetRoomDescription` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_GetRoomDescription`(p_roomID INT) RETURNS varchar(255) CHARSET utf8mb4 COLLATE utf8mb4_general_ci
    DETERMINISTIC
BEGIN
    DECLARE v_desc VARCHAR(255);
    SELECT CONCAT(building, ' – ', floor, ' (', roomType, ')') INTO v_desc
    FROM room WHERE roomID = p_roomID;
    RETURN v_desc;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_IsUsernameAvailable` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_IsUsernameAvailable`(p_username VARCHAR(50)) RETURNS tinyint(1)
    DETERMINISTIC
BEGIN
    RETURN (SELECT COUNT(*) FROM systemuser WHERE username = p_username) = 0;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_ValidateAdminApproval` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_ValidateAdminApproval`(p_adminID INT, p_code VARCHAR(50)) RETURNS tinyint(1)
    DETERMINISTIC
BEGIN
    RETURN (SELECT COUNT(*) FROM admin WHERE adminID = p_adminID AND approvalCode = p_code) > 0;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_GetAllAttendance` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_GetAllAttendance`()
BEGIN
SELECT
    a.attendanceID, a.startDate, a.endDate, a.instructorStatus, a.remarks,
    a.classCode, cs.instructID AS assignedInstructID, a.actualInstructID,
    a.leaveRequestID, a.checkedBy
FROM attendance a
         JOIN classschedule cs ON a.classCode = cs.classCode
ORDER BY a.startDate DESC;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_GetAllClassSchedules` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_GetAllClassSchedules`()
BEGIN
SELECT s.*, i.name AS instructorName
FROM   classschedule s
           LEFT JOIN instructor i ON s.instructID = i.instructID
ORDER BY s.startTime;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_GetAttendanceByInstructor` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_GetAttendanceByInstructor`(IN p_instructID INT)
BEGIN
SELECT
    a.attendanceID,
    a.startDate,
    a.endDate,
    a.instructorStatus,
    a.remarks,
    a.classCode,
    cs.instructID AS assignedInstructID,
    a.actualInstructID,
    a.leaveRequestID,
    a.checkedBy
FROM attendance a
         JOIN classschedule cs ON a.classCode = cs.classCode
WHERE cs.instructID = p_instructID
ORDER BY a.startDate DESC;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_GetClassSchedulesByDept` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_GetClassSchedulesByDept`(IN p_deptID INT)
BEGIN
SELECT s.*, i.name AS instructorName
FROM   classschedule s
           LEFT JOIN instructor i ON s.instructID = i.instructID
WHERE  i.departmentID = p_deptID OR s.instructID IS NULL
ORDER BY s.startTime;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_SyncLeaveToAttendance` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_SyncLeaveToAttendance`(IN p_leaveRequestID INT)
BEGIN
    DECLARE v_start DATE;
    DECLARE v_end DATE;
    DECLARE v_instructID INT;
    DECLARE v_curr DATE;

    SELECT startDate, endDate, instructID INTO v_start, v_end, v_instructID
    FROM leaverequest WHERE leaveRequestID = p_leaveRequestID;

    SET v_curr = v_start;

    WHILE v_curr <= v_end DO
        -- Map day of week to your letter system (M, T, W, Th, F, S)
        SET @dayLetter = CASE DAYOFWEEK(v_curr)
            WHEN 2 THEN 'M' WHEN 3 THEN 'T' WHEN 4 THEN 'W'
            WHEN 5 THEN 'Th' WHEN 6 THEN 'F' WHEN 7 THEN 'S'
            ELSE '' END;

        INSERT INTO attendance (classCode, instructID, startDate, endDate, instructorStatus, leaveRequestID)
        SELECT classCode, v_instructID, v_curr, v_curr, 'Absent', p_leaveRequestID
        FROM classschedule
        WHERE instructID = v_instructID AND days LIKE CONCAT('%', @dayLetter, '%')
        ON DUPLICATE KEY UPDATE 
            instructorStatus = 'Absent', 
            leaveRequestID = p_leaveRequestID;

        SET v_curr = DATE_ADD(v_curr, INTERVAL 1 DAY);
    END WHILE;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_UpsertAttendance` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_UpsertAttendance`(
    IN p_classCode VARCHAR(50),
    IN p_instructID INT,
    IN p_date DATE,
    IN p_status VARCHAR(50),
    IN p_checkerID INT
)
BEGIN
    INSERT INTO attendance (classCode, instructID, startDate, endDate, instructorStatus, checkedBy)
    VALUES (p_classCode, p_instructID, p_date, p_date, p_status, p_checkerID)
    ON DUPLICATE KEY UPDATE 
        instructorStatus = VALUES(instructorStatus),
        checkedBy = VALUES(checkedBy);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-13 23:14:21

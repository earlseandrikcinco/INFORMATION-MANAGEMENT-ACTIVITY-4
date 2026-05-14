-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: final_final_final_schema
-- ------------------------------------------------------
-- Server version	8.4.7

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
  `adminID` int NOT NULL,
  `approvalCode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `leaveRequestID` int NOT NULL,
  `userID` int NOT NULL,
  `approvalDate` date NOT NULL,
  `sequenceNumber` int NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `attendanceID` int NOT NULL AUTO_INCREMENT,
  `startDate` date NOT NULL,
  `endDate` date NOT NULL,
  `instructorStatus` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `classCode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `actualInstructID` int DEFAULT NULL,
  `leaveRequestID` int DEFAULT NULL,
  `checkedBy` int DEFAULT NULL,
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
  `checkerID` int NOT NULL,
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
  `checkerID` int NOT NULL,
  `scheduleID` int NOT NULL,
  `shiftStart` time NOT NULL,
  `shiftEnd` time NOT NULL,
  `building` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `day` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `classCode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `courseNo` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `startTime` time NOT NULL,
  `endTime` time NOT NULL,
  `days` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `instructID` int DEFAULT NULL,
  `roomID` int DEFAULT NULL,
  `assignedChecker` int DEFAULT NULL,
  PRIMARY KEY (`classCode`),
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
INSERT INTO `classschedule` VALUES ('5656','360','07:00:00','08:00:00','MWF',1,2,NULL),('CS101-A','CS101','08:00:00','09:30:00','MWF',1,1,3),('CS201-A','CS201','10:00:00','11:30:00','TTH',2,2,4),('ENG101-A','ENG101','15:00:00','16:30:00','TTH',NULL,3,3),('MATH101-A','MATH101','13:00:00','14:30:00','MWF',3,1,3),('nicewan','4444','10:00:00','11:00:00','MWF',NULL,2,NULL);
/*!40000 ALTER TABLE `classschedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `departmentID` int NOT NULL AUTO_INCREMENT,
  `departmentName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `instructID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `departmentID` int NOT NULL,
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
  `leaveRequestID` int NOT NULL AUTO_INCREMENT,
  `leaveType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `startDate` date NOT NULL,
  `endDate` date NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `leaveReason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `instructID` int NOT NULL,
  `approvedBy` int DEFAULT NULL,
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
  `roomID` int NOT NULL AUTO_INCREMENT,
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `building` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `capacity` int NOT NULL,
  `roomType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `userID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `createdBy` int NOT NULL,
  `departmentID` int DEFAULT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `ak_systemuser_username` (`username`),
  UNIQUE KEY `ak_systemuser_email` (`email`),
  KEY `fk_systemuser_createdby` (`createdBy`),
  KEY `fk_department_idx` (`departmentID`),
  CONSTRAINT `fk_department` FOREIGN KEY (`departmentID`) REFERENCES `department` (`departmentID`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `systemuser`
--

LOCK TABLES `systemuser` WRITE;
/*!40000 ALTER TABLE `systemuser` DISABLE KEYS */;
INSERT INTO `systemuser` VALUES (1,'John Admin','johnadmin','johnadmin@email.com','pass123','ADMIN',0,NULL),(2,'Maria Admin','mariaadmin','mariaadmin@email.com','pass123','ADMIN',1,NULL),(3,'Kevin Checker','kevinchecker','kevinchecker@email.com','pass123','CHECKER',1,NULL),(4,'Anna Checker','annachecker','annachecker@email.com','pass123','CHECKER',1,NULL),(5,'Harrison Ford','harryforddeptcs','harrythebaddie@slu.edu.ph','pass123','DEPTHEAD',1,NULL),(6,'Sara Bellum','MissBellum','powerpuffsec@gmail.com','pass123','SECRETARY',1,NULL),(7,'Robert Robertson','mechaman','dispatch@gmail.com','pass123','DEPTHEAD',1,NULL);
/*!40000 ALTER TABLE `systemuser` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-14 18:49:26

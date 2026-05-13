DELIMITER $$

DROP PROCEDURE IF EXISTS sp_GetAllClassSchedules$$
CREATE PROCEDURE sp_GetAllClassSchedules()
BEGIN
SELECT s.*, i.name AS instructorName
FROM   classschedule s
           LEFT JOIN instructor i ON s.instructID = i.instructID
ORDER BY s.startTime;
END$$

DROP PROCEDURE IF EXISTS sp_GetClassSchedulesByDept$$
CREATE PROCEDURE sp_GetClassSchedulesByDept(IN p_deptID INT)
BEGIN
SELECT s.*, i.name AS instructorName
FROM   classschedule s
           LEFT JOIN instructor i ON s.instructID = i.instructID
WHERE  i.departmentID = p_deptID OR s.instructID IS NULL
ORDER BY s.startTime;
END$$

DROP PROCEDURE IF EXISTS sp_GetAttendanceByInstructor$$
CREATE PROCEDURE sp_GetAttendanceByInstructor(IN p_instructID INT)
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
END$$

DROP PROCEDURE IF EXISTS sp_GetAllAttendance$$
CREATE PROCEDURE sp_GetAllAttendance()
BEGIN
SELECT
    a.attendanceID, a.startDate, a.endDate, a.instructorStatus, a.remarks,
    a.classCode, cs.instructID AS assignedInstructID, a.actualInstructID,
    a.leaveRequestID, a.checkedBy
FROM attendance a
         JOIN classschedule cs ON a.classCode = cs.classCode
ORDER BY a.startDate DESC;
END$$

DELIMITER ;
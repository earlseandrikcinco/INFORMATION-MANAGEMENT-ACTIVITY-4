DELIMITER $$

-- 1. All class schedules (used by getAllClassSchedules)
DROP PROCEDURE IF EXISTS sp_GetAllClassSchedules$$
CREATE PROCEDURE sp_GetAllClassSchedules()
BEGIN
    SELECT s.*, i.name AS instructorName
    FROM   classschedule s
    LEFT JOIN instructor i ON s.instructID = i.instructID
    ORDER BY s.startTime;
END$$

-- 2. Schedules filtered by department (used by getClassSchedulesByDept)
DROP PROCEDURE IF EXISTS sp_GetClassSchedulesByDept$$
CREATE PROCEDURE sp_GetClassSchedulesByDept(IN p_deptID INT)
BEGIN
    SELECT s.*, i.name AS instructorName
    FROM   classschedule s
    LEFT JOIN instructor i ON s.instructID = i.instructID
    WHERE  i.departmentID = p_deptID
       OR  s.instructID IS NULL
    ORDER BY s.startTime;
END$$

-- 3. Schedules filtered by room (used by getClassSchedulesByRoom)
DROP PROCEDURE IF EXISTS sp_GetClassSchedulesByRoom$$
CREATE PROCEDURE sp_GetClassSchedulesByRoom(IN p_roomID INT)
BEGIN
    SELECT s.*, i.name AS instructorName
    FROM   classschedule s
    LEFT JOIN instructor i ON s.instructID = i.instructID
    WHERE  s.roomID = p_roomID
    ORDER BY FIELD(s.days, 'M', 'T', 'W', 'Th', 'F', 'S'), s.startTime;
END$$

-- 4. Schedules filtered by instructor (used by getClassSchedulesByInstructor)
DROP PROCEDURE IF EXISTS sp_GetClassSchedulesByInstructor$$
CREATE PROCEDURE sp_GetClassSchedulesByInstructor(IN p_instructID INT)
BEGIN
    SELECT s.*, i.name AS instructorName
    FROM   classschedule s
    LEFT JOIN instructor i ON s.instructID = i.instructID
    WHERE  s.instructID = p_instructID
    ORDER BY FIELD(s.days, 'M', 'T', 'W', 'Th', 'F', 'S'), s.startTime;
END$$

-- 5. Schedules filtered by day code and time range (used by getClassSchedulesByTimeRange)
DROP PROCEDURE IF EXISTS sp_GetClassSchedulesByTimeRange$$
CREATE PROCEDURE sp_GetClassSchedulesByTimeRange(
    IN p_dayCode  VARCHAR(20),
    IN p_start    TIME,
    IN p_end      TIME
)
BEGIN
    SELECT s.*, i.name AS instructorName
    FROM   classschedule s
    LEFT JOIN instructor i ON s.instructID = i.instructID
    WHERE  s.days       REGEXP p_dayCode
      AND  s.startTime >= p_start
      AND  s.endTime   <= p_end
    ORDER BY s.startTime;
END$$

DELIMITER ;

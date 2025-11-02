INSERT INTO tb_student (
    student_name,
    student_grade, student_class, student_number,
    student_email,
    student_major,
    student_major_club_id,
    student_job_club_id,
    student_autonomous_club_id,
    room_number,
    student_role,
    student_is_leave_school,
    student_sex
) VALUES
      ('김철수', 1, 1, 1, 'kim.cs@school.edu', 'SW_DEVELOPMENT', NULL, NULL, NULL, 101, 'GENERAL_STUDENT', false, 'MAN'),
      ('이영희', 1, 1, 2, 'lee.yh@school.edu', 'AI', NULL, NULL, NULL, 205, 'GENERAL_STUDENT', false, 'WOMAN'),
      ('박민수', 2, 2, 3, 'park.ms@school.edu', 'SMART_IOT', NULL, NULL, NULL, 302, 'STUDENT_COUNCIL', false, 'MAN'),
      ('정수연', 1, 3, 4, 'jung.sy@school.edu', 'AI', NULL, NULL, NULL, 150, 'GENERAL_STUDENT', false, 'WOMAN'),
      ('최준호', 3, 1, 5, 'choi.jh@school.edu', 'SW_DEVELOPMENT', NULL, NULL, NULL, 401, 'STUDENT_COUNCIL', false, 'MAN'),
      ('한지민', 2, 2, 6, 'han.jm@school.edu', 'SMART_IOT', NULL, NULL, NULL, 203, 'GENERAL_STUDENT', true, 'WOMAN'),
      ('윤태영', 1, 1, 7, 'yoon.ty@school.edu', 'AI', NULL, NULL, NULL, 320, 'GENERAL_STUDENT', false, 'MAN'),
      ('김소희', 3, 3, 8, 'kim.sh@school.edu', 'SMART_IOT', NULL, NULL, NULL, 105, 'MEDIA_DEPARTMENT', false, 'WOMAN'),
      ('서동현', 2, 1, 9, 'seo.dh@school.edu', 'SW_DEVELOPMENT', NULL, NULL, NULL, 425, 'DORMITORY_MANAGER', false, 'MAN'),
      ('송미래', 1, 2, 10, 's24058@gsm.hs.kr', 'AI', NULL, NULL, NULL, 230, 'LIBRARY_MANAGER', false, 'WOMAN');

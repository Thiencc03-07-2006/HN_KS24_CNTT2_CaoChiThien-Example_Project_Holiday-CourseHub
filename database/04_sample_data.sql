-- ============================================================
-- CourseHub - SAMPLE DATA HOÀN CHỈNH (v1.0)
-- Tương thích: MySQL 8.x, Entity JPA, Enum Backend
-- Mục đích: INSERT dữ liệu mẫu - KHÔNG tạo bảng, KHÔNG sửa schema
-- ============================================================
-- THỨ TỰ INSERT:
--   1. roles
--   2. users + user_roles
--   3. instructor_profiles
--   4. categories
--   5. courses
--   6. chapters + lessons + lesson_resources
--   7. quiz_configs + questions + answers
--   8. enrollments
--   9. progress + quiz_attempts
--  10. reviews
--  11. comments
--  12. notifications
--  13. wishlists + favorites
--  14. reports
--  15. course_approval_history
--  16. orders + order_items
-- ============================================================
-- TÀI KHOẢN TEST:
--   admin@coursehub.com         / Admin@123456
--   instructor1@coursehub.com   / Test@123456
--   instructor2@coursehub.com   / Test@123456
--   instructor3@coursehub.com   / Test@123456
--   instructor4@coursehub.com   / Test@123456
--   instructor5@coursehub.com   / Test@123456
--   student1-20@coursehub.com   / Test@123456
-- BCrypt rounds=10
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. ROLES
-- ============================================================
INSERT IGNORE INTO roles (name, description) VALUES
    ('ROLE_STUDENT',    'Hoc vien - dang ky va hoc khoa hoc'),
    ('ROLE_INSTRUCTOR', 'Giang vien - tao va quan ly khoa hoc'),
    ('ROLE_ADMIN',      'Quan tri vien - toan quyen he thong');

-- ============================================================
-- 2. USERS (Admin)
-- BCrypt: Admin@123456 = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ============================================================
INSERT IGNORE INTO users (id, email, password_hash, full_name, phone_number, avatar_url, bio, status, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000001',
 'admin@coursehub.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'CourseHub Admin', '0900000001',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
 'Quan tri vien he thong CourseHub. Phu trach kiem duyet noi dung va quan ly nguoi dung.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 365 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- 2. USERS (Instructors x5)
-- BCrypt: Test@123456 = $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- ============================================================
INSERT IGNORE INTO users (id, email, password_hash, full_name, phone_number, avatar_url, bio, status, created_at, updated_at) VALUES
('a56e8cdf-80bb-11f1-8183-de8e3dc1070d',
 'instructor1@coursehub.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'Nguyen Van Minh', '0901234001',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor1',
 'Senior Full-Stack Developer voi 8 nam kinh nghiem. Chuyen mon Java, Spring Boot, React, AWS. Da dung nhan qua nhan qua cho nhieu du an lon.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 300 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

('00000000-0000-0000-0000-000000000022',
 'instructor2@coursehub.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'Tran Thi Lan', '0901234002',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor2',
 'Mobile Developer chuyen sau ve Flutter va React Native. Da ra mat 10+ app tren AppStore va CHPlay.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 280 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

('00000000-0000-0000-0000-000000000023',
 'instructor3@coursehub.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'Le Duc Thanh', '0901234003',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor3',
 'DevOps & Cloud Expert. 6 nam lam viec tai cac cong ty Silicon Valley. Chuyen mon Docker, Kubernetes, AWS, CI/CD.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 260 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

('00000000-0000-0000-0000-000000000024',
 'instructor4@coursehub.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'Pham Thi Hong', '0901234004',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor4',
 'AI/ML Engineer, Tien si Khoa Hoc May Tinh. Giang vien Dai hoc Bach Khoa Ha Noi. Nghien cuu Deep Learning va Computer Vision.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 240 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

('00000000-0000-0000-0000-000000000025',
 'instructor5@coursehub.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'Vo Thanh Tung', '0901234005',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor5',
 'Database Architect voi 12 nam kinh nghiem. Chuyen gia MySQL, PostgreSQL, MongoDB, Redis. Co nhieu chung chi Oracle va AWS.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 220 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY));

-- ============================================================
-- 2. USERS (Students x20)
-- ============================================================
INSERT IGNORE INTO users (id, email, password_hash, full_name, phone_number, avatar_url, bio, status, created_at, updated_at) VALUES
('00000000-0000-0000-0001-000000000001','student1@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Hoang Van An','0911111001','https://api.dicebear.com/7.x/avataaars/svg?seed=s1','Sinh vien nam 3 CNTT, muon hoc them de thuc tap.','ACTIVE',DATE_SUB(NOW(),INTERVAL 200 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY)),
('00000000-0000-0000-0001-000000000002','student2@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Pham Thi Bich','0911111002','https://api.dicebear.com/7.x/avataaars/svg?seed=s2','Ke toan muon chuyen sang IT. Dang hoc Python de lam data analyst.','ACTIVE',DATE_SUB(NOW(),INTERVAL 190 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY)),
('00000000-0000-0000-0001-000000000003','student3@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Vo Minh Duc','0911111003','https://api.dicebear.com/7.x/avataaars/svg?seed=s3','Fresher Java 6 thang kinh nghiem. Muc tieu tro thanh Senior trong 3 nam.','ACTIVE',DATE_SUB(NOW(),INTERVAL 180 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY)),
('00000000-0000-0000-0001-000000000004','student4@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Nguyen Thi Mai','0911111004','https://api.dicebear.com/7.x/avataaars/svg?seed=s4','Designer muon hoc code de lam full-stack. Yeu thich React va TypeScript.','ACTIVE',DATE_SUB(NOW(),INTERVAL 170 DAY),DATE_SUB(NOW(),INTERVAL 4 DAY)),
('00000000-0000-0000-0001-000000000005','student5@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Tran Van Khanh','0911111005','https://api.dicebear.com/7.x/avataaars/svg?seed=s5','Backend developer 2 nam. Muon hoc them ve DevOps va Cloud.','ACTIVE',DATE_SUB(NOW(),INTERVAL 160 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY)),
('00000000-0000-0000-0001-000000000006','student6@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Le Thi Thu','0911111006','https://api.dicebear.com/7.x/avataaars/svg?seed=s6','Giao vien pho thong muon hoc lap trinh. Bat dau tu Python.','ACTIVE',DATE_SUB(NOW(),INTERVAL 150 DAY),DATE_SUB(NOW(),INTERVAL 6 DAY)),
('00000000-0000-0000-0001-000000000007','student7@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Nguyen Hoang Long','0911111007','https://api.dicebear.com/7.x/avataaars/svg?seed=s7','Mobile developer Android 1 nam. Muon chuyen sang Flutter.','ACTIVE',DATE_SUB(NOW(),INTERVAL 140 DAY),DATE_SUB(NOW(),INTERVAL 7 DAY)),
('00000000-0000-0000-0001-000000000008','student8@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Dao Thi Huong','0911111008','https://api.dicebear.com/7.x/avataaars/svg?seed=s8','Frontend developer React 1 nam. Muon hoc Next.js va TypeScript nang cao.','ACTIVE',DATE_SUB(NOW(),INTERVAL 130 DAY),DATE_SUB(NOW(),INTERVAL 8 DAY)),
('00000000-0000-0000-0001-000000000009','student9@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Bui Van Hieu','0911111009','https://api.dicebear.com/7.x/avataaars/svg?seed=s9','QA Engineer muon hoc automation testing voi Java Selenium.','ACTIVE',DATE_SUB(NOW(),INTERVAL 120 DAY),DATE_SUB(NOW(),INTERVAL 9 DAY)),
('00000000-0000-0000-0001-000000000010','student10@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Luu Thi Nga','0911111010','https://api.dicebear.com/7.x/avataaars/svg?seed=s10','Product Manager muon hoc SQL de tu query database khong can nho dev.','ACTIVE',DATE_SUB(NOW(),INTERVAL 110 DAY),DATE_SUB(NOW(),INTERVAL 10 DAY)),
('00000000-0000-0000-0001-000000000011','student11@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Do Van Cuong','0911111011','https://api.dicebear.com/7.x/avataaars/svg?seed=s11','System Admin muon hoc Docker va Kubernetes de quan ly server hieu qua hon.','ACTIVE',DATE_SUB(NOW(),INTERVAL 100 DAY),DATE_SUB(NOW(),INTERVAL 11 DAY)),
('00000000-0000-0000-0001-000000000012','student12@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Nguyen Thi Lan Anh','0911111012','https://api.dicebear.com/7.x/avataaars/svg?seed=s12','Sinh vien KTPM nam 4. Dang tim kiem cong ty thuc tap Java.','ACTIVE',DATE_SUB(NOW(),INTERVAL 90 DAY),DATE_SUB(NOW(),INTERVAL 12 DAY)),
('00000000-0000-0000-0001-000000000013','student13@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Tran Quoc Bao','0911111013','https://api.dicebear.com/7.x/avataaars/svg?seed=s13','Entrepreneur muon hoc AI de ung dung vao startup cua minh.','ACTIVE',DATE_SUB(NOW(),INTERVAL 80 DAY),DATE_SUB(NOW(),INTERVAL 13 DAY)),
('00000000-0000-0000-0001-000000000014','student14@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Phan Thi Kim Oanh','0911111014','https://api.dicebear.com/7.x/avataaars/svg?seed=s14','Nha bao muon hoc Data Journalism va Python de phan tich du lieu.','ACTIVE',DATE_SUB(NOW(),INTERVAL 70 DAY),DATE_SUB(NOW(),INTERVAL 14 DAY)),
('00000000-0000-0000-0001-000000000015','student15@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Ha Minh Tri','0911111015','https://api.dicebear.com/7.x/avataaars/svg?seed=s15','Game developer Unity muon hoc them backend va REST API.','ACTIVE',DATE_SUB(NOW(),INTERVAL 60 DAY),DATE_SUB(NOW(),INTERVAL 15 DAY)),
('00000000-0000-0000-0001-000000000016','student16@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Nguyen Bao Chau','0911111016','https://api.dicebear.com/7.x/avataaars/svg?seed=s16','Chuyen vien ngan hang muon hoc Python de lam financial analysis.','ACTIVE',DATE_SUB(NOW(),INTERVAL 50 DAY),DATE_SUB(NOW(),INTERVAL 16 DAY)),
('00000000-0000-0000-0001-000000000017','student17@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Ly Van Son','0911111017','https://api.dicebear.com/7.x/avataaars/svg?seed=s17','Sinh vien moi ra truong nganh kinh te. Muon hoc IT de doi nganh.','ACTIVE',DATE_SUB(NOW(),INTERVAL 40 DAY),DATE_SUB(NOW(),INTERVAL 17 DAY)),
('00000000-0000-0000-0001-000000000018','student18@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Tran Ngoc Hoa','0911111018','https://api.dicebear.com/7.x/avataaars/svg?seed=s18','Marketing Manager muon hoc Digital Marketing va SEO.','ACTIVE',DATE_SUB(NOW(),INTERVAL 30 DAY),DATE_SUB(NOW(),INTERVAL 18 DAY)),
('00000000-0000-0000-0001-000000000019','student19@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Nguyen Duc Manh','0911111019','https://api.dicebear.com/7.x/avataaars/svg?seed=s19','Backend Go developer muon hoc them Java Spring Boot.','ACTIVE',DATE_SUB(NOW(),INTERVAL 20 DAY),DATE_SUB(NOW(),INTERVAL 19 DAY)),
('00000000-0000-0000-0001-000000000020','student20@coursehub.com','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Le Thi Cam Tu','0911111020','https://api.dicebear.com/7.x/avataaars/svg?seed=s20','Ky su xay dung muon hoc BIM va lap trinh AutoCAD.','ACTIVE',DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_SUB(NOW(),INTERVAL 20 DAY));

-- ============================================================
-- 3. USER_ROLES
-- ============================================================
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000001', id FROM roles WHERE name='ROLE_ADMIN';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000001', id FROM roles WHERE name='ROLE_STUDENT';

INSERT IGNORE INTO user_roles (user_id, role_id) SELECT 'a56e8cdf-80bb-11f1-8183-de8e3dc1070d', id FROM roles WHERE name='ROLE_INSTRUCTOR';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT 'a56e8cdf-80bb-11f1-8183-de8e3dc1070d', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000022', id FROM roles WHERE name='ROLE_INSTRUCTOR';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000022', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000023', id FROM roles WHERE name='ROLE_INSTRUCTOR';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000023', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000024', id FROM roles WHERE name='ROLE_INSTRUCTOR';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000024', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000025', id FROM roles WHERE name='ROLE_INSTRUCTOR';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0000-000000000025', id FROM roles WHERE name='ROLE_STUDENT';

INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000001', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000002', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000003', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000004', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000005', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000006', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000007', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000008', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000009', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000010', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000011', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000012', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000013', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000014', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000015', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000016', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000017', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000018', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000019', id FROM roles WHERE name='ROLE_STUDENT';
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT '00000000-0000-0000-0001-000000000020', id FROM roles WHERE name='ROLE_STUDENT';

-- ============================================================
-- 4. INSTRUCTOR_PROFILES
-- ============================================================
INSERT IGNORE INTO instructor_profiles (id, user_id, headline, detailed_bio, website_url, linkedin_url, payout_bank_name, payout_account_number, payout_account_name, total_students, total_courses, average_rating, created_at, updated_at) VALUES
('aa000000-0000-0000-0000-000000000001','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',
 'Senior Full-Stack Developer | Java & React | 8+ nam kinh nghiem',
 'Toi la Nguyen Van Minh, ky su phan mem voi hon 8 nam kinh nghiem. Toi da lam viec tai cac cong ty cong nghe hang dau nhu FPT Software, VNG va startup Silicon Valley. Chuyen mon cua toi la xay dung he thong backend voi Java Spring Boot, microservices, va frontend hien dai voi React. Toi tin rang lap trinh la nghe thuat - code phai vua chay tot vua de doc. Moi khoa hoc cua toi deu tap trung vao thuc hanh thuc te qua cac du an that su.',
 'https://minhdev.io', 'https://linkedin.com/in/nguyenvanminh-dev',
 'Vietcombank', '0123456789', 'NGUYEN VAN MINH',
 1250, 6, 4.75, DATE_SUB(NOW(), INTERVAL 300 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

('aa000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000022',
 'Mobile Developer | Flutter & React Native | iOS & Android',
 'Tran Thi Lan, 6 nam phat trien ung dung mobile chuyen nghiep. Toi da phat hanh hon 10 ung dung tren AppStore va CHPlay, dat tong cong hon 500.000 luot tai. Chuyen mon cua toi la Flutter (Dart), React Native va cac tich hop native iOS/Android. Toi tap trung day thuc hanh - moi hoc vien se hoan thanh it nhat 2 ung dung that su truoc khi ket thuc khoa hoc.',
 'https://landeveloper.me', 'https://linkedin.com/in/tranthi-lan-mobile',
 'Techcombank', '9876543210', 'TRAN THI LAN',
 890, 4, 4.60, DATE_SUB(NOW(), INTERVAL 280 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

('aa000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000023',
 'DevOps & Cloud Engineer | Docker | Kubernetes | AWS Certified',
 'Le Duc Thanh, 6 nam kinh nghiem DevOps va Cloud tai cac cong ty o San Francisco va Ha Noi. Hien la AWS Certified Solutions Architect va Kubernetes Administrator. Toi da build va van hanh nhieu he thong xu ly hang trieu request/ngay. Chuyen mon: Docker, Kubernetes, Terraform, CI/CD voi Jenkins/GitLab. Khoa hoc cua toi tap trung 70% thuc hanh tren moi truong that su.',
 'https://thanhdevops.com', 'https://linkedin.com/in/leducthanh-devops',
 'BIDV', '1122334455', 'LE DUC THANH',
 780, 3, 4.82, DATE_SUB(NOW(), INTERVAL 260 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

('aa000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000024',
 'AI/ML Engineer | PhD CNTT | Deep Learning | Computer Vision',
 'Pham Thi Hong, Tien si Khoa Hoc May Tinh tai Dai hoc Bach Khoa Ha Noi. Hien la nghien cuu sinh post-doc va giang vien chinh thuc ve AI/ML. Da cong bo 15+ bai bao khoa hoc tren cac tap chi quoc te. Chuyen mon: TensorFlow, PyTorch, Deep Learning, Computer Vision, NLP. Khoa hoc cua toi ket hop ly thuyet vung chac voi ung dung thuc tien.',
 'https://aihong.edu.vn', 'https://linkedin.com/in/phamthihong-ai',
 'Vietcombank', '5544332211', 'PHAM THI HONG',
 560, 3, 4.70, DATE_SUB(NOW(), INTERVAL 240 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

('aa000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000025',
 'Database Architect | MySQL | PostgreSQL | Redis | 12 nam',
 'Vo Thanh Tung, 12 nam kinh nghiem ve database design va optimization. Da tu van cho 50+ du an lon nho trong nuoc va quoc te. Co chung chi Oracle Certified Professional, AWS Database Specialty. Chuyen mon: MySQL, PostgreSQL, MongoDB, Redis, Elasticsearch. Toi day cach thiet ke database dung tu dau - vi sua sau rat ton kem.',
 'https://tungdba.pro', 'https://linkedin.com/in/vothanhtung-dba',
 'ACB', '6677889900', 'VO THANH TUNG',
 420, 3, 4.55, DATE_SUB(NOW(), INTERVAL 220 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ============================================================
-- 5. CATEGORIES (Cap 1 - 5 danh muc cha)
-- ============================================================
INSERT IGNORE INTO categories (id, parent_id, name, slug, icon, description) VALUES
(1, NULL, 'Cong Nghe Thong Tin', 'cong-nghe-thong-tin', 'laptop', 'Lap trinh, phat trien phan mem, DevOps, Database, AI/ML'),
(2, NULL, 'Kinh Doanh', 'kinh-doanh', 'briefcase', 'Quan tri, Marketing, Tai chinh, Khoi nghiep'),
(3, NULL, 'Thiet Ke', 'thiet-ke', 'palette', 'Do hoa, UI/UX, Motion, Branding'),
(4, NULL, 'Ngoai Ngu', 'ngoai-ngu', 'globe', 'Tieng Anh, Tieng Nhat, Tieng Han, Tieng Trung'),
(5, NULL, 'Ky Nang Mem', 'ky-nang-mem', 'users', 'Giao tiep, Lanh dao, Tu duy, Quan ly thoi gian');

INSERT IGNORE INTO categories (id, parent_id, name, slug, icon, description) VALUES
(11, 1, 'Lap Trinh Web Frontend', 'lap-trinh-web-frontend', 'code', 'HTML, CSS, JavaScript, React, Vue, Angular, TypeScript'),
(12, 1, 'Lap Trinh Backend', 'lap-trinh-backend', 'server', 'Java, Spring Boot, Node.js, Python Django, Go, PHP'),
(13, 1, 'Mobile Development', 'mobile-development', 'smartphone', 'Flutter, React Native, iOS Swift, Android Kotlin'),
(14, 1, 'DevOps va Cloud', 'devops-cloud', 'cloud', 'Docker, Kubernetes, AWS, GCP, Azure, CI/CD, Terraform'),
(15, 1, 'Database', 'database', 'database', 'MySQL, PostgreSQL, MongoDB, Redis, Elasticsearch, Oracle'),
(16, 1, 'AI va Machine Learning', 'ai-machine-learning', 'cpu', 'Python AI, TensorFlow, PyTorch, NLP, Computer Vision, LLM'),
(17, 1, 'Bao Mat Mang', 'bao-mat-mang', 'shield', 'Cybersecurity, Ethical Hacking, OWASP, Penetration Testing'),
(21, 2, 'Marketing Digital', 'marketing-digital', 'trending-up', 'SEO, SEM, Google Ads, Facebook Ads, Content Marketing'),
(22, 2, 'Quan Tri Du An', 'quan-tri-du-an', 'clipboard', 'PMP, Agile, Scrum, Kanban, JIRA');


-- ============================================================
-- 7. COURSES (30 khoa hoc)
-- instructor1 = a56e8cdf-80bb-11f1-8183-de8e3dc1070d (Nguyen Van Minh)
-- instructor2 = 00000000-0000-0000-0000-000000000022 (Tran Thi Lan)
-- instructor3 = 00000000-0000-0000-0000-000000000023 (Le Duc Thanh)
-- instructor4 = 00000000-0000-0000-0000-000000000024 (Pham Thi Hong)
-- instructor5 = 00000000-0000-0000-0000-000000000025 (Vo Thanh Tung)
-- ============================================================

-- 20 PUBLISHED courses -------------------------------------------

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000001','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',12,
'Java Spring Boot tu Trang Thai Den Nang Cao','java-spring-boot-tu-trang-thai-den-nang-cao',
'Hoc toan bo Spring Boot 3.x: REST API, Security JWT, JPA, Microservices. Thuc hanh 3 du an that su.',
'Khoa hoc Java Spring Boot day du nhat. Ban se xay dung he thong backend hoan chinh voi REST API chuan, JWT Authentication, Spring Security, Spring Data JPA, Flyway Migration, Docker va deploy len AWS. Moi chuong co bai tap va project thuc te.',
1590000.00,'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.75,187,DATE_SUB(NOW(),INTERVAL 180 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000002','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',11,
'React 18 va Next.js 14 Chuyen Sau','react-18-nextjs-14-chuyen-sau',
'Lam chu React 18 Hooks, Next.js 14 App Router, TypeScript, Tailwind. Xay dung 4 du an thuc te.',
'Khoa hoc React & Next.js day du: Hooks, Context, Redux Toolkit, React Query. Next.js 14 App Router, Server Components, SSR/SSG. TypeScript strict mode, Tailwind CSS. 4 du an thuc te: E-commerce, Blog, Dashboard, Social App.',
1890000.00,'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.80,312,DATE_SUB(NOW(),INTERVAL 160 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000022',13,
'Flutter 3.x Phat Trien App iOS va Android','flutter-3x-phat-trien-app-ios-android',
'Xay dung app iOS va Android voi Flutter 3 va Dart. Tu Widget co ban den Bloc, REST API, Firebase.',
'Flutter day du: Dart, Widget, State Management Bloc/Provider, Navigation 2.0, REST API, Firebase, Local DB. 3 du an cuoi khoa: E-commerce App, Chat App, Task Manager.',
1290000.00,'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800',
'BEGINNER','Vietnamese','PUBLISHED',4.60,95,DATE_SUB(NOW(),INTERVAL 150 DAY),DATE_SUB(NOW(),INTERVAL 10 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000023',14,
'Docker va Kubernetes Tu Co Ban Den Production','docker-kubernetes-tu-co-ban-den-production',
'Container hoa ung dung voi Docker. Orchestration voi Kubernetes. CI/CD. Deploy microservices that su.',
'Docker va Kubernetes day du: Image, Container, Dockerfile, Compose, K8s Pod/Service/Deployment/Ingress, Helm, Monitoring Prometheus/Grafana, CI/CD Jenkins/GitLab. Thuc hanh deploy he thong microservices tren AWS EKS.',
1990000.00,'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.85,267,DATE_SUB(NOW(),INTERVAL 140 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000024',16,
'Machine Learning va Deep Learning voi Python','machine-learning-deep-learning-python',
'Hoc AI/ML tu dau: Scikit-learn, TensorFlow, PyTorch. Build model that su. Computer Vision va NLP.',
'AI/ML toan dien: NumPy, Pandas, Scikit-learn. Deep Learning: CNN, RNN, LSTM, Transformer. TensorFlow 2 va PyTorch. Ung dung: phan loai anh, nhan dien giong noi, chatbot. 5 du an AI hoan chinh.',
2290000.00,'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=800',
'ADVANCED','Vietnamese','PUBLISHED',4.70,154,DATE_SUB(NOW(),INTERVAL 130 DAY),DATE_SUB(NOW(),INTERVAL 4 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000025',15,
'MySQL Nang Cao: Toi Uu Hieu Nang va Kien Truc','mysql-nang-cao-toi-uu-hieu-nang',
'Master MySQL 8.x: Query optimization, Index, Partition, Replication, Backup, Security.',
'MySQL Expert: EXPLAIN, Index strategy, Partition, Stored Procedure, Trigger, Replication Master-Slave, Backup Percona XtraBackup, Performance tuning OLTP/OLAP.',
1490000.00,'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800',
'ADVANCED','Vietnamese','PUBLISHED',4.55,88,DATE_SUB(NOW(),INTERVAL 120 DAY),DATE_SUB(NOW(),INTERVAL 7 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000007','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',11,
'TypeScript Full-Stack: Node.js va React','typescript-fullstack-nodejs-react',
'Xay dung full-stack voi TypeScript. Node.js Express backend, React frontend, MongoDB. Clone Trello.',
'TypeScript strict mode. Backend Node.js + Express + Prisma + MongoDB. Frontend React 18 + Zustand + TanStack Query. Socket.io real-time. Testing Jest. Deploy Docker + Railway + Vercel.',
1690000.00,'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.65,143,DATE_SUB(NOW(),INTERVAL 110 DAY),DATE_SUB(NOW(),INTERVAL 6 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000008','00000000-0000-0000-0000-000000000022',13,
'React Native: Cross-Platform Mobile','react-native-cross-platform-mobile',
'Phat trien iOS va Android cung mot codebase. Expo Router, Redux, Animations, Payment Stripe.',
'React Native: JSX, Flexbox, Core Components. Expo Router, React Navigation v6. Redux Toolkit. Reanimated 3. Camera, Location, Push Notification. Payment Stripe. Testing Detox. Publish AppStore/PlayStore.',
1390000.00,'https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.50,76,DATE_SUB(NOW(),INTERVAL 100 DAY),DATE_SUB(NOW(),INTERVAL 8 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000009','00000000-0000-0000-0000-000000000023',14,
'AWS Solutions Architect Chuan Chi SAA-C03','aws-solutions-architect-saa-c03',
'Luyen thi AWS Certified Solutions Architect. Ly thuyet va lab 60+ dich vu AWS. Pass rate 95%.',
'Luyen thi SAA-C03: EC2, VPC, IAM, S3, RDS, DynamoDB, Lambda, CloudFront, Route53, ELB, Auto Scaling, CloudWatch, SQS, SNS, Kinesis. 500+ cau hoi practice. Chien luoc lam bai thi.',
1790000.00,'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.90,231,DATE_SUB(NOW(),INTERVAL 90 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000010','00000000-0000-0000-0000-000000000024',16,
'NLP va Large Language Models Thuc Chien','nlp-large-language-models-thuc-chien',
'Xu ly ngon ngu tu nhien. Fine-tune LLM. Build Chatbot AI. RAG Architecture. LangChain.',
'NLP hien dai: BERT, GPT, T5. Fine-tuning Hugging Face. RAG voi LangChain va Chroma. Chatbot voi ChatGPT API va Ollama. FastAPI deploy. 4 du an: Sentiment, QA System, Code Assistant, Summarizer.',
2490000.00,'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=800',
'ADVANCED','Vietnamese','PUBLISHED',4.75,118,DATE_SUB(NOW(),INTERVAL 80 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000011','00000000-0000-0000-0000-000000000025',15,
'Redis: In-Memory Database va Caching','redis-in-memory-database-caching',
'Master Redis: Data Structures, Caching, Pub/Sub, Streams, Cluster. Tich hop Spring Boot, Node.js.',
'Redis: String, List, Set, Sorted Set, Hash. Cache patterns. Pub/Sub. Streams. Cluster va Sentinel. Lua scripting. Spring Data Redis. 6 use case that su: Session, Rate Limiting, Leaderboard, Job Queue.',
990000.00,'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.60,72,DATE_SUB(NOW(),INTERVAL 75 DAY),DATE_SUB(NOW(),INTERVAL 10 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000012','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',12,
'Microservices voi Spring Boot va Spring Cloud','microservices-spring-boot-spring-cloud',
'Kien truc microservices: Service Discovery, API Gateway, Circuit Breaker, Kafka, SAGA, CQRS.',
'Microservices Spring Boot 3: Eureka, Spring Cloud Gateway, OpenFeign, Resilience4j, Zipkin. Kafka event-driven. SAGA Pattern. CQRS va Event Sourcing. Docker Compose. Prometheus/Grafana. Du an Uber-like backend.',
2090000.00,'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800',
'ADVANCED','Vietnamese','PUBLISHED',4.80,156,DATE_SUB(NOW(),INTERVAL 70 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000013','00000000-0000-0000-0000-000000000022',13,
'Kotlin Android Development Hien Dai','kotlin-android-development-hien-dai',
'Android app voi Kotlin. Jetpack Compose, Coroutines, Flow, Room, Retrofit, Hilt DI, MVVM.',
'Kotlin Android: Coroutines, Flow, Compose UI, ViewModel, Room, Retrofit, Hilt, WorkManager, Navigation. Material 3. Testing JUnit, Mockito, Espresso. CI/CD GitHub Actions. Google Play.',
1390000.00,'https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.55,64,DATE_SUB(NOW(),INTERVAL 65 DAY),DATE_SUB(NOW(),INTERVAL 12 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000023',14,
'CI/CD Pipeline voi Jenkins va GitLab','cicd-pipeline-jenkins-gitlab',
'Pipeline chuyen nghiep voi Jenkins, GitLab CI, GitHub Actions. Automated testing va deployment.',
'Git branching strategies, Jenkins Jenkinsfile, GitLab CI YAML, GitHub Actions. Docker build. SonarQube. Blue-Green, Canary, Rolling. ArgoCD GitOps. Monitoring deployment.',
1490000.00,'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.70,91,DATE_SUB(NOW(),INTERVAL 60 DAY),DATE_SUB(NOW(),INTERVAL 8 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000015','00000000-0000-0000-0000-000000000024',16,
'Computer Vision voi TensorFlow va OpenCV','computer-vision-tensorflow-opencv',
'Xu ly anh va video voi Python. CNN, YOLO Object Detection, Face Recognition, Segmentation, OCR.',
'Computer Vision: OpenCV, CNN (ResNet, EfficientNet), Transfer Learning, YOLO v8, Segmentation, Face Detection, OCR Tesseract. Video processing. FastAPI deploy. 5 du an thuc te.',
1990000.00,'https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?w=800',
'ADVANCED','Vietnamese','PUBLISHED',4.65,83,DATE_SUB(NOW(),INTERVAL 55 DAY),DATE_SUB(NOW(),INTERVAL 6 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000016','00000000-0000-0000-0000-000000000025',15,
'MongoDB cho Developer: Co Ban Den Expert','mongodb-developer-co-ban-den-expert',
'MongoDB 7.x: CRUD, Aggregation Pipeline, Indexing, Sharding, Atlas. Tich hop Node.js/Mongoose.',
'MongoDB: Schema design, CRUD, Aggregation Pipeline, Index (B-Tree, Text, Geospatial), Transactions, Replica Set, Sharding, Change Streams, Atlas Search, Mongoose ODM. Performance tuning.',
1190000.00,'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.50,59,DATE_SUB(NOW(),INTERVAL 50 DAY),DATE_SUB(NOW(),INTERVAL 15 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000017','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',12,
'Lap Trinh Java Co Ban Den Nang Cao','lap-trinh-java-co-ban-den-nang-cao',
'Java 21 day du: OOP, Collections, Streams, Concurrency, Design Patterns. Cho nguoi moi bat dau.',
'Java 21: Syntax, OOP, Collections, Generics, Lambda, Streams, Concurrency, Design Patterns GoF 23, JUnit 5, Maven/Gradle. Phu hop nguoi chua biet Java muon lam backend developer.',
890000.00,'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800',
'BEGINNER','Vietnamese','PUBLISHED',4.70,203,DATE_SUB(NOW(),INTERVAL 45 DAY),DATE_SUB(NOW(),INTERVAL 4 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000018','00000000-0000-0000-0000-000000000022',13,
'SwiftUI iOS Development cho Nguoi Moi','swiftui-ios-development-nguoi-moi',
'iOS app voi Swift va SwiftUI. Tu Hello World den app hoan chinh tren AppStore.',
'Swift 5.9, SwiftUI: Views, modifiers, data binding, navigation, async/await, Core Data, URLSession, UserNotifications, In-App Purchase. App Store submission. Du an: Weather va Expense Tracker.',
1290000.00,'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800',
'BEGINNER','Vietnamese','PUBLISHED',4.45,47,DATE_SUB(NOW(),INTERVAL 40 DAY),DATE_SUB(NOW(),INTERVAL 18 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000019','00000000-0000-0000-0000-000000000023',14,
'Terraform: Infrastructure as Code tren AWS','terraform-infrastructure-code-aws',
'Quan ly ha tang AWS bang code. Module, State, Workspace, Remote Backend, Terragrunt.',
'Terraform: HCL, Providers, Resources, Variables, State (S3+DynamoDB), Modules, Workspaces, Terragrunt. CI/CD tich hop. Provision VPC, EC2, RDS, EKS, Lambda tren AWS.',
1590000.00,'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.75,68,DATE_SUB(NOW(),INTERVAL 35 DAY),DATE_SUB(NOW(),INTERVAL 9 DAY));

INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,thumbnail_url,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000020','00000000-0000-0000-0000-000000000024',16,
'Data Science voi Python: Pandas va Visualization','data-science-python-pandas-visualization',
'Phan tich du lieu voi Pandas, NumPy, Matplotlib, Seaborn, Plotly. Build Streamlit Dashboard.',
'Data Science: Jupyter, NumPy, Pandas, EDA, Matplotlib, Seaborn, Plotly Interactive, Streamlit, Statistical analysis, Feature engineering, Sklearn preprocessing. 6 du an phan tich du lieu Kaggle.',
1290000.00,'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800',
'INTERMEDIATE','Vietnamese','PUBLISHED',4.60,97,DATE_SUB(NOW(),INTERVAL 25 DAY),DATE_SUB(NOW(),INTERVAL 7 DAY));

-- 5 PENDING_REVIEW courses -----------------------------------------
INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000021','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',12,
'GraphQL API voi Spring Boot','graphql-api-spring-boot',
'Xay dung GraphQL API voi Spring for GraphQL. Schema, Resolver, Subscription, DataLoader.',
'GraphQL Spring Boot 3: Schema, Query, Mutation, Subscription WebSocket, DataLoader, Spring Security, File upload, Error handling, Testing.',
1490000.00,'ADVANCED','Vietnamese','PENDING_REVIEW',0.00,0,DATE_SUB(NOW(),INTERVAL 15 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY)),

('c0000000-0000-0000-0000-000000000022','00000000-0000-0000-0000-000000000022',13,
'Xamarin MAUI Cross-Platform Development','xamarin-maui-cross-platform',
'Phat trien app iOS, Android, Windows voi .NET MAUI va C#. MVVM, Blazor Hybrid, SQLite.',
'NET MAUI: XAML, MVVM, Data Binding, SQLite EF Core, RESTful API, Auth, Blazor Hybrid. Publish multi-platform.',
1290000.00,'INTERMEDIATE','Vietnamese','PENDING_REVIEW',0.00,0,DATE_SUB(NOW(),INTERVAL 12 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY)),

('c0000000-0000-0000-0000-000000000023','00000000-0000-0000-0000-000000000023',14,
'Ansible: Automation va Configuration Management','ansible-automation-configuration-management',
'Tu dong hoa server voi Ansible. Playbook, Role, Inventory, Vault, AWX. Tich hop CI/CD.',
'Ansible: Inventory, Playbooks, Variables, Roles, Galaxy, Vault, Dynamic inventory, Jinja2, AWX. Provision 20 server AWS.',
1390000.00,'INTERMEDIATE','Vietnamese','PENDING_REVIEW',0.00,0,DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY)),

('c0000000-0000-0000-0000-000000000024','00000000-0000-0000-0000-000000000024',16,
'Reinforcement Learning Tu Dau Voi Python','reinforcement-learning-tu-dau-python',
'Hoc RL: MDP, Q-Learning, DQN, Policy Gradient, PPO. OpenAI Gymnasium, Stable Baselines3.',
'RL: MDP, Bellman, Q-Learning, DQN, PPO, SAC. OpenAI Gym, Stable Baselines3. Game playing va robot control.',
1990000.00,'ADVANCED','Vietnamese','PENDING_REVIEW',0.00,0,DATE_SUB(NOW(),INTERVAL 8 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY)),

('c0000000-0000-0000-0000-000000000025','00000000-0000-0000-0000-000000000025',15,
'PostgreSQL Nang Cao: JSONB va Partition','postgresql-nang-cao-jsonb-partition',
'PostgreSQL 16: JSONB, Partition, Full Text Search, PL/pgSQL, pg_stat, TimescaleDB.',
'PostgreSQL: JSONB, ARRAY, Table partitioning, Full Text Search, Window Functions, CTE Recursive, PL/pgSQL, Triggers, Logical Replication, TimescaleDB.',
1390000.00,'ADVANCED','Vietnamese','PENDING_REVIEW',0.00,0,DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY));

-- 3 DRAFT courses --------------------------------------------------
INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,level,language,status,average_rating,total_reviews,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000026','a56e8cdf-80bb-11f1-8183-de8e3dc1070d',12,
'WebSocket va Real-Time App voi Spring Boot','websocket-realtime-spring-boot',
'Real-time voi WebSocket, STOMP, SockJS. Chat, Notification, Live Dashboard tich hop Spring Boot.',
'WebSocket, Spring WebSocket, STOMP, SockJS, Broadcasting, Room chat, Notification, Auth, Redis Pub/Sub scaling.',
990000.00,'ADVANCED','Vietnamese','DRAFT',0.00,0,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY)),

('c0000000-0000-0000-0000-000000000027','00000000-0000-0000-0000-000000000022',13,
'Wear OS Development voi Compose','wear-os-development-compose',
'Smartwatch app voi Wear OS 4 va Jetpack Compose. Health, Fitness, Notification Tiles.',
'Wear OS 4: Compose for Wear, Tiles API, Sensor data, Health Services API, Background processing, Google Play.',
890000.00,'INTERMEDIATE','Vietnamese','DRAFT',0.00,0,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY)),

('c0000000-0000-0000-0000-000000000028','00000000-0000-0000-0000-000000000024',16,
'LLM Fine-tuning va RAG Architecture','llm-fine-tuning-rag-architecture',
'Fine-tune LLM voi LoRA, QLoRA. RAG Pipeline. Vector DB Chroma, Pinecone, Weaviate.',
'LLM fine-tuning: LoRA, QLoRA, PEFT, Hugging Face. Dataset prep. RAG: chunking, embedding, vector store, LangChain, LlamaIndex. Production deploy.',
2490000.00,'ADVANCED','Vietnamese','DRAFT',0.00,0,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW());

-- 2 REJECTED courses -----------------------------------------------
INSERT IGNORE INTO courses (id,instructor_id,category_id,title,slug,short_description,description,price,level,language,status,average_rating,total_reviews,rejected_reason,rejected_by,rejected_at,created_at,updated_at) VALUES
('c0000000-0000-0000-0000-000000000029','00000000-0000-0000-0000-000000000025',15,
'Hack MySQL: Unauthorized Access Techniques','hack-mysql-unauthorized-access',
'Tim hieu cac ky thuat hack MySQL de phong thu bao mat.',
'Cac ky thuat tan cong MySQL: SQL Injection nang cao, Authentication bypass, Privilege escalation.',
0.00,'ADVANCED','Vietnamese','REJECTED',0.00,0,
'Noi dung co the bi su dung sai muc dich. Can chinh sua theo huong giao duc bao mat hop phap.',
'00000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 30 DAY),DATE_SUB(NOW(),INTERVAL 45 DAY),DATE_SUB(NOW(),INTERVAL 30 DAY)),

('c0000000-0000-0000-0000-000000000030','00000000-0000-0000-0000-000000000022',13,
'Kiem Tien Voi App iOS Khong Can Code','kiem-tien-app-ios-khong-can-code',
'Cach tao app iOS de kiem tien ma khong can biet lap trinh.',
'Dung tool no-code de tao app iOS va phat hanh de kiem tien.',
490000.00,'BEGINNER','Vietnamese','REJECTED',0.00,0,
'Noi dung khong phu hop dinh huong giao duc lap trinh CourseHub. Khoa hoc phai co noi dung ky thuat.',
'00000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 20 DAY),DATE_SUB(NOW(),INTERVAL 35 DAY),DATE_SUB(NOW(),INTERVAL 20 DAY));


-- ============================================================
-- 9. CHAPTERS & LESSONS cho 3 khoa hoc duoc mo rong
-- (C01: Spring Boot, C02: React, C04: Docker/K8s)
-- ============================================================

-- === BO SUNG CHAPTERS CHO CAC KHOA HOC KHAC ===
INSERT IGNORE INTO chapters (id, course_id, title, order_index, created_at) VALUES
('bb000000-0003-0000-0000-000000000001','c0000000-0000-0000-0000-000000000003','Co ban ve Flutter',1,DATE_SUB(NOW(),INTERVAL 150 DAY)),
('bb000000-0005-0000-0000-000000000001','c0000000-0000-0000-0000-000000000005','Co ban ve Machine Learning',1,DATE_SUB(NOW(),INTERVAL 130 DAY)),
('bb000000-0006-0000-0000-000000000001','c0000000-0000-0000-0000-000000000006','MySQL Core concepts',1,DATE_SUB(NOW(),INTERVAL 120 DAY)),
('bb000000-0007-0000-0000-000000000001','c0000000-0000-0000-0000-000000000007','TypeScript setup',1,DATE_SUB(NOW(),INTERVAL 110 DAY)),
('bb000000-0008-0000-0000-000000000001','c0000000-0000-0000-0000-000000000008','React Native setup',1,DATE_SUB(NOW(),INTERVAL 100 DAY)),
('bb000000-0009-0000-0000-000000000001','c0000000-0000-0000-0000-000000000009','AWS Cloud overview',1,DATE_SUB(NOW(),INTERVAL 90 DAY)),
('bb000000-000a-0000-0000-000000000001','c0000000-0000-0000-0000-000000000010','NLP basics',1,DATE_SUB(NOW(),INTERVAL 80 DAY)),
('bb000000-000b-0000-0000-000000000001','c0000000-0000-0000-0000-000000000011','Redis Caching basics',1,DATE_SUB(NOW(),INTERVAL 75 DAY)),
('bb000000-000c-0000-0000-000000000001','c0000000-0000-0000-0000-000000000012','Microservices intro',1,DATE_SUB(NOW(),INTERVAL 70 DAY)),
('bb000000-000d-0000-0000-000000000001','c0000000-0000-0000-0000-000000000013','Kotlin fundamentals',1,DATE_SUB(NOW(),INTERVAL 65 DAY)),
('bb000000-000e-0000-0000-000000000001','c0000000-0000-0000-0000-000000000014','CI/CD overview',1,DATE_SUB(NOW(),INTERVAL 60 DAY)),
('bb000000-000f-0000-0000-000000000001','c0000000-0000-0000-0000-000000000015','Computer Vision basics',1,DATE_SUB(NOW(),INTERVAL 55 DAY)),
('bb000000-0010-0000-0000-000000000001','c0000000-0000-0000-0000-000000000016','MongoDB basics',1,DATE_SUB(NOW(),INTERVAL 50 DAY)),
('bb000000-0011-0000-0000-000000000001','c0000000-0000-0000-0000-000000000017','Java core',1,DATE_SUB(NOW(),INTERVAL 45 DAY)),
('bb000000-0012-0000-0000-000000000001','c0000000-0000-0000-0000-000000000018','SwiftUI basics',1,DATE_SUB(NOW(),INTERVAL 40 DAY)),
('bb000000-0013-0000-0000-000000000001','c0000000-0000-0000-0000-000000000019','Terraform basics',1,DATE_SUB(NOW(),INTERVAL 35 DAY)),
('bb000000-0014-0000-0000-000000000001','c0000000-0000-0000-0000-000000000020','Data science basics',1,DATE_SUB(NOW(),INTERVAL 25 DAY));

-- === BO SUNG LESSONS CHO CAC KHOA HOC KHAC ===
INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0003-0000-0000-000000000001','bb000000-0003-0000-0000-000000000001','Gioi thieu Flutter',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 150 DAY)),
('cc000000-0005-0000-0000-000000000001','bb000000-0005-0000-0000-000000000001','Gioi thieu Machine Learning',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 130 DAY)),
('cc000000-0006-0000-0000-000000000001','bb000000-0006-0000-0000-000000000001','MySQL architecture',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 120 DAY)),
('cc000000-0007-0000-0000-000000000001','bb000000-0007-0000-0000-000000000001','TypeScript introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 110 DAY)),
('cc000000-0008-0000-0000-000000000001','bb000000-0008-0000-0000-000000000001','React Native introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 100 DAY)),
('cc000000-0009-0000-0000-000000000001','bb000000-0009-0000-0000-000000000001','AWS Introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 90 DAY)),
('cc000000-0009-0000-0000-000000000002','bb000000-0009-0000-0000-000000000001','EC2 and VPC core concepts',2,'TEXT',0,DATE_SUB(NOW(),INTERVAL 89 DAY)),
('cc000000-0009-0000-0000-000000000003','bb000000-0009-0000-0000-000000000001','S3 Simple Storage Service',3,'TEXT',0,DATE_SUB(NOW(),INTERVAL 88 DAY)),
('cc000000-000a-0000-0000-000000000001','bb000000-000a-0000-0000-000000000001','NLP Introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 80 DAY)),
('cc000000-000b-0000-0000-000000000001','bb000000-000b-0000-0000-000000000001','Redis introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 75 DAY)),
('cc000000-000c-0000-0000-000000000001','bb000000-000c-0000-0000-000000000001','Microservices core concepts',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 70 DAY)),
('cc000000-000d-0000-0000-000000000001','bb000000-000d-0000-0000-000000000001','Kotlin core syntax',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 65 DAY)),
('cc000000-000e-0000-0000-000000000001','bb000000-000e-0000-0000-000000000001','CI/CD introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 60 DAY)),
('cc000000-000f-0000-0000-000000000001','bb000000-000f-0000-0000-000000000001','Computer Vision introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 55 DAY)),
('cc000000-0010-0000-0000-000000000001','bb000000-0010-0000-0000-000000000001','MongoDB introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 50 DAY)),
('cc000000-0011-0000-0000-000000000001','bb000000-0011-0000-0000-000000000001','Java basic structures',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 45 DAY)),
('cc000000-0011-0000-0000-000000000002','bb000000-0011-0000-0000-000000000001','Java OOP Principles',2,'TEXT',0,DATE_SUB(NOW(),INTERVAL 44 DAY)),
('cc000000-0011-0000-0000-000000000003','bb000000-0011-0000-0000-000000000001','Java Collections Framework',3,'TEXT',0,DATE_SUB(NOW(),INTERVAL 43 DAY)),
('cc000000-0012-0000-0000-000000000001','bb000000-0012-0000-0000-000000000001','SwiftUI introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 40 DAY)),
('cc000000-0013-0000-0000-000000000001','bb000000-0013-0000-0000-000000000001','Terraform introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 35 DAY)),
('cc000000-0014-0000-0000-000000000001','bb000000-0014-0000-0000-000000000001','Data science introduction',1,'TEXT',1,DATE_SUB(NOW(),INTERVAL 25 DAY));

-- === COURSE C01: Java Spring Boot ===
-- Chapter 1
INSERT IGNORE INTO chapters (id, course_id, title, order_index, created_at) VALUES
('bb000000-0001-0000-0000-000000000001','c0000000-0000-0000-0000-000000000001','Gioi Thieu Spring Boot va Moi Truong',1,DATE_SUB(NOW(),INTERVAL 180 DAY)),
('bb000000-0001-0000-0000-000000000002','c0000000-0000-0000-0000-000000000001','REST API va Spring MVC',2,DATE_SUB(NOW(),INTERVAL 178 DAY)),
('bb000000-0001-0000-0000-000000000003','c0000000-0000-0000-0000-000000000001','Spring Data JPA va Database',3,DATE_SUB(NOW(),INTERVAL 175 DAY)),
('bb000000-0001-0000-0000-000000000004','c0000000-0000-0000-0000-000000000001','Spring Security va JWT',4,DATE_SUB(NOW(),INTERVAL 170 DAY)),
('bb000000-0001-0000-0000-000000000005','c0000000-0000-0000-0000-000000000001','Docker va Deploy AWS',5,DATE_SUB(NOW(),INTERVAL 165 DAY));

-- Lessons Chapter 1 (C01)
INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0001-0000-0000-000000000001','bb000000-0001-0000-0000-000000000001','Spring Boot la gi? Lich su va loi ich',1,'VIDEO',1,DATE_SUB(NOW(),INTERVAL 180 DAY)),
('cc000000-0001-0000-0000-000000000002','bb000000-0001-0000-0000-000000000001','Cai dat JDK 21, IntelliJ IDEA va Maven',2,'TEXT',1,DATE_SUB(NOW(),INTERVAL 180 DAY)),
('cc000000-0001-0000-0000-000000000003','bb000000-0001-0000-0000-000000000001','Tao Spring Boot project dau tien',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 179 DAY)),
('cc000000-0001-0000-0000-000000000004','bb000000-0001-0000-0000-000000000001','Cau truc project va annotation co ban',4,'TEXT',0,DATE_SUB(NOW(),INTERVAL 179 DAY)),
('cc000000-0001-0000-0000-000000000005','bb000000-0001-0000-0000-000000000001','Bai tap: Hello World Spring Boot',5,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 178 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0001-0000-0000-000000000006','bb000000-0001-0000-0000-000000000002','HTTP Method va REST Conventions',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 177 DAY)),
('cc000000-0001-0000-0000-000000000007','bb000000-0001-0000-0000-000000000002','@RestController, @RequestMapping, @PathVariable',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 177 DAY)),
('cc000000-0001-0000-0000-000000000008','bb000000-0001-0000-0000-000000000002','Request Body, DTO va Validation',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 176 DAY)),
('cc000000-0001-0000-0000-000000000009','bb000000-0001-0000-0000-000000000002','Exception Handling voi @ControllerAdvice',4,'TEXT',0,DATE_SUB(NOW(),INTERVAL 176 DAY)),
('cc000000-0001-0000-0000-000000000010','bb000000-0001-0000-0000-000000000002','Kiem tra kien thuc REST API',5,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 175 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0001-0000-0000-000000000011','bb000000-0001-0000-0000-000000000003','JPA va Hibernate: Khai niem co ban',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 174 DAY)),
('cc000000-0001-0000-0000-000000000012','bb000000-0001-0000-0000-000000000003','Entity, Repository va CRUD co ban',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 174 DAY)),
('cc000000-0001-0000-0000-000000000013','bb000000-0001-0000-0000-000000000003','Quan he OneToMany va ManyToMany',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 173 DAY)),
('cc000000-0001-0000-0000-000000000014','bb000000-0001-0000-0000-000000000003','JPQL va Native Query',4,'TEXT',0,DATE_SUB(NOW(),INTERVAL 173 DAY)),
('cc000000-0001-0000-0000-000000000015','bb000000-0001-0000-0000-000000000003','Flyway Database Migration',5,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 172 DAY)),
('cc000000-0001-0000-0000-000000000016','bb000000-0001-0000-0000-000000000003','Quiz: JPA va Database',6,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 171 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0001-0000-0000-000000000017','bb000000-0001-0000-0000-000000000004','Spring Security Architecture',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 170 DAY)),
('cc000000-0001-0000-0000-000000000018','bb000000-0001-0000-0000-000000000004','JWT: JSON Web Token - Cau truc va cach hoat dong',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 169 DAY)),
('cc000000-0001-0000-0000-000000000019','bb000000-0001-0000-0000-000000000004','Implement JWT Authentication toan dien',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 168 DAY)),
('cc000000-0001-0000-0000-000000000020','bb000000-0001-0000-0000-000000000004','Role-based Authorization',4,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 167 DAY)),
('cc000000-0001-0000-0000-000000000021','bb000000-0001-0000-0000-000000000004','Quiz: Spring Security va JWT',5,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 166 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0001-0000-0000-000000000022','bb000000-0001-0000-0000-000000000005','Docker co ban cho Java Developer',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 165 DAY)),
('cc000000-0001-0000-0000-000000000023','bb000000-0001-0000-0000-000000000005','Dockerfile toi uu cho Spring Boot',2,'TEXT',0,DATE_SUB(NOW(),INTERVAL 164 DAY)),
('cc000000-0001-0000-0000-000000000024','bb000000-0001-0000-0000-000000000005','Docker Compose: Chay app + MySQL + Redis',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 163 DAY)),
('cc000000-0001-0000-0000-000000000025','bb000000-0001-0000-0000-000000000005','Deploy len AWS EC2 voi CI/CD',4,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 162 DAY));

-- Lesson Resources (C01)
INSERT IGNORE INTO lesson_resources (id, lesson_id, resource_url, duration_seconds, video_status) VALUES
('dd000000-0001-0000-0000-000000000001','cc000000-0001-0000-0000-000000000001','https://www.youtube.com/watch?v=spring-intro-1',720,'READY'),
('dd000000-0001-0000-0000-000000000003','cc000000-0001-0000-0000-000000000003','https://www.youtube.com/watch?v=spring-firstapp',1080,'READY'),
('dd000000-0001-0000-0000-000000000006','cc000000-0001-0000-0000-000000000006','https://www.youtube.com/watch?v=spring-rest-1',900,'READY'),
('dd000000-0001-0000-0000-000000000007','cc000000-0001-0000-0000-000000000007','https://www.youtube.com/watch?v=spring-rest-2',1200,'READY'),
('dd000000-0001-0000-0000-000000000008','cc000000-0001-0000-0000-000000000008','https://www.youtube.com/watch?v=spring-rest-3',960,'READY'),
('dd000000-0001-0000-0000-000000000011','cc000000-0001-0000-0000-000000000011','https://www.youtube.com/watch?v=spring-jpa-1',840,'READY'),
('dd000000-0001-0000-0000-000000000012','cc000000-0001-0000-0000-000000000012','https://www.youtube.com/watch?v=spring-jpa-2',1500,'READY'),
('dd000000-0001-0000-0000-000000000013','cc000000-0001-0000-0000-000000000013','https://www.youtube.com/watch?v=spring-jpa-3',1320,'READY'),
('dd000000-0001-0000-0000-000000000015','cc000000-0001-0000-0000-000000000015','https://www.youtube.com/watch?v=spring-flyway',780,'READY'),
('dd000000-0001-0000-0000-000000000017','cc000000-0001-0000-0000-000000000017','https://www.youtube.com/watch?v=spring-security-1',960,'READY'),
('dd000000-0001-0000-0000-000000000018','cc000000-0001-0000-0000-000000000018','https://www.youtube.com/watch?v=spring-jwt-1',1080,'READY'),
('dd000000-0001-0000-0000-000000000019','cc000000-0001-0000-0000-000000000019','https://www.youtube.com/watch?v=spring-jwt-2',2400,'READY'),
('dd000000-0001-0000-0000-000000000020','cc000000-0001-0000-0000-000000000020','https://www.youtube.com/watch?v=spring-rbac',720,'READY'),
('dd000000-0001-0000-0000-000000000022','cc000000-0001-0000-0000-000000000022','https://www.youtube.com/watch?v=spring-docker-1',900,'READY'),
('dd000000-0001-0000-0000-000000000024','cc000000-0001-0000-0000-000000000024','https://www.youtube.com/watch?v=spring-docker-2',1200,'READY'),
('dd000000-0001-0000-0000-000000000025','cc000000-0001-0000-0000-000000000025','https://www.youtube.com/watch?v=spring-aws-deploy',1800,'READY');

-- Text resources
INSERT IGNORE INTO lesson_resources (id, lesson_id, text_content, video_status, is_downloadable) VALUES
('dd000000-0001-0000-0000-000000000002','cc000000-0001-0000-0000-000000000002','# Huong dan Cai Dat Moi Truong\n\n## 1. Cai dat JDK 21\n- Tai JDK 21 tu Oracle hoac Adoptium\n- Set JAVA_HOME\n- Kiem tra: java -version\n\n## 2. Cai dat IntelliJ IDEA\n- Tai ban Community (mien phi) hoac Ultimate\n- Cai dat Spring Boot plugin\n\n## 3. Cai dat Maven\n- Tai Maven 3.9.x\n- Set M2_HOME va PATH','NONE',1),
('dd000000-0001-0000-0000-000000000004','cc000000-0001-0000-0000-000000000004','# Cau Truc Spring Boot Project\n\n## Cac thu muc chinh:\n- src/main/java: Java source code\n- src/main/resources: application.yml, static files\n- src/test: Unit va integration tests\n\n## Cac Annotation co ban:\n- @SpringBootApplication: Entry point\n- @RestController: REST Controller\n- @Service: Business logic\n- @Repository: Data access','NONE',0),
('dd000000-0001-0000-0000-000000000009','cc000000-0001-0000-0000-000000000009','# Exception Handling voi @ControllerAdvice\n\n## Global Exception Handler\n@ControllerAdvice cho phep xu ly tat ca exception o mot noi.\n\n## Cach implement:\n1. Tao class ExceptionHandler\n2. Danh dau @RestControllerAdvice\n3. Them @ExceptionHandler cho tung loai exception\n4. Tra ve ResponseEntity voi thong bao loi ro rang','NONE',0),
('dd000000-0001-0000-0000-000000000014','cc000000-0001-0000-0000-000000000014','# JPQL va Native Query\n\n## JPQL (Java Persistence Query Language)\n- Dung ten Entity va field thay vi ten bang va cot\n- Vi du: SELECT u FROM UserEntity u WHERE u.email = :email\n\n## Native Query\n- Dung SQL thuan tuy voi @Query(nativeQuery=true)\n- Hieu nang tot hon voi query phuc tap\n- Kem portable hon JPQL','NONE',0),
('dd000000-0001-0000-0000-000000000023','cc000000-0001-0000-0000-000000000023','# Dockerfile Toi Uu cho Spring Boot\n\n## Multi-stage build:\n```dockerfile\nFROM eclipse-temurin:21-jdk-alpine AS builder\nWORKDIR /app\nCOPY build.gradle settings.gradle ./\nCOPY src ./src\nRUN gradle build -x test\n\nFROM eclipse-temurin:21-jre-alpine\nWORKDIR /app\nCOPY --from=builder /app/build/libs/*.jar app.jar\nENTRYPOINT ["java", "-jar", "app.jar"]\n```','NONE',1);

-- === COURSE C02: React & Next.js ===
INSERT IGNORE INTO chapters (id, course_id, title, order_index, created_at) VALUES
('bb000000-0002-0000-0000-000000000001','c0000000-0000-0000-0000-000000000002','React Co Ban: Hooks va Component',1,DATE_SUB(NOW(),INTERVAL 160 DAY)),
('bb000000-0002-0000-0000-000000000002','c0000000-0000-0000-0000-000000000002','State Management: Redux Toolkit',2,DATE_SUB(NOW(),INTERVAL 155 DAY)),
('bb000000-0002-0000-0000-000000000003','c0000000-0000-0000-0000-000000000002','Next.js 14 App Router',3,DATE_SUB(NOW(),INTERVAL 150 DAY)),
('bb000000-0002-0000-0000-000000000004','c0000000-0000-0000-0000-000000000002','TypeScript va Performance',4,DATE_SUB(NOW(),INTERVAL 145 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0002-0000-0000-000000000001','bb000000-0002-0000-0000-000000000001','React 18: Gi moi va tai sao quan trong',1,'VIDEO',1,DATE_SUB(NOW(),INTERVAL 160 DAY)),
('cc000000-0002-0000-0000-000000000002','bb000000-0002-0000-0000-000000000001','useState va useEffect nang cao',2,'VIDEO',1,DATE_SUB(NOW(),INTERVAL 159 DAY)),
('cc000000-0002-0000-0000-000000000003','bb000000-0002-0000-0000-000000000001','useCallback, useMemo, useRef - khi nao dung',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 158 DAY)),
('cc000000-0002-0000-0000-000000000004','bb000000-0002-0000-0000-000000000001','Custom Hooks: Tao va tai su dung',4,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 157 DAY)),
('cc000000-0002-0000-0000-000000000005','bb000000-0002-0000-0000-000000000001','Quiz: React Hooks',5,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 156 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0002-0000-0000-000000000006','bb000000-0002-0000-0000-000000000002','Redux Toolkit: Tai sao thay Redux co',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 155 DAY)),
('cc000000-0002-0000-0000-000000000007','bb000000-0002-0000-0000-000000000002','createSlice, createAsyncThunk trong thuc te',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 154 DAY)),
('cc000000-0002-0000-0000-000000000008','bb000000-0002-0000-0000-000000000002','RTK Query: Data fetching hien dai',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 153 DAY)),
('cc000000-0002-0000-0000-000000000009','bb000000-0002-0000-0000-000000000002','Quiz: State Management',4,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 152 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0002-0000-0000-000000000010','bb000000-0002-0000-0000-000000000003','Next.js App Router: Thay doi tu dau trang',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 150 DAY)),
('cc000000-0002-0000-0000-000000000011','bb000000-0002-0000-0000-000000000003','Server Components vs Client Components',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 149 DAY)),
('cc000000-0002-0000-0000-000000000012','bb000000-0002-0000-0000-000000000003','SSR, SSG, ISR - Khi nao dung cai nao',3,'TEXT',0,DATE_SUB(NOW(),INTERVAL 148 DAY)),
('cc000000-0002-0000-0000-000000000013','bb000000-0002-0000-0000-000000000003','API Routes va Server Actions',4,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 147 DAY)),
('cc000000-0002-0000-0000-000000000014','bb000000-0002-0000-0000-000000000003','Quiz: Next.js App Router',5,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 146 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0002-0000-0000-000000000015','bb000000-0002-0000-0000-000000000004','TypeScript voi React: Typing Props va Hooks',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 145 DAY)),
('cc000000-0002-0000-0000-000000000016','bb000000-0002-0000-0000-000000000004','Performance: React.memo, lazy, Suspense',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 144 DAY)),
('cc000000-0002-0000-0000-000000000017','bb000000-0002-0000-0000-000000000004','Web Vitals va Optimization',3,'TEXT',0,DATE_SUB(NOW(),INTERVAL 143 DAY));

-- Resources for C02
INSERT IGNORE INTO lesson_resources (id, lesson_id, resource_url, duration_seconds, video_status) VALUES
('dd000000-0002-0000-0000-000000000001','cc000000-0002-0000-0000-000000000001','https://cdn.coursehub.io/react-18-intro.mp4',840,'READY'),
('dd000000-0002-0000-0000-000000000002','cc000000-0002-0000-0000-000000000002','https://cdn.coursehub.io/react-hooks-advanced.mp4',1320,'READY'),
('dd000000-0002-0000-0000-000000000003','cc000000-0002-0000-0000-000000000003','https://cdn.coursehub.io/react-performance-hooks.mp4',1080,'READY'),
('dd000000-0002-0000-0000-000000000004','cc000000-0002-0000-0000-000000000004','https://cdn.coursehub.io/react-custom-hooks.mp4',960,'READY'),
('dd000000-0002-0000-0000-000000000006','cc000000-0002-0000-0000-000000000006','https://cdn.coursehub.io/redux-toolkit-intro.mp4',720,'READY'),
('dd000000-0002-0000-0000-000000000007','cc000000-0002-0000-0000-000000000007','https://cdn.coursehub.io/redux-toolkit-advanced.mp4',1440,'READY'),
('dd000000-0002-0000-0000-000000000008','cc000000-0002-0000-0000-000000000008','https://cdn.coursehub.io/rtk-query.mp4',1200,'READY'),
('dd000000-0002-0000-0000-000000000010','cc000000-0002-0000-0000-000000000010','https://cdn.coursehub.io/nextjs-app-router.mp4',1080,'READY'),
('dd000000-0002-0000-0000-000000000011','cc000000-0002-0000-0000-000000000011','https://cdn.coursehub.io/nextjs-server-components.mp4',900,'READY'),
('dd000000-0002-0000-0000-000000000013','cc000000-0002-0000-0000-000000000013','https://cdn.coursehub.io/nextjs-api-routes.mp4',1020,'READY'),
('dd000000-0002-0000-0000-000000000015','cc000000-0002-0000-0000-000000000015','https://cdn.coursehub.io/typescript-react.mp4',960,'READY'),
('dd000000-0002-0000-0000-000000000016','cc000000-0002-0000-0000-000000000016','https://cdn.coursehub.io/react-performance.mp4',840,'READY');

INSERT IGNORE INTO lesson_resources (id, lesson_id, text_content, video_status) VALUES
('dd000000-0002-0000-0000-000000000012','cc000000-0002-0000-0000-000000000012','# SSR vs SSG vs ISR trong Next.js\n\n## SSR (Server-Side Rendering)\n- Render moi request\n- Dung cho data thay doi lien tuc\n\n## SSG (Static Site Generation)\n- Build time rendering\n- Nhanh nhat, cache CDN\n\n## ISR (Incremental Static Regeneration)\n- Ket hop SSG + revalidation\n- Best of both worlds','NONE'),
('dd000000-0002-0000-0000-000000000017','cc000000-0002-0000-0000-000000000017','# Core Web Vitals\n\n## LCP (Largest Contentful Paint)\n- Muc tieu: < 2.5s\n- Optimize: Image priority, preload fonts\n\n## FID / INP (Interaction to Next Paint)\n- Muc tieu: < 200ms\n- Optimize: Code splitting, avoid long tasks\n\n## CLS (Cumulative Layout Shift)\n- Muc tieu: < 0.1\n- Optimize: Image dimensions, avoid dynamic inserts','NONE');

-- === COURSE C04: Docker & Kubernetes ===
INSERT IGNORE INTO chapters (id, course_id, title, order_index, created_at) VALUES
('bb000000-0004-0000-0000-000000000001','c0000000-0000-0000-0000-000000000004','Docker Co Ban: Container va Image',1,DATE_SUB(NOW(),INTERVAL 140 DAY)),
('bb000000-0004-0000-0000-000000000002','c0000000-0000-0000-0000-000000000004','Docker Compose: Multi-Container',2,DATE_SUB(NOW(),INTERVAL 135 DAY)),
('bb000000-0004-0000-0000-000000000003','c0000000-0000-0000-0000-000000000004','Kubernetes Fundamentals',3,DATE_SUB(NOW(),INTERVAL 130 DAY)),
('bb000000-0004-0000-0000-000000000004','c0000000-0000-0000-0000-000000000004','K8s Advanced: Helm va Monitoring',4,DATE_SUB(NOW(),INTERVAL 125 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0004-0000-0000-000000000001','bb000000-0004-0000-0000-000000000001','Docker la gi? So sanh VM va Container',1,'VIDEO',1,DATE_SUB(NOW(),INTERVAL 140 DAY)),
('cc000000-0004-0000-0000-000000000002','bb000000-0004-0000-0000-000000000001','Docker Image: Layers va Dockerfile',2,'VIDEO',1,DATE_SUB(NOW(),INTERVAL 139 DAY)),
('cc000000-0004-0000-0000-000000000003','bb000000-0004-0000-0000-000000000001','Docker Container: Run, Stop, Logs, Exec',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 138 DAY)),
('cc000000-0004-0000-0000-000000000004','bb000000-0004-0000-0000-000000000001','Docker Network va Volume',4,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 137 DAY)),
('cc000000-0004-0000-0000-000000000005','bb000000-0004-0000-0000-000000000001','Docker Registry: Docker Hub va ECR',5,'TEXT',0,DATE_SUB(NOW(),INTERVAL 136 DAY)),
('cc000000-0004-0000-0000-000000000006','bb000000-0004-0000-0000-000000000001','Quiz: Docker Co Ban',6,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 135 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0004-0000-0000-000000000007','bb000000-0004-0000-0000-000000000002','Docker Compose: Chay nhieu container cung luc',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 135 DAY)),
('cc000000-0004-0000-0000-000000000008','bb000000-0004-0000-0000-000000000002','Environment Variables va Secrets trong Compose',2,'TEXT',0,DATE_SUB(NOW(),INTERVAL 134 DAY)),
('cc000000-0004-0000-0000-000000000009','bb000000-0004-0000-0000-000000000002','Thuc hanh: Spring Boot + MySQL + Redis voi Compose',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 133 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0004-0000-0000-000000000010','bb000000-0004-0000-0000-000000000003','Kubernetes Architecture: Master va Worker',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 130 DAY)),
('cc000000-0004-0000-0000-000000000011','bb000000-0004-0000-0000-000000000003','Pod, ReplicaSet, Deployment',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 129 DAY)),
('cc000000-0004-0000-0000-000000000012','bb000000-0004-0000-0000-000000000003','Service: ClusterIP, NodePort, LoadBalancer',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 128 DAY)),
('cc000000-0004-0000-0000-000000000013','bb000000-0004-0000-0000-000000000003','ConfigMap va Secret',4,'TEXT',0,DATE_SUB(NOW(),INTERVAL 127 DAY)),
('cc000000-0004-0000-0000-000000000014','bb000000-0004-0000-0000-000000000003','Persistent Volume va PVC',5,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 126 DAY)),
('cc000000-0004-0000-0000-000000000015','bb000000-0004-0000-0000-000000000003','Quiz: Kubernetes Fundamentals',6,'QUIZ',0,DATE_SUB(NOW(),INTERVAL 125 DAY));

INSERT IGNORE INTO lessons (id, chapter_id, title, order_index, lesson_type, is_preview, created_at) VALUES
('cc000000-0004-0000-0000-000000000016','bb000000-0004-0000-0000-000000000004','Helm: Package Manager cho K8s',1,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 125 DAY)),
('cc000000-0004-0000-0000-000000000017','bb000000-0004-0000-0000-000000000004','Monitoring voi Prometheus va Grafana',2,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 124 DAY)),
('cc000000-0004-0000-0000-000000000018','bb000000-0004-0000-0000-000000000004','Logging voi ELK Stack',3,'VIDEO',0,DATE_SUB(NOW(),INTERVAL 123 DAY));

-- Resources for C04
INSERT IGNORE INTO lesson_resources (id, lesson_id, resource_url, duration_seconds, video_status) VALUES
('dd000000-0004-0000-0000-000000000001','cc000000-0004-0000-0000-000000000001','https://cdn.coursehub.io/docker-intro.mp4',780,'READY'),
('dd000000-0004-0000-0000-000000000002','cc000000-0004-0000-0000-000000000002','https://cdn.coursehub.io/docker-image.mp4',1080,'READY'),
('dd000000-0004-0000-0000-000000000003','cc000000-0004-0000-0000-000000000003','https://cdn.coursehub.io/docker-container.mp4',960,'READY'),
('dd000000-0004-0000-0000-000000000004','cc000000-0004-0000-0000-000000000004','https://cdn.coursehub.io/docker-network.mp4',900,'READY'),
('dd000000-0004-0000-0000-000000000007','cc000000-0004-0000-0000-000000000007','https://cdn.coursehub.io/docker-compose.mp4',1200,'READY'),
('dd000000-0004-0000-0000-000000000009','cc000000-0004-0000-0000-000000000009','https://cdn.coursehub.io/docker-compose-lab.mp4',2400,'READY'),
('dd000000-0004-0000-0000-000000000010','cc000000-0004-0000-0000-000000000010','https://cdn.coursehub.io/k8s-arch.mp4',1080,'READY'),
('dd000000-0004-0000-0000-000000000011','cc000000-0004-0000-0000-000000000011','https://cdn.coursehub.io/k8s-pod.mp4',1320,'READY'),
('dd000000-0004-0000-0000-000000000012','cc000000-0004-0000-0000-000000000012','https://cdn.coursehub.io/k8s-service.mp4',960,'READY'),
('dd000000-0004-0000-0000-000000000014','cc000000-0004-0000-0000-000000000014','https://cdn.coursehub.io/k8s-pvc.mp4',840,'READY'),
('dd000000-0004-0000-0000-000000000016','cc000000-0004-0000-0000-000000000016','https://cdn.coursehub.io/helm-intro.mp4',1080,'READY'),
('dd000000-0004-0000-0000-000000000017','cc000000-0004-0000-0000-000000000017','https://cdn.coursehub.io/prometheus-grafana.mp4',1440,'READY'),
('dd000000-0004-0000-0000-000000000018','cc000000-0004-0000-0000-000000000018','https://cdn.coursehub.io/elk-stack.mp4',1200,'READY');

INSERT IGNORE INTO lesson_resources (id, lesson_id, text_content, video_status) VALUES
('dd000000-0004-0000-0000-000000000005','cc000000-0004-0000-0000-000000000005','# Docker Registry\n\n## Docker Hub\n- Registry cong khai mien phi\n- Public images: ubuntu, nginx, mysql...\n- Private repos (gioi han free tier)\n\n## Amazon ECR\n- Private registry tren AWS\n- Tich hop IAM\n- Quet vulnerability tu dong','NONE'),
('dd000000-0004-0000-0000-000000000008','cc000000-0004-0000-0000-000000000008','# Environment Variables trong Docker Compose\n\n## Cac cach truyen config:\n1. env_file: .env\n2. environment: key=value\n3. Docker Secrets (swarm)\n\n## Best practices:\n- Khong commit .env vao Git\n- Dung .env.example lam template\n- Secrets nhay cam dung Docker Secret','NONE'),
('dd000000-0004-0000-0000-000000000013','cc000000-0004-0000-0000-000000000013','# ConfigMap va Secret trong K8s\n\n## ConfigMap\n- Non-sensitive config\n- Mount as file hoac env var\n\n## Secret\n- Sensitive data (password, token)\n- Base64 encoded (KHONG phai encrypted)\n- Nen dung External Secrets Operator\n- Hoac HashiCorp Vault tich hop','NONE');

-- ============================================================
-- 10. QUIZ CONFIGS, QUESTIONS, ANSWERS
-- ============================================================
-- Quiz C01 Chapter 1: Hello World Spring Boot
INSERT IGNORE INTO quiz_configs (id, lesson_id, time_limit_minutes, passing_score, max_attempts, shuffle_questions, shuffle_answers) VALUES
('ee000000-0001-0000-0000-000000000005','cc000000-0001-0000-0000-000000000005',15,70.00,3,1,1),
('ee000000-0001-0000-0000-000000000010','cc000000-0001-0000-0000-000000000010',20,70.00,3,1,1),
('ee000000-0001-0000-0000-000000000016','cc000000-0001-0000-0000-000000000016',20,70.00,3,1,1),
('ee000000-0001-0000-0000-000000000021','cc000000-0001-0000-0000-000000000021',25,75.00,2,1,1),
('ee000000-0002-0000-0000-000000000005','cc000000-0002-0000-0000-000000000005',15,70.00,3,1,1),
('ee000000-0002-0000-0000-000000000009','cc000000-0002-0000-0000-000000000009',15,70.00,3,1,1),
('ee000000-0002-0000-0000-000000000014','cc000000-0002-0000-0000-000000000014',20,70.00,3,1,1),
('ee000000-0004-0000-0000-000000000006','cc000000-0004-0000-0000-000000000006',15,70.00,3,1,1),
('ee000000-0004-0000-0000-000000000015','cc000000-0004-0000-0000-000000000015',20,75.00,2,1,1);

-- Questions for ls01-005 (Spring Boot co ban)
INSERT IGNORE INTO questions (id, quiz_id, content, question_type, points, order_index, explanation) VALUES
('ff000000-0001-0000-0000-000000000001','cc000000-0001-0000-0000-000000000005','Spring Boot khac voi Spring Framework o diem gi chinh?','SINGLE_CHOICE',1.00,1,'Spring Boot cung cap auto-configuration va embedded server, giam thieu cau hinh XML.'),
('ff000000-0001-0000-0000-000000000002','cc000000-0001-0000-0000-000000000005','Annotation nao duoc dung de danh dau entry point cua Spring Boot application?','SINGLE_CHOICE',1.00,2,'@SpringBootApplication la combination cua @Configuration, @EnableAutoConfiguration va @ComponentScan.'),
('ff000000-0001-0000-0000-000000000003','cc000000-0001-0000-0000-000000000005','File nao chua cau hinh ung dung Spring Boot mac dinh?','SINGLE_CHOICE',1.00,3,'application.yml hoac application.properties o src/main/resources.'),
('ff000000-0001-0000-0000-000000000004','cc000000-0001-0000-0000-000000000005','Spring Boot co the embed server nao?','MULTIPLE_CHOICE',2.00,4,'Spring Boot ho tro Tomcat (mac dinh), Jetty va Undertow.'),
('ff000000-0001-0000-0000-000000000005','cc000000-0001-0000-0000-000000000005','@SpringBootApplication bao gom cac annotation nao?','MULTIPLE_CHOICE',2.00,5,'Bao gom @Configuration, @EnableAutoConfiguration va @ComponentScan.');

INSERT IGNORE INTO answers (id, question_id, content, is_correct, order_index) VALUES
('ab000000-0001-0001-0000-000000000001','ff000000-0001-0000-0000-000000000001','Spring Boot yeu cau cau hinh nhieu XML hon',0,1),
('ab000000-0001-0001-0000-000000000002','ff000000-0001-0000-0000-000000000001','Spring Boot cung cap auto-configuration va embedded server',1,2),
('ab000000-0001-0001-0000-000000000003','ff000000-0001-0000-0000-000000000001','Spring Boot chi ho tro REST API',0,3),
('ab000000-0001-0001-0000-000000000004','ff000000-0001-0000-0000-000000000001','Spring Boot khong can Dependency Injection',0,4),
('ab000000-0001-0002-0000-000000000001','ff000000-0001-0000-0000-000000000002','@SpringApplication',0,1),
('ab000000-0001-0002-0000-000000000002','ff000000-0001-0000-0000-000000000002','@EnableSpringBoot',0,2),
('ab000000-0001-0002-0000-000000000003','ff000000-0001-0000-0000-000000000002','@SpringBootApplication',1,3),
('ab000000-0001-0002-0000-000000000004','ff000000-0001-0000-0000-000000000002','@BootApplication',0,4),
('ab000000-0001-0003-0000-000000000001','ff000000-0001-0000-0000-000000000003','pom.xml',0,1),
('ab000000-0001-0003-0000-000000000002','ff000000-0001-0000-0000-000000000003','build.gradle',0,2),
('ab000000-0001-0003-0000-000000000003','ff000000-0001-0000-0000-000000000003','application.yml hoac application.properties',1,3),
('ab000000-0001-0003-0000-000000000004','ff000000-0001-0000-0000-000000000003','web.xml',0,4),
('ab000000-0001-0004-0000-000000000001','ff000000-0001-0000-0000-000000000004','Tomcat',1,1),
('ab000000-0001-0004-0000-000000000002','ff000000-0001-0000-0000-000000000004','JBoss',0,2),
('ab000000-0001-0004-0000-000000000003','ff000000-0001-0000-0000-000000000004','Jetty',1,3),
('ab000000-0001-0004-0000-000000000004','ff000000-0001-0000-0000-000000000004','Undertow',1,4),
('ab000000-0001-0005-0000-000000000001','ff000000-0001-0000-0000-000000000005','@Configuration',1,1),
('ab000000-0001-0005-0000-000000000002','ff000000-0001-0000-0000-000000000005','@Transactional',0,2),
('ab000000-0001-0005-0000-000000000003','ff000000-0001-0000-0000-000000000005','@EnableAutoConfiguration',1,3),
('ab000000-0001-0005-0000-000000000004','ff000000-0001-0000-0000-000000000005','@ComponentScan',1,4);

-- Questions for ls01-010 (REST API)
INSERT IGNORE INTO questions (id, quiz_id, content, question_type, points, order_index, explanation) VALUES
('ff000000-0001-0000-0000-000000000006','cc000000-0001-0000-0000-000000000010','HTTP method nao duoc dung de tao moi resource theo REST convention?','SINGLE_CHOICE',1.00,1,'POST duoc dung de tao moi resource. PUT/PATCH de cap nhat.'),
('ff000000-0001-0000-0000-000000000007','cc000000-0001-0000-0000-000000000010','HTTP status code nao tra ve khi tao resource thanh cong?','SINGLE_CHOICE',1.00,2,'201 Created la status code chuan cho tao resource thanh cong.'),
('ff000000-0001-0000-0000-000000000008','cc000000-0001-0000-0000-000000000010','Annotation nao duoc dung de map HTTP GET request?','SINGLE_CHOICE',1.00,3,'@GetMapping la shortcut cua @RequestMapping(method=GET).'),
('ff000000-0001-0000-0000-000000000009','cc000000-0001-0000-0000-000000000010','REST API nen tra ve HTTP 404 khi nao?','SINGLE_CHOICE',1.00,4,'404 Not Found khi resource khong ton tai.'),
('ff000000-0001-0000-0000-000000000010','cc000000-0001-0000-0000-000000000010','Nhung annotation nao duoc dung de lay tham so tu request?','MULTIPLE_CHOICE',2.00,5,'@PathVariable tu URL, @RequestParam tu query string, @RequestBody tu request body.');

INSERT IGNORE INTO answers (id, question_id, content, is_correct, order_index) VALUES
('ab000000-0001-0006-0000-000000000001','ff000000-0001-0000-0000-000000000006','GET',0,1),('ab000000-0001-0006-0000-000000000002','ff000000-0001-0000-0000-000000000006','POST',1,2),('ab000000-0001-0006-0000-000000000003','ff000000-0001-0000-0000-000000000006','PUT',0,3),('ab000000-0001-0006-0000-000000000004','ff000000-0001-0000-0000-000000000006','DELETE',0,4),
('ab000000-0001-0007-0000-000000000001','ff000000-0001-0000-0000-000000000007','200 OK',0,1),('ab000000-0001-0007-0000-000000000002','ff000000-0001-0000-0000-000000000007','201 Created',1,2),('ab000000-0001-0007-0000-000000000003','ff000000-0001-0000-0000-000000000007','204 No Content',0,3),('ab000000-0001-0007-0000-000000000004','ff000000-0001-0000-0000-000000000007','202 Accepted',0,4),
('ab000000-0001-0008-0000-000000000001','ff000000-0001-0000-0000-000000000008','@GetRequest',0,1),('ab000000-0001-0008-0000-000000000002','ff000000-0001-0000-0000-000000000008','@GetMapping',1,2),('ab000000-0001-0008-0000-000000000003','ff000000-0001-0000-0000-000000000008','@RequestGet',0,3),('ab000000-0001-0008-0000-000000000004','ff000000-0001-0000-0000-000000000008','@HttpGet',0,4),
('ab000000-0001-0009-0000-000000000001','ff000000-0001-0000-0000-000000000009','Khi server bi loi',0,1),('ab000000-0001-0009-0000-000000000002','ff000000-0001-0000-0000-000000000009','Khi request khong hop le',0,2),('ab000000-0001-0009-0000-000000000003','ff000000-0001-0000-0000-000000000009','Khi resource khong ton tai',1,3),('ab000000-0001-0009-0000-000000000004','ff000000-0001-0000-0000-000000000009','Khi khong co quyen truy cap',0,4),
('ab000000-0001-0010-0000-000000000001','ff000000-0001-0000-0000-000000000010','@PathVariable',1,1),('ab000000-0001-0010-0000-000000000002','ff000000-0001-0000-0000-000000000010','@RequestParam',1,2),('ab000000-0001-0010-0000-000000000003','ff000000-0001-0000-0000-000000000010','@RequestBody',1,3),('ab000000-0001-0010-0000-000000000004','ff000000-0001-0000-0000-000000000010','@Header',0,4);

-- Questions for ls02-005 (React Hooks)
INSERT IGNORE INTO questions (id, quiz_id, content, question_type, points, order_index, explanation) VALUES
('ff000000-0002-0000-0000-000000000001','cc000000-0002-0000-0000-000000000005','Hook nao duoc dung de quan ly local state trong functional component?','SINGLE_CHOICE',1.00,1,'useState la hook co ban nhat de quan ly state.'),
('ff000000-0002-0000-0000-000000000002','cc000000-0002-0000-0000-000000000005','useEffect voi dependency array rong [] chay khi nao?','SINGLE_CHOICE',1.00,2,'Khi dep array la [], useEffect chi chay 1 lan sau khi component mount.'),
('ff000000-0002-0000-0000-000000000003','cc000000-0002-0000-0000-000000000005','useMemo duoc dung de lam gi?','SINGLE_CHOICE',1.00,3,'useMemo memo hoa ket qua tinh toan de tranh tinh toan lai khong can thiet.'),
('ff000000-0002-0000-0000-000000000004','cc000000-0002-0000-0000-000000000005','Hook nao khong hop le voi React rules of hooks?','SINGLE_CHOICE',1.00,4,'Khong duoc goi hook ben trong conditional, loop hoac nested function.'),
('ff000000-0002-0000-0000-000000000005','cc000000-0002-0000-0000-000000000005','useCallback tra ve gi?','SINGLE_CHOICE',1.00,5,'useCallback tra ve memoized version cua callback function.');

INSERT IGNORE INTO answers (id, question_id, content, is_correct, order_index) VALUES
('ab000000-0002-0001-0000-000000000001','ff000000-0002-0000-0000-000000000001','useEffect',0,1),('ab000000-0002-0001-0000-000000000002','ff000000-0002-0000-0000-000000000001','useState',1,2),('ab000000-0002-0001-0000-000000000003','ff000000-0002-0000-0000-000000000001','useContext',0,3),('ab000000-0002-0001-0000-000000000004','ff000000-0002-0000-0000-000000000001','useReducer',0,4),
('ab000000-0002-0002-0000-000000000001','ff000000-0002-0000-0000-000000000002','Moi re-render',0,1),('ab000000-0002-0002-0000-000000000002','ff000000-0002-0000-0000-000000000002','Chi khi unmount',0,2),('ab000000-0002-0002-0000-000000000003','ff000000-0002-0000-0000-000000000002','Chi 1 lan sau mount',1,3),('ab000000-0002-0002-0000-000000000004','ff000000-0002-0000-0000-000000000002','Khong bao gio',0,4),
('ab000000-0002-0003-0000-000000000001','ff000000-0002-0000-0000-000000000003','Thuc hien side effect',0,1),('ab000000-0002-0003-0000-000000000002','ff000000-0002-0000-0000-000000000003','Memo hoa ket qua tinh toan',1,2),('ab000000-0002-0003-0000-000000000003','ff000000-0002-0000-0000-000000000003','Quan ly state',0,3),('ab000000-0002-0003-0000-000000000004','ff000000-0002-0000-0000-000000000003','Tao ref',0,4),
('ab000000-0002-0004-0000-000000000001','ff000000-0002-0000-0000-000000000004','Goi hook o dau function component',0,1),('ab000000-0002-0004-0000-000000000002','ff000000-0002-0000-0000-000000000004','Goi hook ben trong if statement',1,2),('ab000000-0002-0004-0000-000000000003','ff000000-0002-0000-0000-000000000004','Goi nhieu useState',0,3),('ab000000-0002-0004-0000-000000000004','ff000000-0002-0000-0000-000000000004','Goi hook o custom hook',0,4),
('ab000000-0002-0005-0000-000000000001','ff000000-0002-0000-0000-000000000005','State moi',0,1),('ab000000-0002-0005-0000-000000000002','ff000000-0002-0000-0000-000000000005','Ref object',0,2),('ab000000-0002-0005-0000-000000000003','ff000000-0002-0000-0000-000000000005','Memoized callback function',1,3),('ab000000-0002-0005-0000-000000000004','ff000000-0002-0000-0000-000000000005','Context value',0,4);

-- Questions for ls04-006 (Docker co ban)
INSERT IGNORE INTO questions (id, quiz_id, content, question_type, points, order_index, explanation) VALUES
('ff000000-0004-0000-0000-000000000001','cc000000-0004-0000-0000-000000000006','Docker container khac gi voi Virtual Machine?','SINGLE_CHOICE',1.00,1,'Container chia se OS kernel voi host, nhe hon va khoi dong nhanh hon VM.'),
('ff000000-0004-0000-0000-000000000002','cc000000-0004-0000-0000-000000000006','Lenh nao de chay container tu image?','SINGLE_CHOICE',1.00,2,'docker run de tao va chay container moi tu image.'),
('ff000000-0004-0000-0000-000000000003','cc000000-0004-0000-0000-000000000006','Dockerfile instruction nao duoc chay khi container khoi dong?','SINGLE_CHOICE',1.00,3,'CMD hoac ENTRYPOINT dinh nghia lenh chay khi container start.'),
('ff000000-0004-0000-0000-000000000004','cc000000-0004-0000-0000-000000000006','Docker Volume duoc dung de lam gi?','SINGLE_CHOICE',1.00,4,'Volume dung de luu tru du lieu ben ngoai container lifecycle.'),
('ff000000-0004-0000-0000-000000000005','cc000000-0004-0000-0000-000000000006','Nhung lenh Dockerfile nao tao ra Image layer moi?','MULTIPLE_CHOICE',2.00,5,'RUN, COPY va ADD tao layer moi. ENV, CMD khong tao layer.');

INSERT IGNORE INTO answers (id, question_id, content, is_correct, order_index) VALUES
('ab000000-0004-0001-0000-000000000001','ff000000-0004-0000-0000-000000000001','Container toan bo OS rieng',0,1),('ab000000-0004-0001-0000-000000000002','ff000000-0004-0000-0000-000000000001','Container chia se OS kernel, nhe va nhanh hon',1,2),('ab000000-0004-0001-0000-000000000003','ff000000-0004-0000-0000-000000000001','Container cham hon VM',0,3),('ab000000-0004-0001-0000-000000000004','ff000000-0004-0000-0000-000000000001','Khong co su khac biet',0,4),
('ab000000-0004-0002-0000-000000000001','ff000000-0004-0000-0000-000000000006','docker start',0,1),('ab000000-0004-0002-0000-000000000002','ff000000-0004-0000-0000-000000000006','docker run',1,2),('ab000000-0004-0002-0000-000000000003','ff000000-0004-0000-0000-000000000006','docker exec',0,3),('ab000000-0004-0002-0000-000000000004','ff000000-0004-0000-0000-000000000006','docker create',0,4),
('ab000000-0004-0003-0000-000000000001','ff000000-0004-0000-0000-000000000003','FROM',0,1),('ab000000-0004-0003-0000-000000000002','ff000000-0004-0000-0000-000000000003','WORKDIR',0,2),('ab000000-0004-0003-0000-000000000003','ff000000-0004-0000-0000-000000000003','CMD',1,3),('ab000000-0004-0003-0000-000000000004','ff000000-0004-0000-0000-000000000003','COPY',0,4),
('ab000000-0004-0004-0000-000000000001','ff000000-0004-0000-0000-000000000004','Chia se network giua containers',0,1),('ab000000-0004-0004-0000-000000000002','ff000000-0004-0000-0000-000000000004','Luu tru du lieu ben ngoai container lifecycle',1,2),('ab000000-0004-0004-0000-000000000003','ff000000-0004-0000-0000-000000000004','Dinh nghia bien moi truong',0,3),('ab000000-0004-0004-0000-000000000004','ff000000-0004-0000-0000-000000000004','Gioi han CPU su dung',0,4),
('ab000000-0004-0005-0000-000000000001','ff000000-0004-0000-0000-000000000005','RUN',1,1),('ab000000-0004-0005-0000-000000000002','ff000000-0004-0000-0000-000000000005','CMD',0,2),('ab000000-0004-0005-0000-000000000003','ff000000-0004-0000-0000-000000000005','COPY',1,3),('ab000000-0004-0005-0000-000000000004','ff000000-0004-0000-0000-000000000005','ADD',1,4);

-- ============================================================
-- 11. ENROLLMENTS (50+ bản ghi)
-- student1-10 enrolled in nhiều course PUBLISHED
-- ============================================================
-- student1 (00000000-0000-0000-0001-000000000001): C01, C02, C04, C07, C12, C17
-- student2: C01, C03, C05, C09, C17
-- student3: C02, C04, C06, C09
-- student4: C01, C02, C09
-- student5: C03, C04, C14, C09
-- student6: C17, C01
-- student7: C03, C13, C08
-- student8: C02, C07, C11
-- student9: C01, C06
-- student10: C05, C10, C20
-- student11: C04, C14
-- student12: C01, C17
-- student13: C05, C20
-- student14: C02, C07
-- student15: C01, C12
-- student16: C05
-- student17: C09, C17
-- student18: C02
-- student19: C01
-- student20: C04, C09

INSERT IGNORE INTO enrollments (id, user_id, course_id, enrollment_date, progress_percent, status) VALUES
-- student1 enrollments (varied status)
('ac000000-0001-0001-0000-000000000000','00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 170 DAY),100.00,'COMPLETED'),
('ac000000-0001-0002-0000-000000000000','00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000002',DATE_SUB(NOW(),INTERVAL 150 DAY),75.50,'ACTIVE'),
('ac000000-0001-0004-0000-000000000000','00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000004',DATE_SUB(NOW(),INTERVAL 130 DAY),45.00,'ACTIVE'),
('ac000000-0001-0007-0000-000000000000','00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000007',DATE_SUB(NOW(),INTERVAL 100 DAY),20.00,'ACTIVE'),
('ac000000-0001-000c-0000-000000000000','00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000012',DATE_SUB(NOW(),INTERVAL 60 DAY),10.00,'ACTIVE'),
('ac000000-0001-0011-0000-000000000000','00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000017',DATE_SUB(NOW(),INTERVAL 40 DAY),100.00,'COMPLETED'),
-- student2
('ac000000-0002-0001-0000-000000000000','00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 160 DAY),100.00,'COMPLETED'),
('ac000000-0002-0003-0000-000000000000','00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000003',DATE_SUB(NOW(),INTERVAL 140 DAY),60.00,'ACTIVE'),
('ac000000-0002-0005-0000-000000000000','00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000005',DATE_SUB(NOW(),INTERVAL 120 DAY),30.00,'ACTIVE'),
('ac000000-0002-0009-0000-000000000000','00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000009',DATE_SUB(NOW(),INTERVAL 80 DAY),100.00,'COMPLETED'),
('ac000000-0002-0011-0000-000000000000','00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000017',DATE_SUB(NOW(),INTERVAL 50 DAY),85.00,'ACTIVE'),
-- student3
('ac000000-0003-0002-0000-000000000000','00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000002',DATE_SUB(NOW(),INTERVAL 155 DAY),100.00,'COMPLETED'),
('ac000000-0003-0004-0000-000000000000','00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000004',DATE_SUB(NOW(),INTERVAL 135 DAY),90.00,'ACTIVE'),
('ac000000-0003-0006-0000-000000000000','00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000006',DATE_SUB(NOW(),INTERVAL 110 DAY),55.00,'ACTIVE'),
('ac000000-0003-0009-0000-000000000000','00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000009',DATE_SUB(NOW(),INTERVAL 70 DAY),100.00,'COMPLETED'),
-- student4
('ac000000-0004-0001-0000-000000000000','00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 150 DAY),100.00,'COMPLETED'),
('ac000000-0004-0002-0000-000000000000','00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000002',DATE_SUB(NOW(),INTERVAL 130 DAY),40.00,'ACTIVE'),
('ac000000-0004-0009-0000-000000000000','00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000009',DATE_SUB(NOW(),INTERVAL 60 DAY),70.00,'ACTIVE'),
-- student5
('ac000000-0005-0003-0000-000000000000','00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000003',DATE_SUB(NOW(),INTERVAL 145 DAY),100.00,'COMPLETED'),
('ac000000-0005-0004-0000-000000000000','00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000004',DATE_SUB(NOW(),INTERVAL 125 DAY),100.00,'COMPLETED'),
('ac000000-0005-000e-0000-000000000000','00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000014',DATE_SUB(NOW(),INTERVAL 55 DAY),65.00,'ACTIVE'),
('ac000000-0005-0009-0000-000000000000','00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000009',DATE_SUB(NOW(),INTERVAL 30 DAY),25.00,'ACTIVE'),
-- student6
('ac000000-0006-0011-0000-000000000000','00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000017',DATE_SUB(NOW(),INTERVAL 140 DAY),100.00,'COMPLETED'),
('ac000000-0006-0001-0000-000000000000','00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 90 DAY),50.00,'ACTIVE'),
-- student7
('ac000000-0007-0003-0000-000000000000','00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000003',DATE_SUB(NOW(),INTERVAL 130 DAY),80.00,'ACTIVE'),
('ac000000-0007-000d-0000-000000000000','00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000013',DATE_SUB(NOW(),INTERVAL 90 DAY),45.00,'ACTIVE'),
('ac000000-0007-0008-0000-000000000000','00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000008',DATE_SUB(NOW(),INTERVAL 50 DAY),15.00,'ACTIVE'),
-- student8
('ac000000-0008-0002-0000-000000000000','00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000002',DATE_SUB(NOW(),INTERVAL 120 DAY),100.00,'COMPLETED'),
('ac000000-0008-0007-0000-000000000000','00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000007',DATE_SUB(NOW(),INTERVAL 80 DAY),55.00,'ACTIVE'),
('ac000000-0008-000b-0000-000000000000','00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000011',DATE_SUB(NOW(),INTERVAL 40 DAY),30.00,'ACTIVE'),
-- student9
('ac000000-0009-0001-0000-000000000000','00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 110 DAY),100.00,'COMPLETED'),
('ac000000-0009-0006-0000-000000000000','00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000006',DATE_SUB(NOW(),INTERVAL 70 DAY),40.00,'ACTIVE'),
-- student10
('ac000000-000a-0005-0000-000000000000','00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000005',DATE_SUB(NOW(),INTERVAL 100 DAY),60.00,'ACTIVE'),
('ac000000-000a-000a-0000-000000000000','00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000010',DATE_SUB(NOW(),INTERVAL 70 DAY),35.00,'ACTIVE'),
('ac000000-000a-0014-0000-000000000000','00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000020',DATE_SUB(NOW(),INTERVAL 20 DAY),10.00,'ACTIVE'),
-- student11-20
('ac000000-000b-0004-0000-000000000000','00000000-0000-0000-0001-000000000011','c0000000-0000-0000-0000-000000000004',DATE_SUB(NOW(),INTERVAL 90 DAY),100.00,'COMPLETED'),
('ac000000-000b-000e-0000-000000000000','00000000-0000-0000-0001-000000000011','c0000000-0000-0000-0000-000000000014',DATE_SUB(NOW(),INTERVAL 50 DAY),70.00,'ACTIVE'),
('ac000000-000c-0001-0000-000000000000','00000000-0000-0000-0001-000000000012','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 80 DAY),100.00,'COMPLETED'),
('ac000000-000c-0011-0000-000000000000','00000000-0000-0000-0001-000000000012','c0000000-0000-0000-0000-000000000017',DATE_SUB(NOW(),INTERVAL 35 DAY),60.00,'ACTIVE'),
('ac000000-000d-0005-0000-000000000000','00000000-0000-0000-0001-000000000013','c0000000-0000-0000-0000-000000000005',DATE_SUB(NOW(),INTERVAL 70 DAY),45.00,'ACTIVE'),
('ac000000-000d-0014-0000-000000000000','00000000-0000-0000-0001-000000000013','c0000000-0000-0000-0000-000000000020',DATE_SUB(NOW(),INTERVAL 20 DAY),20.00,'ACTIVE'),
('ac000000-000e-0002-0000-000000000000','00000000-0000-0000-0001-000000000014','c0000000-0000-0000-0000-000000000002',DATE_SUB(NOW(),INTERVAL 60 DAY),55.00,'ACTIVE'),
('ac000000-000e-0007-0000-000000000000','00000000-0000-0000-0001-000000000014','c0000000-0000-0000-0000-000000000007',DATE_SUB(NOW(),INTERVAL 25 DAY),10.00,'ACTIVE'),
('ac000000-000f-0001-0000-000000000000','00000000-0000-0000-0001-000000000015','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 55 DAY),80.00,'ACTIVE'),
('ac000000-000f-000c-0000-000000000000','00000000-0000-0000-0001-000000000015','c0000000-0000-0000-0000-000000000012',DATE_SUB(NOW(),INTERVAL 20 DAY),5.00,'ACTIVE'),
('ac000000-0010-0005-0000-000000000000','00000000-0000-0000-0001-000000000016','c0000000-0000-0000-0000-000000000005',DATE_SUB(NOW(),INTERVAL 45 DAY),25.00,'ACTIVE'),
('ac000000-0011-0009-0000-000000000000','00000000-0000-0000-0001-000000000017','c0000000-0000-0000-0000-000000000009',DATE_SUB(NOW(),INTERVAL 35 DAY),100.00,'COMPLETED'),
('ac000000-0011-0011-0000-000000000000','00000000-0000-0000-0001-000000000017','c0000000-0000-0000-0000-000000000017',DATE_SUB(NOW(),INTERVAL 15 DAY),45.00,'ACTIVE'),
('ac000000-0012-0002-0000-000000000000','00000000-0000-0000-0001-000000000018','c0000000-0000-0000-0000-000000000002',DATE_SUB(NOW(),INTERVAL 25 DAY),20.00,'ACTIVE'),
('ac000000-0013-0001-0000-000000000000','00000000-0000-0000-0001-000000000019','c0000000-0000-0000-0000-000000000001',DATE_SUB(NOW(),INTERVAL 15 DAY),10.00,'ACTIVE'),
('ac000000-0014-0004-0000-000000000000','00000000-0000-0000-0001-000000000020','c0000000-0000-0000-0000-000000000004',DATE_SUB(NOW(),INTERVAL 8 DAY),5.00,'ACTIVE'),
('ac000000-0014-0009-0000-000000000000','00000000-0000-0000-0001-000000000020','c0000000-0000-0000-0000-000000000009',DATE_SUB(NOW(),INTERVAL 5 DAY),2.00,'ACTIVE');

-- ============================================================
-- 12. PROGRESS (Bai hoc da hoan thanh)
-- ============================================================
-- student1 da hoan thanh C01 (ls01-001 to ls01-025)
INSERT IGNORE INTO progress (id, enrollment_id, lesson_id, is_completed, completed_at) VALUES
('ad000000-0001-0001-0000-000000000001','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000001',1,DATE_SUB(NOW(),INTERVAL 165 DAY)),
('ad000000-0001-0001-0000-000000000002','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000002',1,DATE_SUB(NOW(),INTERVAL 165 DAY)),
('ad000000-0001-0001-0000-000000000003','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000003',1,DATE_SUB(NOW(),INTERVAL 163 DAY)),
('ad000000-0001-0001-0000-000000000004','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000004',1,DATE_SUB(NOW(),INTERVAL 163 DAY)),
('ad000000-0001-0001-0000-000000000005','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000005',1,DATE_SUB(NOW(),INTERVAL 162 DAY)),
('ad000000-0001-0001-0000-000000000006','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000006',1,DATE_SUB(NOW(),INTERVAL 160 DAY)),
('ad000000-0001-0001-0000-000000000007','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000007',1,DATE_SUB(NOW(),INTERVAL 159 DAY)),
('ad000000-0001-0001-0000-000000000008','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000008',1,DATE_SUB(NOW(),INTERVAL 158 DAY)),
('ad000000-0001-0001-0000-000000000009','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000009',1,DATE_SUB(NOW(),INTERVAL 157 DAY)),
('ad000000-0001-0001-0000-000000000010','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000010',1,DATE_SUB(NOW(),INTERVAL 156 DAY)),
('ad000000-0001-0001-0000-000000000011','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000011',1,DATE_SUB(NOW(),INTERVAL 154 DAY)),
('ad000000-0001-0001-0000-000000000012','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000012',1,DATE_SUB(NOW(),INTERVAL 153 DAY)),
('ad000000-0001-0001-0000-000000000013','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000013',1,DATE_SUB(NOW(),INTERVAL 152 DAY)),
('ad000000-0001-0001-0000-000000000014','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000014',1,DATE_SUB(NOW(),INTERVAL 151 DAY)),
('ad000000-0001-0001-0000-000000000015','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000015',1,DATE_SUB(NOW(),INTERVAL 150 DAY));

-- student1 progress C17 (COMPLETED)
INSERT IGNORE INTO progress (id, enrollment_id, lesson_id, is_completed, completed_at) VALUES
('ad000000-0001-0011-0000-000000000001','ac000000-0001-0011-0000-000000000000','cc000000-0011-0000-0000-000000000001',1,DATE_SUB(NOW(),INTERVAL 38 DAY)),
('ad000000-0001-0011-0000-000000000002','ac000000-0001-0011-0000-000000000000','cc000000-0011-0000-0000-000000000002',1,DATE_SUB(NOW(),INTERVAL 37 DAY)),
('ad000000-0001-0011-0000-000000000003','ac000000-0001-0011-0000-000000000000','cc000000-0011-0000-0000-000000000003',1,DATE_SUB(NOW(),INTERVAL 36 DAY));

-- student2 progress (C01 COMPLETED)
INSERT IGNORE INTO progress (id, enrollment_id, lesson_id, is_completed, completed_at) VALUES
('ad000000-0002-0001-0000-000000000001','ac000000-0002-0001-0000-000000000000','cc000000-0001-0000-0000-000000000001',1,DATE_SUB(NOW(),INTERVAL 155 DAY)),
('ad000000-0002-0001-0000-000000000002','ac000000-0002-0001-0000-000000000000','cc000000-0001-0000-0000-000000000002',1,DATE_SUB(NOW(),INTERVAL 154 DAY)),
('ad000000-0002-0001-0000-000000000003','ac000000-0002-0001-0000-000000000000','cc000000-0001-0000-0000-000000000003',1,DATE_SUB(NOW(),INTERVAL 153 DAY)),
('ad000000-0002-0001-0000-000000000004','ac000000-0002-0001-0000-000000000000','cc000000-0001-0000-0000-000000000004',1,DATE_SUB(NOW(),INTERVAL 152 DAY)),
('ad000000-0002-0001-0000-000000000005','ac000000-0002-0001-0000-000000000000','cc000000-0001-0000-0000-000000000005',1,DATE_SUB(NOW(),INTERVAL 151 DAY));

-- student2 C09 progress
INSERT IGNORE INTO progress (id, enrollment_id, lesson_id, is_completed, completed_at) VALUES
('ad000000-0002-0009-0000-000000000001','ac000000-0002-0009-0000-000000000000','cc000000-0009-0000-0000-000000000001',1,DATE_SUB(NOW(),INTERVAL 79 DAY)),
('ad000000-0002-0009-0000-000000000002','ac000000-0002-0009-0000-000000000000','cc000000-0009-0000-0000-000000000002',1,DATE_SUB(NOW(),INTERVAL 78 DAY)),
('ad000000-0002-0009-0000-000000000003','ac000000-0002-0009-0000-000000000000','cc000000-0009-0000-0000-000000000003',1,DATE_SUB(NOW(),INTERVAL 77 DAY));

-- student3 C02 COMPLETED
INSERT IGNORE INTO progress (id, enrollment_id, lesson_id, is_completed, completed_at) VALUES
('ad000000-0003-0002-0000-000000000001','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000001',1,DATE_SUB(NOW(),INTERVAL 150 DAY)),
('ad000000-0003-0002-0000-000000000002','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000002',1,DATE_SUB(NOW(),INTERVAL 149 DAY)),
('ad000000-0003-0002-0000-000000000003','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000003',1,DATE_SUB(NOW(),INTERVAL 148 DAY)),
('ad000000-0003-0002-0000-000000000004','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000004',1,DATE_SUB(NOW(),INTERVAL 147 DAY)),
('ad000000-0003-0002-0000-000000000005','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000005',1,DATE_SUB(NOW(),INTERVAL 146 DAY));

-- student8 C02 COMPLETED progress
INSERT IGNORE INTO progress (id, enrollment_id, lesson_id, is_completed, completed_at) VALUES
('ad000000-0008-0002-0000-000000000001','ac000000-0008-0002-0000-000000000000','cc000000-0002-0000-0000-000000000001',1,DATE_SUB(NOW(),INTERVAL 118 DAY)),
('ad000000-0008-0002-0000-000000000002','ac000000-0008-0002-0000-000000000000','cc000000-0002-0000-0000-000000000002',1,DATE_SUB(NOW(),INTERVAL 117 DAY)),
('ad000000-0008-0002-0000-000000000003','ac000000-0008-0002-0000-000000000000','cc000000-0002-0000-0000-000000000003',1,DATE_SUB(NOW(),INTERVAL 116 DAY));

-- ============================================================
-- 13. QUIZ ATTEMPTS
-- ============================================================
-- student1 quiz attempts for C01
INSERT IGNORE INTO quiz_attempts (id, enrollment_id, lesson_id, score, status, started_at, submitted_at) VALUES
('ae000000-0001-0005-0001-000000000001','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000005',80.00,'PASSED',DATE_SUB(NOW(),INTERVAL 162 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 162 DAY), INTERVAL 520 SECOND)),
('ae000000-0001-0010-0001-000000000001','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000010',60.00,'FAILED',DATE_SUB(NOW(),INTERVAL 156 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 156 DAY), INTERVAL 780 SECOND)),
('ae000000-0001-0010-0001-000000000002','ac000000-0001-0001-0000-000000000000','cc000000-0001-0000-0000-000000000010',90.00,'PASSED',DATE_SUB(NOW(),INTERVAL 155 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 155 DAY), INTERVAL 650 SECOND));

-- student2 quiz attempts
INSERT IGNORE INTO quiz_attempts (id, enrollment_id, lesson_id, score, status, started_at, submitted_at) VALUES
('ae000000-0002-0005-0001-000000000001','ac000000-0002-0001-0000-000000000000','cc000000-0001-0000-0000-000000000005',70.00,'PASSED',DATE_SUB(NOW(),INTERVAL 151 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 151 DAY), INTERVAL 600 SECOND)),
('ae000000-0002-0004-0009-000000000001','ac000000-0002-0009-0000-000000000000','cc000000-0004-0000-0000-000000000006',50.00,'FAILED',DATE_SUB(NOW(),INTERVAL 79 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 79 DAY), INTERVAL 400 SECOND)),
('ae000000-0002-0004-0009-000000000002','ac000000-0002-0009-0000-000000000000','cc000000-0004-0000-0000-000000000006',80.00,'PASSED',DATE_SUB(NOW(),INTERVAL 78 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 78 DAY), INTERVAL 480 SECOND));

-- student3 quiz attempts
INSERT IGNORE INTO quiz_attempts (id, enrollment_id, lesson_id, score, status, started_at, submitted_at) VALUES
('ae000000-0003-0005-0002-000000000001','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000005',100.00,'PASSED',DATE_SUB(NOW(),INTERVAL 146 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 146 DAY), INTERVAL 450 SECOND)),
('ae000000-0003-0009-0002-000000000001','ac000000-0003-0002-0000-000000000000','cc000000-0002-0000-0000-000000000009',85.00,'PASSED',DATE_SUB(NOW(),INTERVAL 140 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 140 DAY), INTERVAL 500 SECOND));

-- student5 quiz attempts (C04 COMPLETED)
INSERT IGNORE INTO quiz_attempts (id, enrollment_id, lesson_id, score, status, started_at, submitted_at) VALUES
('ae000000-0005-0006-0004-000000000001','ac000000-0005-0004-0000-000000000000','cc000000-0004-0000-0000-000000000006',75.00,'PASSED',DATE_SUB(NOW(),INTERVAL 124 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 124 DAY), INTERVAL 560 SECOND)),
('ae000000-0005-000f-0004-000000000001','ac000000-0005-0004-0000-000000000000','cc000000-0004-0000-0000-000000000015',80.00,'PASSED',DATE_SUB(NOW(),INTERVAL 115 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 115 DAY), INTERVAL 700 SECOND));

-- ============================================================
-- 14. REVIEWS (Chi user co enrollment COMPLETED moi review)
-- Enrollment COMPLETED: s01-c01, s01-c17, s02-c01, s02-c09,
-- s03-c02, s03-c09, s04-c01, s05-c03, s05-c04, s06-c17,
-- s08-c02, s09-c01, s11-c04, s12-c01, s17-c09
-- ============================================================
INSERT IGNORE INTO reviews (id, enrollment_id, rating, comment, is_hidden, created_at, updated_at) VALUES
('af000000-0001-0001-0000-000000000000','ac000000-0001-0001-0000-000000000000',5,'Khoa hoc xuat sac! Giang vien giai thich rat ro rang, de hieu. Sau khi hoc xong toi da xin duoc viec lam Java Developer. Rat cam on!',0,DATE_SUB(NOW(),INTERVAL 168 DAY),DATE_SUB(NOW(),INTERVAL 168 DAY)),
('af000000-0001-0011-0000-000000000000','ac000000-0001-0011-0000-000000000000',5,'Nen tang Java rat vung chac. Sau khoa nay toi hieu ro OOP hon nhieu. Giang vien rat nhiet tinh ho tro khi hoc vien gap kho khan.',0,DATE_SUB(NOW(),INTERVAL 38 DAY),DATE_SUB(NOW(),INTERVAL 38 DAY)),
('af000000-0002-0001-0000-000000000000','ac000000-0002-0001-0000-000000000000',4,'Khoa hoc tot, noi dung day du. Chi co dieu mot so bai tap kha kho voi nguoi moi. Nhung giang vien luon giai dap thac mac nhanh chong. Recommend!',0,DATE_SUB(NOW(),INTERVAL 158 DAY),DATE_SUB(NOW(),INTERVAL 158 DAY)),
('af000000-0002-0009-0000-000000000000','ac000000-0002-0009-0000-000000000000',5,'Khoa hoc AWS tot nhat toi tung hoc. Tat ca lab thuc hanh deu chay tren AWS that. Pass SAA-C03 sau 2 tuan on luyen voi tai lieu khoa nay!',0,DATE_SUB(NOW(),INTERVAL 78 DAY),DATE_SUB(NOW(),INTERVAL 78 DAY)),
('af000000-0003-0002-0000-000000000000','ac000000-0003-0002-0000-000000000000',5,'React va Next.js duoc day rat bai ban. Tu co ban den nang cao. Cac du an thuc te giup toi xay dung portfolio an tuong. Highly recommended!',0,DATE_SUB(NOW(),INTERVAL 153 DAY),DATE_SUB(NOW(),INTERVAL 153 DAY)),
('af000000-0003-0009-0000-000000000000','ac000000-0003-0009-0000-000000000000',4,'AWS course rat hay, nhung nen co them bai tap thuc hanh cho tung service. Nhin chung rat xung dang dong tien.',0,DATE_SUB(NOW(),INTERVAL 68 DAY),DATE_SUB(NOW(),INTERVAL 68 DAY)),
('af000000-0004-0001-0000-000000000000','ac000000-0004-0001-0000-000000000000',5,'Spring Boot course tuyet voi! Toi la designer muon hoc code va khoa nay giup toi bat dau Backend development rat tot. Cam on instructor!',0,DATE_SUB(NOW(),INTERVAL 148 DAY),DATE_SUB(NOW(),INTERVAL 148 DAY)),
('af000000-0005-0003-0000-000000000000','ac000000-0005-0003-0000-000000000000',4,'Flutter course rat thuc te. Toi da build duoc app iOS va Android that su. Giang vien co kinh nghiem thuc chien tot.',0,DATE_SUB(NOW(),INTERVAL 143 DAY),DATE_SUB(NOW(),INTERVAL 143 DAY)),
('af000000-0005-0004-0000-000000000000','ac000000-0005-0004-0000-000000000000',5,'Docker va K8s course tot nhat toi tung hoc. Giang vien co kinh nghiem thuc te o nuoc ngoai va chia se nhung kinh nghiem quy bau.',0,DATE_SUB(NOW(),INTERVAL 123 DAY),DATE_SUB(NOW(),INTERVAL 123 DAY)),
('af000000-0006-0011-0000-000000000000','ac000000-0006-0011-0000-000000000000',5,'Java co ban day rat de hieu. Toi tu nguoi khong biet gi ve lap trinh da co the viet chuong trinh Java sau khoa nay. Cach giang rat than thien.',0,DATE_SUB(NOW(),INTERVAL 138 DAY),DATE_SUB(NOW(),INTERVAL 138 DAY)),
('af000000-0008-0002-0000-000000000000','ac000000-0008-0002-0000-000000000000',4,'React va Next.js rat hay. Mo rong kien thuc frontend cua toi rat nhieu. Toi la frontend dev 1 nam va khoa nay giup toi hieu sau hon.',0,DATE_SUB(NOW(),INTERVAL 118 DAY),DATE_SUB(NOW(),INTERVAL 118 DAY)),
('af000000-0009-0001-0000-000000000000','ac000000-0009-0001-0000-000000000000',5,'Khoa Spring Boot day du, thuc te. JWT implementation that su clear. Rat hai long voi chat luong noi dung.',0,DATE_SUB(NOW(),INTERVAL 108 DAY),DATE_SUB(NOW(),INTERVAL 108 DAY)),
('af000000-000b-0004-0000-000000000000','ac000000-000b-0004-0000-000000000000',5,'Docker K8s course tot. Giang vien co kinh nghiem thuc chien o silicon valley nen chia se nhieu best practice that su co gia tri.',0,DATE_SUB(NOW(),INTERVAL 88 DAY),DATE_SUB(NOW(),INTERVAL 88 DAY)),
('af000000-000c-0001-0000-000000000000','ac000000-000c-0001-0000-000000000000',4,'Spring Boot course kha hay. Nen them phan testing (JUnit, Mockito) cho day du hon. Nhung noi chung rat tot cho nguoi moi bat dau.',0,DATE_SUB(NOW(),INTERVAL 78 DAY),DATE_SUB(NOW(),INTERVAL 78 DAY)),
('af000000-0011-0009-0000-000000000000','ac000000-0011-0009-0000-000000000000',5,'AWS SAA-C03 course xuat sac! Toi da pass ky thi sau 3 tuan hoc. Lab thuc hanh rat chat luong. Cam on giang vien rat nhieu!',0,DATE_SUB(NOW(),INTERVAL 33 DAY),DATE_SUB(NOW(),INTERVAL 33 DAY));

-- ============================================================
-- 15. COMMENTS (50+ bản ghi, bao gom parent va reply)
-- ============================================================
INSERT IGNORE INTO comments (id, lesson_id, user_id, parent_id, content, is_hidden, created_at) VALUES
('ba000000-0000-0000-0000-000000000001','cc000000-0001-0000-0000-000000000018','00000000-0000-0000-0001-000000000001',NULL,'Phan JWT authentication rat hay! Giang vien giai thich cach tao refresh token va access token rat ro rang. Cam on!',0,DATE_SUB(NOW(),INTERVAL 165 DAY)),
('ba000000-0000-0000-0000-000000000002','cc000000-0001-0000-0000-000000000018','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','ba000000-0000-0000-0000-000000000001','Cam on ban da dong gop y kien! Phan refresh token moi trong Spring Boot 3 co nhieu thay doi so voi phien ban cu. Neu ban co cau hoi gi cu hoi nhe!',0,DATE_SUB(NOW(),INTERVAL 164 DAY)),
('ba000000-0000-0000-0000-000000000003','cc000000-0001-0000-0000-000000000024','00000000-0000-0000-0001-000000000002',NULL,'Toi bi loi khi chay docker-compose, MySQL khoi dong roi nhung Spring Boot khong ket noi duoc. Ai giup toi voi?',0,DATE_SUB(NOW(),INTERVAL 155 DAY)),
('ba000000-0000-0000-0000-000000000004','cc000000-0001-0000-0000-000000000024','00000000-0000-0000-0001-000000000003','ba000000-0000-0000-0000-000000000003','Ban can doi MySQL healthy truoc khi Spring Boot start. Them depends_on va condition: service_healthy vao docker-compose.yml nhe!',0,DATE_SUB(NOW(),INTERVAL 154 DAY)),
('ba000000-0000-0000-0000-000000000005','cc000000-0001-0000-0000-000000000024','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','ba000000-0000-0000-0000-000000000003','Chinh xac! Hoac them SPRING_DATASOURCE_URL voi tham so connectionTimeout. Toi se them mot bai extra ve Docker Compose healthcheck.',0,DATE_SUB(NOW(),INTERVAL 153 DAY)),
('ba000000-0000-0000-0000-000000000006','cc000000-0002-0000-0000-000000000011','00000000-0000-0000-0001-000000000004',NULL,'Server Components trong Next.js 14 con kho hieu qua. Co ai giai thich them cho toi duoc khong? Khi nao dung Server Components, khi nao Client?',0,DATE_SUB(NOW(),INTERVAL 148 DAY)),
('ba000000-0000-0000-0000-000000000007','cc000000-0002-0000-0000-000000000011','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','ba000000-0000-0000-0000-000000000006','Rule don gian: Mac dinh dung Server Components. Chi them "use client" khi can useState, useEffect, event handlers hoac browser-only APIs.',0,DATE_SUB(NOW(),INTERVAL 147 DAY)),
('ba000000-0000-0000-0000-000000000008','cc000000-0002-0000-0000-000000000011','00000000-0000-0000-0001-000000000005','ba000000-0000-0000-0000-000000000006','Bo sung them: Server Components chay tren server, khong send JS xuong client, toc do nhanh hon. Client Components can interactivity.',0,DATE_SUB(NOW(),INTERVAL 147 DAY)),
('ba000000-0000-0000-0000-000000000009','cc000000-0003-0000-0000-000000000001','00000000-0000-0000-0001-000000000006',NULL,'Toi moi hoc Flutter duoc 2 tuan. Bai nay kha kho voi nguoi moi. Giang vien co the lam cham hon khong?',0,DATE_SUB(NOW(),INTERVAL 140 DAY)),
('ba000000-0000-0000-0000-000000000010','cc000000-0003-0000-0000-000000000001','00000000-0000-0000-0000-000000000022','ba000000-0000-0000-0000-000000000009','Toi hieu! Toi se update video giai thich ro hon. Ban co the xem lai video 2 lan va doc docs chinh thuc cua Flutter nhe. Join Discord de hoi them!',0,DATE_SUB(NOW(),INTERVAL 139 DAY)),
('ba000000-0000-0000-0000-000000000011','cc000000-0004-0000-0000-000000000002','00000000-0000-0000-0001-000000000007',NULL,'Docker pull ve rat cham o Viet Nam. Co cach nao dung mirror khong?',0,DATE_SUB(NOW(),INTERVAL 135 DAY)),
('ba000000-0000-0000-0000-000000000012','cc000000-0004-0000-0000-000000000002','00000000-0000-0000-0000-000000000023','ba000000-0000-0000-0000-000000000011','Dung cach: them "registry-mirrors": ["https://mirror.gcr.io"] vao Docker daemon.json. Hoac dung VPN.',0,DATE_SUB(NOW(),INTERVAL 134 DAY)),
('ba000000-0000-0000-0000-000000000013','cc000000-0004-0000-0000-000000000002','00000000-0000-0000-0001-000000000008','ba000000-0000-0000-0000-000000000011','Toi dung Azk8s mirror cua Azure, kha nhanh: "https://registry.azk8s.cn".',0,DATE_SUB(NOW(),INTERVAL 134 DAY)),
('ba000000-0000-0000-0000-000000000014','cc000000-0005-0000-0000-000000000001','00000000-0000-0000-0001-000000000009',NULL,'Phan backpropagation trong neural network rat kho hieu. Giang vien co the lam video rieng giai thich math behind no khong?',0,DATE_SUB(NOW(),INTERVAL 125 DAY)),
('ba000000-0000-0000-0000-000000000015','cc000000-0005-0000-0000-000000000001','00000000-0000-0000-0000-000000000024','ba000000-0000-0000-0000-000000000014','Da ghi nhan! Toi se lam video bo sung ve math cua backprop. Hien tai ban co the xem series 3Blue1Brown tren YouTube rat hay.',0,DATE_SUB(NOW(),INTERVAL 124 DAY)),
('ba000000-0000-0000-0000-000000000016','cc000000-0006-0000-0000-000000000001','00000000-0000-0000-0001-000000000010',NULL,'MySQL EXPLAIN output kha kho doc. Co cheat sheet khong?',0,DATE_SUB(NOW(),INTERVAL 115 DAY)),
('ba000000-0000-0000-0000-000000000017','cc000000-0006-0000-0000-000000000001','00000000-0000-0000-0000-000000000025','ba000000-0000-0000-0000-000000000016','Toi se upload PDF cheat sheet trong phan resources. Co ban chu y: type=ALL la full scan - can them index. ref=const la tot nhat.',0,DATE_SUB(NOW(),INTERVAL 114 DAY)),
('ba000000-0000-0000-0000-000000000018','cc000000-0009-0000-0000-000000000001','00000000-0000-0000-0001-000000000011',NULL,'Pass SAA-C03 roi! Cam on khoa hoc rat nhieu. Practice exam trong khoa sat voi de thi that.',0,DATE_SUB(NOW(),INTERVAL 88 DAY)),
('ba000000-0000-0000-0000-000000000019','cc000000-0009-0000-0000-000000000001','00000000-0000-0000-0000-000000000023','ba000000-0000-0000-0000-000000000018','Chuc mung ban! Chia se thanh cong cua ban len LinkedIn nhe. Toi rat vui khi biet hoc vien pass ky thi!',0,DATE_SUB(NOW(),INTERVAL 87 DAY)),
('ba000000-0000-0000-0000-000000000020','cc000000-0001-0000-0000-000000000006','00000000-0000-0000-0001-000000000012',NULL,'Co ai test thu dang nhap vao swagger khong? Toi bi loi 403 sau khi dang nhap.',0,DATE_SUB(NOW(),INTERVAL 75 DAY)),
('ba000000-0000-0000-0000-000000000021','cc000000-0001-0000-0000-000000000006','00000000-0000-0000-0001-000000000001','ba000000-0000-0000-0000-000000000020','Ban phai them Bearer token vao Authorize button trong Swagger UI. Xem video bai 18 nhe.',0,DATE_SUB(NOW(),INTERVAL 75 DAY)),
('ba000000-0000-0000-0000-000000000022','cc000000-000a-0000-0000-000000000001','00000000-0000-0000-0001-000000000013',NULL,'Large Language Models rat thu vi! Phan RAG architecture giai thich rat tot. Dang thi nghiem tren du lieu cong ty minh.',0,DATE_SUB(NOW(),INTERVAL 70 DAY)),
('ba000000-0000-0000-0000-000000000023','cc000000-000a-0000-0000-000000000001','00000000-0000-0000-0000-000000000024','ba000000-0000-0000-0000-000000000022','Tuyet! Neu ban dung cho production, nho implement reranking va hybrid search de tang do chinh xac nhe!',0,DATE_SUB(NOW(),INTERVAL 69 DAY)),
('ba000000-0000-0000-0000-000000000024','cc000000-0007-0000-0000-000000000001','00000000-0000-0000-0001-000000000014',NULL,'Node.js voi TypeScript rat hay. Sau khoa nay toi se hoc them NestJS. Giang vien co plan day NestJS khong?',0,DATE_SUB(NOW(),INTERVAL 60 DAY)),
('ba000000-0000-0000-0000-000000000025','cc000000-0007-0000-0000-000000000001','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','ba000000-0000-0000-0000-000000000024','Co! NestJS course dang trong ke hoach cho Q3 nam nay. Subscribe newsletter de nhan thong bao som nhe!',0,DATE_SUB(NOW(),INTERVAL 59 DAY)),
('ba000000-0000-0000-0000-000000000026','cc000000-000c-0000-0000-000000000001','00000000-0000-0000-0001-000000000015',NULL,'Microservices phuc tap hon toi nghi. Phan SAGA Pattern rat kho implement. Co project mau nao khong?',0,DATE_SUB(NOW(),INTERVAL 55 DAY)),
('ba000000-0000-0000-0000-000000000027','cc000000-000c-0000-0000-000000000001','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','ba000000-0000-0000-0000-000000000026','Co! Toi se upload source code du an mau len GitHub. SAGA that su phuc tap nhung khi hieu roi no rat powerful.',0,DATE_SUB(NOW(),INTERVAL 54 DAY)),
('ba000000-0000-0000-0000-000000000028','cc000000-000b-0000-0000-000000000001','00000000-0000-0000-0001-000000000016',NULL,'Redis Sorted Set cho leaderboard rat hay! Dang ung dung vao game server cua minh.',0,DATE_SUB(NOW(),INTERVAL 45 DAY)),
('ba000000-0000-0000-0000-000000000029','cc000000-0009-0000-0000-000000000001','00000000-0000-0000-0001-000000000017',NULL,'Practice exam rat sat voi de thi that. Pass SAA sau 2.5 tuan hoc. Co chung nhan cua AWS roi!',0,DATE_SUB(NOW(),INTERVAL 33 DAY)),
('ba000000-0000-0000-0000-000000000030','cc000000-0002-0000-0000-000000000010','00000000-0000-0000-0001-000000000018',NULL,'Next.js App Router kha khac voi Pages Router truoc. Phan Server Actions moi la game changer!',0,DATE_SUB(NOW(),INTERVAL 23 DAY)),
('ba000000-0000-0000-0000-000000000031','cc000000-0001-0000-0000-000000000003','00000000-0000-0000-0001-000000000019',NULL,'Vua join khoa hoc hom nay. Code theo video bi loi "Port already in use". Ai giup voi?',0,DATE_SUB(NOW(),INTERVAL 14 DAY)),
('ba000000-0000-0000-0000-000000000032','cc000000-0001-0000-0000-000000000003','00000000-0000-0000-0001-000000000001','ba000000-0000-0000-0000-000000000031','Kill process dang chiem port: netstat -ano | findstr :8080 roi taskkill /PID <pid> /F nhe!',0,DATE_SUB(NOW(),INTERVAL 14 DAY));

-- ============================================================
-- 16. NOTIFICATIONS
-- ============================================================
INSERT IGNORE INTO notifications (id, recipient_id, title, content, notification_type, is_read, created_at) VALUES
-- Instructor notifications
('bc000000-0000-0000-0000-000000000001','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','Khoa hoc duoc duyet!','Khoa hoc "Java Spring Boot tu Trang Thai Den Nang Cao" da duoc admin duyet va phat hanh.','COURSE_APPROVED',0,DATE_SUB(NOW(),INTERVAL 178 DAY)),
('bc000000-0000-0000-0000-000000000002','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','Co hoc vien moi dang ky','100 hoc vien da dang ky khoa hoc Spring Boot cua ban!','NEW_ENROLLMENT',0,DATE_SUB(NOW(),INTERVAL 150 DAY)),
('bc000000-0000-0000-0000-000000000003','a56e8cdf-80bb-11f1-8183-de8e3dc1070d','Khoa hoc duoc duyet!','Khoa hoc "React 18 va Next.js 14 Chuyen Sau" da duoc phat hanh.','COURSE_APPROVED',0,DATE_SUB(NOW(),INTERVAL 158 DAY)),
('bc000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000022','Khoa hoc duoc duyet!','Khoa hoc "Flutter 3.x Phat Trien App iOS va Android" da duoc duyet.','COURSE_APPROVED',0,DATE_SUB(NOW(),INTERVAL 148 DAY)),
('bc000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000023','Khoa hoc duoc duyet!','Khoa hoc Docker va Kubernetes da duoc phat hanh.','COURSE_APPROVED',0,DATE_SUB(NOW(),INTERVAL 138 DAY)),
('bc000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000025','Khoa hoc bi tu choi','Khoa hoc "Hack MySQL" bi tu choi. Ly do: Noi dung co the bi su dung sai muc dich.','COURSE_REJECTED',0,DATE_SUB(NOW(),INTERVAL 30 DAY)),
('bc000000-0000-0000-0000-000000000007','00000000-0000-0000-0000-000000000022','Khoa hoc bi tu choi','Khoa hoc "Kiem Tien Voi App iOS Khong Can Code" bi tu choi.','COURSE_REJECTED',0,DATE_SUB(NOW(),INTERVAL 20 DAY)),
-- Student notifications
('bc000000-0000-0000-0000-000000000008','00000000-0000-0000-0001-000000000001','Chuc mung hoan thanh khoa hoc!','Ban da hoan thanh khoa hoc Java Spring Boot. Chia se thanh cong cua ban!','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 168 DAY)),
('bc000000-0000-0000-0000-000000000009','00000000-0000-0000-0001-000000000001','Reply comment cua ban','Giang vien da tra loi binh luan cua ban trong khoa Spring Boot.','COMMENT_REPLY',0,DATE_SUB(NOW(),INTERVAL 164 DAY)),
('bc000000-0000-0000-0000-000000000010','00000000-0000-0000-0001-000000000002','Chuc mung hoan thanh!','Ban da hoan thanh khoa hoc AWS Solutions Architect.','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 78 DAY)),
('bc000000-0000-0000-0000-000000000011','00000000-0000-0000-0001-000000000002','Reply comment cua ban','Nguoi dung khac da tra loi binh luan cua ban.','COMMENT_REPLY',0,DATE_SUB(NOW(),INTERVAL 154 DAY)),
('bc000000-0000-0000-0000-000000000012','00000000-0000-0000-0001-000000000003','Chuc mung hoan thanh!','Ban da hoan thanh khoa hoc React 18 va Next.js 14.','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 153 DAY)),
('bc000000-0000-0000-0000-000000000013','00000000-0000-0000-0001-000000000005','Chuc mung hoan thanh!','Ban da hoan thanh khoa hoc Flutter. Chia se app cua ban!','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 143 DAY)),
('bc000000-0000-0000-0000-000000000014','00000000-0000-0000-0001-000000000011','Chuc mung hoan thanh!','Ban da hoan thanh khoa hoc Docker va Kubernetes.','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 88 DAY)),
('bc000000-0000-0000-0000-000000000015','00000000-0000-0000-0001-000000000017','Chuc mung hoan thanh!','Ban da hoan thanh khoa hoc AWS va co the thi chung chi.','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 33 DAY)),
-- System notifications
('bc000000-0000-0000-0000-000000000016','00000000-0000-0000-0001-000000000001','Khuyen mai cuoi tuan','Giam 20% tat ca khoa hoc tu Thu 6 den Chu Nhat. Su dung ma WEEKEND20.','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 5 DAY)),
('bc000000-0000-0000-0000-000000000017','00000000-0000-0000-0001-000000000002','Khoa hoc moi ra mat','Giang vien ban theo doi vua ra mat khoa hoc moi: GraphQL API voi Spring Boot.','SYSTEM',0,DATE_SUB(NOW(),INTERVAL 15 DAY)),
('bc000000-0000-0000-0000-000000000018','00000000-0000-0000-0001-000000000018','Report duoc xu ly','Bao cao cua ban ve comment vi pham da duoc xu ly boi admin.','REPORT_RESOLVED',0,DATE_SUB(NOW(),INTERVAL 10 DAY));

-- ============================================================
-- 18. WISHLISTS (50+ bản ghi)
-- ============================================================
INSERT IGNORE INTO wishlists (user_id, course_id) VALUES
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000010'),
('00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000007'),
('00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000012'),
('00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000012'),
('00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000012'),
('00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000010'),
('00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000011','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000011','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000012','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000012','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000013','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000013','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000014','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000014','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000015','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000015','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000016','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000016','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000017','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000017','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000018','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000018','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000019','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000019','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000020','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000020','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000020','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000010');

-- ============================================================
-- 19. FAVORITES (50+ bản ghi - khac hoan toan voi wishlist)
-- ============================================================
INSERT IGNORE INTO favorites (user_id, course_id) VALUES
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000017'),
('00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000017'),
('00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000008'),
('00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000007'),
('00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000010'),
('00000000-0000-0000-0001-000000000011','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000012','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000013','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000014','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000015','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000016','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0001-000000000017','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000018','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000019','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000020','c0000000-0000-0000-0000-000000000004'),
('a56e8cdf-80bb-11f1-8183-de8e3dc1070d','c0000000-0000-0000-0000-000000000005'),
('a56e8cdf-80bb-11f1-8183-de8e3dc1070d','c0000000-0000-0000-0000-000000000010'),
('00000000-0000-0000-0000-000000000022','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000022','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000023','c0000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000024','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000025','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000009'),
-- Additional favorites to reach 50+
('00000000-0000-0000-0001-000000000001','c0000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0001-000000000002','c0000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0001-000000000003','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000004','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000005','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000006','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000007','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000008','c0000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0001-000000000009','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000010','c0000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0001-000000000011','c0000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0001-000000000012','c0000000-0000-0000-0000-000000000009');

-- ============================================================
-- 20. REPORTS (dung reportable_type + reportable_id pattern)
-- reportable_type: 'COURSE', 'COMMENT', 'REVIEW'
-- ============================================================
INSERT IGNORE INTO reports (id, reporter_id, reportable_type, reportable_id, reason, status, admin_note, updated_at, created_at) VALUES
('be000000-0000-0000-0000-000000000001','00000000-0000-0000-0001-000000000018','COURSE','c0000000-0000-0000-0000-000000000001',
 'Khoa hoc nay co link affiliate ngoai trong phan mo ta, vi pham quy dinh.','RESOLVED',
 'Da kiem tra va xoa link affiliate vi pham. Giang vien da duoc canh bao.',DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_SUB(NOW(),INTERVAL 15 DAY)),
('be000000-0000-0000-0000-000000000002','00000000-0000-0000-0001-000000000015','COMMENT','ba000000-0000-0000-0000-000000000009',
 'Comment nay chua thong tin sai ve Flutter, co the gay nham lan cho nguoi moi.','REVIEWING',
 NULL,DATE_SUB(NOW(),INTERVAL 8 DAY),DATE_SUB(NOW(),INTERVAL 12 DAY)),
('be000000-0000-0000-0000-000000000003','00000000-0000-0000-0001-000000000010','REVIEW','af000000-0001-0001-0000-000000000000',
 'Review nay co ve gia mao, ngon tu chuyen nghiep qua muc so voi binh thuong.','PENDING',
 NULL,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY)),
('be000000-0000-0000-0000-000000000004','00000000-0000-0000-0001-000000000007','COURSE','c0000000-0000-0000-0000-000000000004',
 'Noi dung mot so bai dang bi het han do AWS cap nhat.','REVIEWING',
 NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 4 DAY)),
('be000000-0000-0000-0000-000000000005','00000000-0000-0000-0001-000000000013','COMMENT','ba000000-0000-0000-0000-000000000003',
 'Comment nay chua thong tin ky thuat sai co the gay loi cho nguoi hoc theo.','REJECTED',
 'Kiem tra lai: thong tin trong comment chinh xac. Report bi tu choi.',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY));

-- ============================================================
-- 21. COURSE_APPROVAL_HISTORY
-- ============================================================
INSERT IGNORE INTO course_approval_history (id, course_id, actor_id, action, note, created_at) VALUES
('bf000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','APPROVE',
'Noi dung chat luong cao, code mau ro rang, phu hop voi muc tieu khoa hoc.',DATE_SUB(NOW(),INTERVAL 178 DAY)),
('bf000000-0000-0000-0000-000000000002','c0000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000001','APPROVE',
'Khoa hoc React & Next.js cap nhat va day du. Phong cach day hieu qua.',DATE_SUB(NOW(),INTERVAL 158 DAY)),
('bf000000-0000-0000-0000-000000000003','c0000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000001','APPROVE',
'Flutter course tot, nen bo sung them phan testing trong khoa hoc luc sau.',DATE_SUB(NOW(),INTERVAL 148 DAY)),
('bf000000-0000-0000-0000-000000000004','c0000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000001','APPROVE',
'DevOps course xuat sac. Lab thuc hanh that su. Giang vien co kinh nghiem thuc chien.',DATE_SUB(NOW(),INTERVAL 138 DAY)),
('bf000000-0000-0000-0000-000000000005','c0000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000001','APPROVE',
'AI/ML course day du, math giai thich kha ro. Nen them quiz cho moi chapter.',DATE_SUB(NOW(),INTERVAL 128 DAY)),
('bf000000-0000-0000-0000-000000000006','c0000000-0000-0000-0000-000000000029','00000000-0000-0000-0000-000000000001','REJECT',
'Noi dung co the bi su dung sai muc dich. Can chinh sua lai theo huong giao duc bao mat hop phap.',DATE_SUB(NOW(),INTERVAL 30 DAY)),
('bf000000-0000-0000-0000-000000000007','c0000000-0000-0000-0000-000000000030','00000000-0000-0000-0000-000000000001','REJECT',
'Khoa hoc khong co noi dung ky thuat thuc su. Khong phu hop voi tieu chi CourseHub.',DATE_SUB(NOW(),INTERVAL 20 DAY));

-- ============================================================
-- 22. ORDERS & ORDER_ITEMS
-- ============================================================
INSERT IGNORE INTO orders (id, user_id, total_amount, discount_amount, final_amount, payment_status, created_at) VALUES
('ca000000-0001-0001-0000-000000000000','00000000-0000-0000-0001-000000000001',1590000.00,0.00,1590000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 170 DAY)),
('ca000000-0001-0002-0000-000000000000','00000000-0000-0000-0001-000000000001',1890000.00,0.00,1890000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 150 DAY)),
('ca000000-0001-0003-0000-000000000000','00000000-0000-0000-0001-000000000001',1990000.00,0.00,1990000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 130 DAY)),
('ca000000-0002-0001-0000-000000000000','00000000-0000-0000-0001-000000000002',1590000.00,0.00,1590000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 160 DAY)),
('ca000000-0002-0002-0000-000000000000','00000000-0000-0000-0001-000000000002',2290000.00,200000.00,2090000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 120 DAY)),
('ca000000-0002-0003-0000-000000000000','00000000-0000-0000-0001-000000000002',1790000.00,0.00,1790000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 80 DAY)),
('ca000000-0003-0001-0000-000000000000','00000000-0000-0000-0001-000000000003',1890000.00,0.00,1890000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 155 DAY)),
('ca000000-0003-0002-0000-000000000000','00000000-0000-0000-0001-000000000003',1490000.00,0.00,1490000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 110 DAY)),
('ca000000-0004-0001-0000-000000000000','00000000-0000-0000-0001-000000000004',1590000.00,0.00,1590000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 150 DAY)),
('ca000000-0004-0002-0000-000000000000','00000000-0000-0000-0001-000000000004',1890000.00,189000.00,1701000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 130 DAY)),
('ca000000-0005-0001-0000-000000000000','00000000-0000-0000-0001-000000000005',1290000.00,0.00,1290000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 145 DAY)),
('ca000000-0005-0002-0000-000000000000','00000000-0000-0000-0001-000000000005',1990000.00,0.00,1990000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 125 DAY)),
('ca000000-0006-0001-0000-000000000000','00000000-0000-0000-0001-000000000006',890000.00,0.00,890000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 140 DAY)),
('ca000000-0007-0001-0000-000000000000','00000000-0000-0000-0001-000000000007',1290000.00,0.00,1290000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 130 DAY)),
('ca000000-0008-0001-0000-000000000000','00000000-0000-0000-0001-000000000008',1890000.00,0.00,1890000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 120 DAY)),
('ca000000-0009-0001-0000-000000000000','00000000-0000-0000-0001-000000000009',1590000.00,0.00,1590000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 110 DAY)),
('ca000000-000a-0001-0000-000000000000','00000000-0000-0000-0001-000000000010',3580000.00,358000.00,3222000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 100 DAY)),
('ca000000-000b-0001-0000-000000000000','00000000-0000-0000-0001-000000000011',1990000.00,0.00,1990000.00,'COMPLETED',DATE_SUB(NOW(),INTERVAL 90 DAY)),
('ca000000-000d-0001-0000-000000000000','00000000-0000-0000-0001-000000000013',2290000.00,0.00,2290000.00,'PENDING',DATE_SUB(NOW(),INTERVAL 1 DAY)),
('ca000000-0010-0001-0000-000000000000','00000000-0000-0000-0001-000000000016',2290000.00,0.00,2290000.00,'FAILED',DATE_SUB(NOW(),INTERVAL 2 DAY));

INSERT IGNORE INTO order_items (id, order_id, course_id, price) VALUES
('cb000000-0001-0001-0000-000000000001','ca000000-0001-0001-0000-000000000000','c0000000-0000-0000-0000-000000000001',1590000.00),
('cb000000-0001-0002-0000-000000000001','ca000000-0001-0002-0000-000000000000','c0000000-0000-0000-0000-000000000002',1890000.00),
('cb000000-0001-0003-0000-000000000001','ca000000-0001-0003-0000-000000000000','c0000000-0000-0000-0000-000000000004',1990000.00),
('cb000000-0002-0001-0000-000000000001','ca000000-0002-0001-0000-000000000000','c0000000-0000-0000-0000-000000000001',1590000.00),
('cb000000-0002-0002-0000-000000000001','ca000000-0002-0002-0000-000000000000','c0000000-0000-0000-0000-000000000005',2290000.00),
('cb000000-0002-0003-0000-000000000001','ca000000-0002-0003-0000-000000000000','c0000000-0000-0000-0000-000000000009',1790000.00),
('cb000000-0003-0001-0000-000000000001','ca000000-0003-0001-0000-000000000000','c0000000-0000-0000-0000-000000000002',1890000.00),
('cb000000-0003-0002-0000-000000000001','ca000000-0003-0002-0000-000000000000','c0000000-0000-0000-0000-000000000006',1490000.00),
('cb000000-0004-0001-0000-000000000001','ca000000-0004-0001-0000-000000000000','c0000000-0000-0000-0000-000000000001',1590000.00),
('cb000000-0004-0002-0000-000000000001','ca000000-0004-0002-0000-000000000000','c0000000-0000-0000-0000-000000000002',1890000.00),
('cb000000-0005-0001-0000-000000000001','ca000000-0005-0001-0000-000000000000','c0000000-0000-0000-0000-000000000003',1290000.00),
('cb000000-0005-0002-0000-000000000001','ca000000-0005-0002-0000-000000000000','c0000000-0000-0000-0000-000000000004',1990000.00),
('cb000000-0006-0001-0000-000000000001','ca000000-0006-0001-0000-000000000000','c0000000-0000-0000-0000-000000000017',890000.00),
('cb000000-0007-0001-0000-000000000001','ca000000-0007-0001-0000-000000000000','c0000000-0000-0000-0000-000000000003',1290000.00),
('cb000000-0008-0001-0000-000000000001','ca000000-0008-0001-0000-000000000000','c0000000-0000-0000-0000-000000000002',1890000.00),
('cb000000-0009-0001-0000-000000000001','ca000000-0009-0001-0000-000000000000','c0000000-0000-0000-0000-000000000001',1590000.00),
('cb000000-000a-0001-0000-000000000001','ca000000-000a-0001-0000-000000000000','c0000000-0000-0000-0000-000000000005',2290000.00),
('cb000000-000a-0001-0000-000000000002','ca000000-000a-0001-0000-000000000000','c0000000-0000-0000-0000-000000000010',2490000.00),
('cb000000-000b-0001-0000-000000000001','ca000000-000b-0001-0000-000000000000','c0000000-0000-0000-0000-000000000004',1990000.00),
('cb000000-000d-0001-0000-000000000001','ca000000-000d-0001-0000-000000000000','c0000000-0000-0000-0000-000000000005',2290000.00),
('cb000000-0010-0001-0000-000000000001','ca000000-0010-0001-0000-000000000000','c0000000-0000-0000-0000-000000000005',2290000.00);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- KIEM TRA CUOI: So luong ban ghi
-- ============================================================
SELECT 'roles' AS table_name, COUNT(*) AS row_count FROM roles
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'user_roles', COUNT(*) FROM user_roles
UNION ALL SELECT 'instructor_profiles', COUNT(*) FROM instructor_profiles
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'chapters', COUNT(*) FROM chapters
UNION ALL SELECT 'lessons', COUNT(*) FROM lessons
UNION ALL SELECT 'lesson_resources', COUNT(*) FROM lesson_resources
UNION ALL SELECT 'quiz_configs', COUNT(*) FROM quiz_configs
UNION ALL SELECT 'questions', COUNT(*) FROM questions
UNION ALL SELECT 'answers', COUNT(*) FROM answers
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL SELECT 'progress', COUNT(*) FROM progress
UNION ALL SELECT 'quiz_attempts', COUNT(*) FROM quiz_attempts
UNION ALL SELECT 'reviews', COUNT(*) FROM reviews
UNION ALL SELECT 'comments', COUNT(*) FROM comments
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL SELECT 'wishlists', COUNT(*) FROM wishlists
UNION ALL SELECT 'favorites', COUNT(*) FROM favorites
UNION ALL SELECT 'reports', COUNT(*) FROM reports
UNION ALL SELECT 'course_approval_history', COUNT(*) FROM course_approval_history
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items;
-- ============================================================
-- END OF SAMPLE DATA
-- ============================================================

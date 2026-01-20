-- 초기 관리자 계정 생성
-- 비밀번호: admin123 (BCrypt 해싱된 값)
-- BCrypt 해싱: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
MERGE INTO admin_users (id, username, password) 
KEY (username)
VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

#!/usr/bin/env bash
set -e

echo "=== 1. MySQL 저메모리 설정 적용 + 재시작 ==="
systemctl restart mysql
sleep 3

echo "=== 2. DB / 유저 생성 (env에서 비번 읽음) ==="
DBPW=$(grep '^DB_PASSWORD=' /etc/kaidoku/kaidoku.env | cut -d= -f2-)
mysql <<SQL
CREATE DATABASE IF NOT EXISTS kaidoku CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'kaidoku'@'localhost' IDENTIFIED BY '${DBPW}';
GRANT ALL PRIVILEGES ON kaidoku.* TO 'kaidoku'@'localhost';
FLUSH PRIVILEGES;
SQL
echo "MySQL DB/user ready"

echo "=== 3. nginx 사이트 활성화 ==="
ln -sf /etc/nginx/sites-available/kaidoku /etc/nginx/sites-enabled/kaidoku
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx
echo "nginx ready"

echo "=== 4. systemd 서비스 등록 + 시작 ==="
systemctl daemon-reload
systemctl enable kaidoku
systemctl restart kaidoku
echo "=== SETUP_DONE ==="

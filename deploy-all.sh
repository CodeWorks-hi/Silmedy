#!/bin/bash

set -e

# 기본 설정
KEY_PATH="$HOME/KOOKIZ/KEYS/RDS/rds-key.pem"
EC2_HOST="ec2-user@43.201.73.161"
APK_PATH="apk/Silmedy.apk"
IMAGE_NAME="silmedy-distribution"
TAR_FILE="$IMAGE_NAME.tar.gz"

echo "🔨 [1/6] Gradle로 앱 빌드"
./gradlew clean assembleRelease

echo "📁 [2/6] APK 복사"
mkdir -p apk
cp app/build/outputs/apk/release/app-release-unsigned.apk "$APK_PATH"

echo "🐳 [3/6] Docker 이미지 빌드"
docker build --platform=linux/amd64 -f Dockerfile.nginx -t "$IMAGE_NAME" .

echo "🗜️ [4/6] Docker 이미지 압축"
docker save "$IMAGE_NAME" | gzip > "$TAR_FILE"

echo "🚚 [5/6] EC2로 전송"
scp -i "$KEY_PATH" "$TAR_FILE" "$EC2_HOST":~

echo "🚀 [6/6] EC2에서 이미지 로드 및 컨테이너 실행"
ssh -i "$KEY_PATH" "$EC2_HOST" << 'EOF'
docker stop silmedy-nginx 2>/dev/null || true
docker rm silmedy-nginx 2>/dev/null || true
docker load < silmedy-distribution.tar.gz
docker run -d --name silmedy-nginx -p 8080:80 silmedy-distribution
docker ps | grep silmedy-nginx
EOF

echo "✅ 배포 완료: http://43.201.73.161:8080/Silmedy.apk"
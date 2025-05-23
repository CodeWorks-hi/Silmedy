# ✅ Java 17을 포함한 공식 이미지 사용
FROM --platform=linux/amd64 eclipse-temurin:17-jdk

# ✅ 필수 도구 설치
RUN apt-get update && \
    apt-get install -y wget unzip git curl

# ✅ Android SDK 설치 경로 설정
ENV ANDROID_SDK_ROOT=/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# ✅ Android SDK Command Line Tools 다운로드 및 설치
RUN mkdir -p $ANDROID_SDK_ROOT && \
    cd $ANDROID_SDK_ROOT && \
    wget -O cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip cmdline-tools.zip -d cmdline-tools && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/cmdline-tools/* cmdline-tools/latest/

# ✅ 필요한 SDK 패키지 설치
RUN yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} \
    "platform-tools" \
    "platforms;android-33" \
    "build-tools;33.0.2"

# ✅ 작업 디렉토리 설정 및 프로젝트 복사
WORKDIR /app
COPY . /app

# ✅ 빌드 대신 릴리즈된 APK 복사
RUN mkdir -p /usr/share/nginx/html && \
    cp /app/app/build/outputs/apk/release/*.apk /usr/share/nginx/html/Silmedy.apk
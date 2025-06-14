# Firebase FCM 알림 앱

Firebase Cloud Messaging을 사용하여 푸시 알림을 받을 수 있는 Android 앱입니다.

## 주요 기능

- 🔔 Firebase FCM을 통한 푸시 알림 수신
- 📱 깔끔한 Material Design 3 UI
- 🏗️ Clean Architecture 구조
- 🔄 MVI 패턴 적용
- 💾 Room 데이터베이스를 통한 알림 저장
- 🎯 Hilt 의존성 주입
- 📊 알림 통계 및 관리

## 프로젝트 구조

```
app/
├── data/
│   ├── firebase/          # Firebase FCM 서비스
│   ├── local/             # Room 데이터베이스
│   └── repository/        # Repository 구현체
├── domain/
│   ├── model/             # 도메인 모델
│   ├── repository/        # Repository 인터페이스
│   └── usecase/           # Use Case 클래스
├── presentation/
│   ├── activity/          # Activity 클래스
│   ├── fragment/          # Fragment 클래스
│   ├── viewmodel/         # ViewModel 클래스
│   ├── adapter/           # RecyclerView 어댑터
│   └── state/             # UI 상태 관리
└── di/                    # Hilt 의존성 주입 모듈
```

## 설정 방법

### 1. Firebase 프로젝트 설정

1. [Firebase Console](https://console.firebase.google.com/)에 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. Android 앱 추가
   - 패키지 이름: `com.example.my_notification`
   - SHA-1 키 등록 (선택사항)

### 2. google-services.json 설정

1. Firebase Console에서 `google-services.json` 파일 다운로드
2. 현재 프로젝트의 `app/google-services.json`을 다운로드한 파일로 교체

### 3. 앱 빌드 및 실행

```bash
# 프로젝트 디렉토리로 이동
cd my_notification

# 앱 빌드
./gradlew build

# 앱 실행
./gradlew installDebug
```

## 푸시 알림 테스트

### 1. FCM 토큰 확인

앱 실행 후 홈 화면에서 FCM 토큰을 확인하고 복사합니다.

### 2. Firebase Console에서 테스트

1. Firebase Console > Cloud Messaging
2. "Send your first message" 클릭
3. 메시지 작성 후 "Send test message"
4. 복사한 FCM 토큰 입력 후 테스트

### 3. 서버에서 직접 전송

```bash
curl -X POST "https://fcm.googleapis.com/fcm/send" \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "FCM_TOKEN",
    "notification": {
      "title": "테스트 알림",
      "body": "Firebase FCM 테스트 메시지입니다."
    },
    "data": {
      "key1": "value1",
      "key2": "value2"
    }
  }'
```

## 화면 구성

### 1. 홈 화면
- FCM 토큰 표시 및 복사
- 알림 통계 (총 알림 수, 읽지 않은 알림 수)
- 토큰 새로고침 기능

### 2. 알림 목록 화면
- 받은 알림 목록 표시
- 읽음/읽지 않음 상태 표시
- 알림 삭제 기능
- 스와이프 새로고침

### 3. 설정 화면
- 앱 정보 및 설명

## 사용된 기술 스택

- **Architecture**: Clean Architecture + MVI
- **DI**: Hilt
- **Database**: Room
- **Network**: Firebase FCM
- **UI**: Material Design 3, View Binding
- **Async**: Coroutines + Flow
- **Navigation**: Navigation Component

## 권한

앱에서 사용하는 권한:
- `INTERNET`: 네트워크 통신
- `POST_NOTIFICATIONS`: 알림 표시 (Android 13+)
- `ACCESS_NETWORK_STATE`: 네트워크 상태 확인
- `WAKE_LOCK`: FCM 백그라운드 처리

## 문제 해결

### 알림이 오지 않는 경우
1. 인터넷 연결 확인
2. 앱 알림 권한 확인
3. Firebase 프로젝트 설정 확인
4. google-services.json 파일 확인

### 토큰이 표시되지 않는 경우
1. google-services.json 파일 위치 확인
2. Firebase 프로젝트 설정 확인
3. 앱 재시작 후 재시도

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 
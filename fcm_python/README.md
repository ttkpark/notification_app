# FCM Notification Service (Python)

Firebase Cloud Messaging HTTP v1 API를 사용하여 푸시 알림을 전송하는 Python 프로젝트입니다.

## 🚀 기능

- **단일 디바이스 알림 전송**: 특정 디바이스 토큰으로 알림 전송
- **다중 디바이스 알림 전송**: 여러 디바이스에 동시 알림 전송
- **토픽 알림 전송**: 특정 토픽을 구독한 모든 디바이스에 알림 전송
- **Flask 웹 API**: RESTful API 엔드포인트 제공
- **OAuth 2.0 인증**: Google Service Account를 사용한 안전한 인증

## 📋 필수 요구사항

- Python 3.8+
- Firebase 프로젝트
- Firebase Service Account Key (JSON 파일)

## 🛠️ 설치 및 설정

### 1. 가상환경 생성 (권장)
```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 또는
venv\Scripts\activate     # Windows
```

### 2. 의존성 설치
```bash
pip install -r requirements.txt
```

### 3. Firebase Service Account Key 설정
1. Firebase Console → 프로젝트 설정 → 서비스 계정
2. "새 비공개 키 생성" 클릭하여 JSON 파일 다운로드
3. 프로젝트 루트에 `firebase-service-account-key.json`으로 저장

## 🎯 사용 방법

### 1. 간단한 테스트 스크립트 실행
```bash
python simple_test.py
```

### 2. Flask 웹 서버 실행
```bash
python flask_app.py
```
서버가 `http://localhost:5000`에서 실행됩니다.

### 3. FCM 서비스 직접 사용
```python
from fcm_service import FCMService

# FCM 서비스 초기화
fcm_service = FCMService(
    project_id="your-firebase-project-id",
    service_account_key_path="firebase-service-account-key.json"
)

# 알림 전송
success = fcm_service.send_notification(
    device_token="device_token_here",
    title="안녕하세요!",
    body="Python에서 보낸 메시지입니다.",
    data={"custom_key": "custom_value"}
)
```

## 📡 Flask API 엔드포인트

### 홈페이지
```http
GET /
```

### 단일 디바이스 알림 전송
```http
POST /send
Content-Type: application/json

{
  "deviceToken": "device_token_here",
  "title": "알림 제목",
  "body": "알림 내용",
  "data": {
    "custom_key": "custom_value"
  }
}
```

### 다중 디바이스 알림 전송
```http
POST /send-multiple
Content-Type: application/json

{
  "deviceTokens": ["token1", "token2", "token3"],
  "title": "알림 제목",
  "body": "알림 내용"
}
```

### 토픽 알림 전송
```http
POST /send-topic
Content-Type: application/json

{
  "topic": "news",
  "title": "뉴스 알림",
  "body": "새로운 뉴스가 있습니다."
}
```

### 테스트 알림 전송
```http
POST /test
```

## 📁 파일 구조

```
fcm_python/
├── fcm_service.py      # FCM 서비스 클래스
├── flask_app.py        # Flask 웹 애플리케이션
├── simple_test.py      # 간단한 테스트 스크립트
├── requirements.txt    # Python 의존성
└── README.md          # 프로젝트 문서
```

## 🔧 사용 예시

### 환영 메시지 전송
```python
from fcm_service import FCMService

fcm_service = FCMService("project-id", "service-key.json")

success = fcm_service.send_notification(
    device_token="user_device_token",
    title="환영합니다! 🎉",
    body="회원가입을 축하드립니다!",
    data={"user_id": "12345", "action": "welcome"}
)
```

### 여러 사용자에게 공지사항 전송
```python
device_tokens = ["token1", "token2", "token3"]

results = fcm_service.send_notification_to_multiple(
    device_tokens=device_tokens,
    title="중요 공지사항",
    body="시스템 점검이 예정되어 있습니다."
)

print(f"전송 결과: {results}")
```

### 뉴스 토픽 구독자에게 알림
```python
success = fcm_service.send_notification_to_topic(
    topic="breaking_news",
    title="속보",
    body="중요한 뉴스가 발생했습니다!"
)
```

## 🧪 테스트

### 단위 테스트 실행
```bash
python -m pytest tests/
```

### 수동 테스트
```bash
# 간단한 테스트
python simple_test.py

# Flask 서버 테스트
curl -X POST http://localhost:5000/test
```

## 🔒 보안 고려사항

- Service Account Key 파일을 버전 관리에 포함하지 마세요
- 프로덕션 환경에서는 환경 변수를 사용하세요
- HTTPS를 사용하여 통신을 암호화하세요
- API 키와 토큰을 안전하게 관리하세요

## 📦 의존성

- `flask` - 웹 프레임워크
- `google-auth` - Google OAuth 2.0 인증
- `requests` - HTTP 클라이언트

## 🚀 배포

### Docker 사용
```dockerfile
FROM python:3.9-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .
EXPOSE 5000

CMD ["python", "flask_app.py"]
```

### Heroku 배포
```bash
# Procfile 생성
echo "web: python flask_app.py" > Procfile

# Heroku 앱 생성 및 배포
heroku create your-app-name
git push heroku main
```

## 📝 라이선스

MIT License 
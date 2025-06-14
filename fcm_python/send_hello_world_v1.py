#!/usr/bin/env python3
"""
Firebase Cloud Messaging HTTP v1 API를 사용한 "Hello World" 푸시 알림
"""

import requests
import json
import time
from google.auth.transport.requests import Request
from google.oauth2 import service_account

# 제공받은 정보
DEVICE_TOKEN = "dVFgj5gCw1tUG4hlgXL7i-:APA91bGNXCdH4LmgDbLTupQYJtDH1gYcHrbJkoYi3idPqn62bdLRNP8S2F4lR7b887mKfbrpjHobFyQY2WuXAnu4I6wMSc17RkJ-wBmwzj-Jm9S4XUP1dW8"
PROJECT_ID = "my-notification-4d6dc"
SENDER_ID = "587467948856"

# FCM v1 API 스코프
SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']

def get_access_token_from_service_account(service_account_file_path):
    """
    Service Account JSON 파일로부터 OAuth 2.0 액세스 토큰 생성
    """
    try:
        credentials = service_account.Credentials.from_service_account_file(
            service_account_file_path, scopes=SCOPES
        )
        request = Request()
        credentials.refresh(request)
        return credentials.token
    except Exception as e:
        print(f"❌ 액세스 토큰 생성 실패: {e}")
        return None

def send_fcm_v1_notification(access_token, device_token, title, body):
    """
    FCM HTTP v1 API를 사용하여 푸시 알림 전송
    """
    print("🔗 FCM HTTP v1 API로 푸시 알림 전송 중...")
    
    url = f"https://fcm.googleapis.com/v1/projects/{PROJECT_ID}/messages:send"
    
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Content-Type': 'application/json; UTF-8'
    }
    
    payload = {
        "message": {
            "token": device_token,
            "notification": {
                "title": title,
                "body": body
            },
            "data": {
                "message": "Hello from FCM v1!",
                "timestamp": str(int(time.time())),
                "custom_key": "custom_value"
            },
            "android": {
                "priority": "high",
                "notification": {
                    "sound": "default",
                    "click_action": "OPEN_ACTIVITY_1",
                    "channel_id": "high_importance_channel"
                }
            }
        }
    }
    
    print(f"📤 요청 URL: {url}")
    print(f"📋 헤더: {headers}")
    print(f"📦 페이로드: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(payload))
        
        print(f"/n📊 응답 상태 코드: {response.status_code}")
        print(f"📄 응답 헤더: {dict(response.headers)}")
        print(f"📝 응답 내용: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ 푸시 알림 전송 성공!")
            print(f"메시지 이름: {result.get('name', 'N/A')}")
            return True
        else:
            print("❌ 푸시 알림 전송 실패!")
            try:
                error_info = response.json()
                print(f"오류 정보: {error_info}")
            except:
                print(f"오류 내용: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ 예외 발생: {e}")
        return False

def explain_fcm_v1_tcp_connection():
    """
    FCM HTTP v1 API TCP 연결 기반 통신 과정 설명
    """
    print("/n" + "="*70)
    print("🌐 FCM HTTP v1 API TCP 연결 기반 통신 과정")
    print("="*70)
    
    print("""
🔄 OAuth 2.0 토큰 생성 과정:
1. 📄 Service Account JSON 파일 읽기
2. 🔐 JWT(JSON Web Token) 생성 및 서명
3. 🔗 Google OAuth 2.0 서버에 토큰 요청
4. 🎫 Bearer 토큰 수신

📡 FCM v1 API 통신 과정:
1. 🌐 DNS 해석:
   - 클라이언트가 'fcm.googleapis.com' 도메인을 IP 주소로 변환
   - Google Cloud의 글로벌 CDN 엣지 서버 IP 획득

2. 🔌 TCP 연결 설정 (TCP 3-Way Handshake):
   - 클라이언트 → FCM 서버: SYN 패킷 (포트 443)
   - FCM 서버 → 클라이언트: SYN-ACK 패킷
   - 클라이언트 → FCM 서버: ACK 패킷 (연결 확립)

3. 🔐 TLS 1.3 핸드셰이크:
   - 클라이언트: TLS Client Hello (지원 암호화 스위트 포함)
   - 서버: TLS Server Hello + 인증서 + 키 교환
   - 클라이언트: 인증서 검증 + 마스터 시크릿 생성
   - 양방향: Finished 메시지로 암호화 터널 확립

 4. 📤 HTTP/2 요청 전송:
    POST /v1/projects/my-notification-4d6dc/messages:send HTTP/2
    Host: fcm.googleapis.com
    Authorization: Bearer [ACCESS_TOKEN]
    Content-Type: application/json; charset=UTF-8
    
    {
      "message": {
        "token": "[DEVICE_TOKEN]",
        "notification": {
          "title": "Hello World",
          "body": "FCM v1 테스트 메시지"
        }
      }
    }

5. 🏗️ FCM 서버 처리:
   - OAuth 2.0 Bearer 토큰 검증
   - 프로젝트 ID 및 권한 확인
   - 디바이스 토큰 유효성 검사
   - 메시지 큐에 추가 및 라우팅

6. 📱 Google Play Services 경유 전송:
   - FCM 서버 → Google Play Services (Long-lived connection)
   - Google Play Services → 타겟 앱
   - 앱의 FirebaseMessagingService.onMessageReceived() 호출

 7. 📊 HTTP/2 응답:
    - FCM 서버 → 클라이언트: HTTP 200 OK
    - JSON 응답: {"name": "projects/my-notification-4d6dc/messages/MESSAGE_ID"}

8. 🔚 연결 종료:
   - HTTP/2 스트림 종료
   - TLS 세션 종료 (또는 Keep-Alive로 재사용)
   - TCP 연결 FIN/ACK 패킷으로 정상 종료

💡 주요 차이점 (Legacy vs v1):
- ✅ OAuth 2.0 Bearer 토큰 (vs API Key)
- ✅ 프로젝트별 엔드포인트 (vs 글로벌 엔드포인트)  
- ✅ 향상된 보안 및 권한 관리
- ✅ HTTP/2 지원으로 성능 향상
- ✅ 더 정확한 오류 메시지 및 상태 코드
""")

def get_service_account_instructions():
    """
    Service Account 키 파일 가져오는 방법 안내
    """
    print("/n" + "="*70)
    print("🔑 Service Account 키 파일 가져오기")
    print("="*70)
    
    print("""
FCM HTTP v1 API를 사용하려면 Service Account JSON 키 파일이 필요합니다.

 📝 Service Account 키 파일 다운로드:
 1. 🌐 Firebase Console 접속: https://console.firebase.google.com/
 2. 📂 프로젝트 선택: my-notification-4d6dc
3. ⚙️  왼쪽 상단 톱니바퀴 → "프로젝트 설정"
4. 📋 "서비스 계정" 탭 클릭
5. 🔽 "새 비공개 키 생성" → "키 생성" 클릭
6. 💾 JSON 파일 다운로드 (예: service-account-key.json)

⚠️  보안 주의사항:
- 🔒 Service Account 키 파일은 절대 외부에 노출하지 마세요
- 🗂️  안전한 서버 환경에서만 사용하세요
- 🔄 정기적으로 키를 교체하는 것이 좋습니다
- 📁 Git 저장소에 커밋하지 마세요 (.gitignore 추가)

 📄 키 파일 예시 구조:
 {
   "type": "service_account",
   "project_id": "my-notification-4d6dc",
   "private_key_id": "...",
   "private_key": "-----BEGIN PRIVATE KEY-----//n...//n-----END PRIVATE KEY-----//n",
   "client_email": "firebase-adminsdk-xxxxx@my-notification-4d6dc.iam.gserviceaccount.com",
   "client_id": "...",
   "auth_uri": "https://accounts.google.com/o/oauth2/auth",
   "token_uri": "https://oauth2.googleapis.com/token"
 }
""")

def send_with_requests_only(device_token, title, body):
    """
    google-auth 라이브러리 없이 requests만으로 FCM v1 API 호출
    (Service Account 키 수동 처리)
    """
    print("/n" + "="*70)
    print("🔧 수동 OAuth 2.0 토큰 생성 방법 (참고용)")
    print("="*70)
    
    print("""
google-auth 라이브러리가 없는 경우 수동으로 JWT 토큰을 생성할 수 있습니다:

1. 📦 필요한 라이브러리 설치:
   pip install PyJWT cryptography

2. 🔐 JWT 토큰 생성 코드:
   import jwt
   import time
   
   def create_jwt_token(service_account_info):
       now = int(time.time())
       payload = {
           "iss": service_account_info["client_email"],
           "scope": "https://www.googleapis.com/auth/firebase.messaging",
           "aud": "https://oauth2.googleapis.com/token",
           "iat": now,
           "exp": now + 3600
       }
       return jwt.encode(payload, service_account_info["private_key"], algorithm="RS256")

 3. 🎫 액세스 토큰 교환:
    POST https://oauth2.googleapis.com/token
    Content-Type: application/x-www-form-urlencoded
    
    grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=[JWT_TOKEN]
""")

if __name__ == "__main__":
    print("🔥 Firebase Cloud Messaging HTTP v1 API - Hello World 푸시 전송")
    print("="*70)
    
    # TCP 연결 과정 설명
    explain_fcm_v1_tcp_connection()
    
    # Service Account 키 안내
    get_service_account_instructions()
    
    # 수동 구현 방법 안내
    send_with_requests_only(DEVICE_TOKEN, "Hello World", "FCM v1 테스트")
    
    # 실제 Service Account 키 파일 경로 입력 받기
    print("/n" + "="*70)
    service_account_path = "C:/Users/parkg/Downloads/my-notification-4d6dc-firebase-adminsdk-fbsvc-e4c2c74dcc.json"
    #input("🔑 Service Account JSON 키 파일 경로를 입력하세요: ").strip()
    
    if service_account_path and service_account_path.endswith('.json'):
        print(f"/n🔄 OAuth 2.0 액세스 토큰 생성 중...")
        access_token = get_access_token_from_service_account(service_account_path)
        
        if access_token:
            print(f"✅ 액세스 토큰 생성 성공!")
            print(f"🎫 토큰: {access_token[:50]}...")
            
            print(f"/n📤 Hello World 푸시 알림 전송 중...")
            print(f"📱 대상 디바이스: {DEVICE_TOKEN[:20]}...")
            
            success = send_fcm_v1_notification(
                access_token=access_token,
                device_token=DEVICE_TOKEN,
                title="Hello World",
                body="FCM HTTP v1 API 테스트 메시지입니다! 🚀"
            )
            
            if success:
                print("/n🎉 푸시 알림이 성공적으로 전송되었습니다!")
            else:
                print("/n😞 푸시 알림 전송에 실패했습니다.")
        else:
            print("/n❌ 액세스 토큰 생성에 실패했습니다.")
            
    else:
        print("/n⚠️  올바른 Service Account JSON 파일 경로를 입력해주세요.")
        print("예시: C://path//to//service-account-key.json") 
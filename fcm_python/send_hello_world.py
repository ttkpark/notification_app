#!/usr/bin/env python3
"""
FCM "Hello World" 푸시 알림 보내기
"""

import requests
import json
import time

# 제공받은 정보
DEVICE_TOKEN = "dVFgj5gCw1tUG4hlgXL7i-:APA91bGNXCdH4LmgDbLTupQYJtDH1gYcHrbJkoYi3idPqn62bdLRNP8S2F4lR7b887mKfbrpjHobFyQY2WuXAnu4I6wMSc17RkJ-wBmwzj-Jm9S4XUP1dW8"
SENDER_ID = "587467948856"  # 프로젝트 번호 (실제 서버 키 아님)
WEB_PUSH_CERT = "BJFqdr5Ft4ZF8qkt2i1TJN37m1ePszOjd8G8Xg1r2GhdGaRwnfe0baRE934pgZhgQewtc9yxluk5ipD2neOrmMg"

# Firebase Console에서 가져와야 하는 실제 서버 키 (예시)
# SERVER_KEY = "AAAA..."  # Firebase Console > Project Settings > Cloud Messaging > Server Key

def send_fcm_notification_legacy_api(server_key, device_token, title, body):
    """
    Legacy FCM API를 사용하여 푸시 알림 전송
    """
    print("🔗 FCM Legacy API로 푸시 알림 전송 중...")
    
    url = "https://fcm.googleapis.com/fcm/send"
    
    headers = {
        'Authorization': f'key={server_key}',
        'Content-Type': 'application/json; UTF-8'
    }
    
    payload = {
        "to": device_token,
        "notification": {
            "title": title,
            "body": body,
            "sound": "default",
            "click_action": "OPEN_ACTIVITY_1"
        },
        "data": {
            "message": "Hello from FCM!",
            "timestamp": str(int(time.time()))
        },
        "priority": "high"
    }
    
    print(f"📤 요청 URL: {url}")
    print(f"📋 헤더: {headers}")
    print(f"📦 페이로드: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(payload))
        
        print(f"\n📊 응답 상태 코드: {response.status_code}")
        print(f"📄 응답 헤더: {dict(response.headers)}")
        print(f"📝 응답 내용: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get('success') == 1:
                print("✅ 푸시 알림 전송 성공!")
                return True
            else:
                print("❌ 푸시 알림 전송 실패!")
                print(f"오류 정보: {result}")
                return False
        else:
            print("❌ HTTP 요청 실패!")
            return False
            
    except Exception as e:
        print(f"❌ 예외 발생: {e}")
        return False

def explain_tcp_connection():
    """
    TCP 연결 기반 FCM 통신 과정 설명
    """
    print("\n" + "="*60)
    print("🌐 FCM TCP 연결 기반 통신 과정")
    print("="*60)
    
    print("""
1. 📡 DNS 해석 (DNS Resolution):
   - 클라이언트가 'fcm.googleapis.com' 도메인을 IP 주소로 변환
   - DNS 서버에 쿼리하여 Google FCM 서버의 실제 IP 주소 얻음
   
2. 🔌 TCP 연결 설정 (TCP Handshake):
   - 클라이언트 → FCM 서버: SYN 패킷 전송 (포트 443)
   - FCM 서버 → 클라이언트: SYN-ACK 패킷 응답
   - 클라이언트 → FCM 서버: ACK 패킷으로 연결 확립
   
3. 🔐 TLS 핸드셰이크 (TLS Handshake):
   - 클라이언트가 TLS Client Hello 메시지 전송
   - 서버가 인증서와 함께 Server Hello 응답
   - 암호화 키 교환 및 보안 채널 설정
   
4. 📤 HTTP 요청 전송:
   - POST /fcm/send HTTP/1.1
   - Host: fcm.googleapis.com
   - Authorization: key=[SERVER_KEY]
   - Content-Type: application/json
   - 
   - {JSON 페이로드}
   
5. 📥 FCM 서버 처리:
   - 서버 키 검증
   - 디바이스 토큰 유효성 확인
   - Google Play Services를 통해 디바이스에 푸시 전송
   
6. 📱 디바이스 수신:
   - Android 디바이스의 Google Play Services가 푸시 수신
   - 앱이 포그라운드/백그라운드 상태에 따라 다른 처리
   - FirebaseMessagingService.onMessageReceived() 호출
   
7. 📊 HTTP 응답:
   - FCM 서버 → 클라이언트: HTTP 200 OK
   - JSON 응답: {"multicast_id": ..., "success": 1, "failure": 0}
   
8. 🔚 연결 종료:
   - TCP FIN/ACK 패킷 교환으로 연결 정상 종료
""")

def get_server_key_instructions():
    """
    올바른 서버 키 가져오는 방법 안내
    """
    print("\n" + "="*60)
    print("🔑 올바른 서버 키 가져오기")
    print("="*60)
    
    print("""
발신자 ID (587467948856)는 프로젝트 번호이고, 실제 푸시를 보내려면 서버 키가 필요합니다.

📝 서버 키 가져오는 방법:
1. Firebase Console 접속: https://console.firebase.google.com/
2. 프로젝트 선택: my-notification-4d6dc
3. 왼쪽 상단 톱니바퀴 → "프로젝트 설정"
4. "Cloud Messaging" 탭 클릭
5. "서버 키" 복사 (AAAA로 시작하는 긴 문자열)

⚠️  주의사항:
- 서버 키는 보안에 민감하므로 외부에 노출하지 마세요
- Legacy API는 2024년 6월부터 deprecated 예정
- 새로운 프로젝트는 HTTP v1 API 사용 권장
""")

if __name__ == "__main__":
    print("🔥 Firebase Cloud Messaging - Hello World 푸시 전송")
    print("="*60)
    
    # TCP 연결 과정 설명
    explain_tcp_connection()
    
    # 서버 키 안내
    get_server_key_instructions()
    
    # 실제 서버 키 입력 받기
    print("\n" + "="*60)
    server_key = input("🔑 Firebase Console에서 가져온 서버 키를 입력하세요: ").strip()
    
    if server_key and server_key != "YOUR_SERVER_KEY_HERE":
        print(f"\n📤 Hello World 푸시 알림 전송 중...")
        print(f"📱 대상 디바이스: {DEVICE_TOKEN[:20]}...")
        
        success = send_fcm_notification_legacy_api(
            server_key=server_key,
            device_token=DEVICE_TOKEN,
            title="Hello World",
            body="FCM 테스트 메시지입니다! 🚀"
        )
        
        if success:
            print("\n🎉 푸시 알림이 성공적으로 전송되었습니다!")
        else:
            print("\n😞 푸시 알림 전송에 실패했습니다.")
            
    else:
        print("\n⚠️  올바른 서버 키를 입력해주세요.")
        print("발신자 ID (587467948856)로는 푸시를 보낼 수 없습니다.") 
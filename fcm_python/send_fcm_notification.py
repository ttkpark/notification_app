#!/usr/bin/env python3
"""
FCM 알림 보내기 스크립트
Firebase Admin SDK를 사용하여 FCM 알림을 보냅니다.
"""

import requests
import json

def send_fcm_notification_with_legacy_api(device_token, title, body):
    """
    Legacy FCM API를 사용하여 알림 전송
    """
    # Firebase 프로젝트의 Server Key가 필요합니다
    # Firebase Console > Project Settings > Cloud Messaging에서 확인 가능
    server_key = "YOUR_SERVER_KEY_HERE"  # 실제 서버 키로 교체하세요
    
    url = "https://fcm.googleapis.com/fcm/send"
    
    headers = {
        'Authorization': f'key={server_key}',
        'Content-Type': 'application/json'
    }
    
    payload = {
        "to": device_token,
        "notification": {
            "title": title,
            "body": body,
            "sound": "default"
        },
        "data": {
            "click_action": "FLUTTER_NOTIFICATION_CLICK",
            "custom_data": "추가 데이터"
        }
    }
    
    response = requests.post(url, headers=headers, data=json.dumps(payload))
    
    if response.status_code == 200:
        result = response.json()
        print("✅ 알림 전송 성공!")
        print(f"결과: {result}")
        return True
    else:
        print("❌ 알림 전송 실패!")
        print(f"상태 코드: {response.status_code}")
        print(f"응답: {response.text}")
        return False

def send_fcm_notification_with_v1_api(device_token, title, body):
    """
    Firebase Admin SDK v1 API를 사용하여 알림 전송
    OAuth 2.0 토큰이 필요합니다.
    """
    # 이 방법은 서비스 계정 키가 필요합니다
    # 실제 구현 시 firebase-admin SDK 사용을 권장합니다
    pass

if __name__ == "__main__":
    # 사용 예시
    device_token = input("FCM 토큰을 입력하세요: ").strip()
    title = input("알림 제목을 입력하세요: ").strip() or "테스트 알림"
    body = input("알림 내용을 입력하세요: ").strip() or "FCM 테스트 메시지입니다."
    
    print(f"\n📤 알림 전송 중...")
    print(f"제목: {title}")
    print(f"내용: {body}")
    print(f"토큰: {device_token[:20]}...")
    
    send_fcm_notification_with_legacy_api(device_token, title, body) 
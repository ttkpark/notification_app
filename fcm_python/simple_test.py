#!/usr/bin/env python3
"""
간단한 FCM 테스트 스크립트
Firebase Service Account Key 파일이 필요합니다.
"""

from fcm_service import FCMService
import time

def main():
    """메인 함수"""
    print("🚀 FCM 테스트 시작")
    
    # FCM 서비스 초기화
    try:
        fcm_service = FCMService(
            project_id="my-notification-4d6dc",
            service_account_key_path="firebase-service-account-key.json"
        )
        print("✅ FCM 서비스 초기화 완료")
    except Exception as e:
        print(f"❌ FCM 서비스 초기화 실패: {e}")
        return
    
    # 테스트 디바이스 토큰
    device_token = "dVFgj5gCw1tUG4hlgXL7i-:APA91bGNXCdH4LmgDbLTupQYJtDH1gYcHrbJkoYi3idPqn62bdLRNP8S2F4lR7b887mKfbrpjHobFyQY2WuXAnu4I6wMSc17RkJ-wBmwzj-Jm9S4XUP1dW8"
    
    # 테스트 데이터
    test_data = {
        "custom_key": "test_value",
        "timestamp": str(int(time.time())),
        "message": "Hello from Python Test Script!",
        "test_id": "python_simple_test"
    }
    
    print("\n📱 단일 디바이스 알림 전송 테스트")
    success = fcm_service.send_notification(
        device_token=device_token,
        title="Python 테스트 알림",
        body="Python 스크립트에서 보낸 테스트 메시지입니다! 🐍",
        data=test_data
    )
    
    if success:
        print("✅ 단일 디바이스 알림 전송 성공")
    else:
        print("❌ 단일 디바이스 알림 전송 실패")
    
    print("\n📱📱 다중 디바이스 알림 전송 테스트")
    # 같은 토큰을 여러 번 사용 (테스트용)
    device_tokens = [device_token, device_token]
    
    results = fcm_service.send_notification_to_multiple(
        device_tokens=device_tokens,
        title="Python 다중 알림",
        body="여러 디바이스에 보내는 테스트 메시지입니다!",
        data=test_data
    )
    
    success_count = sum(1 for success in results.values() if success)
    total_count = len(results)
    print(f"📊 다중 전송 결과: {total_count}개 중 {success_count}개 성공")
    
    print("\n🎯 토픽 알림 전송 테스트")
    topic_success = fcm_service.send_notification_to_topic(
        topic="test_topic",
        title="Python 토픽 알림",
        body="토픽 구독자들에게 보내는 메시지입니다!",
        data=test_data
    )
    
    if topic_success:
        print("✅ 토픽 알림 전송 성공")
    else:
        print("❌ 토픽 알림 전송 실패")
    
    print("\n🎉 FCM 테스트 완료!")

if __name__ == "__main__":
    main() 
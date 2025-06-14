import json
import requests
from google.auth.transport.requests import Request
from google.oauth2 import service_account
from typing import Dict, List, Optional
import time

class FCMService:
    """Firebase Cloud Messaging HTTP v1 API를 사용한 푸시 알림 서비스"""
    
    def __init__(self, project_id: str, service_account_key_path: str):
        """
        FCM 서비스 초기화
        
        Args:
            project_id: Firebase 프로젝트 ID
            service_account_key_path: 서비스 계정 키 파일 경로
        """
        self.project_id = project_id
        self.service_account_key_path = service_account_key_path
        self.base_url = f"https://fcm.googleapis.com/v1/projects/{project_id}/messages:send"
        
    def _get_access_token(self) -> str:
        """Google Service Account를 사용하여 OAuth 2.0 액세스 토큰 획득"""
        credentials = service_account.Credentials.from_service_account_file(
            self.service_account_key_path,
            scopes=['https://www.googleapis.com/auth/firebase.messaging']
        )
        
        request = Request()
        credentials.refresh(request)
        return credentials.token
    
    def send_notification(
        self, 
        device_token: str, 
        title: str, 
        body: str, 
        data: Optional[Dict[str, str]] = None
    ) -> bool:
        """
        단일 디바이스에 푸시 알림 전송
        
        Args:
            device_token: 대상 디바이스 토큰
            title: 알림 제목
            body: 알림 내용
            data: 추가 데이터 (선택사항)
            
        Returns:
            bool: 전송 성공 여부
        """
        try:
            access_token = self._get_access_token()
            
            # FCM 메시지 구성
            message = {
                "message": {
                    "token": device_token,
                    "notification": {
                        "title": title,
                        "body": body
                    },
                    "data": data or {},
                    "android": {
                        "notification": {
                            "channel_id": "notification_channel",
                            "priority": "high"
                        }
                    }
                }
            }
            
            # HTTP 요청 헤더
            headers = {
                'Authorization': f'Bearer {access_token}',
                'Content-Type': 'application/json'
            }
            
            # FCM API 호출
            response = requests.post(
                self.base_url,
                headers=headers,
                data=json.dumps(message)
            )
            
            if response.status_code == 200:
                print(f"FCM 전송 성공: {response.json()}")
                return True
            else:
                print(f"FCM 전송 실패: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            print(f"FCM 전송 중 오류 발생: {str(e)}")
            return False
    
    def send_notification_to_multiple(
        self, 
        device_tokens: List[str], 
        title: str, 
        body: str, 
        data: Optional[Dict[str, str]] = None
    ) -> Dict[str, bool]:
        """
        여러 디바이스에 동시 푸시 알림 전송
        
        Args:
            device_tokens: 대상 디바이스 토큰 리스트
            title: 알림 제목
            body: 알림 내용
            data: 추가 데이터 (선택사항)
            
        Returns:
            Dict[str, bool]: 각 토큰별 전송 결과
        """
        results = {}
        
        for token in device_tokens:
            success = self.send_notification(token, title, body, data)
            results[token] = success
            
        return results
    
    def send_notification_to_topic(
        self, 
        topic: str, 
        title: str, 
        body: str, 
        data: Optional[Dict[str, str]] = None
    ) -> bool:
        """
        토픽에 푸시 알림 전송
        
        Args:
            topic: 대상 토픽
            title: 알림 제목
            body: 알림 내용
            data: 추가 데이터 (선택사항)
            
        Returns:
            bool: 전송 성공 여부
        """
        try:
            access_token = self._get_access_token()
            
            # FCM 메시지 구성
            message = {
                "message": {
                    "topic": topic,
                    "notification": {
                        "title": title,
                        "body": body
                    },
                    "data": data or {}
                }
            }
            
            # HTTP 요청 헤더
            headers = {
                'Authorization': f'Bearer {access_token}',
                'Content-Type': 'application/json'
            }
            
            # FCM API 호출
            response = requests.post(
                self.base_url,
                headers=headers,
                data=json.dumps(message)
            )
            
            if response.status_code == 200:
                print(f"토픽 알림 전송 성공: {response.json()}")
                return True
            else:
                print(f"토픽 알림 전송 실패: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            print(f"토픽 알림 전송 중 오류 발생: {str(e)}")
            return False

# 사용 예시
if __name__ == "__main__":
    # FCM 서비스 초기화
    fcm_service = FCMService(
        project_id="my-notification-4d6dc",
        service_account_key_path="firebase-service-account-key.json"
    )
    
    # 테스트 알림 전송
    device_token = "dVFgj5gCw1tUG4hlgXL7i-:APA91bGNXCdH4LmgDbLTupQYJtDH1gYcHrbJkoYi3idPqn62bdLRNP8S2F4lR7b887mKfbrpjHobFyQY2WuXAnu4I6wMSc17RkJ-wBmwzj-Jm9S4XUP1dW8"
    
    data = {
        "custom_key": "custom_value",
        "timestamp": str(int(time.time())),
        "message": "Hello from Python!"
    }
    
    success = fcm_service.send_notification(
        device_token=device_token,
        title="Python 테스트",
        body="Python에서 보낸 테스트 메시지입니다! 🐍",
        data=data
    )
    
    if success:
        print("테스트 알림이 성공적으로 전송되었습니다.")
    else:
        print("테스트 알림 전송에 실패했습니다.") 
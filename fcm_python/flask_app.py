from flask import Flask, request, jsonify
from fcm_service import FCMService
import time
import os

app = Flask(__name__)

# FCM 서비스 초기화
fcm_service = FCMService(
    project_id="my-notification-4d6dc",
    service_account_key_path="firebase-service-account-key.json"
)

@app.route('/')
def home():
    """홈 페이지"""
    return {
        "message": "FCM Notification Service (Python Flask)",
        "version": "1.0.0",
        "endpoints": {
            "POST /send": "단일 디바이스 알림 전송",
            "POST /send-multiple": "다중 디바이스 알림 전송", 
            "POST /send-topic": "토픽 알림 전송",
            "POST /test": "테스트 알림 전송"
        }
    }

@app.route('/send', methods=['POST'])
def send_notification():
    """단일 디바이스에 푸시 알림 전송"""
    try:
        data = request.get_json()
        
        # 필수 필드 검증
        if not data or not all(k in data for k in ('deviceToken', 'title', 'body')):
            return jsonify({
                "error": "deviceToken, title, body는 필수입니다."
            }), 400
        
        device_token = data['deviceToken']
        title = data['title']
        body = data['body']
        custom_data = data.get('data', {})
        
        # FCM 알림 전송
        success = fcm_service.send_notification(
            device_token=device_token,
            title=title,
            body=body,
            data=custom_data
        )
        
        if success:
            return jsonify({
                "message": "알림이 성공적으로 전송되었습니다."
            })
        else:
            return jsonify({
                "error": "알림 전송에 실패했습니다."
            }), 500
            
    except Exception as e:
        return jsonify({
            "error": f"서버 오류: {str(e)}"
        }), 500

@app.route('/send-multiple', methods=['POST'])
def send_notification_to_multiple():
    """여러 디바이스에 푸시 알림 전송"""
    try:
        data = request.get_json()
        
        # 필수 필드 검증
        if not data or not all(k in data for k in ('deviceTokens', 'title', 'body')):
            return jsonify({
                "error": "deviceTokens, title, body는 필수입니다."
            }), 400
        
        device_tokens = data['deviceTokens']
        title = data['title']
        body = data['body']
        custom_data = data.get('data', {})
        
        if not isinstance(device_tokens, list) or len(device_tokens) == 0:
            return jsonify({
                "error": "deviceTokens는 비어있지 않은 배열이어야 합니다."
            }), 400
        
        # FCM 알림 전송
        results = fcm_service.send_notification_to_multiple(
            device_tokens=device_tokens,
            title=title,
            body=body,
            data=custom_data
        )
        
        success_count = sum(1 for success in results.values() if success)
        total_count = len(results)
        
        return jsonify({
            "message": f"{total_count}개 중 {success_count}개 전송 성공",
            "results": results
        })
        
    except Exception as e:
        return jsonify({
            "error": f"서버 오류: {str(e)}"
        }), 500

@app.route('/send-topic', methods=['POST'])
def send_notification_to_topic():
    """토픽에 푸시 알림 전송"""
    try:
        data = request.get_json()
        
        # 필수 필드 검증
        if not data or not all(k in data for k in ('topic', 'title', 'body')):
            return jsonify({
                "error": "topic, title, body는 필수입니다."
            }), 400
        
        topic = data['topic']
        title = data['title']
        body = data['body']
        custom_data = data.get('data', {})
        
        # FCM 알림 전송
        success = fcm_service.send_notification_to_topic(
            topic=topic,
            title=title,
            body=body,
            data=custom_data
        )
        
        if success:
            return jsonify({
                "message": "토픽 알림이 성공적으로 전송되었습니다."
            })
        else:
            return jsonify({
                "error": "토픽 알림 전송에 실패했습니다."
            }), 500
            
    except Exception as e:
        return jsonify({
            "error": f"서버 오류: {str(e)}"
        }), 500

@app.route('/test', methods=['POST'])
def send_test_notification():
    """테스트용 - Android 앱에 알림 전송"""
    try:
        device_token = "dVFgj5gCw1tUG4hlgXL7i-:APA91bGNXCdH4LmgDbLTupQYJtDH1gYcHrbJkoYi3idPqn62bdLRNP8S2F4lR7b887mKfbrpjHobFyQY2WuXAnu4I6wMSc17RkJ-wBmwzj-Jm9S4XUP1dW8"
        
        data = {
            "custom_key": "custom_value",
            "timestamp": str(int(time.time())),
            "message": "Hello from Python Flask!"
        }
        
        success = fcm_service.send_notification(
            device_token=device_token,
            title="Python Flask 테스트",
            body="Python Flask에서 보낸 테스트 메시지입니다! 🐍",
            data=data
        )
        
        if success:
            return jsonify({
                "message": "테스트 알림이 성공적으로 전송되었습니다."
            })
        else:
            return jsonify({
                "error": "테스트 알림 전송에 실패했습니다."
            }), 500
            
    except Exception as e:
        return jsonify({
            "error": f"서버 오류: {str(e)}"
        }), 500

@app.errorhandler(404)
def not_found(error):
    """404 에러 핸들러"""
    return jsonify({
        "error": "요청한 엔드포인트를 찾을 수 없습니다.",
        "available_endpoints": [
            "GET /",
            "POST /send",
            "POST /send-multiple", 
            "POST /send-topic",
            "POST /test"
        ]
    }), 404

@app.errorhandler(405)
def method_not_allowed(error):
    """405 에러 핸들러"""
    return jsonify({
        "error": "허용되지 않은 HTTP 메서드입니다."
    }), 405

@app.errorhandler(500)
def internal_error(error):
    """500 에러 핸들러"""
    return jsonify({
        "error": "내부 서버 오류가 발생했습니다."
    }), 500

if __name__ == '__main__':
    # 개발 서버 실행
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=True) 
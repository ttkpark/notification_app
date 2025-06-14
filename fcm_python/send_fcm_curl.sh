#!/bin/bash

# FCM 알림 보내기 스크립트 (curl 사용)
# 사용법: ./send_fcm_curl.sh "FCM_TOKEN" "제목" "내용"

# 서버 키 설정 (Firebase Console > Project Settings > Cloud Messaging에서 가져오세요)
SERVER_KEY="YOUR_SERVER_KEY_HERE"

# 매개변수 확인
if [ $# -lt 1 ]; then
    echo "사용법: $0 \"FCM_TOKEN\" [\"제목\"] [\"내용\"]"
    echo "예시: $0 \"dA1B2C3D4E5F...\" \"테스트 알림\" \"안녕하세요!\""
    exit 1
fi

# 매개변수 설정
FCM_TOKEN="$1"
TITLE="${2:-테스트 알림}"
BODY="${3:-FCM 테스트 메시지입니다.}"

echo "📤 FCM 알림 전송 중..."
echo "제목: $TITLE"
echo "내용: $BODY"
echo "토큰: ${FCM_TOKEN:0:20}..."

# FCM API 호출
curl -X POST https://fcm.googleapis.com/fcm/send \
    -H "Authorization: key=$SERVER_KEY" \
    -H "Content-Type: application/json" \
    -d "{
        \"to\": \"$FCM_TOKEN\",
        \"notification\": {
            \"title\": \"$TITLE\",
            \"body\": \"$BODY\",
            \"sound\": \"default\"
        },
        \"data\": {
            \"custom_key\": \"custom_value\",
            \"timestamp\": \"$(date +%s)\"
        }
    }"

echo -e "\n\n✅ 요청 완료!" 
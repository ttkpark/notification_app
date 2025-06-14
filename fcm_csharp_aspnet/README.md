# FCM Notification Service (C# ASP.NET Core)

Firebase Cloud Messaging HTTP v1 API를 사용하여 푸시 알림을 전송하는 C# ASP.NET Core Web API 프로젝트입니다.

## 🚀 기능

- **단일 디바이스 알림 전송**: 특정 디바이스 토큰으로 알림 전송
- **다중 디바이스 알림 전송**: 여러 디바이스에 동시 알림 전송
- **토픽 알림 전송**: 특정 토픽을 구독한 모든 디바이스에 알림 전송
- **OAuth 2.0 인증**: Google Service Account를 사용한 안전한 인증
- **Swagger UI**: API 문서화 및 테스트 인터페이스

## 📋 필수 요구사항

- .NET 8.0 SDK
- Firebase 프로젝트
- Firebase Service Account Key (JSON 파일)

## 🛠️ 설치 및 설정

### 1. 프로젝트 복원
```bash
dotnet restore
```

### 2. Firebase Service Account Key 설정
1. Firebase Console → 프로젝트 설정 → 서비스 계정
2. "새 비공개 키 생성" 클릭하여 JSON 파일 다운로드
3. 프로젝트 루트에 `firebase-service-account-key.json`으로 저장

### 3. appsettings.json 설정
```json
{
  "Firebase": {
    "ProjectId": "your-firebase-project-id",
    "ServiceAccountKeyPath": "firebase-service-account-key.json"
  }
}
```

### 4. 프로젝트 실행
```bash
dotnet run
```

## 📡 API 엔드포인트

### 테스트 알림 전송
```http
POST /api/notification/test
```

### 단일 디바이스 알림 전송
```http
POST /api/notification/send
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
POST /api/notification/send-multiple
Content-Type: application/json

{
  "deviceTokens": ["token1", "token2", "token3"],
  "title": "알림 제목",
  "body": "알림 내용"
}
```

### 토픽 알림 전송
```http
POST /api/notification/send-topic
Content-Type: application/json

{
  "topic": "news",
  "title": "뉴스 알림",
  "body": "새로운 뉴스가 있습니다."
}
```

## 🔧 사용 예시

### 컨트롤러에서 FCM 서비스 사용
```csharp
[ApiController]
public class MyController : ControllerBase
{
    private readonly FCMService _fcmService;

    public MyController(FCMService fcmService)
    {
        _fcmService = fcmService;
    }

    [HttpPost("send-welcome")]
    public async Task<IActionResult> SendWelcome(string deviceToken)
    {
        var success = await _fcmService.SendNotificationAsync(
            deviceToken,
            "환영합니다! 🎉",
            "회원가입을 축하드립니다!"
        );

        return success ? Ok() : StatusCode(500);
    }
}
```

## 📦 NuGet 패키지

- `Google.Apis.Auth` - Google OAuth 2.0 인증
- `Newtonsoft.Json` - JSON 직렬화/역직렬화
- `Swashbuckle.AspNetCore` - Swagger UI

## 🌐 Swagger UI

프로젝트 실행 후 `https://localhost:5001/swagger`에서 API 문서를 확인하고 테스트할 수 있습니다.

## 🔒 보안 고려사항

- Service Account Key 파일을 버전 관리에 포함하지 마세요
- 프로덕션 환경에서는 환경 변수나 Azure Key Vault 등을 사용하세요
- HTTPS를 사용하여 통신을 암호화하세요

## 📝 라이선스

MIT License 
using Microsoft.AspNetCore.Mvc;
using FCMNotificationService.Services;

namespace FCMNotificationService.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class NotificationController : ControllerBase
    {
        private readonly FCMService _fcmService;

        public NotificationController(FCMService fcmService)
        {
            _fcmService = fcmService;
        }

        /// <summary>
        /// 단일 디바이스에 푸시 알림 전송
        /// </summary>
        [HttpPost("send")]
        public async Task<IActionResult> SendNotification([FromBody] SendNotificationRequest request)
        {
            if (string.IsNullOrEmpty(request.DeviceToken) || 
                string.IsNullOrEmpty(request.Title) || 
                string.IsNullOrEmpty(request.Body))
            {
                return BadRequest("DeviceToken, Title, Body는 필수입니다.");
            }

            var success = await _fcmService.SendNotificationAsync(
                request.DeviceToken, 
                request.Title, 
                request.Body, 
                request.Data
            );

            if (success)
            {
                return Ok(new { message = "알림이 성공적으로 전송되었습니다." });
            }
            else
            {
                return StatusCode(500, new { message = "알림 전송에 실패했습니다." });
            }
        }

        /// <summary>
        /// 여러 디바이스에 푸시 알림 전송
        /// </summary>
        [HttpPost("send-multiple")]
        public async Task<IActionResult> SendNotificationToMultiple([FromBody] SendMultipleNotificationRequest request)
        {
            if (request.DeviceTokens == null || !request.DeviceTokens.Any() ||
                string.IsNullOrEmpty(request.Title) || 
                string.IsNullOrEmpty(request.Body))
            {
                return BadRequest("DeviceTokens, Title, Body는 필수입니다.");
            }

            var results = await _fcmService.SendNotificationToMultipleAsync(
                request.DeviceTokens, 
                request.Title, 
                request.Body, 
                request.Data
            );

            var successCount = results.Values.Count(x => x);
            var totalCount = results.Count;

            return Ok(new 
            { 
                message = $"{totalCount}개 중 {successCount}개 전송 성공",
                results = results
            });
        }

        /// <summary>
        /// 토픽에 푸시 알림 전송
        /// </summary>
        [HttpPost("send-topic")]
        public async Task<IActionResult> SendNotificationToTopic([FromBody] SendTopicNotificationRequest request)
        {
            if (string.IsNullOrEmpty(request.Topic) || 
                string.IsNullOrEmpty(request.Title) || 
                string.IsNullOrEmpty(request.Body))
            {
                return BadRequest("Topic, Title, Body는 필수입니다.");
            }

            var success = await _fcmService.SendNotificationToTopicAsync(
                request.Topic, 
                request.Title, 
                request.Body, 
                request.Data
            );

            if (success)
            {
                return Ok(new { message = "토픽 알림이 성공적으로 전송되었습니다." });
            }
            else
            {
                return StatusCode(500, new { message = "토픽 알림 전송에 실패했습니다." });
            }
        }

        /// <summary>
        /// 테스트용 - Android 앱에 알림 전송
        /// </summary>
        [HttpPost("test")]
        public async Task<IActionResult> SendTestNotification()
        {
            var deviceToken = "dVFgj5gCw1tUG4hlgXL7i-:APA91bGNXCdH4LmgDbLTupQYJtDH1gYcHrbJkoYi3idPqn62bdLRNP8S2F4lR7b887mKfbrpjHobFyQY2WuXAnu4I6wMSc17RkJ-wBmwzj-Jm9S4XUP1dW8";
            
            var data = new Dictionary<string, string>
            {
                ["custom_key"] = "custom_value",
                ["timestamp"] = DateTimeOffset.UtcNow.ToUnixTimeSeconds().ToString(),
                ["message"] = "Hello from C# ASP.NET!"
            };

            var success = await _fcmService.SendNotificationAsync(
                deviceToken,
                "C# ASP.NET 테스트",
                "C# ASP.NET에서 보낸 테스트 메시지입니다! 🚀",
                data
            );

            if (success)
            {
                return Ok(new { message = "테스트 알림이 성공적으로 전송되었습니다." });
            }
            else
            {
                return StatusCode(500, new { message = "테스트 알림 전송에 실패했습니다." });
            }
        }
    }

    // Request DTOs
    public class SendNotificationRequest
    {
        public string DeviceToken { get; set; } = string.Empty;
        public string Title { get; set; } = string.Empty;
        public string Body { get; set; } = string.Empty;
        public Dictionary<string, string>? Data { get; set; }
    }

    public class SendMultipleNotificationRequest
    {
        public List<string> DeviceTokens { get; set; } = new();
        public string Title { get; set; } = string.Empty;
        public string Body { get; set; } = string.Empty;
        public Dictionary<string, string>? Data { get; set; }
    }

    public class SendTopicNotificationRequest
    {
        public string Topic { get; set; } = string.Empty;
        public string Title { get; set; } = string.Empty;
        public string Body { get; set; } = string.Empty;
        public Dictionary<string, string>? Data { get; set; }
    }
} 
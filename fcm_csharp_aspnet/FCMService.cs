using Google.Apis.Auth.OAuth2;
using Newtonsoft.Json;
using System.Text;

namespace FCMNotificationService.Services
{
    public class FCMService
    {
        private readonly string _projectId;
        private readonly string _serviceAccountKeyPath;
        private readonly HttpClient _httpClient;

        public FCMService(string projectId, string serviceAccountKeyPath)
        {
            _projectId = projectId;
            _serviceAccountKeyPath = serviceAccountKeyPath;
            _httpClient = new HttpClient();
        }

        /// <summary>
        /// FCM HTTP v1 API를 사용하여 푸시 알림 전송
        /// </summary>
        public async Task<bool> SendNotificationAsync(string deviceToken, string title, string body, Dictionary<string, string>? data = null)
        {
            try
            {
                // OAuth 2.0 액세스 토큰 획득
                var accessToken = await GetAccessTokenAsync();
                
                // FCM 메시지 구성
                var message = new
                {
                    message = new
                    {
                        token = deviceToken,
                        notification = new
                        {
                            title = title,
                            body = body
                        },
                        data = data ?? new Dictionary<string, string>(),
                        android = new
                        {
                            notification = new
                            {
                                channel_id = "notification_channel",
                                priority = "high"
                            }
                        }
                    }
                };

                // HTTP 요청 전송
                var json = JsonConvert.SerializeObject(message);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                
                _httpClient.DefaultRequestHeaders.Clear();
                _httpClient.DefaultRequestHeaders.Add("Authorization", $"Bearer {accessToken}");

                var url = $"https://fcm.googleapis.com/v1/projects/{_projectId}/messages:send";
                var response = await _httpClient.PostAsync(url, content);

                if (response.IsSuccessStatusCode)
                {
                    var responseContent = await response.Content.ReadAsStringAsync();
                    Console.WriteLine($"FCM 전송 성공: {responseContent}");
                    return true;
                }
                else
                {
                    var errorContent = await response.Content.ReadAsStringAsync();
                    Console.WriteLine($"FCM 전송 실패: {response.StatusCode} - {errorContent}");
                    return false;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"FCM 전송 중 오류 발생: {ex.Message}");
                return false;
            }
        }

        /// <summary>
        /// 여러 디바이스에 동시 전송
        /// </summary>
        public async Task<Dictionary<string, bool>> SendNotificationToMultipleAsync(
            List<string> deviceTokens, 
            string title, 
            string body, 
            Dictionary<string, string>? data = null)
        {
            var results = new Dictionary<string, bool>();
            var tasks = deviceTokens.Select(async token =>
            {
                var success = await SendNotificationAsync(token, title, body, data);
                return new { Token = token, Success = success };
            });

            var taskResults = await Task.WhenAll(tasks);
            
            foreach (var result in taskResults)
            {
                results[result.Token] = result.Success;
            }

            return results;
        }

        /// <summary>
        /// 토픽에 메시지 전송
        /// </summary>
        public async Task<bool> SendNotificationToTopicAsync(string topic, string title, string body, Dictionary<string, string>? data = null)
        {
            try
            {
                var accessToken = await GetAccessTokenAsync();
                
                var message = new
                {
                    message = new
                    {
                        topic = topic,
                        notification = new
                        {
                            title = title,
                            body = body
                        },
                        data = data ?? new Dictionary<string, string>()
                    }
                };

                var json = JsonConvert.SerializeObject(message);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                
                _httpClient.DefaultRequestHeaders.Clear();
                _httpClient.DefaultRequestHeaders.Add("Authorization", $"Bearer {accessToken}");

                var url = $"https://fcm.googleapis.com/v1/projects/{_projectId}/messages:send";
                var response = await _httpClient.PostAsync(url, content);

                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"토픽 전송 중 오류 발생: {ex.Message}");
                return false;
            }
        }

        /// <summary>
        /// Google Service Account를 사용하여 OAuth 2.0 액세스 토큰 획득
        /// </summary>
        private async Task<string> GetAccessTokenAsync()
        {
            var credential = GoogleCredential.FromFile(_serviceAccountKeyPath)
                .CreateScoped("https://www.googleapis.com/auth/firebase.messaging");

            var token = await credential.UnderlyingCredential.GetAccessTokenForRequestAsync();
            return token;
        }

        public void Dispose()
        {
            _httpClient?.Dispose();
        }
    }
} 
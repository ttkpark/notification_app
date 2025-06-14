using FCMNotificationService.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// FCM 서비스 등록
builder.Services.AddSingleton<FCMService>(provider =>
{
    var configuration = provider.GetRequiredService<IConfiguration>();
    var projectId = configuration["Firebase:ProjectId"] ?? "my-notification-4d6dc";
    var serviceAccountKeyPath = configuration["Firebase:ServiceAccountKeyPath"] ?? "firebase-service-account-key.json";
    return new FCMService(projectId, serviceAccountKeyPath);
});

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run(); 
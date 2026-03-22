using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using iPosApi.Middleware;
using iPosApi.Models.Data;
using iPosApi.Services;

var builder = WebApplication.CreateBuilder(args);

// ── Controllers & API docs ──────────────────────────────────────────────────
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new() { Title = "iPOS API", Version = "v1" });
});

// ── Caching ─────────────────────────────────────────────────────────────────
builder.Services.AddMemoryCache();
builder.Services.AddScoped<CacheService>();

// ── Database ─────────────────────────────────────────────────────────────────
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(connectionString));

// ── Services ─────────────────────────────────────────────────────────────────
builder.Services.AddScoped<ISyncService, SyncService>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IInvoiceService, InvoiceService>();
builder.Services.AddScoped<IDeviceService, DeviceService>();
builder.Services.AddSingleton<IJwtService, JwtService>();
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IStoreDeviceService, StoreDeviceService>();

// ── Authentication / JWT Bearer ──────────────────────────────────────────────
var jwtSecret   = builder.Configuration["Jwt:Secret"]   ?? throw new InvalidOperationException("Jwt:Secret is not configured.");
var jwtIssuer   = builder.Configuration["Jwt:Issuer"]   ?? throw new InvalidOperationException("Jwt:Issuer is not configured.");
var jwtAudience = builder.Configuration["Jwt:Audience"] ?? throw new InvalidOperationException("Jwt:Audience is not configured.");

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer           = true,
            ValidateAudience         = true,
            ValidateLifetime         = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer              = jwtIssuer,
            ValidAudience            = jwtAudience,
            IssuerSigningKey         = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecret))
        };
    });

builder.Services.AddAuthorization();

// ── CORS ─────────────────────────────────────────────────────────────────────
builder.Services.AddCors(options =>
{
    options.AddPolicy("DashboardPolicy", policy =>
    {
        policy.WithOrigins("http://localhost:4200", "https://ipos.xaspro.co.uk")
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });

    // Allow Android devices on local network
    options.AddPolicy("AndroidPolicy", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});

var app = builder.Build();

// ── Database init ─────────────────────────────────────────────────────────────
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    context.Database.Migrate();
    try { DataSeeder.SeedData(context); }
    catch (Exception ex) { Console.WriteLine($"Seeding skipped: {ex.Message}"); }
}

// ── Middleware pipeline ───────────────────────────────────────────────────────
app.UseMiddleware<ErrorHandlingMiddleware>();
app.UseMiddleware<RequestLoggingMiddleware>();

app.MapGet("/", () => "iPOS API is running");
app.UseSwagger();
app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "iPOS API v1"));

app.UseCors("DashboardPolicy");

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();

using iPosApi.Models;
using iPosApi.Models.Data;
using Microsoft.EntityFrameworkCore;

namespace iPosApi.Services;

public class UserService : IUserService
{
    private readonly AppDbContext _db;
    private readonly ILogger<UserService> _logger;

    public UserService(AppDbContext db, ILogger<UserService> logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task<List<User>> GetAllAsync() =>
        await _db.Users.AsNoTracking().ToListAsync();

    public async Task<User?> GetByIdAsync(long id) =>
        await _db.Users.FindAsync(id);

    public async Task<User?> GetByEmailAsync(string email) =>
        await _db.Users.AsNoTracking().FirstOrDefaultAsync(u => u.Email == email);

    public async Task<User> UpsertAsync(User user)
    {
        var existing = await _db.Users.FindAsync(user.Id);
        if (existing == null)
        {
            _db.Users.Add(user);
            _logger.LogInformation("Created user {Id} ({Email})", user.Id, user.Email);
        }
        else
        {
            existing.Name = user.Name; existing.Contact = user.Contact;
            existing.UserRole = user.UserRole; existing.Email = user.Email;
            existing.StoreId = user.StoreId; existing.Address = user.Address;
            _logger.LogInformation("Updated user {Id} ({Email})", user.Id, user.Email);
        }
        await _db.SaveChangesAsync();
        return existing ?? user;
    }
}

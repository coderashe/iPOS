using iPosApi.Models;

namespace iPosApi.Services;

public interface IUserService
{
    Task<List<User>> GetAllAsync();
    Task<User?> GetByIdAsync(long id);
    Task<User?> GetByEmailAsync(string email);
    Task<User> UpsertAsync(User user);
}

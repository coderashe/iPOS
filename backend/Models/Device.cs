namespace iPosApi.Models;

public class Device
{
    public string DeviceId { get; set; } = string.Empty;
    public string DeviceName { get; set; } = string.Empty;
    public string StoreId { get; set; } = string.Empty;
    public long LastSeen { get; set; }
}

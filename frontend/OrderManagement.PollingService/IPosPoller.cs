using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OrderManagement.PollingService
{
    public class IPosPoller
    {
        private readonly Timer _timer;
        private readonly HttpClient _httpClient;
        private readonly string _apiBaseUrl;
        private readonly ILogger<IPosPoller> _logger;

        public IPosPoller(string apiBaseUrl, ILogger<IPosPoller> logger)
        {
            _apiBaseUrl = apiBaseUrl;
            _logger = logger;
            _httpClient = new HttpClient();
            _timer = new Timer(PollCallback, null, Timeout.Infinite, Timeout.Infinite);
        }

        public void Start()
        {
            // Poll immediately, then every 5 minutes
            _logger.LogInformation("📦 Products Poller starting...");
            _timer.Change(TimeSpan.Zero, TimeSpan.FromMinutes(5));
        }

        public void Stop()
        {
            _logger.LogInformation("📦 Products Poller stopping...");
            _timer.Change(Timeout.Infinite, Timeout.Infinite);
        }

        private async void PollCallback(object state)
        {
            try
            {
                _logger.LogInformation($"[{DateTime.Now:HH:mm:ss}] 🔄 Polling products from API...");

                // GET request to fetch all products
                var response = await _httpClient.GetAsync($"{_apiBaseUrl}/api/products");

                _logger.LogDebug($"API Response Status: {response.StatusCode}");

                if (response.IsSuccessStatusCode)
                {
                    var result = await response.Content.ReadAsStringAsync();
                    _logger.LogInformation($"[{DateTime.Now:HH:mm:ss}] ✅ Poll successful - Products retrieved");

                    // Parse the JSON response
                    var products = Newtonsoft.Json.JsonConvert.DeserializeObject<List<Product>>(result);

                    if (products != null && products.Any())
                    {
                        _logger.LogInformation($"📊 Found {products.Count} products");

                        // Log first few products as sample
                        foreach (var product in products.Take(3))
                        {
                            _logger.LogDebug($"  Product: {product.Name} - Price: {product.Price}");
                        }

                        // Here you can process the products data
                        // For example: save to database, send to another system, etc.
                        ProcessProducts(products);
                    }
                    else
                    {
                        _logger.LogWarning($"⚠️ No products found in the response");
                    }
                }
                else
                {
                    var errorContent = await response.Content.ReadAsStringAsync();
                    _logger.LogError($"[{DateTime.Now:HH:mm:ss}] ❌ Poll failed: {response.StatusCode}");
                    _logger.LogError($"Error details: {errorContent}");
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"[{DateTime.Now:HH:mm:ss}] 💥 Poll error");
            }
        }

        private void ProcessProducts(List<Product> products)
        {
            try
            {
                _logger.LogDebug($"Processing {products.Count} products...");

                // Example: Calculate total value
                decimal totalValue = products.Sum(p => p.Price);
                _logger.LogInformation($"💰 Total products value: {totalValue:C}");

                // Example: Group by category if you have that property
                // var byCategory = products.GroupBy(p => p.Category);
                // foreach (var group in byCategory)
                // {
                //     _logger.LogInformation($"  Category {group.Key}: {group.Count()} products");
                // }

                // TODO: Add your business logic here
                // - Save to local database
                // - Send to another API
                // - Generate reports
                // - Check for stock levels
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing products");
            }
        }
    }

    // Add this Product class to match your API response structure
    public class Product
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public decimal Price { get; set; }
        // Add other properties that match your Product model
    }
}

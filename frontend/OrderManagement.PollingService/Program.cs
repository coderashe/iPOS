using Microsoft.Extensions.Logging;
using System;
using System.Threading;

namespace OrderManagement.PollingService

{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("H & P Products Polling Service");
            Console.WriteLine("==================================");

            // Configuration
            string apiBaseUrl = "https://localhost:44363"; // Your API base URL 


            // Create logger using LoggerFactory
            using var loggerFactory = LoggerFactory.Create(builder =>
            {
                builder
                    .AddConsole()        // Logs to console
                    .AddDebug()          // Logs to Debug output (Visual Studio)
                    .SetMinimumLevel(LogLevel.Trace); // Set log level
            });

            // Create logger for IPosPoller
            ILogger<IPosPoller> logger = loggerFactory.CreateLogger<IPosPoller>();

            // TEST LOGGER
            logger.LogInformation("Logger test - Information level");
            logger.LogDebug("Logger test - Debug level");
            logger.LogWarning("Logger test - Warning level");
            logger.LogError("Logger test - Error level");


            // Create poller
            var poller = new IPosPoller(apiBaseUrl,logger);

            // Start polling
            poller.Start();

            Console.WriteLine("Service started. Polling every 5 minutes.");
            Console.WriteLine("Press 'Q' to quit.");

            // Keep running until Q is pressed
            while (Console.ReadKey().Key != ConsoleKey.Q)
            {
                Thread.Sleep(100);
            }

            // Stop polling
            poller.Stop();
            Console.WriteLine("\nService stopped.");
        }
    }
}



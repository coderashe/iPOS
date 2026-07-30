using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace iPosApi.Migrations
{
    /// <inheritdoc />
    public partial class AddStoreIdToAllTables : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // Invoice — already has DeviceId/DeviceName, just add StoreId
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "Invoices",
                type: "nvarchar(max)",
                nullable: true);

            // InvoiceLines
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "InvoiceLines",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "InvoiceLines",
                type: "nvarchar(max)",
                nullable: true);

            // Products — already has DeviceId/DeviceName, add StoreId
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "Products",
                type: "nvarchar(max)",
                nullable: true);

            // Payments
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "Payments",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "Payments",
                type: "nvarchar(max)",
                nullable: true);

            // Expenses
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "Expenses",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "Expenses",
                type: "nvarchar(max)",
                nullable: true);

            // CashFlows
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "CashFlows",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "CashFlows",
                type: "nvarchar(max)",
                nullable: true);

            // DayReports
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "DayReports",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "DayReports",
                type: "nvarchar(max)",
                nullable: true);

            // TradeReceivables
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "TradeReceivables",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "TradeReceivables",
                type: "nvarchar(max)",
                nullable: true);

            // DiscountsReceived
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "DiscountsReceived",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "DiscountsReceived",
                type: "nvarchar(max)",
                nullable: true);

            // CartItems
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "CartItems",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "CartItems",
                type: "nvarchar(max)",
                nullable: true);

            // StockTakes
            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "StockTakes",
                type: "nvarchar(max)",
                nullable: true);
            migrationBuilder.AddColumn<string>(
                name: "StoreId",
                table: "StockTakes",
                type: "nvarchar(max)",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(name: "StoreId",  table: "Invoices");
            migrationBuilder.DropColumn(name: "DeviceId", table: "InvoiceLines");
            migrationBuilder.DropColumn(name: "StoreId",  table: "InvoiceLines");
            migrationBuilder.DropColumn(name: "StoreId",  table: "Products");
            migrationBuilder.DropColumn(name: "DeviceId", table: "Payments");
            migrationBuilder.DropColumn(name: "StoreId",  table: "Payments");
            migrationBuilder.DropColumn(name: "DeviceId", table: "Expenses");
            migrationBuilder.DropColumn(name: "StoreId",  table: "Expenses");
            migrationBuilder.DropColumn(name: "DeviceId", table: "CashFlows");
            migrationBuilder.DropColumn(name: "StoreId",  table: "CashFlows");
            migrationBuilder.DropColumn(name: "DeviceId", table: "DayReports");
            migrationBuilder.DropColumn(name: "StoreId",  table: "DayReports");
            migrationBuilder.DropColumn(name: "DeviceId", table: "TradeReceivables");
            migrationBuilder.DropColumn(name: "StoreId",  table: "TradeReceivables");
            migrationBuilder.DropColumn(name: "DeviceId", table: "DiscountsReceived");
            migrationBuilder.DropColumn(name: "StoreId",  table: "DiscountsReceived");
            migrationBuilder.DropColumn(name: "DeviceId", table: "CartItems");
            migrationBuilder.DropColumn(name: "StoreId",  table: "CartItems");
            migrationBuilder.DropColumn(name: "DeviceId", table: "StockTakes");
            migrationBuilder.DropColumn(name: "StoreId",  table: "StockTakes");
        }
    }
}

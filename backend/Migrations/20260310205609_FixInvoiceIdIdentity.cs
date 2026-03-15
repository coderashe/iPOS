using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace iPosApi.Migrations
{
    /// <inheritdoc />
    public partial class FixInvoiceIdIdentity : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // No-op: InvoiceId IDENTITY was already removed via raw SQL in FixInvoicesTable.sql.
            // This migration is kept for history tracking only.
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<long>(
                name: "InvoiceId",
                table: "Invoices",
                type: "bigint",
                nullable: false,
                oldClrType: typeof(long),
                oldType: "bigint")
                .Annotation("SqlServer:Identity", "1, 1");
        }
    }
}

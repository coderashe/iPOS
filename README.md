# iPOS App (Mobile Point of Sale)

iPOS is an Android mobile application designed for small and medium-sized businesses to manage sales, inventory, and financial data efficiently. It offers key features such as managing products, loading stock, generating invoices, and producing sales and VAT reports, all wrapped in an intuitive user interface.

## Project Overview

### Key Features

- **Product Management**: Add, edit, and track products, including their stock levels, costs, and sales tax details.
- **Stock Loading**: Admin functionality to load stock into the system with validation for accurate data entry.
- **Invoicing**: Create invoices for customer purchases, calculate totals, and store transaction history.
- **Reports**: Generate detailed sales and VAT reports with insights into gross profit and tax amounts.
- **CSV Import/Export**: Upload product data in bulk via CSV files and ensure validity through automated checks.
- **Backup**: Firebase integration for secure data backup.

### Modules

1. **Customer Selection Screen**: Allows the user to choose a customer for the invoice.
2. **Product Selection Screen**: Users can select products to add to the cart, with quantities adjustable on the Cart Screen.
3. **Cart Screen**: Displays selected products with total price calculation and checkout functionality.
4. **Reports Screen**: Shows a list of invoices in card form, each clickable for detailed invoice information.
5. **Order Screen**: Salesmen can record customer orders with product details, quantities, and due dates.

## Getting Started

### Prerequisites

- **Android Studio**: Version 4.1 or later
- **Firebase Account**: For database backup and authentication
- **CSV Files**: For uploading product data in bulk

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/simbastart001/iPOS-App.git
Open the project in Android Studio.
Configure Firebase:
Create a Firebase project.
Add your Android app package to the Firebase console.
Download the google-services.json file and place it in the app directory.
Run the app on your preferred Android device or emulator.
Technologies Used
Kotlin: Primary programming language for Android development.
Jetpack Compose: For building UI with a declarative approach.
Room Database: Local storage of product, invoice, and financial data.
Firebase: For authentication and data backup.
CSV Parsing: For importing bulk product data via CSV files.
Hilt: Dependency injection for efficient app architecture.
License
This project is licensed under the MIT License.

### Screenshots

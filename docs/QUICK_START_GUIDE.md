# iPOS Analytics Dashboard - Quick Start Guide

## 🚀 Get Started in 5 Minutes

This guide will help you get the iPOS Analytics Dashboard up and running quickly.

## ✅ Prerequisites Check

Before starting, ensure you have:
- [ ] Node.js 18+ installed (`node --version`)
- [ ] .NET 9 SDK installed (`dotnet --version`)
- [ ] SQL Server LocalDB or Express
- [ ] Git installed

## 📥 Step 1: Clone and Checkout

```bash
# Clone the repository (if not already cloned)
git clone https://github.com/simbastart001/iPOS.git
cd iPOS

# Checkout the analytics dashboard branch
git checkout feature/analytics-dashboard
```

## 🗄️ Step 2: Setup Backend API

```bash
# Navigate to backend directory
cd backend

# Restore .NET dependencies
dotnet restore

# Run database migrations
dotnet ef database update

# Start the API server
dotnet run
```

**Expected Output:**
```
Now listening on: http://localhost:5000
Application started. Press Ctrl+C to shut down.
```

✅ Backend is ready at: http://localhost:5000
📖 API docs available at: http://localhost:5000/swagger

## 🎨 Step 3: Setup Frontend Dashboard

Open a **new terminal** window:

```bash
# Navigate to frontend directory
cd frontend/ipos-dashboard

# Install dependencies
npm install --legacy-peer-deps

# Start the development server
npm start
```

**Expected Output:**
```
Application bundle generation complete.
Watch mode enabled. Watching for file changes...
➜  Local:   http://localhost:4200/
```

✅ Dashboard is ready at: http://localhost:4200

## 🌐 Step 4: Access the Dashboard

Open your browser and navigate to:
```
http://localhost:4200
```

You should see:
- Dashboard overview with summary cards
- Navigation menu (Dashboard, Sales, Inventory, Customers, Financial)
- Date range picker at the top
- Top products table

## 🎯 Step 5: Test the Features

### Test Dashboard
1. Open http://localhost:4200
2. Verify summary cards display data
3. Check top products table

### Test Date Filtering
1. Click the date range picker
2. Select "Last 7 days"
3. Verify data updates

### Test Navigation
1. Click "Sales" in the navigation menu
2. View sales analytics and cashier performance
3. Navigate to other sections (Inventory, Customers, Financial)

### Test API
1. Open http://localhost:5000/swagger
2. Try the `/api/dashboard/summary` endpoint
3. Verify it returns data

## 🛠️ Troubleshooting

### Backend Issues

**Problem**: Database connection error
```bash
# Solution: Update connection string in appsettings.json
# Or use SQL Server Express instead of LocalDB
```

**Problem**: Port 5000 already in use
```bash
# Solution: Change port in Program.cs or stop other process
netstat -ano | findstr :5000
taskkill /PID <process_id> /F
```

### Frontend Issues

**Problem**: npm install fails
```bash
# Solution: Use legacy peer deps flag
npm install --legacy-peer-deps

# Or clear cache and retry
npm cache clean --force
npm install --legacy-peer-deps
```

**Problem**: Port 4200 already in use
```bash
# Solution: Use different port
ng serve --port 4300
```

**Problem**: API connection error
```bash
# Solution: Verify backend is running at http://localhost:5000
# Check environment.ts has correct API URL
```

## 📱 Responsive Testing

Test the dashboard on different screen sizes:

### Desktop (1920x1080)
- Summary cards in 4-column grid
- Charts side by side
- Full navigation sidebar

### Tablet (768x1024)
- Summary cards in 2-column grid
- Charts stacked
- Collapsible navigation

### Mobile (375x667)
- Summary cards in 1 column
- Charts stacked vertically
- Hamburger menu

## 🔄 Development Workflow

### Making Changes

**Backend Changes:**
```bash
cd backend
# Make your changes
# API auto-reloads with dotnet watch run
```

**Frontend Changes:**
```bash
cd frontend/ipos-dashboard
# Make your changes
# Browser auto-reloads (watch mode enabled)
```

### Viewing Changes
- Backend: Check http://localhost:5000/swagger
- Frontend: Check http://localhost:4200 (auto-refreshes)

## 📊 Sample Data

The backend includes a data seeder that creates sample data:
- Products
- Customers
- Invoice lines (sales transactions)
- Orders

This allows you to test the dashboard immediately without real data.

## 🎨 Customization

### Change API URL
Edit `frontend/ipos-dashboard/src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://your-api-url/api'  // Change this
};
```

### Change Theme Colors
Edit `frontend/ipos-dashboard/src/styles.scss`:
```scss
:root {
  --primary-color: #3f51b5;  // Change primary color
  --accent-color: #ff4081;   // Change accent color
}
```

### Change Date Range Defaults
Edit `frontend/ipos-dashboard/src/app/core/services/dashboard-state.service.ts`:
```typescript
private dateRangeSubject = new BehaviorSubject<DateRange>({
  startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), // Last 7 days
  endDate: new Date()
});
```

## 📞 Getting Help

### Check Logs

**Backend Logs:**
- Console output where `dotnet run` is running
- Check for database connection errors
- Check for query errors

**Frontend Logs:**
- Browser console (F12 → Console tab)
- Check for API connection errors
- Check for component errors

### Common Commands

```bash
# Check if backend is running
curl http://localhost:5000/api/dashboard/summary?startDate=2026-03-01&endDate=2026-03-10

# Check Angular version
ng version

# Check .NET version
dotnet --version

# Rebuild frontend
cd frontend/ipos-dashboard
rm -rf node_modules package-lock.json
npm install --legacy-peer-deps
npm start
```

## ✨ What's Next?

After getting the dashboard running:

1. **Explore the Features**
   - Navigate through all 5 sections
   - Test date range filtering
   - Check responsive design

2. **Connect Real Data**
   - Ensure your SQL Server database has real data
   - Verify data sync from Android POS devices

3. **Customize**
   - Adjust colors and branding
   - Modify date range defaults
   - Add company logo

4. **Deploy**
   - Choose deployment strategy (IIS, Azure, Docker)
   - Configure production environment
   - Set up SSL/HTTPS

5. **Enhance**
   - Add authentication
   - Implement export functionality
   - Add more chart types
   - Create custom reports

## 🎉 Success!

If you can see the dashboard at http://localhost:4200 with data displayed, you're all set! The iPOS Analytics Dashboard is now running and ready to provide insights into your retail operations.

---

**Need Help?** Check the full documentation in `docs/ANALYTICS_DASHBOARD_IMPLEMENTATION.md`

# Frontend Setup Guide - iPOS Analytics Dashboard

## Current Status

✅ Backend API running on http://localhost:5000
✅ Angular project created at `frontend/ipos-dashboard`
✅ Core services and models created
✅ Basic dashboard component created
⚠️ npm dependencies installation in progress

## Manual Setup Steps

Due to npm cache issues, you may need to complete the setup manually:

### 1. Clean Install Dependencies

```bash
cd frontend/ipos-dashboard
rm -rf node_modules package-lock.json
npm cache clean --force
npm install --legacy-peer-deps
```

### 2. Install Additional Dependencies

```bash
npm install chart.js ng2-charts --legacy-peer-deps
npm install @angular/material --legacy-peer-deps
npm install date-fns html2canvas jspdf xlsx --legacy-peer-deps
```

### 3. Start Development Server

```bash
npm start
```

The app will be available at http://localhost:4200

## What's Already Created

### Core Services
- `src/app/core/services/api.service.ts` - Complete API integration
- `src/app/core/services/dashboard-state.service.ts` - State management
- `src/app/core/models/api-models.ts` - TypeScript interfaces

### Components
- `src/app/features/dashboard/dashboard.component.ts` - Main dashboard
- `src/app/shared/components/summary-card/` - Reusable KPI card

### Configuration
- `src/environments/environment.ts` - Dev config (API: http://localhost:5000/api)
- `src/environments/environment.prod.ts` - Prod config
- `src/app/app.routes.ts` - Routing configured
- `src/app/app.config.ts` - HttpClient configured

## Testing the Dashboard

Once the dev server starts:

1. Open http://localhost:4200
2. You should see the dashboard with 6 KPI cards:
   - Total Revenue
   - Total Sales
   - Transactions
   - Avg Transaction
   - Active Customers
   - Low Stock Items
3. Below that, a table showing top 5 products

## Troubleshooting

### If npm install fails:
- Try deleting `node_modules` and `package-lock.json`
- Run `npm cache clean --force`
- Use `--legacy-peer-deps` flag

### If dev server won't start:
- Check if port 4200 is available
- Verify Angular CLI is installed: `npm install -g @angular/cli`

### If API calls fail:
- Verify backend is running on port 5000
- Check browser console for CORS errors
- Verify environment.ts has correct API URL

## Next Steps

After the dashboard loads successfully:

1. Add charting components (Chart.js integration)
2. Create sales analytics views
3. Build inventory reports
4. Implement customer insights
5. Add financial reports
6. Implement export functionality
7. Add responsive navigation

All the groundwork is in place - the API service has all methods ready, models are defined, and the basic structure is set up.

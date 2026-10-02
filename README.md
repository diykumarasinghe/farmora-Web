# 🌿 FARMORA — Agricultural Marketplace Platform

> A full-stack digital marketplace connecting **Farmers**, **Buyers**, **Delivery Partners**, and **Administrators** in a transparent, bid-driven agricultural supply chain.

---

## 📦 Technology Stack

| Layer      | Technology                                              |
|------------|---------------------------------------------------------|
| Frontend   | Vue 3 + Vite + Pinia + Vue Router + Axios + Bootstrap 5 |
| Backend    | Java 21 + Spring Boot 3.3 + Maven + Spring Security (JWT) |
| Database   | MySQL 8.x                                               |

---

## 🗂️ Project Structure

```
farmora/
├── backend/          ← Spring Boot backend (Maven project)
│   └── src/
│       └── main/java/com/farmora/
│           ├── controller/    ← REST API endpoints
│           ├── service/       ← Business logic
│           ├── repository/    ← Spring Data JPA
│           ├── model/         ← JPA entity classes
│           ├── dto/           ← Request/Response DTOs
│           ├── security/      ← JWT auth, Spring Security config
│           └── exception/     ← Global exception handler
│
├── frontend/         ← Vue 3 + Vite frontend
│   └── src/
│       ├── views/             ← Page views (public, farmer, buyer, delivery, admin)
│       ├── components/        ← Reusable UI components
│       ├── stores/            ← Pinia state stores
│       ├── services/          ← Axios API service layer
│       └── router/            ← Vue Router with navigation guards
│
└── database/
    └── create_database.sql    ← Database schema (run in MySQL Workbench)
```

---

## ⚡ Quick Setup Guide

### Step 1: Database Setup (MySQL Workbench)

1. Open **MySQL Workbench** and connect to your local MySQL server.
2. Open the file `database/create_database.sql`.
3. Execute the entire script (`Ctrl + Shift + Enter`).
4. Verify that the `farmora_db` database and all tables are created successfully.

---

### Step 2: Backend Setup (Spring Boot)

**2a. Create the `.env` file**

```bash
cd backend
copy .env.example .env
```

Edit `backend/.env` and fill in your credentials:

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB=farmora_db
MYSQL_USER=your_mysql_username
MYSQL_PASSWORD=your_mysql_password
JWT_SECRET=replace_with_a_strong_random_secret_at_least_32_chars
JWT_EXPIRATION_MS=86400000
```

**2b. Start the backend**

```bash
cd backend
mvn spring-boot:run
```

The backend API will start at: **`http://localhost:8080`**

> ✅ Check: `http://localhost:8080/api/auth/me` should return `401 Unauthorized` (not a network error).

---

### Step 3: Frontend Setup (Vue 3)

**3a. Install dependencies**

```bash
cd frontend
npm install
```

**3b. Create the `.env` file**

```bash
copy .env.example .env
```

The default `.env` content for local development:

```env
VITE_API_BASE_URL=http://localhost:8080
```

**3c. Start the frontend dev server**

```bash
npm run dev
```

The app will be available at: **`http://localhost:5173`**

---

## 👥 User Roles & Access

| Role              | Registration | Dashboard Route         | Capabilities                                 |
|-------------------|--------------|--------------------------|----------------------------------------------|
| **FARMER**        | ✅ Public    | `/farmer/dashboard`      | List products, manage bids, view orders       |
| **BUYER**         | ✅ Public    | `/buyer/dashboard`       | Browse products, place bids, track orders     |
| **DELIVERY_PARTNER** | ✅ Public | `/delivery/dashboard`    | Accept deliveries, update delivery status     |
| **ADMIN**         | ❌ Manual only | `/admin/dashboard`     | Manage all users, products, orders, reports   |

> **Admin Account**: Cannot be created via the public registration form. Must be manually inserted into the database:
> ```sql
> INSERT INTO users (full_name, email, password, role, active, created_at)
> VALUES (
>   'Admin Name',
>   'admin@farmora.com',
>   '$2a$10$BCRYPT_HASH_OF_YOUR_PASSWORD',  -- use BCrypt encoder
>   'ADMIN',
>   TRUE,
>   NOW()
> );
> ```
> Or use a tool like [bcrypt-generator.com](https://bcrypt-generator.com) to hash a password first.

---

## 🔐 API Authentication

All protected API endpoints require a **Bearer JWT token** in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

The token is returned from:
- `POST /api/auth/register` — New user registration
- `POST /api/auth/login` — User login

---

## 🛒 Core Business Flow

```
1. FARMER registers → lists a Produce Product with quantity & price
2. BUYER browses → places a Bid on the product
3. FARMER reviews bids → accepts the best offer
4. When bid is accepted → an Order is auto-created
5. DELIVERY PARTNER sees the order → claims the delivery job
6. DELIVERY PARTNER picks up → updates status in real-time
7. BUYER receives produce → order marked DELIVERED
8. Either party can leave a Review
```

---

## 🌐 Key API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/api/auth/register` | Public | Register as Farmer/Buyer/Delivery |
| `POST` | `/api/auth/login` | Public | Login and receive JWT token |
| `GET`  | `/api/auth/me` | Auth | Get current user profile |
| `PUT`  | `/api/auth/profile` | Auth | Update profile details |
| `PUT`  | `/api/auth/change-password` | Auth | Change password |

### Products
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/products` | Public |
| `GET` | `/api/products/{id}` | Public |
| `POST` | `/api/products` | FARMER |
| `PUT` | `/api/products/{id}` | FARMER (owner) |
| `DELETE` | `/api/products/{id}` | FARMER (owner) |

### Bids
| Method | Endpoint | Access |
|--------|----------|--------|
| `POST` | `/api/bids` | BUYER |
| `GET` | `/api/bids/my` | BUYER |
| `GET` | `/api/bids/product/{productId}` | FARMER |
| `PUT` | `/api/bids/{id}/accept` | FARMER |
| `PUT` | `/api/bids/{id}/reject` | FARMER |

### Orders
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/orders/my` | FARMER / BUYER |
| `PUT` | `/api/orders/{id}/confirm` | FARMER |
| `PUT` | `/api/orders/{id}/cancel` | FARMER / BUYER |

### Deliveries
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/deliveries/available` | DELIVERY_PARTNER |
| `POST` | `/api/deliveries/{id}/accept` | DELIVERY_PARTNER |
| `PUT` | `/api/deliveries/{id}/status` | DELIVERY_PARTNER |

### Admin
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/admin/users` | ADMIN |
| `PUT` | `/api/admin/users/{id}/status` | ADMIN |
| `GET` | `/api/admin/stats` | ADMIN |

---

## 🏗️ Build for Production

**Backend:**
```bash
cd backend
mvn clean package -DskipTests
java -jar target/farmora-*.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Output goes to frontend/dist/
```

---

## ❓ Troubleshooting

| Problem | Solution |
|---------|----------|
| `Access denied` on MySQL | Check `MYSQL_USER` and `MYSQL_PASSWORD` in `backend/.env` |
| `JWT secret` error | Ensure `JWT_SECRET` is at least 32 characters |
| `CORS error` in browser | Ensure backend is running on port `8080` |
| `404` on API calls | Check `VITE_API_BASE_URL` in `frontend/.env` |
| Frontend won't compile | Run `npm install` in the `frontend/` directory first |
| Table doesn't exist | Re-run `database/create_database.sql` in MySQL Workbench |

---

## 🤝 Contributing

Built with ❤️ for Sri Lankan agriculture. Farmora bridges the gap between the fields and the markets.

---

*Farmora v1.0 — Full-Stack Agricultural Marketplace*

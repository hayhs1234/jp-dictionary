# JP Dictionary Frontend

Cấu trúc frontend chuẩn hóa cho dự án **JP Dictionary (Từ điển Tiếng Nhật Thông Minh)**, được xây dựng với hiệu năng cao, kiến trúc module rõ ràng và thiết kế thẩm mỹ cao (Aesthetic Glassmorphism & Japanese Lacquer Red/Sakura theme).

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

1. **Nền tảng & Framework cốt lõi**:
   - `react` (^19.2.8) & `react-dom` (^19.2.8)
   - `vite` (^8.3.0) + `@vitejs/plugin-react` (^6.1.1)
   - JavaScript (ES Modules, JSX)
2. **Định tuyến (Routing)**:
   - `react-router-dom` (^7.18.4) với mô hình Layout & Protected Route
3. **Giao diện & Styling (UI / CSS)**:
   - Tailwind CSS Play CDN (kèm plugins `@tailwindcss/forms`, `@tailwindcss/container-queries`)
   - CSS Variables / Custom Tokens Design System (`src/styles/tokens.css`, `src/styles/index.css`)
   - `clsx` (^2.1.1) & `tailwind-merge` (^3.7.0) kết hợp trong `src/utils/cn.js`
   - Google Fonts (`Inter`, `Plus Jakarta Sans`, `Noto Sans JP`)
4. **Hiệu ứng & Đồ họa (Animations & WebGL)**:
   - `framer-motion` (^14.0.0) - Micro-interactions, Modal & Page animations
   - `ogl` (^1.0.11) - WebGL canvas background hiệu ứng cánh hoa anh đào & hạt lơ lửng tương tác chuột
5. **Bộ Icon**:
   - `lucide-react` (^1.47.0)
6. **Quản lý trạng thái & Gọi API**:
   - `axios` (^1.20.0) - Interceptors tự động gắn JWT `Bearer` token & bóc tách `ApiResponse<T>` từ backend
   - React Context API: `AuthContext`, `ThemeContext`, `I18nContext`
7. **Xác thực (Authentication)**:
   - `@react-oauth/google` (^0.13.5) - Google OAuth 2.0 Login
8. **Đa ngôn ngữ (i18n)**:
   - Custom i18n System (`src/i18n`) hỗ trợ Tiếng Việt (`vi`) & Tiếng Anh (`en`), tra cứu key phân cấp (`a.b.c`) và thế biến (`{count}`)
9. **Kiểm tra & Kiểm thử (Tooling & Testing)**:
   - `oxlint` (^1.81.0) - Rust-based linter siêu tốc
   - Node.js Native Test Runner (`node --test`)

---

## 📁 Cấu Trúc Thư Mục Chuẩn

```
frontend/
├── .env.example              # Mẫu biến môi trường
├── .env                      # Cấu hình biến môi trường cục bộ
├── .gitignore                # Danh sách file bỏ qua khi commit Git
├── index.html                # HTML entry, tải Tailwind CDN, Plugins, Google Fonts
├── package.json              # Khai báo dependency và scripts
├── vite.config.js            # Cấu hình Vite, alias '@', proxy sang Spring Boot :8080/api
└── src/
    ├── assets/               # Hình ảnh, logo, icons tĩnh
    ├── components/
    │   ├── auth/             # Component xác thực (AuthModal, GoogleLogin)
    │   ├── canvas/           # WebGL OGL Particles Background
    │   ├── common/           # UI Reusable (Button, Input, Badge, Modal)
    │   ├── dictionary/       # UI Từ điển (SearchBar, WordCard, KanjiCard)
    │   └── layout/           # Layout cố định (Header, Footer, MainLayout)
    ├── contexts/             # React Contexts (AuthContext, ThemeContext, I18nContext)
    ├── i18n/                 # Hệ thống đa ngôn ngữ tự xây dựng
    │   ├── index.js          # Core engine dịch thuật & fallback
    │   ├── i18n.test.js      # Unit test cho i18n engine
    │   └── locales/
    │       ├── en.js         # Bản dịch Tiếng Anh
    │       └── vi.js         # Bản dịch Tiếng Việt
    ├── pages/                # Các trang chính
    │   ├── HomePage.jsx      # Trang chủ với tra cứu nhanh & thẻ nổi bật
    │   ├── SearchPage.jsx    # Trang kết quả tìm kiếm đa năng
    │   ├── KanjiPage.jsx     # Trang tra cứu Hán tự theo cấp độ JLPT
    │   ├── ProfilePage.jsx   # Trang tài khoản cá nhân & từ đã lưu
    │   └── NotFoundPage.jsx  # Trang 404 phong cách Nhật Bản
    ├── router/
    │   ├── AppRouter.jsx     # Định tuyến ứng dụng React Router v7
    │   └── ProtectedRoute.jsx# Bảo vệ route yêu cầu đăng nhập
    ├── services/
    │   ├── api/
    │   │   └── axiosClient.js# Axios instance + JWT Interceptor + Auto ApiResponse unwrapper
    │   ├── authService.js    # API Đăng nhập, Google OAuth, Đăng xuất
    │   └── dictionaryService.js # API Tra từ vựng & Hán tự
    ├── styles/
    │   ├── tokens.css        # Biến màu Design System (Light & Dark theme)
    │   └── index.css         # Glassmorphism utilities & global reset
    ├── utils/
    │   ├── cn.js             # Kết hợp clsx + tailwind-merge
    │   ├── cn.test.js        # Unit test cho cn utility
    │   ├── storage.js        # Safe LocalStorage wrapper
    │   └── token.js          # Quản lý JWT token & user profile
    ├── App.jsx               # Root Provider Wrapper
    └── main.jsx              # React 19 Entry Point
```

---

## 🚀 Hướng Dẫn Chạy Dự Án

### 1. Cài đặt thư viện
```bash
cd frontend
npm install
```

### 2. Chạy môi trường phát triển (Dev Server)
```bash
npm run dev
```
Truy cập tại: `http://localhost:5173`

> Lưu ý: Vite đã được cấu hình proxy `/api` tự động chuyển tiếp đến backend Spring Boot tại `http://localhost:8080/api`.

### 3. Kiểm tra mã nguồn (Oxlint)
```bash
npm run lint
```

### 4. Chạy Unit Tests (Node.js Native Test Runner)
```bash
npm test
```

### 5. Đóng gói sản phẩm (Production Build)
```bash
npm run build
```

# JP Dictionary (Từ Điển Tiếng Nhật Thông Minh)

Dự án từ điển và nền tảng học tiếng Nhật toàn diện kết hợp Spring Boot backend và React 19 frontend hiện đại.

## Cấu Trúc Dự Án
- **`backend/`**: Spring Boot 3 API Server (Java 21, Spring Security, JWT, JPA, PostgreSQL/H2, OpenAPI Swagger).
- **`frontend/`**: React 19 SPA (Vite 8, Tailwind CSS CDN + Tokens, Framer Motion, OGL WebGL, Lucide, Axios, Custom i18n, Oxlint, Node Native Test).

## 🚀 CI/CD Pipeline & Quality Gate
Dự án được tích hợp sẵn hệ thống **Continuous Integration (CI)** chuẩn hóa trên GitHub Actions:
- 🐳 **Hadolint**: Rà soát cú pháp và bảo mật `Dockerfile` (FE & BE).
- 🌐 **Frontend Quality**: Oxlint (0 warnings/errors), npm audit, Unit Tests, Vite build.
- ⚙️ **Backend Quality**: Java Checkstyle (Line length 120, naming, complexity <= 10), Spring Boot tests, Jar package.
- 🛡️ **Security Scan**: Trivy SAST quét lỗ hổng CVE repository & dependencies.
- 📦 **Container Verification**: Build container multi-stage thật và scan lỗ hổng bằng Trivy.
- 🚫 **Strict Policy**: Tuyệt đối không dùng `continue-on-error: true`. Lỗi là fail ngay lập tức!

👉 **Xem hướng dẫn chi tiết**: [Tài liệu Hướng dẫn CI/CD Pipeline](CI_CD_GUIDE.md)

### Kiểm thử nhanh trên máy cục bộ (Windows):
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-ci.ps1
```

---

## 📚 Tài Liệu Tham Khảo
- [Tài liệu Hướng dẫn CI/CD Pipeline](CI_CD_GUIDE.md)
- [Tài liệu Frontend](frontend/README.md)
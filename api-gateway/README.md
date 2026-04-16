# API Gateway

## Mô tả

`api-gateway` là cổng vào của hệ thống, chịu trách nhiệm kiểm tra JWT trước khi cho phép request đi tiếp vào các service phía sau.

Ở trạng thái hiện tại, module này dùng Spring Cloud Gateway MVC kết hợp Spring Security và filter tùy chỉnh để xác thực bearer token theo cơ chế stateless.

README này chỉ mô tả hành vi đang có trong source code hiện tại của `api-gateway`.

## Chức năng hiện tại

- Đóng vai trò API Gateway cho hệ thống.
- Validate JWT từ header `Authorization: Bearer <token>`.
- Từ chối request nếu thiếu token, token sai hoặc token hết hạn.
- Trích xuất `subject` và `role` từ JWT để đưa vào `SecurityContext`.
- Cho phép một số endpoint auth đi qua mà không cần bearer token.
- Dùng Spring Cloud Config Client để nhận cấu hình runtime.
- Có dependency để đăng ký service với Eureka.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Security
- Spring Cloud Gateway Server Web MVC
- Spring Cloud Config Client
- Eureka Client
- JJWT
- Lombok
- Maven

## Luồng xử lý JWT hiện tại

1. Client gửi request đến gateway.
2. `JwtAuthenticationFilter` kiểm tra path hiện tại có nằm trong danh sách bỏ qua hay không.
3. Nếu path không bị bỏ qua, filter đọc header `Authorization`.
4. Nếu header thiếu hoặc không bắt đầu bằng `Bearer `, gateway trả về `401`.
5. `JwtService` kiểm tra chữ ký và tính hợp lệ của token.
6. Nếu token không hợp lệ hoặc đã hết hạn, gateway trả về `401`.
7. Nếu token hợp lệ, gateway lấy `subject` và `role` từ claims.
8. Gateway tạo `Authentication` và set vào `SecurityContextHolder`.
9. Request tiếp tục đi qua security chain để được route tới downstream service.

Ví dụ response khi thiếu token:

```json
{
  "message": "Missing token"
}
```

Ví dụ response khi token không hợp lệ:

```json
{
  "message": "Invalid or expired token"
}
```

## Cấu trúc source chính

- `src/main/java/com/vieguys/apigateway/config/SecurityConfig.java`
  - Cấu hình Spring Security cho gateway.
  - Tắt CSRF.
  - Cấu hình session stateless.
  - Cho phép public các path auth đã khai báo.
  - Chèn `JwtAuthenticationFilter` trước `UsernamePasswordAuthenticationFilter`.
- `src/main/java/com/vieguys/apigateway/security/JwtAuthenticationFilter.java`
  - Bỏ qua một số path không cần kiểm tra JWT.
  - Đọc bearer token từ request.
  - Validate token và dựng `Authentication` từ claims.
  - Trả về `401` dạng JSON nếu token không hợp lệ.
- `src/main/java/com/vieguys/apigateway/service/JwtService.java`
  - Parse và verify JWT bằng `jwt.secret`.
  - Trích xuất claims, username và role từ token.
- `src/main/java/com/vieguys/apigateway/ApiGatewayApplication.java`
  - Điểm khởi động của ứng dụng Spring Boot.

## Security hiện tại

`SecurityConfig` đang cấu hình:

- `SessionCreationPolicy.STATELESS`
- `permitAll()` cho:
  - `/api/auth/login`
  - `/api/auth/oauth2/**`
- `authenticated()` cho các request còn lại

`JwtAuthenticationFilter` hiện đang bỏ qua các path:

- `/api/auth/`
- `/login/oauth2/`
- `/oauth2/`
- `/eureka/`

Điều này cho phép các endpoint liên quan đến đăng nhập OAuth2 và service discovery không bị chặn bởi JWT filter.

## Hành vi JWT

Gateway kỳ vọng JWT có các claims chính sau:

- `sub`: email hoặc username của người dùng
- `role`: vai trò của người dùng

Khi token hợp lệ, gateway ánh xạ `role` thành authority theo dạng:

```text
ROLE_<role>
```

Ví dụ:

- `USER` -> `ROLE_USER`
- `ADMIN` -> `ROLE_ADMIN`

## Cấu hình cần chuẩn bị

Trong source hiện tại, file `application.yaml` mới khai báo:

```yaml
spring:
  application:
    name: api-gateway
  config:
    import: optional:configserver:http://localhost:8888
```

Để chạy service đầy đủ, cần bổ sung cấu hình tương ứng từ local config hoặc Config Server, tối thiểu gồm:

- `jwt.secret`
- cấu hình route cho Spring Cloud Gateway
- `eureka.client.service-url.defaultZone` nếu dùng Eureka

Lưu ý:

- `jwt.secret` phải đồng bộ với secret dùng để ký token ở `auth-service`.
- Route thực tế của gateway không được khai báo trong source hiện tại, nên nhiều khả năng đang được cung cấp từ Config Server.

## Chạy service

### Windows

```powershell
./mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

## Phạm vi trách nhiệm

- `api-gateway` chịu trách nhiệm validate bearer token cho request đi vào hệ thống.
- `api-gateway` là nơi thiết lập security context từ JWT claims.
- `api-gateway` không phát token; token được cấp bởi `auth-service`.
- `api-gateway` không chứa route config cụ thể trong source hiện tại; phần này cần lấy từ cấu hình ngoài.

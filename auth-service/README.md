# Auth Service

## Mô tả

`auth-service` là microservice chịu trách nhiệm đăng nhập người dùng bằng Google OAuth2, lưu thông tin người dùng vào MongoDB và sinh JWT để trả về sau khi đăng nhập thành công.

README này chỉ mô tả hành vi đang có trong source code hiện tại của `auth-service`.

Trong định hướng hiện tại, `auth-service` chỉ phát JWT. Việc validate bearer token cho các request đi vào hệ thống thuộc trách nhiệm của API Gateway.

## Chức năng hiện tại

- Đăng nhập bằng Google OAuth2 qua Spring Security.
- Lưu người dùng vào MongoDB nếu email chưa tồn tại.
- Sinh JWT chứa `subject` là email và `role` của user.
- Cung cấp endpoint kiểm tra thông tin người dùng đã đăng nhập.
- Cung cấp endpoint test nhanh để kiểm tra service đang chạy.
- Không tự validate bearer token cho các request API downstream.
- Có cấu hình để nhận cấu hình từ Spring Cloud Config Server.
- Có dependency để đăng ký service với Eureka.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Security
- Spring OAuth2 Client
- Spring Data MongoDB
- Spring Cloud Config Client
- Eureka Client
- JJWT
- Lombok
- Maven

## Luồng xác thực hiện tại

1. Người dùng thực hiện đăng nhập qua Google OAuth2.
2. Sau khi xác thực thành công, `OAuth2SuccessHandler` lấy thông tin `email` và `name` từ `OAuth2User`.
3. Service kiểm tra user theo email trong MongoDB.
4. Nếu user chưa tồn tại, service tạo mới user với role mặc định là `USER`.
5. `JwtService` sinh JWT từ thông tin user.
6. Service trả về response JSON chứa token.

Token được phát ra để các thành phần khác trong hệ thống sử dụng. Theo định hướng hiện tại, việc kiểm tra bearer token không được xử lý tại `auth-service`.

Ví dụ response:

```json
{
  "token": "<jwt-token>"
}
```

## Cấu trúc source chính

- `src/main/java/com/vieguys/authservice/config/SecurityConfig.java`
  - Cấu hình Spring Security.
  - Tắt CSRF.
  - Yêu cầu xác thực với các request đi vào application theo flow Spring Security hiện tại.
  - Cấu hình OAuth2 login với custom success handler.
  - Nếu không override thêm endpoint, Spring Security sẽ dùng các OAuth2 endpoint mặc định.
- `src/main/java/com/vieguys/authservice/security/OAuth2SuccessHandler.java`
  - Xử lý sau khi OAuth2 login thành công.
  - Lưu user vào MongoDB nếu chưa có.
  - Sinh JWT và trả về JSON.
- `src/main/java/com/vieguys/authservice/service/JwtService.java`
  - Sinh JWT bằng khóa bí mật cấu hình qua `jwt.secret`.
- `src/main/java/com/vieguys/authservice/model/User.java`
  - Document MongoDB cho collection `users`.
- `src/main/java/com/vieguys/authservice/repository/UserRepository.java`
  - Truy vấn user theo email.
- `src/main/java/com/vieguys/authservice/controller/AuthTestController.java`
  - Trả về thông tin người dùng OAuth2 hiện tại.
- `src/main/java/com/vieguys/authservice/controller/TestController.java`
  - Endpoint test trạng thái service.

## API hiện có

Ngoài các controller endpoint bên dưới, Spring Security OAuth2 còn tự xử lý các endpoint mặc định của flow đăng nhập, thường là:

- `GET /oauth2/authorization/{registrationId}` để bắt đầu đăng nhập với provider, ví dụ `/oauth2/authorization/google`
- `GET /login/oauth2/code/{registrationId}` là callback endpoint sau khi provider xác thực xong

Với cấu hình hiện tại, các endpoint ứng dụng đi qua Spring Security và flow đăng nhập dùng các endpoint OAuth2 mặc định do framework cung cấp. README này không xem `auth-service` là nơi validate bearer token cho hệ thống.

### `GET /oauth2/me`

Trả về thông tin của user đang đăng nhập.

Endpoint này yêu cầu user đã hoàn tất đăng nhập OAuth2.

Ví dụ response:

```json
{
  "authenticated": true,
  "name": "Nguyen Van A",
  "authorities": [
    {
      "authority": "..."
    }
  ]
}
```

### `GET /test`

Endpoint test nhanh để kiểm tra service đang hoạt động.

Endpoint này cũng đang nằm sau lớp xác thực của Spring Security.

Ví dụ response:

```json
{
  "service": "auth-service",
  "status": "ok",
  "time": "2026-04-16T10:00:00"
}
```

## Cấu hình cần chuẩn bị

Trong source hiện tại, file `application.yaml` mới khai báo:

```yaml
spring:
  application:
    name: auth-service
  config:
    import: optional:configserver:http://localhost:8888
```

Để chạy service đầy đủ, cần bổ sung cấu hình tương ứng từ local config hoặc Config Server, tối thiểu gồm:

- `jwt.secret`
- `spring.data.mongodb.uri`
- `spring.security.oauth2.client.registration.google.client-id`
- `spring.security.oauth2.client.registration.google.client-secret`
- `spring.security.oauth2.client.registration.google.scope`
- `spring.security.oauth2.client.provider.google.*` nếu cần override
- `eureka.client.service-url.defaultZone` nếu dùng Eureka

Lưu ý:

- `jwt.secret` cần đủ độ dài để dùng với HMAC SHA-256.
- Nếu không có MongoDB hoặc OAuth2 config, luồng đăng nhập sẽ không hoạt động đúng.

## Phạm vi trách nhiệm

- `auth-service` chịu trách nhiệm đăng nhập OAuth2, lưu user và phát JWT.
- `auth-service` không triển khai flow validate bearer token cho các request API downstream.
- Việc validate JWT cho request đi vào hệ thống thuộc về API Gateway theo định hướng kiến trúc hiện tại.

## Chạy service

### Windows

```powershell
./mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

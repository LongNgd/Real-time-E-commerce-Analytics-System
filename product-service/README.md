# Product Service

## Mô tả

`product-service` là microservice chịu trách nhiệm quản lý sản phẩm, ảnh sản phẩm và review sản phẩm.

Service lưu dữ liệu sản phẩm và review trong MongoDB, lưu cache product detail trong Redis, và lưu file ảnh qua FTP.

README này chỉ mô tả hành vi đang có trong source code hiện tại của `product-service`.

Trong định hướng hiện tại, prefix `/api/product` được API Gateway route vào service này, không được khai báo trực tiếp ở `ProductController`.

## Chức năng hiện tại

- Tạo sản phẩm mới kèm danh sách ảnh upload multipart.
- Lấy danh sách sản phẩm có phân trang và sắp xếp.
- Lấy product detail kèm toàn bộ review của sản phẩm.
- Cập nhật thông tin sản phẩm.
- Xóa sản phẩm cùng review và file ảnh liên quan.
- Thêm ảnh cho sản phẩm.
- Xóa một phần ảnh của sản phẩm.
- Download ảnh sản phẩm theo remote path đã lưu.
- Tạo review cho sản phẩm theo user hiện tại từ header gateway.
- Xóa review của chính user hiện tại theo header gateway.
- Cache product detail bằng Redis và xóa cache khi dữ liệu product/review thay đổi.
- Có cấu hình để nhận cấu hình từ Spring Cloud Config Server.
- Có dependency để đăng ký service với Eureka.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Data MongoDB
- Spring Data Redis
- Spring Cloud Config Client
- Eureka Client
- Springdoc OpenAPI
- Apache Commons Net FTP
- Lombok
- Maven

## Luồng dữ liệu hiện tại

### Product detail và cache

1. Client gọi endpoint lấy chi tiết sản phẩm.
2. `ProductServiceImpl#getProductDetail` kiểm tra Redis cache `productDetail` theo `productId`.
3. Nếu cache miss, service đọc product từ MongoDB và đọc review theo `productId`.
4. Service sort review theo `createdAt` giảm dần.
5. Service trả về `ProductDetailResponseDTO` và lưu object này vào Redis với TTL 10 phút.
6. Khi product hoặc review thay đổi, các write method sẽ `@CacheEvict(allEntries = true)` để xóa cache detail cũ.

### Upload ảnh sản phẩm

1. Client gửi request multipart chứa file ảnh.
2. `FtpStorageServiceImpl` kết nối FTP server bằng cấu hình `ftp.*`.
3. Service đảm bảo thư mục đích tồn tại dưới đường dẫn `products`.
4. Mỗi file được sinh tên ngẫu nhiên và upload lên FTP.
5. Remote path được lưu vào `imageUrls` của product trong MongoDB.
6. Nếu save product thất bại sau khi upload, service sẽ cố xóa lại các file vừa upload.

### Review sản phẩm

1. Client gửi review cùng các header user do API Gateway forward xuống.
2. Service chỉ cho phép một email review một sản phẩm một lần.
3. Sau khi tạo hoặc xóa review, service tính lại `averageRating` và `totalReviews` cho product.
4. Endpoint trả về product detail mới nhất sau thay đổi.

## Cấu trúc source chính

- `src/main/java/com/vieguys/productservice/controller/ProductController.java`
  - Expose các endpoint product, image và review.
  - Swagger description được khai báo trực tiếp tại controller.
- `src/main/java/com/vieguys/productservice/service/impl/ProductServiceImpl.java`
  - Chứa logic CRUD product, review, cache invalidation và tính rating trung bình.
- `src/main/java/com/vieguys/productservice/service/impl/FtpStorageServiceImpl.java`
  - Xử lý upload, download và delete file ảnh trên FTP server.
- `src/main/java/com/vieguys/productservice/config/RedisConfig.java`
  - Cấu hình Redis cache cho `ProductDetailResponseDTO` với TTL 10 phút.
- `src/main/java/com/vieguys/productservice/config/FtpProperties.java`
  - Binding cấu hình FTP theo prefix `ftp`.
- `src/main/java/com/vieguys/productservice/domain/model/Product.java`
  - Document MongoDB cho collection `products`.
- `src/main/java/com/vieguys/productservice/domain/model/Review.java`
  - Document MongoDB cho collection `reviews`.
- `src/main/java/com/vieguys/productservice/repository/ProductRepository.java`
  - Truy vấn product theo `id` và kiểm tra trùng `name` không phân biệt hoa thường.
- `src/main/java/com/vieguys/productservice/repository/ReviewRepository.java`
  - Truy vấn review theo `productId` và kiểm tra quyền xóa review theo `id + productId + userEmail`.
- `src/main/java/com/vieguys/productservice/controller/GlobalExceptionHandler.java`
  - Xử lý lỗi upload vượt quá dung lượng cho phép.

## API hiện có

Lưu ý: các path dưới đây là path nghiệp vụ theo cách service đang được sử dụng qua API Gateway. Prefix `/api/product` hiện do API Gateway đảm nhiệm.

### `POST /api/product`

Tạo sản phẩm mới.

Request dùng `multipart/form-data` với các field:

- `name`
- `description` (optional)
- `price`
- `stock`
- `images`

Response trả về `ProductDetailResponseDTO` của sản phẩm vừa tạo.

### `GET /api/product`

Lấy danh sách sản phẩm có phân trang.

Query params:

- `page` mặc định `0`
- `size` mặc định `10`
- `sort` mặc định `createdAt`
- `direction` mặc định `desc`

Response là `Page<ProductResponseDTO>`.

### `GET /api/product/{id}`

Lấy chi tiết sản phẩm kèm review.

Response là `ProductDetailResponseDTO`:

```json
{
  "product": {
    "id": "...",
    "name": "...",
    "description": "...",
    "price": 100.0,
    "stock": 10,
    "imageUrls": [
      "/api/product/images?path=%2Fproducts%2F...jpg"
    ],
    "averageRating": 4.5,
    "totalReviews": 2,
    "createdAt": "2026-05-05T10:00:00",
    "updatedAt": "2026-05-05T10:05:00"
  },
  "reviews": [
    {
      "id": "...",
      "productId": "...",
      "userEmail": "user@example.com",
      "userName": "Nguyen Van A",
      "content": "San pham tot",
      "rating": 5,
      "createdAt": "2026-05-05T10:06:00"
    }
  ]
}
```

### `PUT /api/product/{id}`

Cập nhật thông tin sản phẩm.

Request body là `UpdateProductRequestDTO`:

```json
{
  "name": "New product name",
  "description": "Updated description",
  "price": 199.99,
  "stock": 20
}
```

Response trả về `ProductDetailResponseDTO` mới nhất sau cập nhật.

### `DELETE /api/product/{id}`

Xóa sản phẩm, toàn bộ review của sản phẩm và các file ảnh liên quan.

Response: `204 No Content`.

### `POST /api/product/{id}/images`

Thêm ảnh cho sản phẩm.

Request dùng `multipart/form-data` với field `images`.

Response trả về `ProductDetailResponseDTO` mới nhất.

### `DELETE /api/product/{id}/images`

Xóa một phần ảnh của sản phẩm.

Request body:

```json
{
  "imagePaths": [
    "/products/.../image-1.jpg",
    "/products/.../image-2.jpg"
  ]
}
```

Response trả về `ProductDetailResponseDTO` mới nhất.

Lưu ý: source hiện tại không cho phép xóa hết toàn bộ ảnh của product, sản phẩm phải còn ít nhất một ảnh.

### `GET /api/product/images?path=...`

Download ảnh sản phẩm theo remote path đã lưu trên FTP.

Response là mảng byte với `Content-Type` được suy ra từ tên file, fallback về `application/octet-stream` nếu không xác định được.

### `POST /api/product/{id}/review`

Tạo review cho sản phẩm.

Headers bắt buộc:

- `X-User-Email`
- `X-User-Name`

Request body:

```json
{
  "content": "San pham rat tot",
  "rating": 5
}
```

Response trả về `ProductDetailResponseDTO` mới nhất.

Lưu ý: mỗi email chỉ được review một sản phẩm một lần theo logic hiện tại.

### `DELETE /api/product/{id}/review/{reviewId}`

Xóa review của chính user hiện tại.

Header bắt buộc:

- `X-User-Email`

Response trả về `ProductDetailResponseDTO` mới nhất.

Source hiện tại chỉ cho phép xóa review nếu `reviewId`, `productId` và `userEmail` cùng khớp một record.

## Cấu hình cần chuẩn bị

Trong source hiện tại, file `application.yaml` mới khai báo:

```yaml
spring:
  application:
    name: product-service
  config:
    import: optional:configserver:http://localhost:8888
```

Để chạy service đầy đủ, cần bổ sung cấu hình tương ứng từ local config hoặc Config Server, tối thiểu gồm:

- `spring.data.mongodb.uri`
- `spring.data.redis.host`
- `spring.data.redis.port`
- `ftp.host`
- `ftp.port`
- `ftp.username`
- `ftp.password`
- `ftp.basePath` nếu muốn lưu ảnh dưới base path riêng
- `eureka.client.service-url.defaultZone` nếu dùng Eureka

Lưu ý:

- Nếu không có MongoDB, các API product và review sẽ không hoạt động đúng.
- Nếu không có Redis, phần cache product detail sẽ không hoạt động đúng.
- Nếu không có FTP config, các API tạo product hoặc thao tác ảnh sẽ thất bại.

## Phạm vi trách nhiệm

- `product-service` chịu trách nhiệm quản lý sản phẩm, review và ảnh sản phẩm.
- `product-service` không tự xác thực người dùng bằng bearer token.
- Các thông tin user hiện tại cho review được nhận qua header do API Gateway forward xuống.
- `product-service` hiện chỉ cache `ProductDetailResponseDTO` của endpoint detail.

## Chạy service

### Windows

```powershell
./mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

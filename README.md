# HMS (Hotel Management System)

This project follows a Servlet MVC pattern with the following structured layout.

## Project Structure

```text
HMS-JBS/
│
├── src/                                    # Java source (Servlet MVC pattern)
│   └── com/jbs/hms/
│       │
│       ├── controller/                     # Servlets — page controllers & AJAX/JSON endpoints
│       │   ├── page/                       # Forward sang JSP (full page load)
│       │   │   ├── HomeServlet.java
│       │   │   ├── SearchServlet.java
│       │   │   ├── RoomDetailServlet.java
│       │   │   ├── CartServlet.java
│       │   │   ├── CheckoutServlet.java
│       │   │   ├── BookingConfirmationServlet.java
│       │   │   ├── AuthServlet.java             (login/register/logout)
│       │   │   ├── MyBookingsServlet.java
│       │   │   ├── ReceptionBookingServlet.java
│       │   │   ├── CheckInServlet.java
│       │   │   ├── CheckOutServlet.java
│       │   │   ├── RoomMapServlet.java
│       │   │   ├── HousekeepingServlet.java
│       │   │   ├── EquipmentServlet.java
│       │   │   ├── RoomTypeManagementServlet.java
│       │   │   ├── PricingServlet.java
│       │   │   ├── PolicyServlet.java
│       │   │   ├── ReportServlet.java
│       │   │   └── admin/
│       │   │       ├── UserManagementServlet.java
│       │   │       ├── RoleManagementServlet.java
│       │   │       ├── SystemConfigServlet.java
│       │   │       ├── LogViewerServlet.java
│       │   │       └── BackupServlet.java
│       │   │
│       │   └── api/                        # Trả JSON, gọi bằng AJAX (fetch/jQuery) — cho phần động
│       │       ├── AvailabilityApiServlet.java
│       │       ├── CartHoldApiServlet.java
│       │       ├── BookingApiServlet.java
│       │       ├── PaymentApiServlet.java
│       │       ├── VNPayReturnServlet.java
│       │       ├── VNPayIpnServlet.java
│       │       ├── RoomAssignmentApiServlet.java
│       │       ├── HousekeepingStatusApiServlet.java
│       │       └── EquipmentStatusApiServlet.java
│       │
│       ├── service/                        # Business logic
│       │   ├── BookingService.java
│       │   ├── AvailabilityService.java
│       │   ├── CartHoldService.java
│       │   ├── CheckInService.java
│       │   ├── CheckOutService.java
│       │   ├── PaymentService.java
│       │   ├── VNPayService.java
│       │   ├── HousekeepingService.java
│       │   ├── EquipmentService.java
│       │   ├── PricingService.java
│       │   ├── ReportService.java
│       │   ├── UserService.java
│       │   └── AuditLogService.java
│       │
│       ├── dao/                            # JDBC Data Access Object
│       │   ├── BaseDao.java
│       │   ├── RoomTypeDao.java
│       │   ├── RoomDao.java
│       │   ├── RoomTypeHoldDao.java
│       │   ├── BookingDao.java
│       │   ├── BookingLineDao.java
│       │   ├── RoomAssignmentDao.java
│       │   ├── PaymentDao.java
│       │   ├── EquipmentDao.java
│       │   ├── MaintenanceLogDao.java
│       │   ├── UserDao.java
│       │   ├── RoleDao.java
│       │   └── AuditLogDao.java
│       │
│       ├── model/                          # POJO Entity
│       │   ├── RoomType.java
│       │   ├── Room.java
│       │   ├── RoomTypeHold.java
│       │   ├── Booking.java
│       │   ├── BookingLine.java
│       │   ├── RoomAssignment.java
│       │   ├── Payment.java
│       │   ├── Equipment.java
│       │   ├── MaintenanceLog.java
│       │   ├── User.java
│       │   ├── Role.java
│       │   └── AuditLog.java
│       │
│       ├── dto/                            # Request/Response DTO cho JSON API
│       │   ├── BookingRequestDto.java
│       │   ├── CartHoldRequestDto.java
│       │   ├── PaymentRequestDto.java
│       │   └── ApiResponse.java
│       │
│       ├── filter/                         # javax/jakarta.servlet.Filter
│       │   ├── CharacterEncodingFilter.java
│       │   ├── AuthenticationFilter.java
│       │   ├── AuthorizationFilter.java
│       │   └── LocaleFilter.java
│       │
│       ├── listener/                       # ServletContextListener
│       │   ├── AppInitListener.java
│       │   └── HoldExpiryCleanupListener.java
│       │
│       ├── util/
│       │   ├── DBConnectionUtil.java
│       │   ├── PasswordUtil.java
│       │   ├── ValidationUtil.java
│       │   ├── DateTimeUtil.java
│       │   └── JsonUtil.java
│       │
│       ├── config/
│       │   ├── AppConfig.java
│       │   └── VNPayConfig.java
│       │
│       └── exception/
│           ├── BusinessException.java
│           ├── ResourceNotFoundException.java
│           └── GlobalExceptionHandler.java
│
├── WebContent/                             # (web/ trong NetBeans)
│   ├── WEB-INF/
│   │   ├── web.xml                         # Deployment Descriptor
│   │   ├── lib/                            # Libraries (.jar)
│   │   ├── classes/                        # Compiled Output (.class)
│   │   └── views/                          # JSP Files
│   │       ├── common/                     # Common components (Header, Footer, Sidebar, etc.)
│   │       ├── public/                     # Public customer facing pages
│   │       ├── reception/                  # Reception and Booking workflows
│   │       ├── housekeeping/               # Cleaning and Maintenance tasks
│   │       ├── technician/                 # Equipment management
│   │       ├── manager/                    # Hotel Management operations
│   │       └── admin/                      # Admin and System configs
│   │
│   │
│   └── index.jsp                           # Root redirect
│
├── database/                               # Database Scripts
│   ├── schema.sql
│   ├── seed_data.sql
│   └── migrations/
│
├── resources/                              # Configuration resources
│   ├── config.properties
│   └── i18n/
│
│
├── build.xml                               # Apache Ant build script
└── README.md
```

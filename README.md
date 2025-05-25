# 링글 코딩과제 - 수강신청 API

학생과 튜터 간의 1:1 수업 예약을 위한 RESTful API 서비스입니다. 튜터는 수업 가능한 시간대를 등록하고, 학생은 원하는 시간대와 수업 길이로 수업을 예약할 수 있습니다.

##  주요 설계 결정 및 고민 사항

### 1. 유연한 슬롯 관리와 조회
*   **고민**: 튜터는 자유롭게 시간 슬롯(30분, 60분 등)을 등록하고, 학생은 원하는 수업 길이(30분, 60분)로 가능한 시간대를 조회해야 했습니다.
*   **해결**:
    *   튜터는 원하는 길이로 `SessionSlot`을 등록합니다. (예: 30분 단위 또는 60분 단위)
    *   학생이 특정 `duration`으로 가능한 시간대를 조회하면, 서비스 로직에서 등록된 `SessionSlot`들을 조합하거나 필터링하여 요청된 `duration`에 맞는 시간대 정보를 제공합니다.
        *   예: 튜터가 10:00-10:30, 10:30-11:00 두 개의 30분 슬롯을 등록 -> 학생이 60분 수업 조회 시 10:00-11:00 시간대 제공.
    *   **날짜가 넘어가는 슬롯 조회**: 초기에는 슬롯 전체가 검색 범위 내에 완전히 포함되어야 조회되었으나, 슬롯이 검색 기간과 **겹치기만 하면** 조회되도록 Repository 쿼리를 수정하여 사용성을 개선했습니다. (`s.startTime < :endTime AND s.endTime > :startTime`)

### 2. 데이터 로딩과 성능 (LazyInitializationException 해결)
*   **고민**: 초기 구현에서 DTO 변환 시 연관된 엔티티(예: `Booking` 조회 시 `Tutor` 정보)에 접근할 때 `LazyInitializationException`이 발생하는 문제가 있었습니다. 이는 JPA의 지연 로딩(Lazy Loading) 전략과 영속성 컨텍스트 관리와 관련된 일반적인 문제입니다.
*   **해결**:
    *   **Fetch Join / @EntityGraph 적용**: `SessionSlotRepository` 및 `SessionBookingRepository`의 주요 조회 메소드에 Fetch Join 또는 `@EntityGraph`를 적용하여, 연관 엔티티를 조회 시점에 즉시 함께 로드하도록 수정했습니다. 이를 통해 N+1 문제를 방지하고 예외 발생을 해결했습니다.
        *   예: `SessionBooking` 조회 시 `Tutor`와 `Student` 정보를 함께 가져오도록 Repository 쿼리 수정.
    *   **Open Session In View (OSIV) 비활성화 유지**: `spring.jpa.open-in-view=false` 설정을 유지하여, 영속성 컨텍스트의 범위를 트랜잭션 내로 제한했습니다. 이는 잠재적인 성능 문제를 방지하고 데이터 접근 패턴을 명확하게 관리하기 위함입니다.

### 3. 초기 데이터 관리 (`import.sql`)
*   **고민**: 개발 및 테스트 시 일관된 초기 데이터 환경을 구성하고, `INSERT` 문법 오류로 인한 실행 실패를 방지해야 했습니다.
*   **해결**:
    *   `src/main/resources/import.sql` 파일을 사용하여 애플리케이션 시작 시 초기 데이터를 삽입합니다.
    *   `spring.jpa.hibernate.ddl-auto=create` (또는 `create-drop`) 설정을 사용하여 매 실행 시 스키마를 재생성하고, `import.sql`이 깨끗한 상태에서 실행되도록 했습니다.
    *   `spring.jpa.defer-datasource-initialization=true` 설정을 통해 Hibernate가 스키마를 생성한 후 `import.sql`이 실행되도록 순서를 보장했습니다.
    *   `import.sql` 내의 `INSERT` 문은 각 행마다 완전한 SQL 문으로 작성하거나, 표준적인 다중 값 `INSERT` 구문을 사용하여 H2 데이터베이스에서 발생하는 SQL 파싱 오류를 해결했습니다.

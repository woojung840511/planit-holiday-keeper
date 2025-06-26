# holiday-keeper 개발 로그

## 2025.06.25 
### 1. table 설계
- Country 처리에 대한 고민
  - 국가별 holiday를 관리하는 테이블을 따로 두는 것이 좋을지, 아니면 holiday 테이블에 country 컬럼을 추가하는 것이 좋을지 고민
  - 현재는 holiday 테이블에 country code 컬럼을 추가하여 관리하기로 결정. 
    - country name 은 테이블에 저장하지 않고 메모리에 캐싱하여 사용
        - 시작시 Nager.Date API를 통해 최신 국가 목록을 가져온다.
        - 이후 name 조회시 캐시에서 조회한다.
        - API 실패시 기본값 상수 사용
### 2. entity 설계
- Holiday
  - api 에서 받아온 holiday 정보를 저장하는 엔티티
  - api 에서 제공하는 필드 중에 주요필드 선별하여 사용

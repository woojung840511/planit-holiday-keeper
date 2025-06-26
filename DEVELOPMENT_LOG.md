# holiday-keeper 개발 특이사항 기록

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
### 3. repository, service 구현을 위한 기능 요구사항 파악
- 데이터 적재
  - 최근 5 년(2020 ~ 2025)의 공휴일을 외부 API에서 수집하여 저장 
  - 최초 실행시 5 년 × N 개 국가를 일괄 적재하는 기능 포함
- 검색
  - customRepository를 통해 holiday 검색 기능 구현
  - 검색 조건
    - country code
    - year
    - date (from, to)
- 재동기화
  - 특정 연도와 국가 데이터를 재호출하여 upsert 하는 기능 구현
  - 요청 dto 
    - country code
    - year
  - 로직
    - 특정 연도와 국가의 holiday 데이터를 Nager.Date API를 통해 조회
    - 조회된 데이터를 기존 데이터와 비교하여 변경된 부분만 업데이트
    - Nager.Date API에서 제공하지 않는 데이터를 삭제
    - Nager.Date API에서만 제공하는 데이터는 추가
- 삭제 
  - 특정 연도와 국가의 holiday 데이터를 삭제하는 기능 구현
  - 요청 dto 
    - country code
    - year
  - 로직
    - 해당 연도와 국가의 holiday 데이터를 삭제 (기본 repository delete 메소드 사용)
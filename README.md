# 🏗️ Project Structure

## 📂 프로젝트 폴더 구조

## 📦 주요 패키지 설명

| 패키지 | 설명 |  
|--------|------|  
| `featart.api` | **Controller** - API 엔드포인트 |  
| `featart.service` | **Service** - 비즈니스 로직 |  
| `featart.repository` | **Repository** - MongoDB 데이터 처리 |  
| `featart.domain` | **Entity & DTO** - 데이터 모델 |  
| `featart.scheduler` | **Scheduler** - 정기적 작업 수행 (`@Scheduled`) |  
| `featart.util` | **Utility** - 공통 유틸리티 클래스 |  
| `common.exception` | **Exception Handling** - 글로벌 예외 처리 |  
| `common.response` | **API Response** - 공통 응답 객체 |  
| `common.config` | **Config** - 프로젝트 설정 파일 |  
| `infra.mongo` | **MongoDB Configuration** - MongoDB 다중 커넥션 관리 |  
| `infra.security` | **Security** - 인증 및 권한 관리 | 
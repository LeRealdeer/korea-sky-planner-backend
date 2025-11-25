# 스카이 플래너

# 프로젝트 개요

프로젝트 이름: korea-sky-planner

설명: 한국 유저들을 위한 스카이 계획표 사이트. 키재기와 양초 계산 기능, 유랑대백과 등의 게임 정보을 제공해준다.

사이트 URL: https://korea-sky-planner.com/

## 주요 기능
| 기능 | 설명 | 엔드포인트 |
|------|------|------------|
| 영혼 노드 목록 조회 | 페이지네이션된 목록 반환 | GET `/api/v1/souls?page={n}` |
| 영혼 노드 상세 조회 | 단일 노드 조회 | GET `/api/v1/souls/{id}` |
| 영혼 노드 생성 | 새로운 노드 등록 | POST `/api/v1/souls` |
| 영혼 노드 수정 | 노드 정보 갱신 | PUT `/api/v1/souls/{id}` |
| 영혼 노드 삭제 | 노드 삭제 | DELETE `/api/v1/souls/{id}` |
| 영혼 노드 검색 | 키워드 기반 검색 | GET `/api/v1/souls/search?query=...` |
| 전체 노드 조회 | 페이징 없이 전체 목록 조회 | GET `/api/v1/souls/all` |
| 역정렬 목록 조회 | 최근 등록 순 조회 | GET `/api/v1/souls/reverse` |
| 이웃 노드 조회 | 이전/다음 글 조회 | GET `/api/v1/souls/{id}/neighbors` |
| 파일 업로드 | 이미지 업로드 및 고유 URL 발급 | POST `/api/v1/upload` |

## 기술 스택
| Category | Technologies |
|----------|--------------|
| Language | Java 17 |
| Framework | Spring Boot |
| Architecture | Layered Architecture |
| Build Tool | Maven |
| Documentation | Swagger (springdoc-openapi) |
| CI/CD | GitHub Actions, AWS EC2 |
| Domain Management | Gabia |
| Dev Tools | vsCode, Postman |

## 로컬 실행
```bash
git clone https://github.com/deerking0923/ezen-mini-backend.git
cd ezen-mini-backend
mvn clean install
java -jar target/your-app.jar
```


## 학습 포인트
- Swagger 기반 API 문서 자동화 경험
- 파일 업로드 처리 및 고유 URL 반환 로직 설계
- 영혼 노드 기반의 CRUD 및 검색 API 설계 및 구현
- NGINX + API Gateway를 통한 서비스 연동 구성
- 구글 애널리틱스 연동


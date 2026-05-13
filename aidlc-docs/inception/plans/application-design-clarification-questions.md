# Application Design Clarification Questions

Q5에서 하이브리드(동기/비동기) 처리를 선택하셨습니다. IMPORT 파일 크기 제한이 5MB인 상황에서 기준을 명확히 해야 합니다.

---

## Clarification Question 1
IMPORT/EXPORT 동기/비동기 전환 기준은 무엇입니까? (최대 파일 크기 5MB 기준)

A) 1MB 이하 동기, 1~5MB 비동기
B) 2MB 이하 동기, 2~5MB 비동기
C) 데이터 건수 기준 (예: 1000건 이하 동기, 초과 비동기)
D) IMPORT만 비동기, EXPORT는 항상 동기
X) Other (please describe after [Answer]: tag below)

[Answer]: 5MB 이하 동기, 이상은 비동기

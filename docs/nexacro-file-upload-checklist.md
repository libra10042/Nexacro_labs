# 넥사크로(Nexacro) 파일업로드 구현 체크리스트

Nexacro Platform 클라이언트 + 서버(Java/Spring 등) 조합 기준 체크리스트입니다.

## 1. 사전 준비

- [ ] 업로드 대상 컴포넌트 결정 (FileUpload 컴포넌트 / Grid + FileDownload plugin / Div fileupload / nexacro.FormFileDownload)
- [ ] 서버 프레임워크 및 통신 방식 확인 (Nexacro의 Multipart 전송 vs 일반 Form 전송, `nexacro:ssn` 세션 처리 여부)
- [ ] 첨부파일 저장소 결정 (로컬 디스크, NAS, S3/오브젝트 스토리지 등)
- [ ] 최대 업로드 용량, 동시 업로드 개수, 허용 확장자 정책 확정

## 2. 클라이언트 (Nexacro Script) 구현

- [ ] `FileUpload` 컴포넌트 또는 `nexacro.getApplication().gfnAttachFileUpload` 등 업로드 방식 선정
- [ ] 업로드 URL 및 `formid`, `datasetid` 파라미터 설정 (`this.FileUpload00.set_url(...)`)
- [ ] 다중 파일 선택 허용 여부 (`multiplefiles` property) 설정
- [ ] 클라이언트 단 확장자 필터링 (`filter` property: 예 `"*.jpg;*.png;*.pdf"`)
- [ ] 파일 크기 제한 클라이언트 측 사전 체크 (전송 전 `File Object`의 size 확인)
- [ ] 업로드 진행률 표시 (`ontransfer` 이벤트 바인딩, progressbar 갱신)
- [ ] 업로드 성공/실패 콜백 처리 (`onuploadcompleted`, `onuploaderror` 이벤트)
- [ ] 업로드 취소 기능 (`cancelTransfer` 또는 유사 API)
- [ ] 파일명 한글/특수문자 인코딩 처리 확인 (UTF-8 vs EUC-KR 이슈)
- [ ] 드래그 앤 드롭 지원 여부 검토 (필요 시)
- [ ] 업로드 완료 후 Dataset/Grid 갱신 로직 작성

## 3. 서버 구현

- [ ] Multipart 요청 파싱 설정 (`MultipartResolver`, `commons-fileupload` 등)
- [ ] 저장 경로를 웹 루트 외부(비공개 디렉토리)로 지정
- [ ] 저장 파일명은 원본명 그대로 사용하지 않고 UUID 등으로 치환, 원본명은 DB에 별도 저장
- [ ] 업로드 완료 후 XML/PlatformData 응답 포맷 준수 (`nexacro:...` 규격에 맞는 응답)
- [ ] 서버 측 파일 크기 제한 재검증 (클라이언트 검증 우회 대비)
- [ ] 서버 측 확장자/MIME 타입 화이트리스트 재검증 (Content-Type 스니핑 대비, 매직바이트 검사)
- [ ] 실행 가능 확장자(.jsp, .exe, .sh, .php 등) 업로드 차단
- [ ] 이중 확장자, 대소문자 우회(`.jSp`), null byte 등 우회 패턴 방어
- [ ] 업로드 파일 바이러스 검사 연동 여부 검토 (백신 API/모듈)
- [ ] 다운로드 시 저장 파일명이 아닌 DB의 원본 파일명으로 응답 (`Content-Disposition`)
- [ ] 다운로드 경로 조작(Path Traversal) 방지 - 파일 ID 기반 조회, 경로 문자열 직접 사용 금지
- [ ] 트랜잭션 처리 - 파일 저장과 DB 메타데이터 저장 실패 시 롤백/정리 로직

## 4. 보안 체크리스트

- [ ] 인증/인가 확인 - 업로드/다운로드 API에 로그인 세션 및 권한 체크 적용
- [ ] CSRF 토큰 적용 여부 확인 (Nexacro 통신 규격에 맞는 토큰 전달 방식 검토)
- [ ] 업로드 요청에 대한 Rate Limiting / 동시 업로드 제한
- [ ] 저장소 디렉토리 실행 권한 제거 (웹서버가 업로드 폴더의 스크립트를 실행하지 못하도록 설정)
- [ ] 파일명에 대한 XSS 방어 (다운로드 목록 화면 등에서 파일명 출력 시 escape 처리)
- [ ] 로그에 개인정보/민감 파일명이 과도하게 노출되지 않는지 확인

## 5. 테스트 체크리스트

- [ ] 정상 파일 업로드/다운로드 End-to-End 테스트
- [ ] 대용량 파일 업로드 (제한 초과 시 정상 에러 처리 확인)
- [ ] 허용되지 않은 확장자 업로드 시도 (차단 확인)
- [ ] 동시에 여러 파일 업로드 테스트
- [ ] 업로드 중 네트워크 끊김/취소 시나리오
- [ ] 한글/특수문자/공백 포함 파일명 테스트
- [ ] 0바이트 파일, 손상된 파일 업로드 테스트
- [ ] 동일 파일명 중복 업로드 시 처리 확인
- [ ] 모바일/저사양 환경에서의 업로드 성능 확인 (해당되는 경우)

## 6. 배포 전 확인사항

- [ ] 운영 환경 업로드 저장 경로 및 용량 모니터링 설정
- [ ] 업로드 파일 백업/보존 주기 정책 확인
- [ ] 장애 시 재시도/복구 절차 문서화
- [ ] 관련 설정값(최대 용량, 허용 확장자 등) 환경설정 파일로 분리하여 운영/개발 환경별 관리

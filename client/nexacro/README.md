# Nexacro 파일업로드 클라이언트 예제

`FileUploadForm.js`는 Nexacro Studio에서 만든 폼(.xfdl)의 **Form Script** 영역에 붙여넣어 사용하는
예제 스크립트입니다. `.xfdl` 자체는 바이너리에 가까운 XML이라 Studio 없이 직접 만들기 어렵기 때문에,
스크립트 로직만 별도 파일로 분리해두었습니다.

## Nexacro Studio에서 폼 구성하기

1. 새 폼을 만들고 다음 컴포넌트를 배치합니다.
   - `FileUpload00` : `nexacro:FileUpload` 컴포넌트 (비표시 가능)
   - `btnSelect`, `btnUpload`, `btnCancel` : Button
   - `prgUpload` : ProgressBar
   - `grdFiles` : Grid
   - `dsFiles` : Dataset — 컬럼 `id`, `originalName`, `size`, `contentType`
2. `grdFiles`의 `binddataset`을 `dsFiles`로 지정하고, 컬럼을 Dataset 컬럼에 매핑합니다.
3. 폼의 Script 편집기를 열고 `FileUploadForm.js`의 내용을 붙여넣습니다.

## 서버 연동

`server/` 디렉토리의 Spring Boot 예제를 그대로 띄우면 기본 포트 `8080`에서 아래 API가 열립니다.

- `POST /api/files/upload` — multipart part 이름은 `file`
- `GET /api/files/{id}/download` — 원본 파일명으로 다운로드

Nexacro 프로젝트의 `environments.xml` 또는 앱 설정에서 서버 baseURL을 지정하는 경우, 스크립트 상단의
`UPLOAD_URL`, `DOWNLOAD_URL_PREFIX` 값을 상대경로 대신 절대경로(`http://host:8080/api/files/...`)로
바꿔야 할 수 있습니다.

## 주의사항

- 스크립트에서 사용한 `FileUpload` 컴포넌트의 속성/이벤트 이름(`set_url`, `addFile`, `onuploadcompleted` 등)은
  Nexacro14/17 등 버전별 Reference Manual 기준으로 다소 다를 수 있습니다. 실제 프로젝트 적용 전
  사용 중인 버전 문서와 반드시 대조하세요.
- 클라이언트 측 확장자/용량 검증은 UX 편의를 위한 것이며, 우회 가능하므로 서버 측 검증
  (`server/.../FileStorageService.java`)이 실제 보안 경계입니다.

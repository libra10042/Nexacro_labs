# Nexacro Excel Upload Sample

Nexacro 클라이언트에서 로컬 엑셀 파일을 선택해 서버로 업로드하고, 서버(Spring Boot + Apache POI)가
엑셀을 파싱해 Nexacro Dataset(Platform XML) 형태로 응답하면, 클라이언트 Grid에 바로 표시하는
최소 예제입니다.

## 구성

```
client/Excel_Upload_Sample.xfdl   # Nexacro 화면 (Nexacro Studio로 열기)
server/                           # Spring Boot 백엔드
```

## 동작 흐름

1. `[파일찾기]` 클릭 → `showOpenDialog` 로 로컬 xls/xlsx 파일 선택
2. `[업로드]` 클릭 → `trans_upload.addParameter("uploadfile", path, "FILE")` 로 파일을 등록하고
   `this.transaction(..., "trans_upload=ds_list:multipart", "ds_list=output", ...)` 로 멀티파트 전송
3. 서버 `POST /api/excel/upload` 가 `MultipartFile` 로 파일을 받아 Apache POI로 파싱
4. 파싱 결과를 `Root/Dataset/Rows` 형태의 XML 문자열로 만들어 응답 (`NexacroPlatformXmlBuilder`)
5. Nexacro가 응답을 `ds_list`에 채우고, `Grid`가 이를 바인딩해 화면에 표시

엑셀 시트 구조는 1행 헤더 + 2행부터 데이터, A열=이름 / B열=나이 / C열=부서로 가정했습니다.
(`ExcelUploadService`, `.xfdl`의 `ds_list` 컬럼 정의에서 자유롭게 확장 가능)

## 서버 실행

```bash
cd server
mvn spring-boot:run
```

기본 포트는 `8080`, 업로드 엔드포인트는 `http://localhost:8080/api/excel/upload` 입니다.
CORS가 필요하면(클라이언트를 별도 서버로 서빙하는 경우) `ExcelUploadController`에
`@CrossOrigin` 등을 추가하세요.

## 클라이언트 사용

1. Nexacro Studio에서 `client/Excel_Upload_Sample.xfdl` 을 프로젝트에 추가 후 오픈
2. `Form_onload` 의 `this.gv_uploadUrl` 값이 서버 주소와 일치하는지 확인
3. 실행 후 파일찾기 → 업로드 순으로 테스트

## 참고 / 주의사항

- `showOpenDialog`, `this.transaction`, `addParameter(..., "FILE")` 는 Nexacro의 표준
  파일 업로드 패턴이지만, 세부 옵션/시그니처는 Nexacro 버전(14 / 17 / N)마다 조금씩 다를 수
  있습니다. 실제 적용 전에 사용 중인 Nexacro Studio 내장 도움말(Nexacro API Reference)로
  한 번 대조해보는 것을 권장합니다.
- 서버는 정식 Nexacro Server SDK(xapi jar) 없이도 동작하도록 Platform XML(Dataset) 응답을
  직접 문자열로 조립합니다(`NexacroPlatformXmlBuilder`). SDK를 쓸 수 있는 환경이라면
  `PlatformData`/`DataSet` API로 대체해도 됩니다.
- 업로드 용량 제한은 `server/src/main/resources/application.yml` 의
  `spring.servlet.multipart.max-file-size` / `max-request-size` 로 조정하세요.

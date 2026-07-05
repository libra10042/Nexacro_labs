/*
 * Nexacro Studio 폼(.xfdl)의 Form Script 영역에 붙여넣어 사용하는 예제 스크립트.
 *
 * 폼 구성 (Nexacro Studio에서 미리 배치):
 *   - FileUpload00   : nexacro:FileUpload 컴포넌트 (실제 전송 담당, 화면에 보이지 않아도 됨)
 *   - btnSelect      : "파일 선택" 버튼
 *   - btnUpload      : "업로드" 버튼
 *   - btnCancel      : "취소" 버튼
 *   - prgUpload      : ProgressBar 컴포넌트
 *   - grdFiles       : 업로드된 파일 목록을 보여줄 Grid (dsFiles 바인딩)
 *   - dsFiles        : Dataset (컬럼: id, originalName, size, contentType)
 *
 * 서버 API (server/ 디렉토리의 Spring Boot 예제 기준):
 *   POST /api/files/upload         -> multipart/form-data, part name "file"
 *   GET  /api/files/{id}/download  -> 파일 다운로드
 *
 * 주의: 컴포넌트/이벤트 이름은 사용 중인 Nexacro 버전(14/17 등)의 Reference Manual을
 * 기준으로 다를 수 있으므로, 실제 프로젝트에 적용하기 전에 해당 버전 문서와 대조할 것.
 */

var UPLOAD_URL = "/api/files/upload";
var DOWNLOAD_URL_PREFIX = "/api/files/";

// 클라이언트 단 1차 방어용 - 서버에서도 반드시 동일한 정책으로 재검증한다.
var ALLOWED_EXTENSIONS = ["jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip"];
var MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB

function fn_getExtension(fileName)
{
	var dotIndex = fileName.lastIndexOf(".");
	if (dotIndex < 0 || dotIndex === fileName.length - 1) {
		return "";
	}
	return fileName.substring(dotIndex + 1).toLowerCase();
}

function fn_validateFile(fileObj)
{
	var ext = fn_getExtension(fileObj.name);
	if (ALLOWED_EXTENSIONS.indexOf(ext) < 0) {
		this.alert("허용되지 않는 파일 형식입니다: ." + ext);
		return false;
	}
	if (fileObj.size <= 0) {
		this.alert("빈 파일은 업로드할 수 없습니다.");
		return false;
	}
	if (fileObj.size > MAX_FILE_SIZE_BYTES) {
		this.alert("파일 용량이 50MB를 초과했습니다.");
		return false;
	}
	return true;
}

this.form_onload = function(obj, e)
{
	this.FileUpload00.set_url(UPLOAD_URL);
	this.FileUpload00.set_multiplefiles(false);
	// 서버가 지원하지 않는 형식은 파일 선택 다이얼로그에서부터 걸러낸다.
	this.FileUpload00.set_filter("*.jpg;*.jpeg;*.png;*.gif;*.pdf;*.doc;*.docx;*.xls;*.xlsx;*.ppt;*.pptx;*.txt;*.zip");

	this.prgUpload.set_visible(false);
	this.btnCancel.set_enable(false);
};

this.btnSelect_onclick = function(obj, e)
{
	this.FileUpload00.addFile();
};

this.FileUpload00_onaddfile = function(obj, e)
{
	if (!fn_validateFile.call(this, e)) {
		this.FileUpload00.removeFile(e.id);
		return;
	}
	this.btnUpload.set_enable(true);
};

this.btnUpload_onclick = function(obj, e)
{
	if (this.FileUpload00.getFileList().length === 0) {
		this.alert("업로드할 파일을 선택하세요.");
		return;
	}

	this.prgUpload.set_visible(true);
	this.prgUpload.set_pos(0);
	this.btnUpload.set_enable(false);
	this.btnCancel.set_enable(true);

	this.FileUpload00.send();
};

this.btnCancel_onclick = function(obj, e)
{
	this.FileUpload00.cancel();
	this.btnCancel.set_enable(false);
	this.btnUpload.set_enable(true);
	this.prgUpload.set_visible(false);
};

// 전송 진행률 갱신
this.FileUpload00_ontransfer = function(obj, e)
{
	if (e.total > 0) {
		var percent = Math.floor((e.transfer / e.total) * 100);
		this.prgUpload.set_pos(percent);
	}
};

// 업로드 완료 - 서버 응답(JSON)을 파싱해 Grid에 반영
this.FileUpload00_onuploadcompleted = function(obj, e)
{
	this.prgUpload.set_visible(false);
	this.btnCancel.set_enable(false);
	this.btnUpload.set_enable(true);

	try {
		var result = nexacro.JSON.parse(e.responsetext);
		this.dsFiles.addRow();
		var row = this.dsFiles.rowcount - 1;
		this.dsFiles.setColumn(row, "id", result.id);
		this.dsFiles.setColumn(row, "originalName", result.originalName);
		this.dsFiles.setColumn(row, "size", result.size);
		this.dsFiles.setColumn(row, "contentType", result.contentType);

		this.alert("업로드가 완료되었습니다: " + result.originalName);
	} catch (ex) {
		this.alert("서버 응답 처리 중 오류가 발생했습니다.");
	}
};

this.FileUpload00_onuploaderror = function(obj, e)
{
	this.prgUpload.set_visible(false);
	this.btnCancel.set_enable(false);
	this.btnUpload.set_enable(true);
	this.alert("업로드에 실패했습니다: " + e.errormsg);
};

// Grid에서 특정 행을 더블클릭하면 해당 파일 다운로드
this.grdFiles_oncelldblclick = function(obj, e)
{
	var fileId = this.dsFiles.getColumn(e.row, "id");
	if (!fileId) {
		return;
	}
	var url = DOWNLOAD_URL_PREFIX + fileId + "/download";
	nexacro.getApplication().gotoURL(url, "_blank");
};

package com.nexacrolabs.excelupload.util;

import java.util.List;

/**
 * Nexacro 클라이언트의 trans/transaction 이 이해하는 Platform XML(Dataset) 응답을
 * 직접 조립하는 유틸리티. 정식 Nexacro Server SDK(xapi jar) 없이도 동작하도록
 * 표준 Root/DataSet/ColumnInfo/Rows 구조를 그대로 생성한다.
 */
public final class NexacroPlatformXmlBuilder {

    private NexacroPlatformXmlBuilder() {
    }

    public static String buildDatasetResponse(String datasetId, List<String> columnIds, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<Root>\n");
        sb.append("  <Dataset id=\"").append(escapeAttr(datasetId)).append("\">\n");

        sb.append("    <ColumnInfo>\n");
        for (String columnId : columnIds) {
            sb.append("      <Column id=\"").append(escapeAttr(columnId)).append("\" type=\"STRING\" size=\"256\"/>\n");
        }
        sb.append("    </ColumnInfo>\n");

        sb.append("    <Rows>\n");
        for (List<String> row : rows) {
            sb.append("      <Row>\n");
            for (int i = 0; i < columnIds.size(); i++) {
                String value = i < row.size() ? row.get(i) : "";
                sb.append("        <Col id=\"").append(escapeAttr(columnIds.get(i))).append("\">")
                  .append(escapeText(value))
                  .append("</Col>\n");
            }
            sb.append("      </Row>\n");
        }
        sb.append("    </Rows>\n");
        sb.append("  </Dataset>\n");

        sb.append("  <Parameters>\n");
        sb.append("    <Parameter id=\"ErrorCode\">0</Parameter>\n");
        sb.append("    <Parameter id=\"ErrorMsg\"></Parameter>\n");
        sb.append("  </Parameters>\n");
        sb.append("</Root>\n");

        return sb.toString();
    }

    public static String buildErrorResponse(String errorMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<Root>\n");
        sb.append("  <Parameters>\n");
        sb.append("    <Parameter id=\"ErrorCode\">-1</Parameter>\n");
        sb.append("    <Parameter id=\"ErrorMsg\">").append(escapeText(errorMsg)).append("</Parameter>\n");
        sb.append("  </Parameters>\n");
        sb.append("</Root>\n");
        return sb.toString();
    }

    private static String escapeAttr(String value) {
        return escapeText(value);
    }

    private static String escapeText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

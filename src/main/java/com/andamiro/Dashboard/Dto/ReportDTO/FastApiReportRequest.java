package com.andamiro.Dashboard.Dto.ReportDTO;

public record FastApiReportRequest(
        String incident_type,
        String description,
        String location,
        String report_type,
        String language,
        
        Boolean use_rag,
        String collection,
        Integer top_k,
        String model,
        String title
) {
    public FastApiReportRequest { //null로 들어온 값에 대해 내부적으로 기본값을 주기.
        if (use_rag == null) use_rag = true;
        if (collection == null) collection = "marine_laws";
        if (top_k == null) top_k = 5;
        if (model == null) model = "gpt-4o-mini";
        if (title == null) title = "해양 보험 청구 보고서";
    }

}

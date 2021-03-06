package com.harokad.goona.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdmDocumentSearchResult {

    private EdmDocumentFile edmDocument;
    
    private String highlightedName;
    
    private String highlightedDescription;
    
    private String highlightedFileContentMatching;
    
    private String highlightedNodePath;
    
}

package com.harokad.goona.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EdmDocumentSearchResultWrapper {
	    // took time in MS
	    private long tookTime;

	    private long totalHitsCount;

	    private String scrollId;
	    
	    private int resultPage;
	    
		private int resultPageSize;
	    
	    private List<EdmDocumentSearchResult> searchResults = new ArrayList<>();;

	    public void add(EdmDocumentSearchResult edmDocumentSearchResult) {
	        searchResults.add(edmDocumentSearchResult);
	    }

}

package com.harokad.goona.domain;

import java.io.Serializable;

import org.springframework.data.elasticsearch.annotations.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Document(indexName = "documents", type = "category")
public class EdmCategory implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
    
    private String name;
    
    private String description;
    
    private String color;
    
    private String backgroundColor;
}

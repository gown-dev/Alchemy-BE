package alchemy.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Gene")
public class Gene {
	
	@Id
	private String id;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
	    name = "Tag",
	    joinColumns = @JoinColumn(name = "gene_id")
	)
	@Column(name = "tag")
	private List<String> tags;
	
}

package alchemy.model.pets.genes;

import java.util.List;

import alchemy.model.pets.constraints.Constraint;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
	private String image;
	
	@Column(unique = true)
	private String name;
	
	@Enumerated(EnumType.STRING)
	private GeneType type;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
	    name = "Gene_Tag",
	    joinColumns = @JoinColumn(name = "gene_id")
	)
	@Column(name = "tag")
	private List<String> tags;
	
	@OneToMany(cascade = CascadeType.ALL)
    private List<Constraint> constraints;
	
}

package alchemy.model.pets.genes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GeneLoadout")
public class GeneLoadout {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horns_gene_id")
    private Gene horns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ears_gene_id")
    private Gene ears;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_gene_id")
    private Gene head;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floof_gene_id")
    private Gene floof;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_gene_id")
    private Gene body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wings_gene_id")
    private Gene wings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tail_gene_id")
    private Gene tail;
    
    public List<String> getTags() {
    	return Stream.of(horns.getTags(), ears.getTags(), head.getTags(), floof.getTags(), 
    			body.getTags(), wings.getTags(), tail.getTags())
    			.flatMap(List::stream)
    			.collect(Collectors.toList());
    }

}

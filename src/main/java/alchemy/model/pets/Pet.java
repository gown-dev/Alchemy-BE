package alchemy.model.pets;

import java.util.UUID;

import alchemy.model.pets.attributes.AttributeLoadout;
import alchemy.model.pets.genes.GeneLoadout;
import alchemy.model.pets.moves.MoveLoadout;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "Pet")
public class Pet {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	private String name;

	private int level;

	@OneToOne
	private AttributeLoadout attributeLoadout = new AttributeLoadout();

	@OneToOne
	private GeneLoadout geneLoadout = new GeneLoadout();

	@OneToOne
    private MoveLoadout moveLoadout = new MoveLoadout();

}
